package com.carlauncherthatsucks.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoicePhase { IDLE, LISTENING, THINKING, SPEAKING, ERROR }

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.IDLE,
    /** Trascrizione parziale mentre l'utente parla. */
    val partial: String = "",
    val lastQuestion: String = "",
    val lastAnswer: String = "",
    val error: String? = null,
)

/**
 * Chat vocale con un'API compatibile OpenAI:
 * ascolta (SpeechRecognizer) → chiede al modello → legge la risposta (TextToSpeech) → riascolta.
 * Va creata e usata sul main thread.
 */
class VoiceSession(
    private val context: Context,
    private val scope: CoroutineScope,
    private val config: () -> ApiConfig,
) {
    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    /** Dopo la risposta riprende ad ascoltare da sola: le mani restano sul volante. */
    var continuous = true

    private val history = mutableListOf<ChatMessage>()
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var request: Job? = null

    init {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        scope.launch(Dispatchers.Main) { onSpeechFinished() }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        scope.launch(Dispatchers.Main) { onSpeechFinished() }
                    }
                })
            }
        }
    }

    fun recognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /** Avvia l'ascolto (o lo riavvia), interrompendo un'eventuale lettura in corso. */
    fun listen() {
        tts?.stop()
        if (!recognitionAvailable()) {
            fail("Nessun servizio di riconoscimento vocale sul telefono")
            return
        }
        val r = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        _state.value = _state.value.copy(phase = VoicePhase.LISTENING, partial = "", error = null)
        try {
            r.startListening(intent)
        } catch (e: Exception) {
            fail("Impossibile ascoltare: ${e.message}")
        }
    }

    /** Ferma tutto: ascolto, richiesta, lettura. */
    fun stop() {
        request?.cancel()
        recognizer?.cancel()
        tts?.stop()
        _state.value = _state.value.copy(phase = VoicePhase.IDLE, partial = "")
    }

    /** Ricomincia da capo dimenticando la conversazione. */
    fun reset() {
        stop()
        history.clear()
        _state.value = VoiceUiState()
    }

    fun release() {
        stop()
        recognizer?.destroy()
        recognizer = null
        tts?.shutdown()
        tts = null
    }

    private fun ask(question: String) {
        history += ChatMessage("user", question)
        trimHistory()
        _state.value = _state.value.copy(
            phase = VoicePhase.THINKING, partial = "", lastQuestion = question, lastAnswer = "", error = null,
        )
        request = scope.launch {
            try {
                val answer = ChatApi.complete(config(), history.toList())
                history += ChatMessage("assistant", answer)
                trimHistory()
                _state.value = _state.value.copy(phase = VoicePhase.SPEAKING, lastAnswer = answer)
                speak(answer)
            } catch (e: ChatApiException) {
                history.removeLastOrNull()
                fail(e.message ?: "Errore")
            }
        }
    }

    private fun speak(text: String) {
        val engine = tts
        if (engine == null || !ttsReady) {
            // Niente sintesi vocale: la risposta resta scritta sullo schermo.
            _state.value = _state.value.copy(phase = VoicePhase.IDLE)
            return
        }
        engine.speak(speakable(text), TextToSpeech.QUEUE_FLUSH, null, "reply")
    }

    private fun onSpeechFinished() {
        if (_state.value.phase != VoicePhase.SPEAKING) return
        if (continuous) listen() else _state.value = _state.value.copy(phase = VoicePhase.IDLE)
    }

    private fun fail(message: String) {
        _state.value = _state.value.copy(phase = VoicePhase.ERROR, partial = "", error = message)
    }

    private fun trimHistory() {
        while (history.size > MAX_HISTORY) history.removeAt(0)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!text.isNullOrBlank() && _state.value.phase == VoicePhase.LISTENING) {
                _state.value = _state.value.copy(partial = text)
            }
        }

        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (text.isNullOrBlank()) {
                _state.value = _state.value.copy(phase = VoicePhase.IDLE, partial = "")
            } else {
                ask(text)
            }
        }

        override fun onError(error: Int) {
            if (_state.value.phase != VoicePhase.LISTENING) return
            val message = recognitionErrorMessage(error)
            if (message == null) {
                // Silenzio: la conversazione si ferma, si riprende toccando il microfono.
                _state.value = _state.value.copy(phase = VoicePhase.IDLE, partial = "")
            } else {
                fail(message)
            }
        }
    }

    companion object {
        private const val MAX_HISTORY = 12
    }
}

/** Messaggio per un errore del riconoscimento vocale; null = nessuno ha parlato (non è un errore). */
fun recognitionErrorMessage(code: Int): String? = when (code) {
    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> null
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Serve il permesso del microfono"
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
        "Il riconoscimento vocale non raggiunge la rete"
    SpeechRecognizer.ERROR_AUDIO -> "Problema con il microfono"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Riconoscimento vocale occupato, riprova"
    else -> "Riconoscimento vocale non riuscito (codice $code)"
}

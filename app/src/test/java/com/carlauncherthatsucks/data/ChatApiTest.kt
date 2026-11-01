package com.carlauncherthatsucks.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatApiTest {
    @Test
    fun `URL dell'endpoint`() {
        val expected = "https://api.openai.com/v1/chat/completions"
        assertEquals(expected, ChatApi.chatCompletionsUrl("https://api.openai.com/v1"))
        assertEquals(expected, ChatApi.chatCompletionsUrl(" https://api.openai.com/v1/ "))
        assertEquals(expected, ChatApi.chatCompletionsUrl("https://api.openai.com/v1/chat/completions"))
    }

    @Test
    fun `corpo della richiesta con istruzioni di sistema`() {
        val body = JSONObject(
            ChatApi.buildRequestBody(" gpt-x ", listOf(ChatMessage("user", "Ciao"), ChatMessage("assistant", "Ehi")))
        )
        assertEquals("gpt-x", body.getString("model"))
        assertFalse(body.getBoolean("stream"))
        val messages = body.getJSONArray("messages")
        assertEquals(3, messages.length())
        assertEquals("system", messages.getJSONObject(0).getString("role"))
        assertEquals("Ciao", messages.getJSONObject(1).getString("content"))
        assertEquals("assistant", messages.getJSONObject(2).getString("role"))
    }

    @Test
    fun `risposta standard`() {
        val json = """{"id":"x","choices":[{"index":0,"message":{"role":"assistant","content":"  Ciao!  "}}]}"""
        assertEquals("Ciao!", ChatApi.parseCompletion(json))
    }

    @Test
    fun `risposta a blocchi`() {
        val json = """{"choices":[{"message":{"content":[{"type":"text","text":"Uno "},{"type":"text","text":"due"}]}}]}"""
        assertEquals("Uno due", ChatApi.parseCompletion(json))
    }

    @Test(expected = ChatApiException::class)
    fun `risposta senza scelte`() {
        ChatApi.parseCompletion("""{"choices":[]}""")
    }

    @Test
    fun `errori leggibili`() {
        val openAi = """{"error":{"message":"Incorrect API key provided","type":"invalid_request_error"}}"""
        assertEquals("chiave API non valida: Incorrect API key provided", ChatApi.parseError(401, openAi))
        assertEquals("URL o modello non trovato: model not found", ChatApi.parseError(404, """{"error":"model not found"}"""))
        assertEquals("Errore 500", ChatApi.parseError(500, "<html>boom</html>"))
    }

    @Test
    fun `configurazione completa solo con URL e modello`() {
        assertFalse(ApiConfig().isComplete)
        assertFalse(ApiConfig(baseUrl = "http://x/v1").isComplete)
        assertTrue(ApiConfig(baseUrl = "http://x/v1", model = "m").isComplete) // la chiave è facoltativa
    }

    @Test
    fun `testo pulito dal markdown prima di leggerlo`() {
        assertEquals("Ecco: primo secondo vedi qui", speakable("**Ecco:**\n- primo\n- _secondo_\n[vedi qui](https://x.y)"))
    }

    @Test
    fun `preset con URL validi`() {
        API_PRESETS.forEach {
            assertTrue(it.name, it.baseUrl.startsWith("http"))
            assertTrue(it.name, it.exampleModel.isNotBlank())
        }
    }

    @Test
    fun `silenzio non e un errore di riconoscimento`() {
        assertNull(recognitionErrorMessage(android.speech.SpeechRecognizer.ERROR_NO_MATCH))
        assertNull(recognitionErrorMessage(android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        assertEquals("Serve il permesso del microfono", recognitionErrorMessage(android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
    }

    @Test
    fun `scorciatoia Gemini Live`() {
        val shortcuts = listOf(ShortcutCandidate("new", "Nuova chat", null), ShortcutCandidate("live", "Gemini Live", null))
        assertEquals("live", pickVoiceShortcut(shortcuts)?.id)
    }
}

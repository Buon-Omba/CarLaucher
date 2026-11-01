package com.carlauncherthatsucks.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carlauncherthatsucks.data.ApiConfig
import com.carlauncherthatsucks.data.VoicePhase
import com.carlauncherthatsucks.data.VoiceSession
import com.carlauncherthatsucks.data.VoiceUiState

/** Chat vocale con l'API configurata: gestisce microfono, permesso e ciclo di vita della sessione. */
@Composable
fun VoiceChatRoute(
    config: ApiConfig,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { VoiceSession(context, scope) { config } }
    DisposableEffect(Unit) { onDispose { session.release() } }
    val state by session.state.collectAsStateWithLifecycle()

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) session.listen()
    }
    val startListening = {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            session.listen()
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    // In auto: appena si apre, ascolta.
    LaunchedEffect(Unit) { if (config.isComplete) startListening() }

    VoiceChatScreen(
        state = state,
        config = config,
        onMic = {
            when (state.phase) {
                VoicePhase.LISTENING, VoicePhase.THINKING, VoicePhase.SPEAKING -> session.stop()
                else -> startListening()
            }
        },
        onNewConversation = { session.reset() },
        onOpenSettings = onOpenSettings,
        onClose = onClose,
    )
}

@Composable
fun VoiceChatScreen(
    state: VoiceUiState,
    config: ApiConfig,
    onMic: () -> Unit,
    onNewConversation: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Dash.Asphalt)
            .blockTouches()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Assistente", style = MaterialTheme.typography.headlineSmall, color = Dash.Numeral)
                Text(
                    config.model.ifBlank { "Nessun modello configurato" },
                    style = MaterialTheme.typography.labelMedium,
                    color = Dash.Note,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(28.dp), tint = Dash.Label)
            }
        }

        if (!config.isComplete) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Lamp()
                    Spacer(Modifier.width(10.dp))
                    Text("API non configurata", style = MaterialTheme.typography.titleMedium, color = Dash.Numeral)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Imposta URL, chiave e modello in Impostazioni → Assistente vocale.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dash.Label,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(onClick = onOpenSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Apri impostazioni")
                }
            }
            return@Column
        }

        // Punto focale: il microfono. Il suo stato dice cosa sta succedendo.
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MicOrb(state.phase, onMic)
            Spacer(Modifier.height(16.dp))
            Text(
                phaseCaption(state),
                style = MaterialTheme.typography.titleMedium,
                color = if (state.phase == VoicePhase.ERROR) Dash.WarningLamp else Dash.Label,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Ultimo scambio: la domanda in piccolo, la risposta in evidenza.
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            val question = state.partial.ifBlank { state.lastQuestion }
            if (question.isNotBlank()) {
                Text("TU", style = MaterialTheme.typography.labelSmall, color = Dash.Note)
                Spacer(Modifier.height(4.dp))
                Text(question, style = MaterialTheme.typography.bodyLarge, color = Dash.Label)
                Spacer(Modifier.height(16.dp))
            }
            if (state.lastAnswer.isNotBlank()) {
                Text("ASSISTENTE", style = MaterialTheme.typography.labelSmall, color = Dash.Note)
                Spacer(Modifier.height(4.dp))
                Text(
                    state.lastAnswer,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
                    color = Dash.Numeral,
                )
            }
        }

        if (state.lastQuestion.isNotBlank()) {
            TextButton(
                onClick = onNewConversation,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 48.dp),
            ) {
                Text("Nuova conversazione", style = MaterialTheme.typography.labelLarge, color = Dash.Label)
            }
        }
    }
}

@Composable
private fun MicOrb(phase: VoicePhase, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = when (phase) {
            VoicePhase.LISTENING, VoicePhase.SPEAKING -> Dash.Backlight
            VoicePhase.IDLE -> Dash.BacklightDim
            VoicePhase.THINKING, VoicePhase.ERROR -> Dash.Track
        },
        animationSpec = tween(180, easing = EaseOutDash),
        label = "orb",
    )
    val content = when (phase) {
        VoicePhase.LISTENING, VoicePhase.SPEAKING -> Dash.OnBacklight
        VoicePhase.IDLE -> Dash.OnBacklightDim
        else -> Dash.Label
    }
    Box(contentAlignment = Alignment.Center) {
        if (phase == VoicePhase.THINKING) {
            CircularProgressIndicator(
                modifier = Modifier.size(168.dp),
                color = Dash.Backlight,
                trackColor = Dash.Panel,
                strokeWidth = 4.dp,
            )
        }
        Surface(
            onClick = onClick,
            modifier = Modifier.size(144.dp),
            shape = CircleShape,
            color = color,
            contentColor = content,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    when (phase) {
                        VoicePhase.SPEAKING -> Icons.Filled.GraphicEq
                        VoicePhase.ERROR -> Icons.Filled.MicOff
                        else -> Icons.Filled.Mic
                    },
                    contentDescription = when (phase) {
                        VoicePhase.IDLE, VoicePhase.ERROR -> "Parla"
                        else -> "Ferma"
                    },
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

private fun phaseCaption(state: VoiceUiState): String = when (state.phase) {
    VoicePhase.IDLE -> if (state.lastAnswer.isBlank()) "Tocca e parla" else "Tocca per continuare"
    VoicePhase.LISTENING -> "Ti ascolto…"
    VoicePhase.THINKING -> "Ci penso…"
    VoicePhase.SPEAKING -> "Tocca per interrompere"
    VoicePhase.ERROR -> state.error ?: "Errore"
}

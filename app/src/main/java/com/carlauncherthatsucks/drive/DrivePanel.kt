package com.carlauncherthatsucks.drive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.SpeedState
import kotlinx.coroutines.flow.StateFlow

/** Pannello compatto mostrato sopra CoMaps: velocità, musica, ritorno alla home. */
@Composable
fun DrivePanel(
    speedFlow: StateFlow<SpeedState>,
    mediaFlow: StateFlow<MediaState?>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onHome: () -> Unit,
) {
    val speed by speedFlow.collectAsState()
    val media by mediaFlow.collectAsState()
    val scheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
        contentColor = scheme.onSurface,
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
            // Maniglia: trascina per spostare il pannello e non coprire i comandi di CoMaps.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = onDragEnd,
                            onVerticalDrag = { change, dy ->
                                change.consume()
                                onDrag(dy)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(width = 40.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(scheme.onSurfaceVariant.copy(alpha = 0.5f))
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Velocità
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                    modifier = Modifier.size(width = 96.dp, height = 76.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            speed.kmh?.toString() ?: "--",
                            fontSize = 38.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Black,
                        )
                        Text("km/h", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.width(10.dp))

                // Musica
                val m = media
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val art = m?.art
                        if (art != null) {
                            Image(
                                art.asImageBitmap(), null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                            )
                        } else {
                            Icon(Icons.Filled.MusicNote, null, modifier = Modifier.size(18.dp), tint = scheme.primary)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            when {
                                m == null -> "Nessuna riproduzione"
                                m.artist.isNotBlank() -> "${m.title} · ${m.artist}"
                                else -> m.title.ifBlank { m.appLabel }
                            },
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalIconButton(
                            onClick = onPrevious,
                            enabled = m?.canSkipPrevious == true,
                            modifier = Modifier.size(width = 52.dp, height = 46.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) { Icon(Icons.Filled.SkipPrevious, "Precedente", modifier = Modifier.size(28.dp)) }
                        Surface(
                            onClick = onPlayPause,
                            enabled = m != null,
                            modifier = Modifier.size(width = 64.dp, height = 46.dp),
                            shape = RoundedCornerShape(if (m?.isPlaying == true) 14.dp else 23.dp),
                            color = scheme.primary,
                            contentColor = scheme.onPrimary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (m?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    "Play/Pausa",
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = onNext,
                            enabled = m?.canSkipNext == true,
                            modifier = Modifier.size(width = 52.dp, height = 46.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) { Icon(Icons.Filled.SkipNext, "Successiva", modifier = Modifier.size(28.dp)) }
                    }
                }

                // Torna al launcher
                Surface(
                    onClick = onHome,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Home, "Torna alla home", modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}

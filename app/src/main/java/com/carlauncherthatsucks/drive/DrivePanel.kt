package com.carlauncherthatsucks.drive

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.ui.Dash
import com.carlauncherthatsucks.ui.EaseOutDash
import com.carlauncherthatsucks.ui.SpeedDial
import kotlinx.coroutines.flow.StateFlow

/**
 * Pannello sopra CoMaps: piccolo quadrante, brano e comandi, ritorno alla home.
 * Unica superficie con ombra: galleggia sopra la mappa di un'altra app.
 */
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
    val m = media

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = Dash.Panel,
        contentColor = Dash.Numeral,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
            // Maniglia: trascina per non coprire i comandi di CoMaps.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.dp)
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
                        .size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Dash.Off)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                SpeedDial(
                    speed = speed,
                    diameter = 84.dp,
                    numeralStyle = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp, lineHeight = 32.sp),
                    unitStyle = MaterialTheme.typography.labelSmall,
                    stroke = 5.dp,
                    showTicks = false,
                )
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            m == null -> "Niente in riproduzione"
                            else -> m.title.ifBlank { m.appLabel }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (m == null) Dash.Note else Dash.Numeral,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PanelTransport(Icons.Filled.SkipPrevious, "Precedente", m?.canSkipPrevious == true, onPrevious)
                        val playing = m?.isPlaying == true
                        val corner by animateDpAsState(
                            targetValue = if (playing) 14.dp else 24.dp,
                            animationSpec = tween(durationMillis = 180, easing = EaseOutDash),
                            label = "panelPlay",
                        )
                        Surface(
                            onClick = onPlayPause,
                            enabled = m != null,
                            modifier = Modifier.size(width = 64.dp, height = 48.dp),
                            shape = RoundedCornerShape(corner),
                            color = if (m != null) Dash.BacklightDim else Dash.Track,
                            contentColor = if (m != null) Dash.OnBacklightDim else Dash.Off,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    if (playing) "Pausa" else "Riproduci",
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                        PanelTransport(Icons.Filled.SkipNext, "Successiva", m?.canSkipNext == true, onNext)
                    }
                }

                // Torna al launcher.
                IconButton(
                    onClick = onHome,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Dash.Label),
                ) {
                    Icon(Icons.Filled.Home, "Torna alla home", modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
private fun PanelTransport(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Dash.Numeral, disabledContentColor = Dash.Off),
    ) {
        Icon(icon, label, modifier = Modifier.size(28.dp))
    }
}

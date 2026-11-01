package com.carlauncherthatsucks.ui

import android.text.format.DateUtils
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.CarNotification
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.NavigationInfo
import com.carlauncherthatsucks.data.initialsOf
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- Navigazione

/**
 * Prossima manovra di CoMaps. Durante la navigazione è il punto focale della home:
 * unico pannello con la retroilluminazione piena.
 */
@Composable
fun NavigationCard(navigation: NavigationInfo, onClick: () -> Unit) {
    CarCard(Modifier.fillMaxWidth(), onClick = onClick, color = Dash.Backlight, padding = 16) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Raggio interno concentrico: 28 (pannello) − 16 (padding) = 12.
            Box(
                Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Dash.OnBacklight.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = navigation.turnIcon
                if (icon != null) {
                    Image(icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(52.dp))
                } else {
                    Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    navigation.title.ifBlank { "In navigazione" },
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (navigation.text.isNotBlank()) {
                    Text(
                        navigation.text,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (navigation.subText.isNotBlank()) {
                    Text(
                        navigation.subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Dash.OnBacklight.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Multimedia

@Composable
fun MediaCard(
    media: MediaState?,
    hasAccess: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpen: () -> Unit,
) {
    CarCard(Modifier.fillMaxWidth(), padding = 16) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Copertina: raggio concentrico 28 − 16 = 12.
            Box(
                Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Dash.Track),
                contentAlignment = Alignment.Center,
            ) {
                val art = media?.art
                if (art != null) {
                    Image(art.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Filled.MusicNote, null, tint = Dash.Note, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                when {
                    media != null -> {
                        Text(
                            media.title.ifBlank { media.appLabel },
                            style = MaterialTheme.typography.titleLarge,
                            color = Dash.Numeral,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            listOf(media.artist, media.appLabel).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Dash.Label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    !hasAccess -> {
                        Text("Musica", style = MaterialTheme.typography.titleLarge, color = Dash.Label)
                        Text(
                            "Serve l'accesso alle notifiche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Dash.Note,
                        )
                    }
                    else -> {
                        Text("Niente in riproduzione", style = MaterialTheme.typography.titleLarge, color = Dash.Label)
                        Text("Tocca per aprire il lettore", style = MaterialTheme.typography.bodyMedium, color = Dash.Note)
                    }
                }
            }
        }

        if (media != null && media.durationMs > 0) {
            val position by rememberPlaybackPosition(media)
            Spacer(Modifier.height(12.dp))
            ProgressLine((position.toFloat() / media.durationMs).coerceIn(0f, 1f))
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransportButton(Icons.Filled.SkipPrevious, "Precedente", media?.canSkipPrevious == true, onPrevious)

            // Controllo principale, in tinta tenue (il pieno è riservato a quadrante e manovra). Cambia forma: cerchio in pausa, quadrato morbido in riproduzione.
            val playing = media?.isPlaying == true
            val corner by animateDpAsState(
                targetValue = if (playing) 16.dp else 28.dp,
                animationSpec = tween(durationMillis = 180, easing = EaseOutDash),
                label = "playShape",
            )
            Surface(
                onClick = { if (media != null) onPlayPause() else onOpen() },
                modifier = Modifier.size(width = 88.dp, height = 56.dp),
                shape = RoundedCornerShape(corner),
                color = if (media != null) Dash.BacklightDim else Dash.Track,
                contentColor = if (media != null) Dash.OnBacklightDim else Dash.Label,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playing) "Pausa" else "Riproduci",
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            TransportButton(Icons.Filled.SkipNext, "Successiva", media?.canSkipNext == true, onNext)
        }
    }
}

@Composable
private fun TransportButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(width = 72.dp, height = 56.dp),
        colors = IconButtonDefaults.iconButtonColors(contentColor = Dash.Numeral, disabledContentColor = Dash.Off),
    ) {
        Icon(icon, label, modifier = Modifier.size(32.dp))
    }
}

/** Avanzamento: una linea sottile e neutra, l'accento resta al quadrante e al play. */
@Composable
private fun ProgressLine(fraction: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(Dash.Track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(3.dp)
                .clip(CircleShape)
                .background(Dash.Label)
        )
    }
}

/** Posizione di riproduzione aggiornata ogni secondo. */
@Composable
private fun rememberPlaybackPosition(media: MediaState): State<Long> {
    val position = remember(media) { mutableLongStateOf(media.currentPosition()) }
    LaunchedEffect(media) {
        while (true) {
            position.longValue = media.currentPosition()
            delay(1_000)
        }
    }
    return position
}

// ---------------------------------------------------------------- Notifiche

/** Lista delle notifiche: niente pannello attorno, la struttura la fanno le righe tonali. */
@Composable
fun NotificationsList(
    notifications: List<CarNotification>,
    hasAccess: Boolean,
    onGrantAccess: () -> Unit,
    onOpen: (CarNotification) -> Unit,
    onDismiss: (CarNotification) -> Unit,
    onDismissAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("NOTIFICHE", style = MaterialTheme.typography.labelSmall, color = Dash.Label)
            if (hasAccess && notifications.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text("${notifications.size}", style = MaterialTheme.typography.labelSmall, color = Dash.Note)
            }
            Spacer(Modifier.weight(1f))
            if (hasAccess && notifications.any { it.clearable }) {
                TextButton(onClick = onDismissAll, modifier = Modifier.heightIn(min = 40.dp)) {
                    Text("Cancella tutte", style = MaterialTheme.typography.labelLarge, color = Dash.Label)
                }
            }
        }

        when {
            !hasAccess -> Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Serve l'accesso alle notifiche",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dash.Note,
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = onGrantAccess, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Concedi accesso")
                }
            }
            notifications.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("Nessuna notifica", style = MaterialTheme.typography.bodyMedium, color = Dash.Note)
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(notifications, key = { _, n -> n.key }) { index, n ->
                    NotificationRow(
                        n,
                        shape = segmentedShape(index, notifications.size),
                        onOpen = { onOpen(n) },
                        onDismiss = { onDismiss(n) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    n: CarNotification,
    shape: androidx.compose.ui.graphics.Shape,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(onClick = onOpen, modifier = Modifier.fillMaxWidth(), shape = shape, color = Dash.Panel) {
        Row(
            Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = n.largeIcon ?: n.appIcon
            if (icon != null) {
                Image(
                    icon.asImageBitmap(), null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Monogram(n.title.ifBlank { n.appLabel })
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${n.appLabel} · ${formatPostTime(n.postTime)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Dash.Note,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (n.title.isNotBlank()) {
                    Text(
                        n.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Dash.Numeral,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (n.text.isNotBlank()) {
                    Text(
                        n.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Dash.Label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (n.clearable) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, "Elimina", tint = Dash.Note, modifier = Modifier.size(20.dp))
                }
            } else {
                Spacer(Modifier.width(12.dp))
            }
        }
    }
}

/** Iniziali su fondo tonale, al posto di un'icona generica. */
@Composable
fun Monogram(name: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Dash.Track),
        contentAlignment = Alignment.Center,
    ) {
        Text(initialsOf(name), style = MaterialTheme.typography.titleSmall, color = Dash.Label)
    }
}

private fun formatPostTime(time: Long): String {
    val now = System.currentTimeMillis()
    return if (DateUtils.isToday(time)) {
        if (now - time < 60_000L) "ora" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
    } else {
        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(time))
    }
}

// ---------------------------------------------------------------- Footer

@Composable
fun Dock(
    dockApps: List<AppInfo>,
    navigationActive: Boolean,
    onApps: () -> Unit,
    onNavigation: () -> Unit,
    onPhone: () -> Unit,
    onClaude: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
) {
    // Pillola 36 con padding 8 → pulsanti con raggio 28 (concentrici).
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = Dash.Panel,
        contentColor = Dash.Numeral,
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            DockButton(onApps) { Icon(Icons.Filled.Apps, "App", modifier = Modifier.size(26.dp)) }
            DockButton(onNavigation, lit = navigationActive) {
                Icon(
                    if (navigationActive) Icons.Filled.Navigation else Icons.Filled.Search,
                    if (navigationActive) "Navigazione in corso" else "Cerca destinazione",
                    modifier = Modifier.size(26.dp),
                )
            }
            DockButton(onPhone) { Icon(Icons.Filled.Phone, "Telefono", modifier = Modifier.size(26.dp)) }
            dockApps.forEach { app ->
                DockButton({ onLaunch(app) }) {
                    val icon = app.icon
                    if (icon != null) {
                        Image(icon.asImageBitmap(), app.label, modifier = Modifier.size(36.dp))
                    } else {
                        Text(app.label.take(2), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            DockButton(onClaude) {
                Icon(Icons.Filled.GraphicEq, "Chat vocale con Claude", modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun RowScope.DockButton(
    onClick: () -> Unit,
    lit: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        color = if (lit) Dash.BacklightDim else Color.Transparent,
        contentColor = if (lit) Dash.OnBacklightDim else Dash.Label,
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

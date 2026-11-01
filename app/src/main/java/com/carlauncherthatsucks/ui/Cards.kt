package com.carlauncherthatsucks.ui

import android.text.format.DateUtils
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.CarNotification
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.NavigationInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- Navigazione

/** Prossima manovra di CoMaps, mostrata in home solo durante una navigazione. */
@Composable
fun NavigationCard(
    navigation: NavigationInfo,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    CarCard(
        Modifier.fillMaxWidth(),
        onClick = onClick,
        color = scheme.primary,
        shape = MaterialTheme.shapes.extraLarge,
        padding = 14,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(scheme.onPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = navigation.turnIcon
                if (icon != null) {
                    Image(icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(60.dp))
                } else {
                    Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    navigation.title.ifBlank { "Navigazione in corso" },
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
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
    val scheme = MaterialTheme.colorScheme
    CarCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(scheme.secondaryContainer)
                    .clickable(onClick = onOpen),
                contentAlignment = Alignment.Center,
            ) {
                val art = media?.art
                if (art != null) {
                    Image(
                        art.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        Icons.Filled.MusicNote, null,
                        tint = scheme.onSecondaryContainer, modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
            ) {
                when {
                    media != null -> {
                        Text(
                            media.title.ifBlank { media.appLabel },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            listOf(media.artist, media.album).filter { it.isNotBlank() }
                                .joinToString(" · ").ifBlank { " " },
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            media.appLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary,
                            maxLines = 1,
                        )
                    }
                    !hasAccess -> {
                        Text("Musica", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Concedi l'accesso alle notifiche per controllare la riproduzione",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Text("Nessuna riproduzione", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Tocca per aprire il lettore musicale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (media != null && media.durationMs > 0) {
            val position by rememberPlaybackPosition(media)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (position.toFloat() / media.durationMs).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = scheme.primary,
                trackColor = scheme.secondaryContainer,
            )
            Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
                Text(formatDuration(position), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(media.durationMs), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        } else {
            Spacer(Modifier.height(10.dp))
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onPrevious,
                enabled = media?.canSkipPrevious == true,
                modifier = Modifier.size(width = 76.dp, height = 60.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(Icons.Filled.SkipPrevious, "Precedente", modifier = Modifier.size(34.dp))
            }

            // Stile "expressive": il pulsante cambia forma (cerchio ↔ quadrato arrotondato).
            val playing = media?.isPlaying == true
            val corner by animateDpAsState(
                targetValue = if (playing) 22.dp else 40.dp,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                label = "playShape",
            )
            Surface(
                onClick = { if (media != null) onPlayPause() else onOpen() },
                modifier = Modifier.size(width = 104.dp, height = 72.dp),
                shape = RoundedCornerShape(corner),
                color = scheme.primary,
                contentColor = scheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pausa",
                        modifier = Modifier.size(42.dp),
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onNext,
                enabled = media?.canSkipNext == true,
                modifier = Modifier.size(width = 76.dp, height = 60.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Icon(Icons.Filled.SkipNext, "Successiva", modifier = Modifier.size(34.dp))
            }
        }
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

@Composable
fun NotificationsCard(
    notifications: List<CarNotification>,
    hasAccess: Boolean,
    onGrantAccess: () -> Unit,
    onOpen: (CarNotification) -> Unit,
    onDismiss: (CarNotification) -> Unit,
    onDismissAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    CarCard(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, padding = 12) {
        Row(
            Modifier.padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Notifications, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Notifiche", style = MaterialTheme.typography.titleMedium)
            if (notifications.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(scheme.primary)
                        .padding(horizontal = 8.dp, vertical = 1.dp)
                ) {
                    Text(
                        "${notifications.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (notifications.any { it.clearable }) {
                TextButton(onClick = onDismissAll) { Text("Cancella tutte") }
            }
        }
        Spacer(Modifier.height(4.dp))

        when {
            !hasAccess -> Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Per mostrare le notifiche serve l'accesso alle notifiche.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onGrantAccess) { Text("Concedi accesso") }
            }
            notifications.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nessuna notifica",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                itemsIndexed(notifications, key = { _, n -> n.key }) { index, n ->
                    NotificationRow(
                        n,
                        index = index,
                        count = notifications.size,
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
    index: Int,
    count: Int,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = segmentedShape(index, count),
        color = scheme.surfaceContainerHighest,
        contentColor = scheme.onSurface,
    ) {
        Row(
            Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = n.largeIcon ?: n.appIcon
            if (icon != null) {
                Image(
                    icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        n.appLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        " · " + formatPostTime(n.postTime),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (n.title.isNotBlank()) {
                    Text(
                        n.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (n.text.isNotBlank()) {
                    Text(
                        n.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (n.clearable) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Filled.Close, "Elimina", tint = scheme.onSurfaceVariant)
                }
            } else {
                Spacer(Modifier.width(12.dp))
            }
        }
    }
}

private fun formatPostTime(time: Long): String {
    val now = System.currentTimeMillis()
    return if (DateUtils.isToday(time)) {
        if (now - time < 60_000L) "ora"
        else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
    } else {
        SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(time))
    }
}

// ---------------------------------------------------------------- Dock

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
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockButton(onApps, highlighted = true) {
                Icon(Icons.Filled.Apps, "App", modifier = Modifier.size(28.dp))
            }
            DockButton(onNavigation, highlighted = navigationActive, accent = navigationActive) {
                Icon(
                    if (navigationActive) Icons.Filled.Navigation else Icons.Filled.Search,
                    if (navigationActive) "Navigazione in corso" else "Cerca destinazione",
                    modifier = Modifier.size(28.dp),
                )
            }
            DockButton(onPhone) { Icon(Icons.Filled.Phone, "Telefono", modifier = Modifier.size(28.dp)) }
            dockApps.forEach { app ->
                DockButton({ onLaunch(app) }) {
                    val icon = app.icon
                    if (icon != null) {
                        Image(icon.asImageBitmap(), app.label, modifier = Modifier.size(40.dp))
                    } else {
                        Text(app.label.take(2), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            DockButton(onClaude) {
                Icon(Icons.Filled.GraphicEq, "Chat vocale con Claude", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun RowScope.DockButton(
    onClick: () -> Unit,
    highlighted: Boolean = false,
    accent: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(width = 56.dp, height = 52.dp),
            shape = RoundedCornerShape(26.dp),
            color = when {
                accent -> scheme.primary
                highlighted -> scheme.primaryContainer
                else -> Color.Transparent
            },
            contentColor = when {
                accent -> scheme.onPrimary
                highlighted -> scheme.onPrimaryContainer
                else -> scheme.onSurface
            },
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
    }
}

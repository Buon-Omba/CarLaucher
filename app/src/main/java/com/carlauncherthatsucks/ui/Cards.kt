package com.carlauncherthatsucks.ui

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
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

@Composable
fun NavigationCard(
    navigation: NavigationInfo?,
    onOpenNavigation: () -> Unit,
    onSearch: () -> Unit,
    onHome: () -> Unit,
    onWork: () -> Unit,
    onMap: () -> Unit,
) {
    if (navigation != null) {
        // Navigazione attiva in CoMaps: mostriamo la prossima manovra.
        CarCard(
            Modifier
                .fillMaxWidth()
                .border(2.dp, CarColors.Accent, CardShape),
            onClick = onOpenNavigation,
            padding = 12,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CarColors.AccentDim),
                    contentAlignment = Alignment.Center,
                ) {
                    val icon = navigation.turnIcon
                    if (icon != null) {
                        Image(
                            icon.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                        )
                    } else {
                        Icon(
                            Icons.Filled.Navigation, null,
                            tint = CarColors.TextPrimary, modifier = Modifier.size(44.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        navigation.title.ifBlank { "Navigazione in corso" },
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (navigation.text.isNotBlank()) {
                        Text(
                            navigation.text,
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (navigation.subText.isNotBlank()) {
                        Text(
                            navigation.subText,
                            fontSize = 14.sp,
                            color = CarColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    } else {
        CarCard(Modifier.fillMaxWidth(), padding = 10) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BigAction(Icons.Filled.Search, "Cerca", onSearch, primary = true)
                BigAction(Icons.Filled.Home, "Casa", onHome)
                BigAction(Icons.Filled.Work, "Lavoro", onWork)
                BigAction(Icons.Filled.Map, "CoMaps", onMap)
            }
        }
    }
}

@Composable
private fun RowScope.BigAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
) {
    Column(
        Modifier
            .weight(1f)
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) CarColors.AccentDim else CarColors.CardHigh)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp), tint = CarColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 14.sp, maxLines = 1)
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
    CarCard(Modifier.fillMaxWidth(), padding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CarColors.CardHigh)
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
                        tint = CarColors.TextSecondary, modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onOpen)
            ) {
                when {
                    media != null -> {
                        Text(
                            media.title.ifBlank { media.appLabel },
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            listOf(media.artist, media.album).filter { it.isNotBlank() }
                                .joinToString(" · ").ifBlank { " " },
                            fontSize = 15.sp,
                            color = CarColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            media.appLabel,
                            fontSize = 12.sp,
                            color = CarColors.Accent,
                            maxLines = 1,
                        )
                    }
                    !hasAccess -> {
                        Text("Musica", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Concedi l'accesso alle notifiche per controllare la riproduzione",
                            fontSize = 13.sp,
                            color = CarColors.TextSecondary,
                        )
                    }
                    else -> {
                        Text("Nessuna riproduzione", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tocca per aprire il lettore musicale",
                            fontSize = 13.sp,
                            color = CarColors.TextSecondary,
                        )
                    }
                }
            }
        }

        if (media != null && media.durationMs > 0) {
            val position by produceState(media.currentPosition(), media) {
                while (true) {
                    value = media.currentPosition()
                    delay(1_000)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (position.toFloat() / media.durationMs).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CarColors.Accent,
                trackColor = CarColors.CardHigh,
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatDuration(position), fontSize = 11.sp, color = CarColors.TextSecondary)
                Spacer(Modifier.weight(1f))
                Text(formatDuration(media.durationMs), fontSize = 11.sp, color = CarColors.TextSecondary)
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrevious,
                enabled = media?.canSkipPrevious == true,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(Icons.Filled.SkipPrevious, "Precedente", modifier = Modifier.size(40.dp))
            }
            Box(
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CarColors.Accent)
                    .clickable { if (media != null) onPlayPause() else onOpen() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (media?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pausa",
                    tint = CarColors.Background,
                    modifier = Modifier.size(44.dp),
                )
            }
            IconButton(
                onClick = onNext,
                enabled = media?.canSkipNext == true,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(Icons.Filled.SkipNext, "Successiva", modifier = Modifier.size(40.dp))
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%d:%02d", m, s)
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
    CarCard(modifier.fillMaxWidth(), padding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Notifications, null, tint = CarColors.Accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Notifiche", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            if (notifications.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text("${notifications.size}", fontSize = 14.sp, color = CarColors.TextSecondary)
            }
            Spacer(Modifier.weight(1f))
            if (notifications.any { it.clearable }) {
                TextButton(onClick = onDismissAll) { Text("Cancella tutte") }
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
                    "Per mostrare le notifiche serve l'accesso alle notifiche.",
                    color = CarColors.TextSecondary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGrantAccess) { Text("Concedi accesso") }
            }
            notifications.isEmpty() -> Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("Nessuna notifica", color = CarColors.TextSecondary, fontSize = 15.sp)
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(notifications, key = { it.key }) { n ->
                    NotificationRow(n, onOpen = { onOpen(n) }, onDismiss = { onDismiss(n) })
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: CarNotification, onOpen: () -> Unit, onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CarColors.CardHigh)
            .clickable(onClick = onOpen)
            .padding(start = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = n.largeIcon ?: n.appIcon
        if (icon != null) {
            Image(
                icon.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    n.appLabel,
                    fontSize = 12.sp,
                    color = CarColors.Accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    " · " + formatPostTime(n.postTime),
                    fontSize = 12.sp,
                    color = CarColors.TextSecondary,
                    maxLines = 1,
                )
            }
            if (n.title.isNotBlank()) {
                Text(
                    n.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (n.text.isNotBlank()) {
                Text(
                    n.text,
                    fontSize = 14.sp,
                    color = CarColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (n.clearable) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Filled.Close, "Elimina", tint = CarColors.TextSecondary)
            }
        } else {
            Spacer(Modifier.width(10.dp))
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
    onApps: () -> Unit,
    onPhone: () -> Unit,
    onSettings: () -> Unit,
    onLaunch: (AppInfo) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(CardShape)
            .background(CarColors.Card)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockButton(onApps) { Icon(Icons.Filled.Apps, "App", modifier = Modifier.size(34.dp)) }
        DockButton(onPhone) { Icon(Icons.Filled.Phone, "Telefono", modifier = Modifier.size(32.dp)) }
        dockApps.forEach { app ->
            DockButton({ onLaunch(app) }) {
                val icon = app.icon
                if (icon != null) {
                    Image(icon.asImageBitmap(), app.label, modifier = Modifier.size(44.dp))
                } else {
                    Text(app.label.take(2), fontSize = 18.sp)
                }
            }
        }
        DockButton(onSettings) { Icon(Icons.Filled.Settings, "Impostazioni", modifier = Modifier.size(32.dp)) }
    }
}

@Composable
private fun DockButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

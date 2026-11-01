package com.carlauncherthatsucks.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherKind
import com.carlauncherthatsucks.data.WeatherStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Contenitore Material You: superficie tonale con angoli grandi. */
@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = CarColors.Card,
    shape: Shape = MaterialTheme.shapes.large,
    padding: Int = 14,
    content: @Composable ColumnScope.() -> Unit,
) {
    val contentColor = contentColorFor(color).takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onSurface
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
        ) { Column(Modifier.padding(padding.dp), content = content) }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
        ) { Column(Modifier.padding(padding.dp), content = content) }
    }
}

@Composable
fun PermissionBanner(
    needsLocation: Boolean,
    needsNotifications: Boolean,
    onLocation: () -> Unit,
    onNotifications: () -> Unit,
) {
    CarCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer, padding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Servono alcuni permessi", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (needsLocation) {
                FilledTonalButton(onClick = onLocation) { Text("Posizione") }
            }
            if (needsNotifications) {
                FilledTonalButton(onClick = onNotifications) { Text("Notifiche e musica") }
            }
        }
    }
}

@Composable
fun SpeedIndicator(speed: SpeedState, modifier: Modifier = Modifier) {
    CarCard(
        modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        padding = 12,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (speed.hasGpsFix) Icons.Filled.GpsFixed else Icons.Filled.GpsNotFixed,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (speed.hasGpsFix) "GPS" else "Ricerca GPS…",
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = speed.kmh?.toString() ?: "--",
                    fontSize = 78.sp,
                    lineHeight = 78.sp,
                    fontWeight = FontWeight.Black,
                )
                Text("km/h", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun ClockCard(modifier: Modifier = Modifier) {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L - value % 1_000L)
        }
    }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
    val date = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(now))
    CarCard(
        modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        padding = 8,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, fontSize = 32.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                Text(
                    date.replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun weatherIcon(w: Weather?): Pair<ImageVector, Color> {
    val onContainer = MaterialTheme.colorScheme.onTertiaryContainer
    if (w == null) return Icons.Filled.CloudOff to onContainer
    return when (w.kind) {
        WeatherKind.CLEAR -> if (w.isDay) Icons.Filled.WbSunny to CarColors.Sun
        else Icons.Filled.NightsStay to onContainer
        WeatherKind.PARTLY_CLOUDY -> Icons.Filled.FilterDrama to onContainer
        WeatherKind.CLOUDY -> Icons.Filled.Cloud to onContainer
        WeatherKind.FOG -> Icons.Filled.Dehaze to onContainer
        WeatherKind.DRIZZLE -> Icons.Filled.Grain to CarColors.Rain
        WeatherKind.RAIN -> Icons.Filled.Umbrella to CarColors.Rain
        WeatherKind.SNOW -> Icons.Filled.AcUnit to onContainer
        WeatherKind.THUNDER -> Icons.Filled.FlashOn to CarColors.Sun
        WeatherKind.UNKNOWN -> Icons.Filled.Cloud to onContainer
    }
}

@Composable
fun WeatherIndicator(
    weather: Weather?,
    status: WeatherStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = weatherIcon(weather)
    CarCard(
        modifier.fillMaxSize(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        padding = 8,
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = weather?.description, tint = tint, modifier = Modifier.size(38.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    weather?.let { "${it.temperatureC.roundToInt()}°" } ?: "--°",
                    fontSize = 30.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    when {
                        weather != null -> weather.description
                        status == WeatherStatus.NO_LOCATION -> "Imposta città"
                        status == WeatherStatus.NETWORK_ERROR -> "Offline"
                        else -> "Meteo…"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Impedisce che i tocchi su un pannello sovrapposto arrivino alla schermata sottostante. */
@Composable
fun Modifier.blockTouches(): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = {},
)

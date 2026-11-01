package com.carlauncherthatsucks.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherKind
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

val CardShape = RoundedCornerShape(20.dp)

@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = CarColors.Card,
    padding: Int = 14,
    content: @Composable ColumnScope.() -> Unit,
) {
    var m = modifier
        .clip(CardShape)
        .background(color)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(padding.dp), content = content)
}

@Composable
fun PermissionBanner(
    needsLocation: Boolean,
    needsNotifications: Boolean,
    onLocation: () -> Unit,
    onNotifications: () -> Unit,
) {
    CarCard(Modifier.fillMaxWidth(), color = Color(0xFF3A2A00), padding = 10) {
        Text(
            "Servono alcuni permessi per far funzionare tutto",
            color = CarColors.Warning,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Spacer(Modifier.size(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = ButtonDefaults.buttonColors(containerColor = CarColors.Warning)
            if (needsLocation) {
                Button(onClick = onLocation, colors = colors) { Text("Posizione", color = Color.Black) }
            }
            if (needsNotifications) {
                Button(onClick = onNotifications, colors = colors) {
                    Text("Notifiche e musica", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun SpeedIndicator(speed: SpeedState, modifier: Modifier = Modifier) {
    CarCard(modifier.fillMaxSize(), padding = 10) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (speed.hasGpsFix) Icons.Filled.GpsFixed else Icons.Filled.GpsNotFixed,
                contentDescription = null,
                tint = if (speed.hasGpsFix) CarColors.Accent else CarColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (speed.hasGpsFix) "GPS" else "Ricerca GPS…",
                color = CarColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = speed.kmh?.toString() ?: "--",
                    color = CarColors.TextPrimary,
                    fontSize = 76.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("km/h", color = CarColors.TextSecondary, fontSize = 16.sp)
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
    val time = formatTime(now)
    val date = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(now))
    CarCard(modifier.fillMaxSize(), padding = 8) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, fontSize = 30.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
                Text(
                    date.replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    fontSize = 12.sp,
                    color = CarColors.TextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun formatTime(now: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))

fun weatherIcon(w: Weather?): Pair<ImageVector, Color> {
    if (w == null) return Icons.Filled.CloudOff to CarColors.TextSecondary
    return when (w.kind) {
        WeatherKind.CLEAR -> if (w.isDay) Icons.Filled.WbSunny to CarColors.Sun
        else Icons.Filled.NightsStay to Color(0xFFB0C4DE)
        WeatherKind.PARTLY_CLOUDY -> Icons.Filled.FilterDrama to Color(0xFFDDE3EA)
        WeatherKind.CLOUDY -> Icons.Filled.Cloud to Color(0xFFB8C0C8)
        WeatherKind.FOG -> Icons.Filled.Dehaze to Color(0xFFB8C0C8)
        WeatherKind.DRIZZLE -> Icons.Filled.Grain to CarColors.Rain
        WeatherKind.RAIN -> Icons.Filled.Umbrella to CarColors.Rain
        WeatherKind.SNOW -> Icons.Filled.AcUnit to Color(0xFFE8F4FF)
        WeatherKind.THUNDER -> Icons.Filled.FlashOn to CarColors.Warning
        WeatherKind.UNKNOWN -> Icons.Filled.Cloud to CarColors.TextSecondary
    }
}

@Composable
fun WeatherIndicator(
    weather: Weather?,
    hasLocation: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = weatherIcon(weather)
    CarCard(modifier.fillMaxSize(), onClick = onClick, padding = 8) {
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
                    fontSize = 28.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    when {
                        weather != null -> weather.description
                        !hasLocation -> "No posizione"
                        else -> "Meteo…"
                    },
                    fontSize = 12.sp,
                    color = CarColors.TextSecondary,
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

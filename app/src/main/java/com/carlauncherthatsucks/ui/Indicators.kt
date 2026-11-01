package com.carlauncherthatsucks.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Umbrella
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherKind
import com.carlauncherthatsucks.data.WeatherStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Curva d'uscita decisa: parte subito, si posa morbida. */
val EaseOutDash = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
/** Per movimenti sullo schermo (es. la lancetta del quadrante). */
val EaseInOutDash = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

/** Pannello: superficie tonale, un gradino sopra il fondo. */
@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Dash.Panel,
    shape: Shape = MaterialTheme.shapes.large,
    padding: Int = 16,
    content: @Composable ColumnScope.() -> Unit,
) {
    val contentColor = contentColorFor(color).takeIf { it != Color.Unspecified } ?: Dash.Numeral
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, contentColor = contentColor) {
            Column(Modifier.padding(padding.dp), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = color, contentColor = contentColor) {
            Column(Modifier.padding(padding.dp), content = content)
        }
    }
}

// ---------------------------------------------------------------- Spia ambra

/** Lampadina della spia: piccolo punto ambra, come sul cruscotto. */
@Composable
fun Lamp(color: Color = Dash.WarningLamp, size: Dp = 8.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

/** Permessi mancanti: una spia, non un allarme. */
@Composable
fun WarningLampRow(
    needsLocation: Boolean,
    needsNotifications: Boolean,
    onLocation: () -> Unit,
    onNotifications: () -> Unit,
) {
    CarCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, padding = 12) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Lamp()
            Spacer(Modifier.width(10.dp))
            Text(
                "Mancano dei permessi",
                style = MaterialTheme.typography.titleSmall,
                color = Dash.Numeral,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val pad = PaddingValues(horizontal = 16.dp)
            if (needsLocation) {
                FilledTonalButton(onClick = onLocation, contentPadding = pad, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Posizione")
                }
            }
            if (needsNotifications) {
                FilledTonalButton(onClick = onNotifications, contentPadding = pad, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Notifiche e musica")
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Riga di stato

/** In alto, come la striscia del quadro strumenti: ora, spia GPS, meteo. */
@Composable
fun StatusStrip(
    weather: Weather?,
    weatherStatus: WeatherStatus,
    hasGpsFix: Boolean,
    onWeatherClick: () -> Unit,
) {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L - value % 1_000L)
        }
    }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))
    val date = SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(now))
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, style = MaterialTheme.typography.headlineSmall, color = Dash.Numeral)
        Spacer(Modifier.width(8.dp))
        Text(date, style = MaterialTheme.typography.labelLarge, color = Dash.Label)

        Spacer(Modifier.weight(1f))

        if (!hasGpsFix) {
            Lamp()
            Spacer(Modifier.width(6.dp))
            Text("GPS", style = MaterialTheme.typography.labelMedium, color = Dash.WarningLamp)
            Spacer(Modifier.width(16.dp))
        }

        val (icon, tint) = weatherIcon(weather)
        Row(
            Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onWeatherClick)
                .heightIn(min = 48.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = weather?.description, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
            if (weather != null) {
                Text(
                    "${weather.temperatureC.roundToInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Dash.Numeral,
                )
            } else {
                Text(
                    when (weatherStatus) {
                        WeatherStatus.NO_LOCATION -> "Imposta città"
                        WeatherStatus.NETWORK_ERROR -> "Offline"
                        else -> "Meteo…"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = Dash.Label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun weatherIcon(w: Weather?): Pair<ImageVector, Color> {
    val neutral = Dash.Label
    if (w == null) return Icons.Filled.CloudOff to Dash.Note
    return when (w.kind) {
        WeatherKind.CLEAR -> if (w.isDay) Icons.Filled.WbSunny to Dash.Sun else Icons.Filled.NightsStay to neutral
        WeatherKind.PARTLY_CLOUDY -> Icons.Filled.FilterDrama to neutral
        WeatherKind.CLOUDY -> Icons.Filled.Cloud to neutral
        WeatherKind.FOG -> Icons.Filled.Dehaze to neutral
        WeatherKind.DRIZZLE -> Icons.Filled.Grain to Dash.Rain
        WeatherKind.RAIN -> Icons.Filled.Umbrella to Dash.Rain
        WeatherKind.SNOW -> Icons.Filled.AcUnit to neutral
        WeatherKind.THUNDER -> Icons.Filled.FlashOn to Dash.Sun
        WeatherKind.UNKNOWN -> Icons.Filled.Cloud to neutral
    }
}

// ---------------------------------------------------------------- Quadrante

/** Fondo scala del quadrante. */
const val DIAL_MAX_KMH = 180

/**
 * Tachimetro: arco di 240° con tacche ogni 20 km/h; la retroilluminazione riempie l'arco
 * fino alla velocità attuale. "km/h" sta nella fessura in basso, come su un quadrante vero.
 * È l'elemento che domina la home (e, in piccolo, il pannello sopra CoMaps).
 */
@Composable
fun SpeedDial(
    speed: SpeedState,
    diameter: Dp,
    modifier: Modifier = Modifier,
    numeralStyle: TextStyle = MaterialTheme.typography.displayLarge,
    unitStyle: TextStyle = MaterialTheme.typography.labelLarge,
    stroke: Dp = 12.dp,
    showTicks: Boolean = true,
) {
    val kmh = speed.kmh
    val fraction by animateFloatAsState(
        targetValue = ((kmh ?: 0).toFloat() / DIAL_MAX_KMH).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = EaseInOutDash),
        label = "dial",
    )
    val track = Dash.Track
    val glow = Dash.Backlight
    val tick = Dash.Note

    Layout(
        content = {
            Text(kmh?.toString() ?: "--", style = numeralStyle, color = if (kmh != null) Dash.Numeral else Dash.Off)
            Text("km/h", style = unitStyle, color = Dash.Label)
        },
        // L'arco finisce a ~3/4 del diametro: niente spazio morto sotto.
        modifier = modifier
            .size(width = diameter, height = diameter * DIAL_HEIGHT_RATIO)
            .drawBehind {
                val w = size.width
                val sw = stroke.toPx()
                val arcSize = Size(w - sw, w - sw)
                val topLeft = Offset(sw / 2, sw / 2)
                drawArc(track, START_ANGLE, SWEEP, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                if (fraction > 0f) {
                    drawArc(glow, START_ANGLE, SWEEP * fraction, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                }
                if (showTicks) {
                    val r = w / 2
                    val outer = r - sw - 6.dp.toPx()
                    val inner = outer - 6.dp.toPx()
                    val c = Offset(r, r)
                    for (v in 0..DIAL_MAX_KMH step 20) {
                        val a = Math.toRadians((START_ANGLE + SWEEP * v / DIAL_MAX_KMH).toDouble())
                        val dir = Offset(cos(a).toFloat(), sin(a).toFloat())
                        drawLine(tick, c + dir * inner, c + dir * outer, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    }
                }
            },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val numeral = measurables[0].measure(loose)
        val unit = measurables[1].measure(loose)
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        layout(w, h) {
            // Numero centrato sul centro del cerchio, unità nella fessura in basso.
            numeral.place((w - numeral.width) / 2, w / 2 - numeral.height / 2)
            unit.place((w - unit.width) / 2, h - unit.height)
        }
    }
}

private const val DIAL_HEIGHT_RATIO = 0.84f
private const val START_ANGLE = 150f
private const val SWEEP = 240f

/** Impedisce che i tocchi su un pannello sovrapposto arrivino alla schermata sottostante. */
@Composable
fun Modifier.blockTouches(): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = {},
)

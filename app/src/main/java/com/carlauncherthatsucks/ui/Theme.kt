package com.carlauncherthatsucks.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.R

/*
 * Direzione: quadro strumenti di notte.
 * Superfici grafite che salgono di pochi punti di luminosità, una sola "retroilluminazione"
 * (il colore dinamico di Material You) per ciò che conta, l'ambra delle spie per gli avvisi.
 * Profondità solo per cambio di tono: niente bordi, niente ombre (tranne il pannello sopra CoMaps).
 */
object Dash {
    /** Fondo: l'asfalto/la plancia. */
    val Asphalt: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    /** Pannello: un gradino sopra il fondo. */
    val Panel: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
    /** Pannello rialzato: righe ed elementi dentro un pannello. */
    val PanelRaised: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    /** Traccia del quadrante e controlli inattivi. */
    val Track: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest

    /** Testo, 4 livelli: numerale → etichetta → nota → spento. */
    val Numeral: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
    val Label: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Note: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val Off: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    /** Retroilluminazione: l'unico accento (colore dinamico). */
    val Backlight: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
    val OnBacklight: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
    val BacklightDim: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
    val OnBacklightDim: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

    /** Spia ambra del cruscotto: avvisi e permessi mancanti. */
    val WarningLamp: Color @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color(0xFFFFB74D) else Color(0xFF9A5B00)

    /** Verde "cornetta": solo per avviare una chiamata. */
    val CallGreen = Color(0xFF1E8E3E)

    /** Colori del meteo: hanno un significato (sole, pioggia). */
    val Sun = Color(0xFFFFC83D)
    val Rain = Color(0xFF5DADE2)
}

private val fallbackDark = darkColorScheme(
    primary = Color(0xFF7DDBB0),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFF99F8CB),
    secondary = Color(0xFFB4CCBE),
    onSecondary = Color(0xFF20352B),
    secondaryContainer = Color(0xFF364B40),
    onSecondaryContainer = Color(0xFFD0E8DA),
    tertiary = Color(0xFFA4CDDE),
    onTertiary = Color(0xFF063543),
    tertiaryContainer = Color(0xFF234C5A),
    onTertiaryContainer = Color(0xFFC0E9FA),
    background = Color(0xFF0E1110),
    surface = Color(0xFF0E1110),
    surfaceContainerLowest = Color(0xFF090B0A),
    surfaceContainerLow = Color(0xFF151917),
    surfaceContainer = Color(0xFF191D1B),
    surfaceContainerHigh = Color(0xFF222725),
    surfaceContainerHighest = Color(0xFF2C3230),
    onSurface = Color(0xFFE2E6E3),
    onSurfaceVariant = Color(0xFFB7C1BB),
)

private val fallbackLight = lightColorScheme(
    primary = Color(0xFF006C4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF99F8CB),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4D6357),
    secondaryContainer = Color(0xFFD0E8DA),
    onSecondaryContainer = Color(0xFF0A1F16),
    tertiary = Color(0xFF3C6472),
    tertiaryContainer = Color(0xFFC0E9FA),
    onTertiaryContainer = Color(0xFF001F28),
    background = Color(0xFFF4F7F5),
    surface = Color(0xFFF4F7F5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEEF2EF),
    surfaceContainer = Color(0xFFE8ECE9),
    surfaceContainerHigh = Color(0xFFE1E6E3),
    surfaceContainerHighest = Color(0xFFDADFDC),
    onSurface = Color(0xFF151917),
    onSurfaceVariant = Color(0xFF3F4944),
)

/** Raggi: 12 controlli · 20 righe · 28 pannelli · 36 contenitori grandi. */
private val dashShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Barlow Semi Condensed: nasce dalla segnaletica stradale; per numeri e titoli. */
val Signage = FontFamily(
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)

/** Cifre a larghezza fissa: i numeri che cambiano non fanno "ballare" il layout. */
private const val TABULAR = "tnum"

/*
 * Scala tipografica, rapporto ~1.25 su base 14:
 * 11 · 12 · 14 · 16 · 20 · 26 · 34 · display 92 (velocità).
 * La gerarchia la fanno peso e colore oltre alla dimensione.
 */
private val base = Typography()
private val dashType = Typography(
    displayLarge = TextStyle(
        fontFamily = Signage, fontWeight = FontWeight.Bold, fontSize = 92.sp, lineHeight = 92.sp,
        letterSpacing = (-0.02).em, fontFeatureSettings = TABULAR,
    ),
    headlineMedium = TextStyle(
        fontFamily = Signage, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp,
        letterSpacing = (-0.01).em, fontFeatureSettings = TABULAR,
    ),
    headlineSmall = TextStyle(
        fontFamily = Signage, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Signage, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp,
        fontFeatureSettings = TABULAR,
    ),
    titleMedium = base.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = base.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = base.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = TABULAR),
    // Intestazioni di sezione: piccole, spaziate, maiuscole (come le scritte del cruscotto).
    labelSmall = base.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.em),
)

@Composable
fun CarTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> fallbackDark
        else -> fallbackLight
    }
    MaterialTheme(
        colorScheme = scheme,
        shapes = dashShapes,
        typography = dashType,
        content = content,
    )
}

/**
 * Forma "segmentata" per gli elementi di una lista: angoli grandi all'esterno del gruppo,
 * piccoli tra un elemento e l'altro.
 */
fun segmentedShape(index: Int, count: Int, outer: Dp = 20.dp, inner: Dp = 6.dp): Shape {
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

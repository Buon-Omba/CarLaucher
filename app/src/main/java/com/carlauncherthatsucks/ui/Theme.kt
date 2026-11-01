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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ruoli colore usati dal launcher, mappati sullo schema Material You corrente
 * (colori dinamici dallo sfondo su Android 12+, palette verde di riserva altrimenti).
 */
object CarColors {
    val Background: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    val Card: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
    val CardHigh: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
    val Accent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
    val OnAccent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
    val AccentDim: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
    val OnAccentDim: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val Warning: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

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
    background = Color(0xFF0F1512),
    surface = Color(0xFF0F1512),
    surfaceContainerLowest = Color(0xFF0A0F0D),
    surfaceContainerLow = Color(0xFF171D1A),
    surfaceContainer = Color(0xFF1B211E),
    surfaceContainerHigh = Color(0xFF252B28),
    surfaceContainerHighest = Color(0xFF303633),
    onSurface = Color(0xFFDEE4DF),
    onSurfaceVariant = Color(0xFFBFC9C2),
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
    background = Color(0xFFF5FBF6),
    surface = Color(0xFFF5FBF6),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF5F0),
    surfaceContainer = Color(0xFFE9EFEA),
    surfaceContainerHigh = Color(0xFFE3EAE5),
    surfaceContainerHighest = Color(0xFFDEE4DF),
    onSurface = Color(0xFF171D1A),
    onSurfaceVariant = Color(0xFF404943),
)

/** Forme "expressive": angoli molto arrotondati come in Android 16/17. */
private val expressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private val baseType = Typography()
private val expressiveType = Typography(
    displayLarge = baseType.displayLarge.copy(fontWeight = FontWeight.Bold),
    displayMedium = baseType.displayMedium.copy(fontWeight = FontWeight.Bold),
    headlineLarge = baseType.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = baseType.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = baseType.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = baseType.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
fun CarTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> fallbackDark
        else -> fallbackLight
    }
    MaterialTheme(
        colorScheme = scheme,
        shapes = expressiveShapes,
        typography = expressiveType,
        content = content,
    )
}

/**
 * Forma "segmentata" per gli elementi di una lista (stile impostazioni di Android 16):
 * angoli grandi all'esterno del gruppo, piccoli tra un elemento e l'altro.
 */
fun segmentedShape(index: Int, count: Int, outer: Dp = 24.dp, inner: Dp = 6.dp): Shape {
    val top = if (index == 0) outer else inner
    val bottom = if (index == count - 1) outer else inner
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

package com.carlauncherthatsucks.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CarColors {
    val Background = Color(0xFF000000)
    val Card = Color(0xFF15171A)
    val CardHigh = Color(0xFF22252A)
    val Accent = Color(0xFF2ECC8F)
    val AccentDim = Color(0xFF1B5E45)
    val Warning = Color(0xFFFFB020)
    val TextPrimary = Color(0xFFF2F2F2)
    val TextSecondary = Color(0xFF9AA0A6)
    val Sun = Color(0xFFFFC83D)
    val Rain = Color(0xFF5DADE2)
}

private val scheme = darkColorScheme(
    primary = CarColors.Accent,
    onPrimary = Color.Black,
    secondary = CarColors.Accent,
    background = CarColors.Background,
    onBackground = CarColors.TextPrimary,
    surface = CarColors.Card,
    onSurface = CarColors.TextPrimary,
    surfaceVariant = CarColors.CardHigh,
    onSurfaceVariant = CarColors.TextSecondary,
)

@Composable
fun CarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

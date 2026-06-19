package com.finrein.pals.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PalBlack,
    onPrimary = PalWhite,
    background = PalBackground,
    onBackground = PalTextDark,
    surface = PalWhite,
    onSurface = PalTextDark,
    secondary = PalPlantPurple,
    onSecondary = PalWhite,
    error = PalFireRed,
    onError = PalWhite
)

// We fallback to LightColorScheme for both light and dark modes to match the specific 
// playful light graphic design style requested by the user, but we can customize if needed.
private val DarkColorScheme = darkColorScheme(
    primary = PalWhite,
    onPrimary = PalBlack,
    background = Color(0xFF121212),
    onBackground = PalWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = PalWhite,
    secondary = PalPlantPurple,
    onSecondary = PalBlack,
    error = PalFireRed,
    onError = PalBlack
)

@Composable
fun PALTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

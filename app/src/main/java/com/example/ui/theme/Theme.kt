package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = PureBlack,
    primaryContainer = SecondaryLime,
    onPrimaryContainer = PureBlack,
    secondary = AccentGray,
    onSecondary = PureBlack,
    tertiary = AlertAmber,
    background = DarkBackground, // #171717
    surface = DarkCardSurface,    // #1F1F1F
    surfaceVariant = DarkCardElevated,
    onBackground = BackgroundWhite,
    onSurface = BackgroundWhite,
    onSurfaceVariant = TextMuted,
    outline = DarkCardBorder,
    error = EmergencyRedGlow
)

private val LightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = NeonLime,
    primaryContainer = NeonLime,
    onPrimaryContainer = PureBlack,
    secondary = TextMuted,
    onSecondary = BackgroundWhite,
    secondaryContainer = SurfaceLightGray,
    onSecondaryContainer = PureBlack,
    tertiary = AlertAmber,
    background = DarkBackground, // Modern dark-first theme with neon lime
    surface = DarkCardSurface,
    surfaceVariant = DarkCardElevated,
    onBackground = BackgroundWhite,
    onSurface = BackgroundWhite,
    onSurfaceVariant = TextMuted,
    outline = DarkCardBorder,
    error = EmergencyRed
)

@Composable
fun NagpurSurakshaTheme(
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

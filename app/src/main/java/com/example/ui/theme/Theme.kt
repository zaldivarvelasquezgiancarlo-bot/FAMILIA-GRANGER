package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrotexDarkColorScheme = darkColorScheme(
    primary = BrotexCyanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = BrotexCyanSecondary,
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = BrotexCyanAccent,
    onTertiary = Color(0xFF003640),
    background = BrotexDarkBackground,
    onBackground = BrotexSlateText,
    surface = BrotexNavyDark,
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = BrotexNavySurface,
    onSurfaceVariant = BrotexSlateText,
    outline = BrotexBorder,
    error = StatusAbsent,
    onError = Color.White
)

private val BrotexLightColorScheme = lightColorScheme(
    primary = Color(0xFF006877),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA1EFFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF006584),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC2E8FF),
    onSecondaryContainer = Color(0xFF001E2B),
    tertiary = Color(0xFF006876),
    background = Color(0xFFF7FAFC),
    onBackground = Color(0xFF191C1E),
    surface = Color.White,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE0E8F0),
    onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFFB0BEC5),
    error = StatusAbsent,
    onError = Color.White
)

@Composable
fun BrotexTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark mode for mechatronics & night labs
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BrotexDarkColorScheme else BrotexLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

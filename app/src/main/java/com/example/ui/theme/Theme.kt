package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Gold60,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF241F0C),
    onPrimaryContainer = Gold80,
    secondary = StudioAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2B2005),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = StudioCyan,
    onTertiary = Color.Black,
    background = MahoganyDark,
    onBackground = Color(0xFFE0E0E0),
    surface = MahoganyPanel,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = MahoganyRack,
    onSurfaceVariant = Color(0xFF888888),
    outline = MahoganyBorder
)

@Composable
fun ConcertGrandTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

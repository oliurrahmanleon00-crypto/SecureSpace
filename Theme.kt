package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF001B24),
    primaryContainer = Color(0xFF003648),
    onPrimaryContainer = ElectricCyan,
    secondary = SapphireBlue,
    onSecondary = Color(0xFF001F2A),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = SapphireBlue,
    tertiary = NeonEmerald,
    onTertiary = Color(0xFF00210E),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = NeonEmerald,
    background = CyberNavyDark,
    onBackground = TextPrimary,
    surface = CyberSlate900,
    onSurface = TextPrimary,
    surfaceVariant = CyberSlate800,
    onSurfaceVariant = TextSecondary,
    outline = CyberSlate700,
    outlineVariant = CyberSlate600,
    error = CriticalRed,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF001B24),
    background = CyberNavyDark,
    surface = CyberSlate900,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // We enforce the Knox cyber aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

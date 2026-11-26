package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ThemeColors.elegantDarkPrimary,
    secondary = ThemeColors.elegantDarkAccent,
    background = ThemeColors.elegantDarkBackground,
    surface = ThemeColors.elegantDarkSurface,
    onPrimary = ThemeColors.elegantDarkOnPrimary,
    onBackground = ThemeColors.elegantDarkText,
    onSurface = ThemeColors.elegantDarkText,
    surfaceVariant = Color(0xFF161B26),
    onSurfaceVariant = ThemeColors.elegantDarkTextSecondary
)

private val NeonColorScheme = darkColorScheme(
    primary = ThemeColors.neonPrimary,
    secondary = ThemeColors.neonAccent,
    background = ThemeColors.neonBackground,
    surface = ThemeColors.neonSurface,
    onPrimary = ThemeColors.neonOnPrimary,
    onBackground = ThemeColors.neonText,
    onSurface = ThemeColors.neonText,
    surfaceVariant = Color(0xFF1A0E2E),
    onSurfaceVariant = Color(0xFFC084FC)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = ThemeColors.emeraldPrimary,
    secondary = ThemeColors.emeraldAccent,
    background = ThemeColors.emeraldBackground,
    surface = ThemeColors.emeraldSurface,
    onPrimary = ThemeColors.emeraldOnPrimary,
    onBackground = ThemeColors.emeraldText,
    onSurface = ThemeColors.emeraldText,
    surfaceVariant = Color(0xFF0B3A23),
    onSurfaceVariant = Color(0xFF6EE7B7)
)

private val AmberColorScheme = darkColorScheme(
    primary = ThemeColors.amberPrimary,
    secondary = ThemeColors.amberAccent,
    background = ThemeColors.amberBackground,
    surface = ThemeColors.amberSurface,
    onPrimary = ThemeColors.amberOnPrimary,
    onBackground = ThemeColors.amberText,
    onSurface = ThemeColors.amberText,
    surfaceVariant = Color(0xFF2C1908),
    onSurfaceVariant = Color(0xFFFDBA74)
)

private val PastelColorScheme = lightColorScheme(
    primary = ThemeColors.pastelPrimary,
    secondary = ThemeColors.pastelAccent,
    background = ThemeColors.pastelBackground,
    surface = ThemeColors.pastelSurface,
    onPrimary = ThemeColors.pastelOnPrimary,
    onBackground = ThemeColors.pastelText,
    onSurface = ThemeColors.pastelText,
    surfaceVariant = Color(0xFFFFE4E6),
    onSurfaceVariant = Color(0xFF9F1239)
)

@Composable
fun PuzzleNovaTheme(
    themeName: String = "Elegant Dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Neon Eclipse" -> NeonColorScheme
        "Emerald Forest" -> EmeraldColorScheme
        "Amber Glow" -> AmberColorScheme
        "Pastel Dreams" -> PastelColorScheme
        else -> ElegantDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

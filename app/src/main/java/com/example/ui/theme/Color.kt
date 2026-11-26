package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Material 3 colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- Puzzle Nova Dynamic Themes Palettes ---

object ThemeColors {
    // 1. Elegant Dark (Premium Deep Night & Indigo Glow)
    val elegantDarkBackground = Color(0xFF0B0E14)
    val elegantDarkSurface = Color(0xFF1A1F2B)
    val elegantDarkPrimary = Color(0xFF6366F1)   // Indigo 500
    val elegantDarkAccent = Color(0xFF818CF8)    // Indigo 400
    val elegantDarkOnPrimary = Color(0xFFFFFFFF)
    val elegantDarkText = Color(0xFFF1F5F9)      // Slate 100
    val elegantDarkTextSecondary = Color(0xFF94A3B8) // Slate 400

    // 2. Neon Eclipse (Vibrant Midnight Purple Cyberpunk)
    val neonBackground = Color(0xFF090514)
    val neonSurface = Color(0xFF1A0E2E)
    val neonPrimary = Color(0xFFD946EF)     // Neon Magenta
    val neonAccent = Color(0xFF8B5CF6)      // Electric Purple
    val neonOnPrimary = Color(0xFFFFFFFF)
    val neonText = Color(0xFFFDF4FF)
    val neonTextSecondary = Color(0xFFC084FC)

    // 3. Emerald Forest (Relaxing Natural Green)
    val emeraldBackground = Color(0xFF061F12)
    val emeraldSurface = Color(0xFF0B3A23)
    val emeraldPrimary = Color(0xFF10B981)   // Bright Emerald
    val emeraldAccent = Color(0xFFF59E0B)    // Amber Gold
    val emeraldOnPrimary = Color(0xFF061F12)
    val emeraldText = Color(0xFFECFDF5)
    val emeraldTextSecondary = Color(0xFF6EE7B7)

    // 4. Amber Glow (Warm Volcano Orange)
    val amberBackground = Color(0xFF180E04)
    val amberSurface = Color(0xFF2C1908)
    val amberPrimary = Color(0xFFF97316)     // Lava Orange
    val amberAccent = Color(0xFFEF4444)      // Fire Red
    val amberOnPrimary = Color(0xFF180E04)
    val amberText = Color(0xFFFFF7ED)
    val amberTextSecondary = Color(0xFFFDBA74)

    // 5. Pastel Dreams (Sweet Dreamy Playful Light/Kids Theme)
    val pastelBackground = Color(0xFFFFF1F2)
    val pastelSurface = Color(0xFFFFE4E6)
    val pastelPrimary = Color(0xFFEC4899)     // Bubblegum Pink
    val pastelAccent = Color(0xFF14B8A6)      // Mint Green
    val pastelOnPrimary = Color(0xFFFFFFFF)
    val pastelText = Color(0xFF4C0519)
    val pastelTextSecondary = Color(0xFF9F1239)
}

// --- Game Pieces Colors (Vibrant, high-contrast, for color sorting & matching) ---
val PuzzleColors = listOf(
    Color(0xFFEF4444), // 0: Fire Red
    Color(0xFF3B82F6), // 1: Neon Blue
    Color(0xFF10B981), // 2: Emerald Green
    Color(0xFFF59E0B), // 3: Gold Yellow
    Color(0xFF8B5CF6), // 4: Violet Purple
    Color(0xFFEC4899), // 5: Bubblegum Pink
    Color(0xFF06B6D4), // 6: Ocean Cyan
    Color(0xFFF97316), // 7: Sunset Orange
    Color(0xFF14B8A6), // 8: Pale Teal
    Color(0xFF84CC16)  // 9: Lime Lime
)

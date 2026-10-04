package com.focusos.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Glassmorphic Obsidian Dark Theme Palette
val GlassBgDark = Color(0xFF090B10)
val GlassBgDarkSecondary = Color(0xFF101420)
val GlassDarkSurface = Color(0x1AFFFFFF) // 10% white frosted surface
val GlassDarkCard = Color(0x14FFFFFF) // 8% white frosted card
val GlassDarkCardHover = Color(0x22FFFFFF)
val GlassDarkElevated = Color(0x25FFFFFF)
val GlassDarkBorder = Color(0x2BFFFFFF) // 17% white luminous border
val GlassDarkBorderSubtle = Color(0x14FFFFFF) // 8% border
val GlassDarkTextPrimary = Color(0xFFFFFFFF)
val GlassDarkTextSecondary = Color(0xFF9E9EA7)
val GlassDarkTextTertiary = Color(0xFF6E6E77)

// Glassmorphic Light Theme Palette
val GlassLightBg = Color(0xFFF0F2F8)
val GlassLightSurface = Color(0x80FFFFFF)
val GlassLightCard = Color(0x99FFFFFF)
val GlassLightElevated = Color(0xB3FFFFFF)
val GlassLightBorder = Color(0x40FFFFFF)
val GlassLightTextPrimary = Color(0xFF0F172A)
val GlassLightTextSecondary = Color(0xFF475569)
val GlassLightTextTertiary = Color(0xFF94A3B8)

// Vibrant Semantic Accents for Glass Glows
val AccentBlue = Color(0xFF3898FF)
val AccentBlueLight = Color(0xFF007AFF)
val AccentCyan = Color(0xFF00F5D4)
val AccentPurple = Color(0xFFA855F7)
val AccentPink = Color(0xFFF43F5E)

val StatusGreen = Color(0xFF10B981)
val StatusOrange = Color(0xFFF59E0B)
val StatusRed = Color(0xFFEF4444)
val StatusTeal = Color(0xFF06B6D4)
val StatusIndigo = Color(0xFF6366F1)

// Glass Subtle Background Glows
val StatusGreenSubtle = Color(0x2410B981)
val StatusOrangeSubtle = Color(0x24F59E0B)
val StatusRedSubtle = Color(0x24EF4444)
val StatusBlueSubtle = Color(0x243898FF)
val StatusPurpleSubtle = Color(0x24A855F7)
val StatusTealSubtle = Color(0x2406B6D4)

// Gradient Brushes for Glass UI
val GlassBorderGradient = Brush.linearGradient(
    listOf(
        Color(0x55FFFFFF),
        Color(0x18FFFFFF),
        Color(0x08FFFFFF)
    )
)

val GlassAccentBorderGradient = Brush.linearGradient(
    listOf(
        Color(0x803898FF),
        Color(0x20A855F7),
        Color(0x08FFFFFF)
    )
)

val GlassSurfaceGradient = Brush.verticalGradient(
    listOf(
        Color(0x1FFFFFFF),
        Color(0x0AFFFFFF)
    )
)

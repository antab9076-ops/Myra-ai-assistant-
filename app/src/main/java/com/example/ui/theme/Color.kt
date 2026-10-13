package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Obsidian & Deep Purple Backdrops
val BackgroundDark = Color(0xFF070312)
val BackgroundSurface = Color(0xFF0D061F)
val SurfaceCard = Color(0x3328124D)
val SurfaceCardHigh = Color(0x5538186D)
val GlassBorder = Color(0x35E040FB)
val GlassBorderHighlight = Color(0x80BD00FF)

// Vibrant Neon Accents
val NeonPurple = Color(0xFFBD00FF)
val NeonMagenta = Color(0xFFE040FB)
val ElectricViolet = Color(0xFF9D4EDD)
val CyberPink = Color(0xFFF43F5E)
val ElectricCyan = Color(0xFF00F0FF)
val GlowingIndigo = Color(0xFF6366F1)

// Text and Controls
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB8A9D9)
val TextMuted = Color(0xFF75629E)
val TextInverse = Color(0xFF0B0418)

// Status colors
val StatusOnline = Color(0xFF10B981)
val StatusListening = Color(0xFF00F0FF)
val StatusThinking = Color(0xFFF59E0B)
val StatusSpeaking = Color(0xFFE040FB)

// Pre-built gradient brushes
val PrimaryGlowGradient = Brush.linearGradient(
    colors = listOf(NeonMagenta, NeonPurple, GlowingIndigo)
)

val OrbCoreGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFF77E9),
        Color(0xFFBD00FF),
        Color(0xFF6A0DAD),
        Color(0x00000000)
    )
)

val CosmicBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF06020E),
        Color(0xFF0D051F),
        Color(0xFF14082D),
        Color(0xFF080214)
    )
)

val GlassCardBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x28FFFFFF),
        Color(0x12FFFFFF),
        Color(0x05FFFFFF)
    )
)

val BorderGlowBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x80BD00FF),
        Color(0x30E040FB),
        Color(0x6000F0FF)
    )
)

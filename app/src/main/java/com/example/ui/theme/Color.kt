package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/*
 * AntiMager Liquid Glass
 *
 * Inspired by the restrained translucent surfaces used across modern iOS apps:
 * dark graphite canvas, soft atmospheric tint behind glass, bright specular
 * top edges, and one iOS-blue interaction accent. No neon dashboard look.
 */

// Canvas
val LiquidDarkBackground = Color(0xFF050608)
val LiquidDarkElevated = Color(0xFF0B0D12)
val LiquidDarkSurface = Color(0xFF11141A)
val LiquidDarkCard = Color(0x4D202631)
val LiquidDarkCardSolid = Color(0xFF171A20)

val LiquidGlassBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF10131A),
        Color(0xFF080A0F),
        Color(0xFF040506)
    )
)

// Main frosted layers. These remain deliberately translucent.
val LiquidGlassCardSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x302B323E),
        Color(0x20202731),
        Color(0x15171C24)
    )
)

val LiquidGlassCardSheenBrush = Brush.verticalGradient(
    listOf(
        Color(0x16FFFFFF),
        Color(0x07FFFFFF),
        Color(0x00FFFFFF)
    )
)

val LiquidGlassSpecularBorderBrush = Brush.verticalGradient(
    listOf(
        Color(0x44FFFFFF),
        Color(0x1CFFFFFF),
        Color(0x08FFFFFF)
    )
)

val LiquidGlassSpecularBorderBrushElevated = Brush.verticalGradient(
    listOf(
        Color(0x58FFFFFF),
        Color(0x22FFFFFF),
        Color(0x0CFFFFFF)
    )
)

// Floating chrome (bottom dock / floating controls)
val LiquidGlassDockSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x7A242A33),
        Color(0x6813181F)
    )
)

val LiquidGlassModalSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0xF01A1D24),
        Color(0xE80D1015)
    )
)

val LiquidGlassInputSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x2EFFFFFF),
        Color(0x18FFFFFF)
    )
)

val LiquidGlassUserBubbleBrush = Brush.verticalGradient(
    listOf(
        Color(0xF20A84FF),
        Color(0xE00070DC)
    )
)

val LiquidGlassAssistantBubbleBrush = Brush.verticalGradient(
    listOf(
        Color(0x542B323D),
        Color(0x321C222B)
    )
)

// Native translucent layers
val GlassLayer1 = Color(0x14FFFFFF)
val GlassLayer2 = Color(0x24FFFFFF)
val GlassLayer3 = Color(0x38FFFFFF)
val GlassModalBackground = Color(0xEA11141A)

val GlassBorderSubtle = Color(0x1FFFFFFF)
val GlassBorderStandard = Color(0x35FFFFFF)
val GlassBorderHighlight = Color(0x66FFFFFF)

// Accent
val AppleSystemBlue = Color(0xFF0A84FF)
val AppleSystemBlueSubtle = Color(0x260A84FF)
val AppleSystemBlueGlow = Color(0x260A84FF)

val AppleSystemGreen = Color(0xFF30D158)
val AppleSystemRed = Color(0xFFFF453A)
val AppleSystemOrange = Color(0xFFFF9F0A)
val AppleSystemIndigo = Color(0xFF5E5CE6)

// Text
val AppleTextPrimary = Color(0xFFF7F7FA)
val AppleTextSecondary = Color(0xA6FFFFFF)
val AppleTextTertiary = Color(0x70FFFFFF)
val AppleTextMuted = AppleTextTertiary
val AppleTextPlaceholder = Color(0x4FFFFFFF)

// Shadows
val GlassShadowAmbient = Color(0x33000000)
val GlassShadowSpot = Color(0x59000000)

// Status aliases
val UrgencyCritical = AppleSystemRed
val UrgencyWarning = AppleSystemOrange
val UrgencyRelaxed = AppleTextSecondary
val UrgencyCompleted = AppleTextTertiary

// Compatibility aliases used throughout the current screens.
val IosSystemBackground = LiquidDarkBackground
val IosSystemBackgroundSecondary = LiquidDarkElevated
val IosSurfaceCard = LiquidDarkCard
val IosSurfaceCardPressed = Color(0x5A303743)
val IosSurfaceElevated = LiquidDarkElevated

val IosSeparator = GlassBorderStandard
val IosSeparatorLight = GlassBorderSubtle
val IosBorderSubtle = GlassBorderSubtle

val IosBlue = AppleSystemBlue
val IosIndigo = AppleSystemIndigo
val IosGreen = AppleSystemGreen
val IosRed = AppleSystemRed
val IosOrange = AppleSystemOrange
val IosYellow = Color(0xFFFFD60A)
val IosTeal = Color(0xFF64D2FF)
val IosPurple = Color(0xFFBF5AF2)

val IosGray1 = AppleTextSecondary
val IosGray2 = AppleTextTertiary
val IosGray3 = Color(0x58FFFFFF)
val IosGray4 = Color(0x38FFFFFF)
val IosGray5 = Color(0x26FFFFFF)
val IosGray6 = Color(0x16FFFFFF)

val IosTextPrimary = AppleTextPrimary
val IosTextSecondary = AppleTextSecondary
val IosTextMuted = AppleTextTertiary
val IosTextPlaceholder = AppleTextPlaceholder

val IosShadowColor = GlassShadowAmbient
val IosShadowElevatedColor = GlassShadowSpot

val GlassBackgroundDark = LiquidDarkBackground
val GlassSurfaceDark = LiquidDarkCard
val GlassCardFill = GlassLayer1
val GlassCardFillSelected = GlassLayer2
val GlassCardBorder = GlassBorderStandard
val GlassCardBorderFocused = AppleSystemBlue
val CyanAccent = AppleSystemBlue
val LavenderAccent = AppleSystemIndigo
val MintAccent = AppleSystemGreen
val CoralAccent = AppleSystemRed
val AmberAccent = AppleSystemOrange
val TextWhitePrimary = AppleTextPrimary
val TextSecondary = AppleTextSecondary
val TextMuted = AppleTextTertiary

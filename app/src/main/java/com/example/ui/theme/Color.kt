package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// iOS 26 Liquid Glass Design System
// Subtle depth obsidian canvas, translucent frosted glass cards,
// specular top-edge light reflection, disciplined neutral palette.
// Zero neon orbs, zero rainbow gradient. Authentic glass physics.
// ==========================================

// Canvas & Surfaces
val LiquidDarkBackground = Color(0xFF060709)       // Deep obsidian base
val LiquidDarkElevated = Color(0xFF0C0E14)         // Secondary background
val LiquidDarkSurface = Color(0xFF12151E)          // Content container surface
val LiquidDarkCard = Color(0x33202838)             // Translucent glass surface (~20% opacity)
val LiquidDarkCardSolid = Color(0xFF141720)        // Solid fallback when needed

// Liquid Glass Atmosphere Canvas Brush (Gentle neutral depth without bright colors)
val LiquidGlassBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF0F121A), // Subtle neutral slate depth at top
        Color(0xFF080A0F), // Smooth neutral transition
        Color(0xFF040507)  // Deep obsidian ground
    )
)

// Liquid Glass Card Surfaces (Translucent frosted glass with subtle sheen)
val LiquidGlassCardSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x38283344), // Soft top ambient refraction (~22% alpha)
        Color(0x221B2230), // Translucent core (~13% alpha)
        Color(0x18121722)  // Deep base (~10% alpha)
    )
)

val LiquidGlassCardSheenBrush = Brush.verticalGradient(
    listOf(
        Color(0x1EFFFFFF), // Specular light wash across upper glass face (~12%)
        Color(0x06FFFFFF), // Mid fade
        Color(0x00FFFFFF)  // Zero sheen at bottom
    )
)

// Specular Edge Reflection Brushes (Simulates light catching the chamfered glass edge)
val LiquidGlassSpecularBorderBrush = Brush.verticalGradient(
    listOf(
        Color(0x52FFFFFF), // ~32% crisp specular highlight on top edge
        Color(0x1EFFFFFF), // ~12% subtle side reflection
        Color(0x0AFFFFFF)  // ~4% soft shadow-side edge
    )
)

val LiquidGlassSpecularBorderBrushElevated = Brush.verticalGradient(
    listOf(
        Color(0x6EFFFFFF), // ~43% floating specular highlight
        Color(0x28FFFFFF), // ~16% side edge
        Color(0x10FFFFFF)  // ~6% bottom edge
    )
)

// Floating Dock & Modal Sheet Glass Brushes
val LiquidGlassDockSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x801A202C), // Translucent smoky dock glass (~50% opacity)
        Color(0x60111520)  // Subtle depth (~38% opacity)
    )
)

val LiquidGlassModalSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0xF012151C), // Frosted dark modal glass
        Color(0xE60A0D12)
    )
)

// Dedicated Chat Bubble Brushes
val LiquidGlassUserBubbleBrush = Brush.verticalGradient(
    listOf(
        Color(0xEE0A84FF),
        Color(0xD00071E3)
    )
)

val LiquidGlassAssistantBubbleBrush = Brush.verticalGradient(
    listOf(
        Color(0x38283446),
        Color(0x221A2230)
    )
)

// Translucent Input Field Surface Brush
val LiquidGlassInputSurfaceBrush = Brush.verticalGradient(
    listOf(
        Color(0x22FFFFFF),
        Color(0x12FFFFFF)
    )
)

// Native Translucent Surfaces & Subtle Hairlines
val GlassLayer1 = Color(0x12FFFFFF)                // ~7% soft glass wash
val GlassLayer2 = Color(0x22FFFFFF)                // ~13% floating glass
val GlassLayer3 = Color(0x35FFFFFF)                // ~21% active specular surface
val GlassModalBackground = Color(0xE811141A)       // Translucent dark frosted modal

// Borders: Subtle specular hairlines
val GlassBorderSubtle = Color(0x16FFFFFF)          // ~8.5% hairline
val GlassBorderStandard = Color(0x2EFFFFFF)        // ~18% glass border
val GlassBorderHighlight = Color(0x52FFFFFF)       // ~32% specular light catch

// Primary Accent: Single disciplined iOS System Blue
val AppleSystemBlue = Color(0xFF0A84FF)            // iOS primary blue
val AppleSystemBlueSubtle = Color(0x240A84FF)      // 14% tinted touch
val AppleSystemBlueGlow = Color(0x00000000)        // No glow

// Functional Accents (Restrained for real status only)
val AppleSystemGreen = Color(0xFF30D158)           // Completed/success only
val AppleSystemRed = Color(0xFFFF453A)             // Urgent/destructive only
val AppleSystemOrange = Color(0xFFFF9F0A)          // Warning only
val AppleSystemIndigo = Color(0xFF5E5CE6)          // Quiet intelligence tint

// Typography: Strict Native iOS Opacity Hierarchy
val AppleTextPrimary = Color(0xFFF5F5F7)           // #F5F5F7 Near-white
val AppleTextSecondary = Color(0x8CFFFFFF)         // rgba(255,255,255,0.55)
val AppleTextTertiary = Color(0x59FFFFFF)          // rgba(255,255,255,0.35)
val AppleTextMuted = AppleTextTertiary
val AppleTextPlaceholder = Color(0x3DFFFFFF)       // rgba(255,255,255,0.24)

// Shadows: Soft ambient occlusion
val GlassShadowAmbient = Color(0x26000000)
val GlassShadowSpot = Color(0x38000000)

// Urgency and Status Mapping
val UrgencyCritical = AppleSystemRed
val UrgencyWarning = AppleSystemOrange
val UrgencyRelaxed = AppleTextSecondary
val UrgencyCompleted = AppleTextTertiary

// ==========================================
// Compatibility Aliases
// ==========================================
val IosSystemBackground = LiquidDarkBackground
val IosSystemBackgroundSecondary = LiquidDarkElevated
val IosSurfaceCard = LiquidDarkCard
val IosSurfaceCardPressed = Color(0x4D242B38)
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
val IosGray3 = Color(0x4DFFFFFF)
val IosGray4 = Color(0x2EFFFFFF)
val IosGray5 = Color(0x1FFFFFFF)
val IosGray6 = Color(0x12FFFFFF)

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

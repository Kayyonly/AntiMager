package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Native Apple iOS Restrained Dark Theme
// Pitch black canvas (#000000), subtle contrast, single blue accent
// Zero neon, zero excessive outlines, authentic iOS hierarchy
// ==========================================

// Canvas & Surfaces
val LiquidDarkBackground = Color(0xFF000000)       // Pitch black OLED (#000000)
val LiquidDarkElevated = Color(0xFF0F0F11)         // Secondary background
val LiquidDarkSurface = Color(0xFF161618)          // Content container surface
val LiquidDarkCard = Color(0xFF141416)             // Quiet native surface

// Native Translucent Surfaces & Subtle Hairlines
val GlassLayer1 = Color(0x0AFFFFFF)                // 4% very soft surface
val GlassLayer2 = Color(0x12FFFFFF)                // 7% floating surface
val GlassLayer3 = Color(0x1AFFFFFF)                // 10% active surface
val GlassModalBackground = Color(0xF216161A)       // Frosted dark modal background

// Borders: Extremely subtle (almost disappears, ~6-7%)
val GlassBorderSubtle = Color(0x0FFFFFFF)          // ~6% hairline
val GlassBorderStandard = Color(0x12FFFFFF)        // ~7% native divider/border
val GlassBorderHighlight = Color(0x1CFFFFFF)       // ~11% soft light catch

// Primary Accent: Single disciplined iOS System Blue
val AppleSystemBlue = Color(0xFF0A84FF)            // iOS primary blue
val AppleSystemBlueSubtle = Color(0x1A0A84FF)      // 10% tinted touch
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

// Shadows: Extremely soft and quiet
val GlassShadowAmbient = Color(0x1A000000)
val GlassShadowSpot = Color(0x26000000)

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
val IosSurfaceCardPressed = Color(0xFF1E1E22)
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

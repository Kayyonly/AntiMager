package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleLiquidGlassColorScheme = darkColorScheme(
    primary = AppleSystemBlue,
    onPrimary = Color.White,
    primaryContainer = AppleSystemBlueSubtle,
    onPrimaryContainer = Color.White,
    secondary = AppleSystemIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0x335E5CE6),
    onSecondaryContainer = AppleTextPrimary,
    tertiary = AppleSystemGreen,
    onTertiary = Color.White,
    background = LiquidDarkBackground,
    onBackground = AppleTextPrimary,
    surface = LiquidDarkCard,
    onSurface = AppleTextPrimary,
    surfaceVariant = LiquidDarkSurface,
    onSurfaceVariant = AppleTextSecondary,
    outline = GlassBorderStandard,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun AntiMagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleLiquidGlassColorScheme,
        typography = Typography,
        content = content
    )
}

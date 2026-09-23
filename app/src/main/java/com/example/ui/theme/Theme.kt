package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AntiMagerDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF041E2B),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFDDF4FF),
    secondary = LavenderAccent,
    onSecondary = Color(0xFF26104A),
    secondaryContainer = Color(0xFF38235E),
    onSecondaryContainer = Color(0xFFEDE4FF),
    tertiary = MintAccent,
    onTertiary = Color(0xFF003822),
    background = GlassBackgroundDark,
    onBackground = TextWhitePrimary,
    surface = GlassSurfaceDark,
    onSurface = TextWhitePrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder
)

@Composable
fun AntiMagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AntiMagerDarkColorScheme,
        typography = Typography,
        content = content
    )
}

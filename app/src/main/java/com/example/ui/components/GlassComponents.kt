package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.LavenderAccent

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_orbs")
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF0B1120)
                    )
                )
            )
    ) {
        // Dynamic Glowing Blur Mesh (Visible through Frosted Glass layers)
        // Orb 1: Vibrant Cyan Glow (Top Left)
        Box(
            modifier = Modifier
                .size((300 * orbScale).dp)
                .offset(x = (-60 + orbOffset1).dp, y = (20 + orbOffset2).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanAccent.copy(alpha = 0.32f),
                            Color(0xFF0284C7).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
                .blur(50.dp)
        )

        // Orb 2: Soft Violet/Lavender Glow (Middle Right)
        Box(
            modifier = Modifier
                .size((340 * orbScale).dp)
                .offset(x = (160 + orbOffset2).dp, y = (220 + orbOffset1).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LavenderAccent.copy(alpha = 0.28f),
                            Color(0xFF7C3AED).copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
                .blur(56.dp)
        )

        // Orb 3: Radiant Mint/Teal Glow (Bottom Left)
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-30 - orbOffset2).dp, y = (540 + orbOffset1).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.22f),
                            Color(0xFF064E3B).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .blur(52.dp)
        )

        // Orb 4: Subtle Rose/Amber Glow (Bottom Right)
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (190 + orbOffset1).dp, y = (660 - orbOffset2).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF43F5E).copy(alpha = 0.16f),
                            Color(0xFFF59E0B).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .blur(48.dp)
        )

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = GlassCardFill,
    borderColor: Color = GlassCardBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    // Liquid Glass iOS Specular Border: top-left catches light, bottom-right softly dissipates
    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.35f),
            borderColor.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.08f),
            borderColor.copy(alpha = 0.20f)
        )
    )

    // Frosted Glass body with vertical liquid translucency gradient
    val glassFillBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E293B).copy(alpha = 0.65f),
            Color(0xFF0F172A).copy(alpha = 0.78f)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassFillBrush)
            .border(
                width = borderWidth,
                brush = glassBorderBrush,
                shape = shape
            )
            .then(clickableModifier)
    ) {
        // iOS Liquid Glass Top Specular Highlight Line (simulates curved glass light reflection)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Subtle Ambient Corner Light Reflection
        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun UrgencyBadge(
    label: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val baseColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        CyanAccent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(baseColor.copy(alpha = 0.18f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.60f),
                        baseColor.copy(alpha = 0.25f)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = baseColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

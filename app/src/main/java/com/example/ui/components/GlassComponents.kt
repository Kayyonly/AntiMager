package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
import com.example.ui.theme.AppleTextPlaceholder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.AppleTextTertiary
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderStandard
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLayer1
import com.example.ui.theme.GlassLayer2
import com.example.ui.theme.GlassLayer3
import com.example.ui.theme.LiquidDarkBackground
import com.example.ui.theme.LiquidDarkCard
import com.example.ui.theme.LiquidDarkCardSolid
import com.example.ui.theme.LiquidDarkElevated
import com.example.ui.theme.LiquidGlassBackgroundBrush
import com.example.ui.theme.LiquidGlassCardSheenBrush
import com.example.ui.theme.LiquidGlassCardSurfaceBrush
import com.example.ui.theme.LiquidGlassDockSurfaceBrush
import com.example.ui.theme.LiquidGlassModalSurfaceBrush
import com.example.ui.theme.LiquidGlassSpecularBorderBrush
import com.example.ui.theme.LiquidGlassSpecularBorderBrushElevated

/**
 * iOS 26 Liquid Glass Tokens
 * Subtle geometry, authentic glass opacity and specular reflection borders.
 */
object LiquidGlassTokens {
    // Radius System
    val RadiusSmall: Dp = 10.dp
    val RadiusControl: Dp = 12.dp
    val RadiusInput: Dp = 14.dp
    val RadiusButton: Dp = 16.dp
    val RadiusCard: Dp = 20.dp
    val RadiusPanel: Dp = 24.dp
    val RadiusSheet: Dp = 30.dp

    // Spacing System
    val Space4: Dp = 4.dp
    val Space8: Dp = 8.dp
    val Space12: Dp = 12.dp
    val Space16: Dp = 16.dp
    val Space20: Dp = 20.dp
    val Space24: Dp = 24.dp
    val Space32: Dp = 32.dp

    // Brushes
    val GlassCardSurfaceBrush = LiquidGlassCardSurfaceBrush
    val GlassCardSheenBrush = LiquidGlassCardSheenBrush
    val GlassSpecularBorderBrush = LiquidGlassSpecularBorderBrush
    val GlassSpecularBorderBrushElevated = LiquidGlassSpecularBorderBrushElevated
    val GlassDockSurfaceBrush = LiquidGlassDockSurfaceBrush
    val GlassModalSurfaceBrush = LiquidGlassModalSurfaceBrush
}

enum class GlassDepth(val level: Int) {
    LEVEL_1_CONTENT(1),
    LEVEL_2_FLOATING(2),
    LEVEL_3_MODAL(3)
}

/**
 * Tactile spring press feedback modifier for physical touch feel.
 */
@Composable
fun Modifier.tactilePress(
    enabled: Boolean = true,
    targetScale: Float = 0.975f,
    onClick: (() -> Unit)? = null
): Modifier {
    if (!enabled && onClick == null) return this
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    return this
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Specular hairline border modifier with subtle light reflection.
 */
fun Modifier.glassBorder(
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    borderWidth: Dp = 0.8.dp,
    borderBrush: Brush = LiquidGlassTokens.GlassSpecularBorderBrush
): Modifier = this.border(
    width = borderWidth,
    brush = borderBrush,
    shape = shape
)

/**
 * Neutral dark Liquid Glass canvas background.
 * Very subtle deep slate to pitch-black depth with zero neon colors,
 * providing the atmospheric backdrop for translucent glass refraction.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlassBackgroundBrush)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft wallpaper-like light behind the glass. Low alpha keeps it Apple-like,
            // not neon or "AI dashboard" looking.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x180A84FF), Color(0x000A84FF)),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.10f, size.height * 0.02f),
                    radius = size.width * 0.95f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x105E5CE6), Color(0x005E5CE6)),
                    center = androidx.compose.ui.geometry.Offset(size.width * 1.02f, size.height * 0.76f),
                    radius = size.width * 0.90f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0CFFFFFF), Color(0x00FFFFFF)),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.20f),
                    radius = size.width * 0.62f
                )
            )
        }
        content()
    }
}

/**
 * Convenience modifier to give any component a Liquid Glass surface.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    borderWidth: Dp = 0.85.dp,
    borderBrush: Brush = LiquidGlassTokens.GlassSpecularBorderBrush
): Modifier = this
    .clip(shape)
    .background(LiquidGlassTokens.GlassCardSurfaceBrush)
    .background(LiquidGlassTokens.GlassCardSheenBrush)
    .border(width = borderWidth, brush = borderBrush, shape = shape)

/**
 * Native translucent surface for floating overlays / bottom bar.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    depth: GlassDepth = GlassDepth.LEVEL_2_FLOATING,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val (bgBrush, borderBrush, elevation) = when (depth) {
        GlassDepth.LEVEL_1_CONTENT -> Triple(
            LiquidGlassTokens.GlassCardSurfaceBrush,
            LiquidGlassTokens.GlassSpecularBorderBrush,
            2.dp
        )
        GlassDepth.LEVEL_2_FLOATING -> Triple(
            LiquidGlassTokens.GlassDockSurfaceBrush,
            LiquidGlassTokens.GlassSpecularBorderBrushElevated,
            8.dp
        )
        GlassDepth.LEVEL_3_MODAL -> Triple(
            LiquidGlassTokens.GlassModalSurfaceBrush,
            LiquidGlassTokens.GlassSpecularBorderBrushElevated,
            12.dp
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = Color(0x52000000),
                ambientColor = Color(0x2A000000)
            )
            .clip(shape)
            .background(bgBrush)
            .background(LiquidGlassTokens.GlassCardSheenBrush)
            .border(width = 0.75.dp, brush = borderBrush, shape = shape)
            .tactilePress(onClick = onClick)
    ) {
        // Specular top hairline reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x00FFFFFF), Color(0x5AFFFFFF), Color(0x00FFFFFF))
                    )
                )
        )
        content()
    }
}

/**
 * iOS 26 Liquid Glass Card:
 * Translucent frosted glass core, subtle top-down specular sheen,
 * natural top-lit light reflection border, and soft ambient depth.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 0.75.dp,
    elevation: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveBorderBrush = borderBrush ?: if (borderColor != null) {
        SolidColor(borderColor)
    } else {
        LiquidGlassTokens.GlassSpecularBorderBrush
    }

    val effectiveBgBrush = backgroundBrush ?: if (backgroundColor != null && backgroundColor != LiquidDarkCard && backgroundColor != LiquidDarkCardSolid) {
        val translucentColor = if (backgroundColor.alpha == 1f && backgroundColor.red < 0.2f && backgroundColor.green < 0.2f && backgroundColor.blue < 0.25f) {
            backgroundColor.copy(alpha = 0.32f)
        } else {
            backgroundColor
        }
        SolidColor(translucentColor)
    } else {
        LiquidGlassTokens.GlassCardSurfaceBrush
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = Color(0x48000000),
                ambientColor = Color(0x26000000)
            )
            .clip(shape)
            .background(effectiveBgBrush)
            .background(LiquidGlassTokens.GlassCardSheenBrush)
            .border(
                width = borderWidth,
                brush = effectiveBorderBrush,
                shape = shape
            )
            .tactilePress(onClick = onClick)
    ) {
        // Specular top chamfer highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x00FFFFFF), Color(0x52FFFFFF), Color(0x00FFFFFF))
                    )
                )
        )
        content()
    }
}

/**
 * Compatibility alias: IosCard maps cleanly to GlassCard
 */
@Composable
fun IosCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 0.8.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        backgroundBrush = backgroundBrush,
        borderColor = borderColor,
        borderBrush = borderBrush,
        borderWidth = borderWidth,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    GLASS
}

typealias GlassButtonVariant = ButtonVariant

/**
 * iOS 26 Liquid Glass Button:
 * Clean, tactile, frosted glass with subtle specular light reflection.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(LiquidGlassTokens.RadiusButton)

    val (bgBrush, borderBrush, textColor) = when (variant) {
        ButtonVariant.PRIMARY -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF0A84FF), Color(0xFF0071E3))),
            Brush.verticalGradient(listOf(Color(0x60FFFFFF), Color(0x15FFFFFF))),
            Color.White
        )
        ButtonVariant.SECONDARY -> Triple(
            LiquidGlassTokens.GlassCardSurfaceBrush,
            LiquidGlassTokens.GlassSpecularBorderBrush,
            AppleTextPrimary
        )
        ButtonVariant.GLASS -> Triple(
            Brush.verticalGradient(listOf(Color(0x35283040), Color(0x22181C26))),
            LiquidGlassTokens.GlassSpecularBorderBrushElevated,
            AppleTextPrimary
        )
    }

    Box(
        modifier = modifier
            .heightIn(min = 46.dp)
            .shadow(
                elevation = if (variant == ButtonVariant.PRIMARY) 3.dp else 1.dp,
                shape = shape,
                spotColor = Color(0x30000000)
            )
            .clip(shape)
            .background(if (enabled) bgBrush else SolidColor(Color(0x20FFFFFF)))
            .background(LiquidGlassTokens.GlassCardSheenBrush)
            .border(
                width = 0.8.dp,
                brush = if (enabled) borderBrush else SolidColor(Color(0x10FFFFFF)),
                shape = shape
            )
            .tactilePress(enabled = enabled, onClick = if (enabled) onClick else null)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

/**
 * Native iOS Icon Button (48dp touch target)
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppleTextSecondary,
    containerColor: Color = Color.Transparent
) {
    val effectiveContainer = if (containerColor == Color.Transparent) {
        Color(0x16FFFFFF)
    } else {
        containerColor
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                spotColor = Color(0x42000000),
                ambientColor = Color(0x22000000)
            )
            .clip(CircleShape)
            .background(effectiveContainer)
            .background(LiquidGlassTokens.GlassCardSheenBrush)
            .border(
                width = 0.7.dp,
                brush = LiquidGlassTokens.GlassSpecularBorderBrush,
                shape = CircleShape
            )
            .tactilePress(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Restrained Urgency Indicator:
 * Uses a subtle red dot or restrained text label instead of a giant colored pill.
 */
@Composable
fun UrgencyBadge(
    label: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val isUrgent = label.contains("Mendesak", ignoreCase = true) ||
                   label.contains("Tinggi", ignoreCase = true) ||
                   colorHex.equals("#FF453A", ignoreCase = true)

    if (isUrgent) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AppleSystemRed)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Mendesak",
                color = AppleSystemRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        Text(
            text = label,
            color = AppleTextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            modifier = modifier
        )
    }
}

/**
 * Native iOS Section Header: clean uppercase with generous spacing
 */
@Composable
fun IosSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppleTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f)
        )
        if (action != null) {
            action()
        }
    }
}

/**
 * iOS 26 Liquid Glass Segmented Control
 * Translucent frosted glass container with a specular sliding pill.
 */
@Composable
fun <T> IosSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(14.dp)
    val innerShape = RoundedCornerShape(11.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(LiquidGlassTokens.GlassCardSurfaceBrush)
            .border(
                width = 0.8.dp,
                brush = LiquidGlassTokens.GlassSpecularBorderBrush,
                shape = outerShape
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(innerShape)
                        .background(
                            if (isSelected) Color(0x30FFFFFF) else Color.Transparent
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 0.6.dp,
                                    color = Color(0x42FFFFFF),
                                    shape = innerShape
                                )
                            } else Modifier
                        )
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AppleTextPrimary else AppleTextSecondary,
                        letterSpacing = (-0.1).sp
                    )
                }
            }
        }
    }
}

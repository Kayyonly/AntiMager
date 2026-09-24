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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemBlueSubtle
import com.example.ui.theme.AppleSystemRed
import com.example.ui.theme.AppleTextMuted
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
import com.example.ui.theme.LiquidDarkElevated

/**
 * Native Apple iOS Tokens
 * Restrained geometry and spacing
 */
object LiquidGlassTokens {
    // Radius System
    val RadiusSmall: Dp = 8.dp
    val RadiusControl: Dp = 10.dp
    val RadiusInput: Dp = 12.dp
    val RadiusButton: Dp = 14.dp
    val RadiusCard: Dp = 16.dp
    val RadiusPanel: Dp = 22.dp
    val RadiusSheet: Dp = 26.dp

    // Spacing System
    val Space4: Dp = 4.dp
    val Space8: Dp = 8.dp
    val Space12: Dp = 12.dp
    val Space16: Dp = 16.dp
    val Space20: Dp = 20.dp
    val Space24: Dp = 24.dp
    val Space32: Dp = 32.dp
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
    targetScale: Float = 0.98f,
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
 * Extremely subtle hairline border modifier (~6% opacity).
 */
fun Modifier.glassBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.6.dp,
    borderColor: Color = GlassBorderSubtle
): Modifier = this.border(
    width = borderWidth,
    color = borderColor,
    shape = shape
)

/**
 * Pure pitch black canvas.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidDarkBackground)
    ) {
        content()
    }
}

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
    val bgColor = when (depth) {
        GlassDepth.LEVEL_1_CONTENT -> Color(0xFF101012)
        GlassDepth.LEVEL_2_FLOATING -> Color(0xF2161618)
        GlassDepth.LEVEL_3_MODAL -> Color(0xFA18181C)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .glassBorder(shape = shape)
            .tactilePress(onClick = onClick)
    ) {
        content()
    }
}

/**
 * Native iOS Card: Flat, quiet dark surface without heavy borders or glow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    backgroundColor: Color = LiquidDarkCard,
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 0.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, shape)
                } else Modifier
            )
            .tactilePress(onClick = onClick)
    ) {
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
    backgroundColor: Color = LiquidDarkCard,
    borderColor: Color = GlassBorderSubtle,
    borderWidth: Dp = 0.dp,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
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
 * Native iOS Button: Clean, tactile, restrained.
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
    val (bgColor, textColor) = when (variant) {
        ButtonVariant.PRIMARY -> Pair(AppleSystemBlue, Color.White)
        ButtonVariant.SECONDARY -> Pair(Color(0xFF242426), AppleTextPrimary)
        ButtonVariant.GLASS -> Pair(Color(0xFF1C1C1E), AppleTextPrimary)
    }

    val shape = RoundedCornerShape(LiquidGlassTokens.RadiusButton)

    Box(
        modifier = modifier
            .heightIn(min = 46.dp)
            .clip(shape)
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
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
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
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
 * Native iOS Restrained Urgency Indicator:
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
 * Native Apple iOS Segmented Control
 * ONE continuous dark container with a sliding lighter pill
 */
@Composable
fun <T> IosSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(LiquidGlassTokens.RadiusControl)
    val innerShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(Color(0xFF1C1C1E))
            .padding(2.dp)
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
                            if (isSelected) Color(0xFF323236) else Color.Transparent
                        )
                        .clickable { onItemSelected(item) }
                        .padding(vertical = 7.dp),
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

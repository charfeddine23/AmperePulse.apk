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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeumorphBorderDark
import com.example.ui.theme.NeumorphBorderLight
import com.example.ui.theme.NeumorphCardBottomLight
import com.example.ui.theme.NeumorphCardTopLight
import com.example.ui.theme.NeumorphHighlightDark
import com.example.ui.theme.NeumorphHighlightLight
import com.example.ui.theme.NeumorphInsetLight
import com.example.ui.theme.NeumorphShadowDark
import com.example.ui.theme.NeumorphShadowLight
import com.example.ui.theme.NeumorphSurfaceDark
import com.example.ui.theme.NeumorphSurfaceLight
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

/**
 * Tactile Neumorphic & Soft Liquid Glass Card
 * Implements dual-shadow extrusion: top-left specular highlight and bottom-right diffusion shadow.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accentColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    glowIntensity: Float = 0.75f,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier

    val surfaceColor = if (isDarkMode) NeumorphSurfaceDark else NeumorphSurfaceLight
    val shadowColor = if (isDarkMode) NeumorphShadowDark else NeumorphShadowLight
    val highlightColor = if (isDarkMode) NeumorphHighlightDark else NeumorphHighlightLight
    val borderColor = if (isDarkMode) NeumorphBorderDark else NeumorphBorderLight

    Box(
        modifier = modifier
            .then(tagModifier)
            // Neumorphic Dual Shadows (Extruded look)
            .drawBehind {
                val shadowOffset = 5.dp.toPx()
                val highlightOffset = -3.dp.toPx()

                // Bottom-right soft diffusion shadow
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset, shadowOffset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx())
                )

                // Top-left soft specular highlight
                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(highlightOffset, highlightOffset),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx(), 22.dp.toPx())
                )
            }
            .clip(shape)
            .background(surfaceColor)
            // Subtle liquid glass gradient sheen
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                NeumorphCardTopLight,
                                NeumorphSurfaceLight,
                                NeumorphCardBottomLight
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )

                // Subtle accent glow tint from top edge
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isDarkMode) 0.12f * glowIntensity else 0.06f * glowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, 0f),
                        radius = size.width * 0.7f
                    )
                )
            }
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * Pulsing Indicator Dot
 */
@Composable
fun PulsingNeonDot(
    modifier: Modifier = Modifier,
    color: Color = TactileRoyalBlue,
    sizeDp: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = modifier.size(sizeDp * 1.8f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.35f))
        )
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Tactile Neumorphic Pill Badge
 */
@Composable
fun NeonBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDarkMode) Color(0x33334155) else NeumorphInsetLight
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            PulsingNeonDot(color = accentColor, sizeDp = 5.dp)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text.uppercase(),
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            color = accentColor
        )
    }
}

/**
 * Top App Bar with Tactile Neumorphic Styling
 */
@Composable
fun LiquidTopAppBar(
    title: String,
    subtitle: String? = null,
    accentColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    rightAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.2).sp,
                    color = if (isDarkMode) TextWhite else TextCharcoal
                )
                Spacer(modifier = Modifier.width(8.dp))
                PulsingNeonDot(color = accentColor, sizeDp = 6.dp)
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = TextSlate
                )
            }
        }
        if (rightAction != null) {
            rightAction()
        }
    }
}

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
import androidx.compose.foundation.layout.height
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
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.ObsidianOled
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

/**
 * Liquid Glass M5 Card featuring deep dark translucent obsidian glass background,
 * frosted inner highlights, and vivid blurry glowing neon phosphorus green edges.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    accentColor: Color = NeonPhosphorusGreen,
    glowIntensity: Float = 0.85f,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier

    Box(
        modifier = modifier
            .then(tagModifier)
            // Outer glowing blur halo around card edges
            .drawBehind {
                val glowAlpha = (0.22f * glowIntensity).coerceIn(0.05f, 0.45f)
                val strokeWidthPx = 3.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width.coerceAtLeast(size.height) * 0.75f
                    ),
                    size = size
                )
            }
            .clip(shape)
            // Translucent Obsidian Glass Background
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD9121A17), // Frosted obsidian top
                        Color(0xE60D1210)  // Deep obsidian bottom
                    )
                )
            )
            // Specular refraction highlight diagonal sheen
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.01f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                // Soft neon inner glow from top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f * glowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.15f, 0f),
                        radius = size.width * 0.6f
                    )
                )
            }
            // Frosted border with neon luminous edges
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = (0.75f * glowIntensity).coerceAtMost(0.95f)),
                        accentColor.copy(alpha = 0.25f),
                        Color(0x33FFFFFF),
                        accentColor.copy(alpha = (0.5f * glowIntensity).coerceAtMost(0.85f))
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * Pulsing neon indicator dot
 */
@Composable
fun PulsingNeonDot(
    modifier: Modifier = Modifier,
    color: Color = NeonPhosphorusGreen,
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
        initialValue = 0.5f,
        targetValue = 1.0f,
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
        // Outer halo
        Box(
            modifier = Modifier
                .size(sizeDp * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.45f))
        )
        // Core dot
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Neon Pill Badge for status / mode labels
 */
@Composable
fun NeonBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonPhosphorusGreen,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3300FF87).copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.6f),
                        accentColor.copy(alpha = 0.2f)
                    )
                ),
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
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = accentColor
        )
    }
}

/**
 * Top App Header with Liquid Glass styling
 */
@Composable
fun LiquidTopAppBar(
    title: String,
    subtitle: String? = null,
    accentColor: Color = NeonPhosphorusGreen,
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
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    color = TextLuminous
                )
                Spacer(modifier = Modifier.width(8.dp))
                PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextMutedGreen
                )
            }
        }
        if (rightAction != null) {
            rightAction()
        }
    }
}

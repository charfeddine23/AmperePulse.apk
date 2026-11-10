package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Adaptive Liquid Droplet & Ultra-Blur Glass Swipe Container.
 * Detects horizontal swipes and draws a luxury organic adaptive droplet tear
 * that morphs in the direction of the swipe, with frosted glass shimmer and dynamic glow.
 */
@Composable
fun AdaptiveLiquidDropletSwipeContainer(
    modifier: Modifier = Modifier,
    accentColor: Color,
    isDarkMode: Boolean,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val dropletReleaseAlpha = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDragX = 0f
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        coroutineScope.launch {
                            dropletReleaseAlpha.snapTo(1f)
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragX += dragAmount
                        val clamped = totalDragX.coerceIn(-350f, 350f)
                        coroutineScope.launch {
                            dragOffset.snapTo(clamped)
                        }
                    },
                    onDragEnd = {
                        val threshold = 120f
                        if (totalDragX < -threshold) {
                            onSwipeNext()
                        } else if (totalDragX > threshold) {
                            onSwipePrevious()
                        }
                        coroutineScope.launch {
                            dragOffset.animateTo(
                                0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                        coroutineScope.launch {
                            dropletReleaseAlpha.animateTo(0f, animationSpec = tween(350))
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffset.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                            dropletReleaseAlpha.animateTo(0f, animationSpec = tween(250))
                        }
                    }
                )
            }
    ) {
        content()

        // Ultra-luxurious Adaptive Liquid Glass Droplet overlay
        val currentOffset = dragOffset.value
        val alpha = dropletReleaseAlpha.value
        if (alpha > 0.01f && abs(currentOffset) > 10f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h * 0.48f

                val isSwipingLeft = currentOffset < 0
                val progress = (abs(currentOffset) / 350f).coerceIn(0f, 1f)

                // Draw organic adaptive droplet on edge
                drawAdaptiveLiquidDroplet(
                    isLeftEdge = !isSwipingLeft,
                    pullDistance = abs(currentOffset),
                    progress = progress,
                    centerY = midY,
                    height = h,
                    width = w,
                    accentColor = accentColor,
                    isDarkMode = isDarkMode,
                    alpha = alpha
                )
            }
        }
    }
}

private fun DrawScope.drawAdaptiveLiquidDroplet(
    isLeftEdge: Boolean,
    pullDistance: Float,
    progress: Float,
    centerY: Float,
    height: Float,
    width: Float,
    accentColor: Color,
    isDarkMode: Boolean,
    alpha: Float
) {
    val dropletPath = Path()
    val edgeX = if (isLeftEdge) 0f else width
    val bulgeSign = if (isLeftEdge) 1f else -1f

    val maxBulge = (pullDistance * 0.55f).coerceAtMost(160.dp.toPx())
    val spanY = 160.dp.toPx() + (progress * 80.dp.toPx())

    val topY = (centerY - spanY).coerceAtLeast(0f)
    val bottomY = (centerY + spanY).coerceAtMost(height)

    val tipX = edgeX + (bulgeSign * maxBulge)
    val controlX = edgeX + (bulgeSign * maxBulge * 0.95f)

    dropletPath.moveTo(edgeX, topY)
    dropletPath.cubicTo(
        x1 = edgeX + (bulgeSign * 15.dp.toPx()),
        y1 = centerY - (spanY * 0.6f),
        x2 = controlX,
        y2 = centerY - (spanY * 0.25f),
        x3 = tipX,
        y3 = centerY
    )
    dropletPath.cubicTo(
        x1 = controlX,
        y1 = centerY + (spanY * 0.25f),
        x2 = edgeX + (bulgeSign * 15.dp.toPx()),
        y2 = centerY + (spanY * 0.6f),
        x3 = edgeX,
        y3 = bottomY
    )
    dropletPath.close()

    // 1. Soft Ambient Aura behind the glass droplet
    val auraRadius = maxBulge * 1.8f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.35f * alpha * progress),
                accentColor.copy(alpha = 0.12f * alpha),
                Color.Transparent
            ),
            center = Offset(tipX, centerY),
            radius = auraRadius
        ),
        radius = auraRadius,
        center = Offset(tipX, centerY)
    )

    // 2. Liquid Glass Body (Translucent refractive sheen)
    val glassColors = if (isDarkMode) {
        listOf(
            accentColor.copy(alpha = 0.40f * alpha),
            Color(0xFF1E293B).copy(alpha = 0.55f * alpha),
            Color(0xFF0F172A).copy(alpha = 0.70f * alpha)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.75f * alpha),
            accentColor.copy(alpha = 0.30f * alpha),
            Color.White.copy(alpha = 0.50f * alpha)
        )
    }

    drawPath(
        path = dropletPath,
        brush = Brush.linearGradient(
            colors = glassColors,
            start = Offset(edgeX, centerY),
            end = Offset(tipX, centerY)
        )
    )

    // 3. Specular Highlight on the droplet crest (Liquid Lens Effect)
    val highlightPath = Path()
    val highlightTipX = edgeX + (bulgeSign * maxBulge * 0.92f)
    highlightPath.moveTo(edgeX + (bulgeSign * maxBulge * 0.25f), centerY - (spanY * 0.4f))
    highlightPath.cubicTo(
        x1 = controlX * 0.85f,
        y1 = centerY - (spanY * 0.2f),
        x2 = highlightTipX,
        y2 = centerY - (spanY * 0.1f),
        x3 = highlightTipX,
        y3 = centerY
    )

    drawPath(
        path = highlightPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * alpha),
                Color.White.copy(alpha = 0.20f * alpha),
                Color.Transparent
            ),
            startY = centerY - (spanY * 0.4f),
            endY = centerY + (spanY * 0.1f)
        ),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )

    // 4. Outer Glowing Rim
    drawPath(
        path = dropletPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f * alpha),
                accentColor.copy(alpha = 0.9f * alpha),
                accentColor.copy(alpha = 0.3f * alpha)
            ),
            start = Offset(tipX, centerY - spanY),
            end = Offset(edgeX, centerY + spanY)
        ),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )

    // 5. Droplet Core Photon Spark
    if (progress > 0.4f) {
        val sparkAlpha = ((progress - 0.4f) / 0.6f) * alpha
        drawCircle(
            color = Color.White.copy(alpha = sparkAlpha * 0.9f),
            radius = 4.dp.toPx(),
            center = Offset(tipX - (bulgeSign * 10.dp.toPx()), centerY)
        )
        drawCircle(
            color = accentColor.copy(alpha = sparkAlpha * 0.5f),
            radius = 12.dp.toPx(),
            center = Offset(tipX - (bulgeSign * 10.dp.toPx()), centerY)
        )
    }
}

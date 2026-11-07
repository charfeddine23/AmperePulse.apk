package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.TelemetryPoint
import com.example.ui.theme.NeonPhosphorusGreen

/**
 * Compact sparkline waveform designed for floating data cards.
 * Renders smooth cubic bezier interpolation, glowing stroke,
 * translucent obsidian-neon gradient fill, and live tracking head.
 */
@Composable
fun MetricSparkline(
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonPhosphorusGreen,
    glowIntensity: Float = 0.85f,
    heightDp: Dp = 44.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparklinePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklineAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.size < 2) return@Canvas

            val w = size.width
            val h = size.height

            val minVal = points.minOf { it.value }
            val maxVal = points.maxOf { it.value }
            val range = (maxVal - minVal).coerceAtLeast(0.001f)

            // Convert points to canvas coordinates
            val coords = points.mapIndexed { index, pt ->
                val x = (index.toFloat() / (points.size - 1)) * w
                val normalizedY = (pt.value - minVal) / range
                // Invert y: high values near top, with padding
                val y = h - (normalizedY * (h * 0.75f) + h * 0.12f)
                Offset(x, y)
            }

            val strokePath = Path()
            val fillPath = Path()

            strokePath.moveTo(coords.first().x, coords.first().y)
            fillPath.moveTo(coords.first().x, h)
            fillPath.lineTo(coords.first().x, coords.first().y)

            // Cubic Bezier smoothing
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                strokePath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
                fillPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
            }

            fillPath.lineTo(coords.last().x, h)
            fillPath.close()

            // 1. Draw glowing gradient fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.28f * glowIntensity),
                        lineColor.copy(alpha = 0.05f * glowIntensity),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Draw ambient stroke glow halo
            drawPath(
                path = strokePath,
                color = lineColor.copy(alpha = 0.25f * glowIntensity),
                style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Draw sharp foreground stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Draw head pulse dot at latest measurement
            val lastPoint = coords.last()
            drawCircle(
                color = lineColor.copy(alpha = pulseAlpha * 0.5f),
                radius = 5.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

/**
 * Full-scale glowing waveform graph with technical grid lines,
 * high-fidelity smoothing, and real-time pulse dynamics.
 */
@Composable
fun DetailedWaveformGraph(
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonPhosphorusGreen,
    glowIntensity: Float = 0.85f,
    heightDp: Dp = 180.dp,
    showGrid: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseHead")
    val headPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Tech Obsidian Grid lines
            if (showGrid) {
                drawGridLines(w, h)
            }

            if (points.size < 2) return@Canvas

            val minVal = points.minOf { it.value }
            val maxVal = points.maxOf { it.value }
            val range = (maxVal - minVal).coerceAtLeast(0.001f)

            val coords = points.mapIndexed { index, pt ->
                val x = (index.toFloat() / (points.size - 1)) * w
                val normalizedY = (pt.value - minVal) / range
                val y = h - (normalizedY * (h * 0.70f) + h * 0.15f)
                Offset(x, y)
            }

            val strokePath = Path()
            val fillPath = Path()

            strokePath.moveTo(coords.first().x, coords.first().y)
            fillPath.moveTo(coords.first().x, h)
            fillPath.lineTo(coords.first().x, coords.first().y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                strokePath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
                fillPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p1.x, p1.y
                )
            }

            fillPath.lineTo(coords.last().x, h)
            fillPath.close()

            // Translucent glowing gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f * glowIntensity),
                        lineColor.copy(alpha = 0.08f * glowIntensity),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Wide ambient glow halo
            drawPath(
                path = strokePath,
                color = lineColor.copy(alpha = 0.3f * glowIntensity),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Razor-sharp Neon Stroke
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.6f),
                        lineColor,
                        Color.White
                    )
                ),
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Live telemetry tracker head
            val lastPoint = coords.last()
            drawCircle(
                color = lineColor.copy(alpha = 0.4f * glowIntensity),
                radius = 8.dp.toPx() * headPulseScale,
                center = lastPoint
            )
            drawCircle(
                color = lineColor,
                radius = 4.5.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.2.dp.toPx(),
                center = lastPoint
            )
        }
    }
}

private fun DrawScope.drawGridLines(width: Float, height: Float) {
    val horizontalDivisions = 4
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

    for (i in 1..horizontalDivisions) {
        val y = height * (i.toFloat() / (horizontalDivisions + 1))
        drawLine(
            color = Color(0x1F00FF87),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }

    val verticalDivisions = 6
    for (i in 1..verticalDivisions) {
        val x = width * (i.toFloat() / (verticalDivisions + 1))
        drawLine(
            color = Color(0x1200FF87),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}

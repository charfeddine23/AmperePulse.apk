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
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue

/**
 * Compact Tactile Sparkline for Neumorphic Metric Cards
 */
@Composable
fun MetricSparkline(
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    glowIntensity: Float = 0.75f,
    heightDp: Dp = 40.dp
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

            val coords = points.mapIndexed { index, pt ->
                val x = (index.toFloat() / (points.size - 1)) * w
                val normalizedY = (pt.value - minVal) / range
                val y = h - (normalizedY * (h * 0.72f) + h * 0.14f)
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

            // Soft translucent gradient fill under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = if (isDarkMode) 0.28f else 0.20f),
                        lineColor.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Sharp foreground stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Head tracking dot
            val lastPoint = coords.last()
            drawCircle(
                color = lineColor.copy(alpha = pulseAlpha * 0.35f),
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
 * Detailed Waveform Graph for Oscilloscope & History
 */
@Composable
fun DetailedWaveformGraph(
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    glowIntensity: Float = 0.75f,
    heightDp: Dp = 160.dp,
    showGrid: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseHead")
    val headPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
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

            if (showGrid) {
                drawTactileGridLines(w, h, isDarkMode)
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

            // Translucent gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = if (isDarkMode) 0.32f else 0.22f),
                        lineColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Sharp line stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Live telemetry tracker head
            val lastPoint = coords.last()
            drawCircle(
                color = lineColor.copy(alpha = 0.3f),
                radius = 7.dp.toPx() * headPulseScale,
                center = lastPoint
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
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

private fun DrawScope.drawTactileGridLines(width: Float, height: Float, isDarkMode: Boolean) {
    val horizontalDivisions = 4
    val gridColor = if (isDarkMode) Color(0x24475569) else Color(0x2EE2E8F0)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

    for (i in 1..horizontalDivisions) {
        val y = height * (i.toFloat() / (horizontalDivisions + 1))
        drawLine(
            color = gridColor,
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
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
    }
}

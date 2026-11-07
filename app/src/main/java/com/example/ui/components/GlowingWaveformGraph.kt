package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.TelemetryPoint
import com.example.ui.theme.TactileRoyalBlue
import java.util.Locale

/**
 * Ultra-Optimized 90 FPS Tactile Sparkline for Neumorphic Metric Cards.
 * Reuses internal Path allocations and renders hardware-accelerated curves.
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
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklineAlpha"
    )

    // Pre-allocate paths once per composable instance to prevent GC churn at 90Hz
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .graphicsLayer { clip = false }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.size < 2) return@Canvas

            val w = size.width
            val h = size.height

            var minVal = Float.MAX_VALUE
            var maxVal = -Float.MAX_VALUE
            for (i in points.indices) {
                val v = points[i].value
                if (v < minVal) minVal = v
                if (v > maxVal) maxVal = v
            }
            val range = (maxVal - minVal).coerceAtLeast(0.001f)

            strokePath.reset()
            fillPath.reset()

            val stepX = w / (points.size - 1).coerceAtLeast(1)
            var prevX = 0f
            var prevY = h - (((points[0].value - minVal) / range) * (h * 0.72f) + h * 0.14f)

            strokePath.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, h)
            fillPath.lineTo(prevX, prevY)

            for (i in 1 until points.size) {
                val currX = i * stepX
                val normalizedY = (points[i].value - minVal) / range
                val currY = h - (normalizedY * (h * 0.72f) + h * 0.14f)

                val cx1 = prevX + (currX - prevX) / 2f
                val cy1 = prevY
                val cx2 = prevX + (currX - prevX) / 2f
                val cy2 = currY

                strokePath.cubicTo(cx1, cy1, cx2, cy2, currX, currY)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, currX, currY)

                prevX = currX
                prevY = currY
            }

            fillPath.lineTo(prevX, h)
            fillPath.close()

            // Translucent gradient underfill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = if (isDarkMode) 0.32f * glowIntensity else 0.22f * glowIntensity),
                        lineColor.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Outer soft bloom stroke
            drawPath(
                path = strokePath,
                color = lineColor.copy(alpha = if (isDarkMode) 0.35f else 0.20f),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Sharp crisp foreground stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Real-time leading tracking beacon
            val lastX = prevX
            val lastY = prevY

            drawCircle(
                color = lineColor.copy(alpha = pulseAlpha * 0.4f),
                radius = 5.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
    }
}

/**
 * 90 FPS Oscilloscope Waveform Graph with Live Phosphor Scanline,
 * Dual-Layer Bloom Stroke, Dynamic Grid, and Telemetry Peak/Valley Trackers.
 */
@Composable
fun DetailedWaveformGraph(
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = TactileRoyalBlue,
    isDarkMode: Boolean = false,
    glowIntensity: Float = 0.75f,
    heightDp: Dp = 160.dp,
    showGrid: Boolean = true,
    showScanline: Boolean = true,
    unitSymbol: String = ""
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oscilloscopeVFX")

    // 90 FPS Laser Sweep Beam traveling across the display
    val scanlineFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineFrac"
    )

    // Pulsing head beacon scale
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    // Reusable Path instances to eliminate GC overhead at 90Hz
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }
    val gridPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .graphicsLayer { clip = false }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (showGrid) {
                drawOptimizedOscilloscopeGrid(gridPath, w, h, isDarkMode)
            }

            if (points.size < 2) return@Canvas

            var minVal = Float.MAX_VALUE
            var maxVal = -Float.MAX_VALUE
            var minIdx = 0
            var maxIdx = 0

            for (i in points.indices) {
                val v = points[i].value
                if (v < minVal) {
                    minVal = v
                    minIdx = i
                }
                if (v > maxVal) {
                    maxVal = v
                    maxIdx = i
                }
            }
            val range = (maxVal - minVal).coerceAtLeast(0.001f)

            strokePath.reset()
            fillPath.reset()

            val stepX = w / (points.size - 1).coerceAtLeast(1)
            var prevX = 0f
            var prevY = h - (((points[0].value - minVal) / range) * (h * 0.68f) + h * 0.16f)

            strokePath.moveTo(prevX, prevY)
            fillPath.moveTo(prevX, h)
            fillPath.lineTo(prevX, prevY)

            var minCoord = Offset(prevX, prevY)
            var maxCoord = Offset(prevX, prevY)

            for (i in 1 until points.size) {
                val currX = i * stepX
                val normalizedY = (points[i].value - minVal) / range
                val currY = h - (normalizedY * (h * 0.68f) + h * 0.16f)

                if (i == minIdx) minCoord = Offset(currX, currY)
                if (i == maxIdx) maxCoord = Offset(currX, currY)

                val cx1 = prevX + (currX - prevX) / 2f
                val cy1 = prevY
                val cx2 = prevX + (currX - prevX) / 2f
                val cy2 = currY

                strokePath.cubicTo(cx1, cy1, cx2, cy2, currX, currY)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, currX, currY)

                prevX = currX
                prevY = currY
            }

            fillPath.lineTo(prevX, h)
            fillPath.close()

            // 1. Luminous Gradient Underfill with Soft Tactile Fade
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = if (isDarkMode) 0.36f * glowIntensity else 0.24f * glowIntensity),
                        lineColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Wide Glow Bloom Stroke (creates the 90 FPS neon/glass luminescence)
            drawPath(
                path = strokePath,
                color = lineColor.copy(alpha = if (isDarkMode) 0.32f else 0.18f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 3. Crisp High-Precision Center Stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. Oscilloscope Phosphor Scanline Laser Sweep
            if (showScanline) {
                val scanX = scanlineFraction * w
                val beamWidth = 24.dp.toPx()

                // Phosphor decay trail behind the laser sweep
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            lineColor.copy(alpha = if (isDarkMode) 0.18f else 0.10f),
                            lineColor.copy(alpha = if (isDarkMode) 0.38f else 0.22f),
                            Color.Transparent
                        ),
                        startX = (scanX - beamWidth).coerceAtLeast(0f),
                        endX = (scanX + beamWidth * 0.3f).coerceAtMost(w)
                    ),
                    topLeft = Offset((scanX - beamWidth).coerceAtLeast(0f), 0f),
                    size = androidx.compose.ui.geometry.Size(beamWidth * 1.3f, h)
                )

                // Laser sweep vertical needle line
                drawLine(
                    color = Color.White.copy(alpha = if (isDarkMode) 0.75f else 0.55f),
                    start = Offset(scanX, 0f),
                    end = Offset(scanX, h),
                    strokeWidth = 1.2.dp.toPx()
                )
            }

            // 5. Min / Max Telemetry Markers
            // Valley Marker (Min)
            drawCircle(
                color = Color(0xFF64748B).copy(alpha = 0.5f),
                radius = 3.dp.toPx(),
                center = minCoord
            )
            // Peak Marker (Max)
            drawCircle(
                color = lineColor.copy(alpha = 0.6f),
                radius = 4.dp.toPx(),
                center = maxCoord
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = maxCoord
            )

            // 6. Live Telemetry Head Tracking Beacon (at the latest live reading)
            val headOffset = Offset(prevX, prevY)
            drawCircle(
                color = lineColor.copy(alpha = 0.25f),
                radius = 9.dp.toPx() * beaconPulse,
                center = headOffset
            )
            drawCircle(
                color = lineColor,
                radius = 4.5.dp.toPx(),
                center = headOffset
            )
            drawCircle(
                color = Color.White,
                radius = 2.2.dp.toPx(),
                center = headOffset
            )
        }
    }
}

/**
 * High-Performance Vector Grid for Neumorphic Oscilloscope Screen.
 */
private fun DrawScope.drawOptimizedOscilloscopeGrid(
    path: Path,
    width: Float,
    height: Float,
    isDarkMode: Boolean
) {
    val gridColor = if (isDarkMode) Color(0x22475569) else Color(0x33CBD5E1)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
    val stroke = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)

    // Horizontal grid lines (4 horizontal tracks)
    val horizontalDivisions = 4
    for (i in 1..horizontalDivisions) {
        val y = height * (i.toFloat() / (horizontalDivisions + 1))
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = stroke.width,
            pathEffect = stroke.pathEffect
        )
    }

    // Vertical grid divisions (6 time slices)
    val verticalDivisions = 6
    for (i in 1..verticalDivisions) {
        val x = width * (i.toFloat() / (verticalDivisions + 1))
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = stroke.width,
            pathEffect = stroke.pathEffect
        )
    }
}

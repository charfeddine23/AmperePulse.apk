package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryStatus
import com.example.data.ChargePlug
import com.example.data.LivingBatteryState
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 90 FPS Hardware-Accelerated Circular Liquid Progress Ring
 * Supporting 4 Living States: Overdrive/Heating, Cooling, Energy Storing, and Deep Sleep.
 */
@Composable
fun LiquidBatteryRing(
    percentage: Int,
    status: BatteryStatus,
    plugType: ChargePlug,
    wattageW: Float,
    livingState: LivingBatteryState,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 230.dp,
    glowIntensity: Float = 0.88f,
    ambientLiquidWaves: Boolean = true
) {
    // Smooth percentage transition using spring
    val animatedPercent by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "animatedPercent"
    )

    // Dynamic wave speed based on state (Overdrive = fast bubbling, Deep sleep = slow calm)
    val waveDuration = when (livingState) {
        LivingBatteryState.OVERDRIVE_HEATING -> 1400
        LivingBatteryState.COOLING -> 2400
        LivingBatteryState.ENERGY_STORING -> 2000
        LivingBatteryState.DEEP_SLEEP -> 3800
    }

    val infiniteTransition = rememberInfiniteTransition(label = "liquidWaves")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(waveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (livingState == LivingBatteryState.OVERDRIVE_HEATING) 600 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )

    // Animated State Colors
    val activePrimary by animateColorAsState(
        targetValue = livingState.neonPrimary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "neonPrimary"
    )
    val activeSecondary by animateColorAsState(
        targetValue = livingState.neonSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "neonSecondary"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("circular_liquid_progress_ring")
            .graphicsLayer {
                // Hardware layer isolation for 90 FPS rendering
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val canvasSize = size.minDimension
            val strokeWidth = 14.dp.toPx()
            val radius = (canvasSize - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Obsidian Glass Inner Backdrop Circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xD90E1714),
                        Color(0xF50A0E0D)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. State-specific Liquid Fluid Wave
            if (ambientLiquidWaves) {
                drawLivingLiquidWave(
                    center = center,
                    radius = radius * 0.90f,
                    fillRatio = (animatedPercent / 100f).coerceIn(0.05f, 0.96f),
                    wavePhase = wavePhase,
                    livingState = livingState,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    glowIntensity = glowIntensity
                )
            }

            // 3. Inner Specular Glass Arc & Track
            drawCircle(
                color = activePrimary.copy(alpha = 0.14f * glowIntensity),
                radius = radius * 0.92f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Background Track Arc (Frosted Obsidian track)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0x221A2822),
                        Color(0x3310241C),
                        Color(0x221A2822)
                    ),
                    center = center
                ),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 5. Active Glowing Sweep Arc
            val activeSweep = (animatedPercent / 100f).coerceIn(0f, 1f) * 270f
            if (activeSweep > 0f) {
                // Wide ambient glow halo
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to activeSecondary.copy(alpha = 0.28f * glowIntensity * auraPulse),
                        0.6f to activePrimary.copy(alpha = 0.38f * glowIntensity * auraPulse),
                        1.0f to activePrimary.copy(alpha = 0.45f * glowIntensity),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth * 1.9f, cap = StrokeCap.Round)
                )

                // Main Razor-sharp Luminous Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to activeSecondary,
                        0.5f to activePrimary,
                        1.0f to activePrimary,
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Head pulse cap glow dot
                val endAngleRad = Math.toRadians((135f + activeSweep).toDouble())
                val headX = center.x + radius * cos(endAngleRad).toFloat()
                val headY = center.y + radius * sin(endAngleRad).toFloat()

                drawCircle(
                    color = activePrimary.copy(alpha = 0.7f * glowIntensity),
                    radius = strokeWidth * 0.95f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.38f,
                    center = Offset(headX, headY)
                )
            }

            // 6. Perimeter Technical Dial Ticks
            drawTechDialTicks(center = center, radius = radius + 11.dp.toPx(), accentColor = activePrimary)
        }

        // Center Digital Display Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // State Icon & Status
            val stateIcon = when (livingState) {
                LivingBatteryState.OVERDRIVE_HEATING -> Icons.Default.LocalFireDepartment
                LivingBatteryState.COOLING -> Icons.Default.AcUnit
                LivingBatteryState.ENERGY_STORING -> Icons.Default.Bolt
                LivingBatteryState.DEEP_SLEEP -> Icons.Default.Bedtime
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = stateIcon,
                    contentDescription = livingState.title,
                    tint = activePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (status == BatteryStatus.CHARGING)
                        "${String.format("%.1f", wattageW)}W PLASMA"
                    else
                        "${livingState.title.uppercase()}",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = activePrimary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage Readout
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${percentage}",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    letterSpacing = 1.sp,
                    color = TextLuminous
                )
                Text(
                    text = "%",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = activePrimary,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }

            // Living State XP Ticker Label
            Text(
                text = livingState.xpRateLabel.uppercase(),
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                color = if (livingState.isBleeding) Color(0xFFFF453A) else activePrimary
            )
        }
    }
}

/**
 * 90 FPS living liquid wave renderer with state-specific fluid physics
 */
private fun DrawScope.drawLivingLiquidWave(
    center: Offset,
    radius: Float,
    fillRatio: Float,
    wavePhase: Float,
    livingState: LivingBatteryState,
    primaryColor: Color,
    secondaryColor: Color,
    glowIntensity: Float
) {
    val waterLevelY = (center.y + radius) - (radius * 2f * fillRatio)
    val wavePath = Path()
    val waveHeight = if (livingState == LivingBatteryState.OVERDRIVE_HEATING) 9.dp.toPx() else 6.5.dp.toPx()

    wavePath.moveTo(center.x - radius, center.y + radius)

    val segments = 24
    for (i in 0..segments) {
        val x = (center.x - radius) + (radius * 2f * i / segments)
        val normalizedX = (i.toFloat() / segments) * 2 * PI
        val y = waterLevelY + sin(normalizedX + wavePhase).toFloat() * waveHeight
        wavePath.lineTo(x, y)
    }

    wavePath.lineTo(center.x + radius, center.y + radius)
    wavePath.close()

    val clipPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            )
        )
    }

    drawContext.canvas.save()
    drawContext.canvas.clipPath(clipPath)

    drawPath(
        path = wavePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.36f * glowIntensity),
                secondaryColor.copy(alpha = 0.16f * glowIntensity),
                Color.Transparent
            ),
            startY = waterLevelY - waveHeight,
            endY = center.y + radius
        )
    )

    drawPath(
        path = wavePath,
        color = primaryColor.copy(alpha = 0.6f * glowIntensity),
        style = Stroke(width = 1.6.dp.toPx())
    )

    drawContext.canvas.restore()
}

private fun DrawScope.drawTechDialTicks(
    center: Offset,
    radius: Float,
    accentColor: Color
) {
    val totalTicks = 36
    for (i in 0 until totalTicks) {
        val angleDeg = 135f + (i * (270f / (totalTicks - 1)))
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val isMajor = i % 6 == 0
        val tickLen = if (isMajor) 5.dp.toPx() else 2.5.dp.toPx()

        val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
        val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
        val endX = center.x + radius * cos(angleRad).toFloat()
        val endY = center.y + radius * sin(angleRad).toFloat()

        drawLine(
            color = if (isMajor) accentColor.copy(alpha = 0.5f) else Color(0x3386A396),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

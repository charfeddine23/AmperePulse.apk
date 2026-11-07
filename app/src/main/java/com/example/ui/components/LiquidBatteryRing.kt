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
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Tactile Neumorphic & Soft Liquid Glass Battery Ring
 * Supports Light & Dark themes with 4 dynamic living states.
 */
@Composable
fun LiquidBatteryRing(
    percentage: Int,
    status: BatteryStatus,
    plugType: ChargePlug,
    wattageW: Float,
    livingState: LivingBatteryState,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    sizeDp: Dp = 230.dp,
    glowIntensity: Float = 0.75f,
    ambientLiquidWaves: Boolean = true
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "animatedPercent"
    )

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
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraPulse"
    )

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
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val canvasSize = size.minDimension
            val strokeWidth = 14.dp.toPx()
            val radius = (canvasSize - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Neumorphic Extruded Dial Dual Shadows
            val shadowOffset = 6.dp.toPx()
            val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x2E9AAEC4)
            val highlightColor = if (isDarkMode) Color(0x1F94A3B8) else Color(0xE6FFFFFF)

            // Bottom-right shadow
            drawCircle(
                color = shadowColor,
                radius = radius,
                center = Offset(center.x + shadowOffset, center.y + shadowOffset)
            )

            // Top-left highlight
            drawCircle(
                color = highlightColor,
                radius = radius,
                center = Offset(center.x - shadowOffset * 0.7f, center.y - shadowOffset * 0.7f)
            )

            // Dial Disc Surface
            val discColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkMode) {
                        listOf(Color(0xFF243348), Color(0xFF1A2433))
                    } else {
                        listOf(Color(0xFFFFFFFF), Color(0xFFF7FAFD), Color(0xFFEFF3F8))
                    },
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Liquid Fluid Wave inside circle
            if (ambientLiquidWaves) {
                drawNeumorphicLiquidWave(
                    center = center,
                    radius = radius * 0.88f,
                    fillRatio = (animatedPercent / 100f).coerceIn(0.05f, 0.96f),
                    wavePhase = wavePhase,
                    livingState = livingState,
                    primaryColor = activePrimary,
                    secondaryColor = activeSecondary,
                    isDarkMode = isDarkMode
                )
            }

            // 3. Tactile Inner Ring Bevel
            drawCircle(
                color = if (isDarkMode) Color(0x33475569) else Color(0x66CBD5E1),
                radius = radius * 0.90f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Background Track Arc (Soft Neumorphic Groove)
            val trackColor = if (isDarkMode) Color(0xFF28364B) else Color(0xFFE2E8F0)
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 5. Active Tactile Liquid Progress Arc
            val activeSweep = (animatedPercent / 100f).coerceIn(0f, 1f) * 270f
            if (activeSweep > 0f) {
                // Soft glow halo behind active arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to activeSecondary.copy(alpha = 0.22f * auraPulse),
                        0.5f to activePrimary.copy(alpha = 0.35f * auraPulse),
                        1.0f to activePrimary.copy(alpha = 0.45f),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth * 1.6f, cap = StrokeCap.Round)
                )

                // Crisp Vibrant Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to activeSecondary,
                        0.6f to activePrimary,
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

                // Head tracking pulse dot
                val endAngleRad = Math.toRadians((135f + activeSweep).toDouble())
                val headX = center.x + radius * cos(endAngleRad).toFloat()
                val headY = center.y + radius * sin(endAngleRad).toFloat()

                drawCircle(
                    color = activePrimary.copy(alpha = 0.4f),
                    radius = strokeWidth * 0.85f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = Offset(headX, headY)
                )
                drawCircle(
                    color = activePrimary,
                    radius = strokeWidth * 0.25f,
                    center = Offset(headX, headY)
                )
            }

            // 6. Perimeter Technical Graduation Ticks
            drawDialTicks(center = center, radius = radius + 10.dp.toPx(), accentColor = activePrimary, isDarkMode = isDarkMode)
        }

        // Center Digital Display Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (status == BatteryStatus.CHARGING)
                        "${String.format("%.1f", wattageW)}W CHARGING"
                    else
                        livingState.title.uppercase(),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = activePrimary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage Readout with High-Contrast Dark Charcoal Text
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$percentage",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp,
                    lineHeight = 54.sp,
                    letterSpacing = (-1).sp,
                    color = if (isDarkMode) TextWhite else TextCharcoal
                )
                Text(
                    text = "%",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = activePrimary,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }

            // Real-time hardware sensor status indicator (no XP display)
            Text(
                text = if (status == BatteryStatus.CHARGING)
                    "${String.format("%.1f", wattageW)}W • ACTIVE CHARGE"
                else
                    "HARDWARE SENSORS ACTIVE",
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = TextSlate
            )
        }
    }
}

private fun DrawScope.drawNeumorphicLiquidWave(
    center: Offset,
    radius: Float,
    fillRatio: Float,
    wavePhase: Float,
    livingState: LivingBatteryState,
    primaryColor: Color,
    secondaryColor: Color,
    isDarkMode: Boolean
) {
    val waterLevelY = (center.y + radius) - (radius * 2f * fillRatio)
    val wavePath = Path()
    val waveHeight = 6.dp.toPx()

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
                primaryColor.copy(alpha = if (isDarkMode) 0.32f else 0.22f),
                secondaryColor.copy(alpha = if (isDarkMode) 0.14f else 0.08f),
                Color.Transparent
            ),
            startY = waterLevelY - waveHeight,
            endY = center.y + radius
        )
    )

    drawPath(
        path = wavePath,
        color = primaryColor.copy(alpha = 0.45f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    drawContext.canvas.restore()
}

private fun DrawScope.drawDialTicks(
    center: Offset,
    radius: Float,
    accentColor: Color,
    isDarkMode: Boolean
) {
    val totalTicks = 36
    val tickColor = if (isDarkMode) Color(0x4494A3B8) else Color(0x66CBD5E1)

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
            color = if (isMajor) accentColor.copy(alpha = 0.65f) else tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

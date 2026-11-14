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
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LivingBatteryState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileViolet
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-Performance 90Hz Morphing Energy Core with Biological Breathing Semicircles.
 * Seamlessly morphs between a fluid pulsing Energy Flame and a 3D-styled faceted Cryo Ice Cube,
 * surrounded by human-breathing multi-layered concentric arcs that dynamically accelerate with wattage/thermals.
 */
@Composable
fun MorphingEnergyCore(
    livingState: LivingBatteryState,
    percentage: Int,
    wattageW: Float,
    temperatureC: Float,
    isOverload: Boolean,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    sizeDp: Dp = 250.dp
) {
    // Morph progress: 0f = Pure Energy Flame, 1f = Shimmering Cryo Ice Cube
    val targetMorph = if (livingState == LivingBatteryState.COOLING) 1f else 0f
    val morphProgress by animateFloatAsState(
        targetValue = targetMorph,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "coreMorphProgress"
    )

    // Breathing speed dynamically accelerates with wattage and thermal spikes
    val breathingDurationMs = when {
        isOverload || temperatureC >= 42f -> 950 // Rapid hyperventilation emergency pulse
        temperatureC >= 40f -> 1400            // Thermal stress elevated breathing
        wattageW >= 20f -> 1600                // High wattage fast charge influx
        wattageW >= 8f -> 2400                 // Standard charge breathing
        livingState == LivingBatteryState.DEEP_SLEEP -> 4200 // Deep calm hibernation breath
        else -> 3400                           // Balanced homeostasis rhythm
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coreAnimation")

    // Organic sinusoidal breathing cycle (scale & alpha oscillation)
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(breathingDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathCycle"
    )

    // Flame flicker phase
    val flamePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flameCycle"
    )

    // Cryo Cube crystal rotation shimmer
    val crystalShimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "crystalShimmer"
    )

    // Primary state color
    val activeStateColor by animateColorAsState(
        targetValue = if (isOverload) TactileCoralRed else livingState.neonPrimary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "activeStateColor"
    )

    val breathingScale = 1.0f + 0.06f * sin(breathPhase)
    val breathingAlpha = 0.55f + 0.35f * sin(breathPhase)

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("flame_to_ice_morphing_core")
            .graphicsLayer {
                // 90 FPS isolated rendering layer
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f

            // 1. Dual Soft Neumorphic Outer Base Bed
            val shadowOffset = 5.dp.toPx()
            val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x407589A2)
            val highlightColor = if (isDarkMode) Color(0x1F94A3B8) else Color(0x7AF2F5FA)

            drawCircle(
                color = shadowColor,
                radius = baseRadius * 0.94f,
                center = Offset(center.x + shadowOffset, center.y + shadowOffset)
            )
            drawCircle(
                color = highlightColor,
                radius = baseRadius * 0.94f,
                center = Offset(center.x - shadowOffset * 0.7f, center.y - shadowOffset * 0.7f)
            )

            // Tactile disc plate
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkMode) {
                        listOf(Color(0xFF243348), Color(0xFF1E293B))
                    } else {
                        listOf(Color(0xFFECF1F7), Color(0xFFE5EBF2), Color(0xFFD9E1EB))
                    },
                    center = center,
                    radius = baseRadius * 0.94f
                ),
                radius = baseRadius * 0.94f,
                center = center
            )

            // 2. BIOLOGICAL BREATHING SEMICIRCLES (Multi-Layered Concentric Arcs)
            drawBiologicalBreathingSemicircles(
                center = center,
                baseRadius = baseRadius * 0.90f,
                breathingScale = breathingScale,
                breathingAlpha = breathingAlpha,
                breathPhase = breathPhase,
                accentColor = activeStateColor,
                isOverload = isOverload,
                isDarkMode = isDarkMode
            )

            // 3. FLAME-TO-ICE MORPHING CORE
            if (morphProgress < 1.0f) {
                // Render Fluid Pulsing Energy Flame with continuous particles
                val flameWeight = 1f - morphProgress
                drawEnergyFlame(
                    center = center,
                    radius = baseRadius * 0.44f,
                    flamePhase = flamePhase,
                    flameWeight = flameWeight,
                    stateColor = activeStateColor,
                    isOverload = isOverload,
                    isDarkMode = isDarkMode
                )
            }

            if (morphProgress > 0.0f) {
                // Render 3D Shimmering Cryo Ice Cube with faceted refraction
                drawCryoIceCube(
                    center = center,
                    cubeSize = baseRadius * 0.50f,
                    morphAlpha = morphProgress,
                    shimmerPhase = crystalShimmer,
                    isDarkMode = isDarkMode
                )
            }
        }

        // Central Telemetry Digital Badge & State Emblem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val stateIcon = when {
                isOverload -> Icons.Default.Warning
                livingState == LivingBatteryState.COOLING -> Icons.Default.AcUnit
                livingState == LivingBatteryState.OVERDRIVE_HEATING -> Icons.Default.LocalFireDepartment
                livingState == LivingBatteryState.ENERGY_STORING -> Icons.Default.Bolt
                else -> Icons.Default.Bedtime
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = stateIcon,
                    contentDescription = livingState.title,
                    tint = activeStateColor,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        isOverload -> "THERMAL OVERLOAD!"
                        livingState == LivingBatteryState.COOLING -> "CRYO MATRIX ACTIVE"
                        livingState == LivingBatteryState.OVERDRIVE_HEATING -> "HIGH THERMAL LOAD"
                        livingState == LivingBatteryState.ENERGY_STORING -> "RAPID SATURATION"
                        else -> "HIBERNATION MODE"
                    },
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = activeStateColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Percentage readout
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$percentage",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 50.sp,
                    lineHeight = 50.sp,
                    letterSpacing = (-1).sp,
                    color = if (isDarkMode) TextWhite else TextCharcoal
                )
                Text(
                    text = "%",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = activeStateColor,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }

            // Real-time thermal / wattage label
            Text(
                text = "${String.format("%.1f", temperatureC)}°C  •  ${String.format("%.1f", wattageW)}W",
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = if (temperatureC >= 42f) TactileCoralRed else TextSlate
            )
        }
    }
}

/**
 * Concentric multi-layered breathing semicircles that dynamically scale and pulse.
 */
private fun DrawScope.drawBiologicalBreathingSemicircles(
    center: Offset,
    baseRadius: Float,
    breathingScale: Float,
    breathingAlpha: Float,
    breathPhase: Float,
    accentColor: Color,
    isOverload: Boolean,
    isDarkMode: Boolean
) {
    val trackBg = if (isDarkMode) Color(0xFF28364B) else Color(0xFFCFD9E6)
    val overloadRed = Color(0xFFFF3D00)
    val effectiveColor = if (isOverload) overloadRed else accentColor

    // Layer 1: Outer Atmosphere Breathing Semicircle Arc (Top & Bottom)
    val outerRadius = (baseRadius * 0.98f) * breathingScale
    val outerStroke = 3.dp.toPx()

    // Top Semicircular Breathing Arc
    drawArc(
        brush = Brush.sweepGradient(
            0.0f to effectiveColor.copy(alpha = 0.05f),
            0.25f to effectiveColor.copy(alpha = 0.45f * breathingAlpha),
            0.5f to effectiveColor.copy(alpha = 0.8f * breathingAlpha),
            0.75f to effectiveColor.copy(alpha = 0.45f * breathingAlpha),
            1.0f to effectiveColor.copy(alpha = 0.05f),
            center = center
        ),
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = outerStroke, cap = StrokeCap.Round)
    )

    // Bottom Semicircular Breathing Arc
    drawArc(
        brush = Brush.sweepGradient(
            0.0f to effectiveColor.copy(alpha = 0.05f),
            0.25f to effectiveColor.copy(alpha = 0.45f * breathingAlpha),
            0.5f to effectiveColor.copy(alpha = 0.8f * breathingAlpha),
            0.75f to effectiveColor.copy(alpha = 0.45f * breathingAlpha),
            1.0f to effectiveColor.copy(alpha = 0.05f),
            center = center
        ),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        style = Stroke(width = outerStroke, cap = StrokeCap.Round)
    )

    // Layer 2: Mid Precision Calibration Track (Dotted & Graduated Arc)
    val midRadius = baseRadius * 0.84f
    drawArc(
        color = trackBg.copy(alpha = 0.7f),
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(center.x - midRadius, center.y - midRadius),
        size = Size(midRadius * 2, midRadius * 2),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    )

    // Layer 3: Inner Vital Energy Flow Arc
    val innerRadius = baseRadius * 0.70f
    val sweepAngle = 240f
    val dynamicSweep = sweepAngle * (0.85f + 0.15f * sin(breathPhase))

    drawArc(
        brush = Brush.sweepGradient(
            0.0f to effectiveColor.copy(alpha = 0.15f),
            0.5f to effectiveColor,
            1.0f to effectiveColor.copy(alpha = 0.3f),
            center = center
        ),
        startAngle = 150f,
        sweepAngle = dynamicSweep,
        useCenter = false,
        topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
        size = Size(innerRadius * 2, innerRadius * 2),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Orbiting Biological Vital Pulse Head Dot
    val orbitAngleRad = Math.toRadians((150f + dynamicSweep).toDouble())
    val headX = center.x + innerRadius * cos(orbitAngleRad).toFloat()
    val headY = center.y + innerRadius * sin(orbitAngleRad).toFloat()

    drawCircle(
        color = effectiveColor.copy(alpha = 0.35f * breathingAlpha),
        radius = 8.dp.toPx(),
        center = Offset(headX, headY)
    )
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(headX, headY)
    )
}

/**
 * Fluid, pulsing energy flame with upward plasma filaments and floating particle sparks.
 */
private fun DrawScope.drawEnergyFlame(
    center: Offset,
    radius: Float,
    flamePhase: Float,
    flameWeight: Float,
    stateColor: Color,
    isOverload: Boolean,
    isDarkMode: Boolean
) {
    val flameAlpha = flameWeight.coerceIn(0f, 1f)
    if (flameAlpha <= 0.01f) return

    val flameHeight = radius * 1.5f
    val flameWidth = radius * 1.1f
    val flicker = 4.dp.toPx() * sin(flamePhase * 2.5f)

    // Outer Flame Tongue Path
    val outerFlame = Path().apply {
        moveTo(center.x, center.y - flameHeight + flicker)
        // Right side of flame
        cubicTo(
            center.x + flameWidth * 0.9f, center.y - flameHeight * 0.4f,
            center.x + flameWidth * 0.8f, center.y + flameHeight * 0.6f,
            center.x, center.y + flameHeight * 0.7f
        )
        // Left side of flame
        cubicTo(
            center.x - flameWidth * 0.8f, center.y + flameHeight * 0.6f,
            center.x - flameWidth * 0.9f, center.y - flameHeight * 0.4f,
            center.x, center.y - flameHeight + flicker
        )
        close()
    }

    // Radiant flame aura glow
    drawPath(
        path = outerFlame,
        brush = Brush.radialGradient(
            colors = listOf(
                stateColor.copy(alpha = (if (isDarkMode) 0.30f else 0.20f) * flameAlpha),
                stateColor.copy(alpha = 0.05f * flameAlpha),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.6f
        )
    )

    // Core Incandescent Flame Path
    val innerFlame = Path().apply {
        moveTo(center.x, center.y - flameHeight * 0.65f + flicker * 0.6f)
        cubicTo(
            center.x + flameWidth * 0.45f, center.y - flameHeight * 0.1f,
            center.x + flameWidth * 0.4f, center.y + flameHeight * 0.4f,
            center.x, center.y + flameHeight * 0.5f
        )
        cubicTo(
            center.x - flameWidth * 0.4f, center.y + flameHeight * 0.4f,
            center.x - flameWidth * 0.45f, center.y - flameHeight * 0.1f,
            center.x, center.y - flameHeight * 0.65f + flicker * 0.6f
        )
        close()
    }

    drawPath(
        path = innerFlame,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * flameAlpha),
                stateColor.copy(alpha = 0.65f * flameAlpha),
                stateColor.copy(alpha = 0.15f * flameAlpha)
            ),
            startY = center.y - flameHeight,
            endY = center.y + flameHeight
        )
    )

    // Floating Plasma Sparks / Particle Emitters ascending at 90 FPS
    val particleCount = 8
    for (i in 0 until particleCount) {
        val seed = i.toFloat() / particleCount
        val progress = (seed + (flamePhase / (2 * PI).toFloat()) * 0.8f) % 1.0f
        val particleY = center.y + flameHeight * 0.4f - (progress * flameHeight * 1.2f)
        val particleX = center.x + (flameWidth * 0.4f * sin(progress * 4f + i * 1.2f))
        val particleSize = (3.dp.toPx() * (1f - progress * 0.7f)).coerceAtLeast(1.dp.toPx())

        drawCircle(
            color = Color.White.copy(alpha = (1f - progress) * 0.75f * flameAlpha),
            radius = particleSize,
            center = Offset(particleX, particleY)
        )
    }
}

/**
 * 3D-styled faceted Cryo Ice Cube / Crystal with cold refraction facets and frost edges.
 */
private fun DrawScope.drawCryoIceCube(
    center: Offset,
    cubeSize: Float,
    morphAlpha: Float,
    shimmerPhase: Float,
    isDarkMode: Boolean
) {
    val alpha = morphAlpha.coerceIn(0f, 1f)
    if (alpha <= 0.01f) return

    val w = cubeSize * 0.85f
    val h = cubeSize * 0.75f
    val cornerOffset = cubeSize * 0.42f
    val shimmerSine = sin(shimmerPhase)

    // Isometric 3D Cube Points
    val pTop = Offset(center.x, center.y - h)
    val pCenter = Offset(center.x, center.y - h * 0.15f)
    val pBottom = Offset(center.x, center.y + h * 0.75f)
    val pLeft = Offset(center.x - w * 0.85f, center.y - cornerOffset * 0.4f)
    val pRight = Offset(center.x + w * 0.85f, center.y - cornerOffset * 0.4f)
    val pBottomLeft = Offset(center.x - w * 0.85f, center.y + cornerOffset * 0.4f)
    val pBottomRight = Offset(center.x + w * 0.85f, center.y + cornerOffset * 0.4f)

    // 1. Shimmering Cryo Ambient Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                TactileCyan.copy(alpha = (if (isDarkMode) 0.35f else 0.22f) * alpha),
                Color(0xFFE0F7FA).copy(alpha = 0.08f * alpha),
                Color.Transparent
            ),
            center = center,
            radius = cubeSize * 1.5f
        )
    )

    // 2. Top Face of Ice Cube (Icy Specular Crystalline Glare)
    val topFace = Path().apply {
        moveTo(pTop.x, pTop.y)
        lineTo(pRight.x, pRight.y)
        lineTo(pCenter.x, pCenter.y)
        lineTo(pLeft.x, pLeft.y)
        close()
    }
    drawPath(
        path = topFace,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f * alpha),
                Color(0xFFE0F7FA).copy(alpha = 0.65f * alpha),
                TactileCyan.copy(alpha = 0.45f * alpha)
            ),
            start = pTop,
            end = pCenter
        )
    )

    // 3. Left Face of Ice Cube (Cold Deep Refraction)
    val leftFace = Path().apply {
        moveTo(pLeft.x, pLeft.y)
        lineTo(pCenter.x, pCenter.y)
        lineTo(pBottom.x, pBottom.y)
        lineTo(pBottomLeft.x, pBottomLeft.y)
        close()
    }
    drawPath(
        path = leftFace,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF40C4FF).copy(alpha = 0.60f * alpha),
                Color(0xFF0091EA).copy(alpha = 0.75f * alpha)
            ),
            start = pLeft,
            end = pBottom
        )
    )

    // 4. Right Face of Ice Cube (Cyan Ice Depths)
    val rightFace = Path().apply {
        moveTo(pCenter.x, pCenter.y)
        lineTo(pRight.x, pRight.y)
        lineTo(pBottomRight.x, pBottomRight.y)
        lineTo(pBottom.x, pBottom.y)
        close()
    }
    drawPath(
        path = rightFace,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF00E5FF).copy(alpha = 0.70f * alpha),
                Color(0xFF00B0FF).copy(alpha = 0.85f * alpha)
            ),
            start = pCenter,
            end = pBottomRight
        )
    )

    // 5. Razor-sharp crystalline frost borders
    val strokeStyle = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
    val borderColor = Color.White.copy(alpha = 0.9f * alpha)
    drawPath(topFace, borderColor, style = strokeStyle)
    drawPath(leftFace, borderColor.copy(alpha = 0.6f * alpha), style = strokeStyle)
    drawPath(rightFace, borderColor.copy(alpha = 0.6f * alpha), style = strokeStyle)

    // Center internal refractive light beam
    drawLine(
        color = Color.White.copy(alpha = (0.7f + 0.3f * shimmerSine) * alpha),
        start = pTop,
        end = pBottom,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Shimmering Star Sparkles orbiting the Ice Cube
    val sparkleAngle = shimmerPhase
    val sparkleDist = cubeSize * 0.88f
    val sX = center.x + sparkleDist * cos(sparkleAngle)
    val sY = center.y + sparkleDist * sin(sparkleAngle) * 0.65f

    drawCircle(
        color = Color.White.copy(alpha = 0.85f * alpha),
        radius = 2.5.dp.toPx(),
        center = Offset(sX, sY)
    )
}

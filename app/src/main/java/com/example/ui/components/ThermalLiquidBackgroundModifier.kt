package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import com.example.ui.theme.NeumorphBgDark
import com.example.ui.theme.NeumorphBgLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dynamic Thermal Liquid Glass Background Modifier for the Main Dashboard.
 *
 * Smoothly shifts between:
 * - Cool Blue / Cyan Frost (<= 34°C - 37°C): Serene cryogenic liquid glow during normal operation.
 * - High-Intensity Blazing Orange & Crimson Red (> 40°C): Urgent thermal stress radiance.
 *
 * Utilizes multi-layer overlapping radial gradients, soft Gaussian chromatic falloffs,
 * and undulating fluid phase drift to achieve the soft blurred Liquid Glass aesthetic at locked 90 FPS.
 */
fun Modifier.thermalLiquidGlassBackground(
    temperatureC: Float,
    isDarkMode: Boolean,
    glowIntensity: Float = 0.85f,
    criticalTempThreshold: Float = 40.0f,
    normalTempThreshold: Float = 34.0f
): Modifier = composed {
    // 1. Color Palettes
    // Cool Blue Normal Spectrum
    val coolBluePrimary = Color(0xFF00B0FF)      // Electric Cryo Cyan
    val coolBlueSecondary = Color(0xFF00E5FF)    // Frost Azure
    val coolBlueAmbient = Color(0xFF0288D1)      // Deep Oceanic Blue

    // High-Intensity Thermal Overheat Spectrum (> 40°C)
    val hotOrangePrimary = Color(0xFFFF3D00)     // High-Intensity Neon Orange-Red
    val hotCrimsonSecondary = Color(0xFFFF1744)  // Blazing Crimson Stress Red
    val hotAmberAmbient = Color(0xFFFF9100)      // Radiant Molten Amber

    // 2. Continuous thermal interpolation ratio (0.0f = Cool Blue, 1.0f = Blazing Red/Orange)
    val rawRatio = ((temperatureC - normalTempThreshold) / (criticalTempThreshold - normalTempThreshold))
        .coerceIn(0f, 1f)

    // Target colors interpolated according to real-time temperature
    val targetPrimary = lerp(coolBluePrimary, hotOrangePrimary, rawRatio)
    val targetSecondary = lerp(coolBlueSecondary, hotCrimsonSecondary, rawRatio)
    val targetAmbient = lerp(coolBlueAmbient, hotAmberAmbient, rawRatio)

    // 3. Ultra-smooth color morphing animations (prevents abrupt sensor jumps)
    val animatedPrimary by animateColorAsState(
        targetValue = targetPrimary,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "thermalPrimaryColor"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "thermalSecondaryColor"
    )
    val animatedAmbient by animateColorAsState(
        targetValue = targetAmbient,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "thermalAmbientColor"
    )

    // 4. Undulating liquid fluid phase and heat pulse transitions
    val infiniteTransition = rememberInfiniteTransition(label = "thermalLiquidOrbs")

    // Gentle fluid floating motion
    val fluidPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fluidPhase"
    )

    // Dynamic pulse rate: speeds up and intensifies when temperature >= 40°C
    val pulseDuration = if (temperatureC >= criticalTempThreshold) 750 else 2400
    val thermalPulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = if (temperatureC >= criticalTempThreshold) 1.28f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thermalPulse"
    )

    // Base Neumorphic background color
    val rootBg = if (isDarkMode) NeumorphBgDark else NeumorphBgLight

    // Alpha multipliers calibrated for Soft Stone Light Neumorphism vs Dark Obsidian
    val alphaScale = if (isDarkMode) 1.0f else 0.78f
    val baseGlow = glowIntensity * alphaScale * thermalPulse

    val isOverheated = temperatureC >= criticalTempThreshold

    this
        .graphicsLayer { clip = false }
        .drawBehind {
            // A. Solid base substrate
            drawRect(color = rootBg)

            // Dynamic fluid center offsets
            val orb1X = size.width * 0.50f + (sin(fluidPhase) * 36f)
            val orb1Y = size.height * 0.20f + (cos(fluidPhase) * 22f)

            val orb2X = size.width * 0.78f - (cos(fluidPhase) * 30f)
            val orb2Y = size.height * 0.52f + (sin(fluidPhase) * 26f)

            val orb3X = size.width * 0.22f + (sin(fluidPhase * 0.8f) * 24f)
            val orb3Y = size.height * 0.76f - (cos(fluidPhase * 0.8f) * 20f)

            // B. Primary Top Ambient Halo (Upper Dashboard aura behind battery core)
            val primaryRadius = size.width * (if (isOverheated) 1.15f else 0.95f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            animatedPrimary.copy(alpha = 0.16f * baseGlow),
                            animatedSecondary.copy(alpha = 0.08f * baseGlow),
                            animatedAmbient.copy(alpha = 0.02f * baseGlow),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            animatedPrimary.copy(alpha = 0.12f * baseGlow),
                            animatedSecondary.copy(alpha = 0.06f * baseGlow),
                            Color.Transparent
                        )
                    },
                    center = Offset(orb1X, orb1Y),
                    radius = primaryRadius
                )
            )

            // C. Secondary Floating Liquid Lens (Mid-right asymmetric depth)
            val lensRadius = size.width * (if (isOverheated) 0.85f else 0.70f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            animatedSecondary.copy(alpha = 0.12f * baseGlow),
                            animatedPrimary.copy(alpha = 0.04f * baseGlow),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            animatedSecondary.copy(alpha = 0.08f * baseGlow),
                            Color.Transparent
                        )
                    },
                    center = Offset(orb2X, orb2Y),
                    radius = lensRadius
                )
            )

            // D. Lower Swell Diffuser (Subtle ground reflection)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedAmbient.copy(alpha = 0.07f * baseGlow),
                        Color.Transparent
                    ),
                    center = Offset(orb3X, orb3Y),
                    radius = size.width * 0.80f
                )
            )

            // E. High-Intensity Thermal Overheat Perimeter Flare (Activated > 40°C)
            if (rawRatio > 0.6f) {
                val warningAlpha = ((rawRatio - 0.6f) / 0.4f) * (0.09f * baseGlow)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            hotOrangePrimary.copy(alpha = warningAlpha * 1.5f),
                            Color.Transparent,
                            hotCrimsonSecondary.copy(alpha = warningAlpha * 0.7f)
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }
        }
}

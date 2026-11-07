package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CyberEggData
import com.example.data.GlassThemeConfig
import com.example.data.HatchedCompanion
import com.example.data.StreakData
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.ElectricMint
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

/**
 * PAGE 2: Streak & Egg System
 * Features battery compliance streaks, 3 protective health shields,
 * and 100% incubation egg hatching mechanics.
 */
@Composable
fun Page2StreaksAndEggScreen(
    streakData: StreakData,
    eggData: CyberEggData,
    themeConfig: GlassThemeConfig,
    onHatchEgg: () -> HatchedCompanion,
    onRepairShield: () -> Unit
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    var newlyHatchedCompanion by remember { mutableStateOf<HatchedCompanion?>(null) }
    var showHatchCelebration by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
            .graphicsLayer {
                // 90 FPS rendering isolation
                clip = false
            }
    ) {
        // Hero Streak Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LITHIUM HEALTH STREAK",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp,
                                    color = TextLuminous
                                )
                                Text(
                                    text = "20%-80% Charging & Thermal Compliance",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMutedGreen
                                )
                            }
                        }

                        // Day counter
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${streakData.currentStreakDays}",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = accentColor
                            )
                            Text(
                                text = "DAYS ACTIVE",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TextLuminous
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Streak Metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreakStatPill(label = "TODAY COMPLIANCE", value = "${(streakData.todayComplianceRate * 100).toInt()}%")
                        StreakStatPill(label = "HEALTHY MINUTES", value = "${streakData.healthyChargeMinutesToday}m")
                        StreakStatPill(label = "RECORD BEST", value = "${streakData.bestStreakDays} Days")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shield System Card (3 Shields)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = Color(0xFFFFB800),
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shields",
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BATTERY HEALTH SHIELDS",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                        }

                        Text(
                            text = "${streakData.shieldsAvailable} / ${streakData.maxShields} READY",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFFFB800)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Shields automatically absorb sudden thermal spikes or accidental 0% dropouts without breaking your daily streak score.",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextMutedGreen,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Shield Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 1..streakData.maxShields) {
                            val isActive = i <= streakData.shieldsAvailable
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isActive) Color(0x33FFB800) else Color(0x221A201C))
                                    .border(
                                        1.dp,
                                        if (isActive) Color(0xFFFFB800) else Color(0x33446655),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Shield $i",
                                        tint = if (isActive) Color(0xFFFFB800) else Color(0x5586A396),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isActive) "SHIELD $i: ACTIVE" else "EXPENDED",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isActive) TextLuminous else TextMutedGreen
                                    )
                                }
                            }
                        }
                    }

                    if (streakData.shieldsAvailable < streakData.maxShields) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FFB800))
                                .border(1.dp, Color(0xFFFFB800), RoundedCornerShape(10.dp))
                                .clickable { onRepairShield() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RECHARGE 1 SHIELD",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFFFB800)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cyber Egg Incubator Chamber Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CYBER INCUBATION CHAMBER",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = eggData.eggName,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = accentColor
                            )
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Living Incubating Egg Graphic with Floating Animation
                    CyberEggGraphic(
                        progress = eggData.incubationProgress,
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Readout
                    val percentInt = (eggData.incubationProgress * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "INCUBATION STATUS",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextMutedGreen
                        )
                        Text(
                            text = "$percentInt% (${eggData.currentXP} / ${eggData.xpToHatch} XP)",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (percentInt >= 100) Color(0xFF00FF87) else accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { eggData.incubationProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (percentInt >= 100) Color(0xFF00FF87) else accentColor,
                        trackColor = Color(0x3310241C)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hatch Action Button (or simulate boost to 100%)
                    if (eggData.incubationProgress >= 0.99f || percentInt >= 100) {
                        Button(
                            onClick = {
                                val companion = onHatchEgg()
                                newlyHatchedCompanion = companion
                                showHatchCelebration = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FF87),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_hatch_egg")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HATCH CYBER EGG NOW (100%)",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Info & Boost Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+5 XP/min while charging on healthy AC wall adapter.",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextMutedGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        // Trigger Hatch simulation
                                        val comp = onHatchEgg()
                                        newlyHatchedCompanion = comp
                                        showHatchCelebration = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "FAST-FORWARD HATCH",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hatch Celebration Modal Reveal
        AnimatedVisibility(visible = showHatchCelebration && newlyHatchedCompanion != null) {
            newlyHatchedCompanion?.let { companion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = Color(0xFF00FF87),
                        glowIntensity = 1.0f
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✦ NEW COMPANION HATCHED! ✦",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFF00FF87)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(companion.color.copy(alpha = 0.25f))
                                    .border(2.dp, companion.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👾",
                                    fontSize = 28.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = companion.name,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = "RARITY: ${companion.rarity.uppercase()}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = companion.color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ACTIVE PERK: ${companion.perk}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF00FF87))
                                    .clickable { showHatchCelebration = false }
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "COLLECT TO CREW",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hatched Companions Roster Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HATCHED CYBER COMPANIONS (${eggData.hatchedList.size})",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    eggData.hatchedList.forEach { comp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(comp.color.copy(alpha = 0.2f))
                                    .border(1.dp, comp.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👾", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.name,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextLuminous
                                )
                                Text(
                                    text = comp.perk,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.sp,
                                    color = TextMutedGreen
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(comp.color.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = comp.rarity,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = comp.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakStatPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 9.sp,
            color = TextMutedGreen
        )
        Text(
            text = value,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextLuminous
        )
    }
}

/**
 * 90 FPS Floating Cyber Egg with neon fluid plasma shell
 */
@Composable
private fun CyberEggGraphic(
    progress: Float,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eggFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eggY"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = Modifier
            .size(120.dp, 140.dp)
            .graphicsLayer {
                translationY = floatOffset * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Radiant Egg shape path
            val eggPath = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                // Right side curvature
                cubicTo(w * 0.95f, h * 0.15f, w * 0.95f, h * 0.85f, w * 0.5f, h * 0.95f)
                // Left side curvature
                cubicTo(w * 0.05f, h * 0.85f, w * 0.05f, h * 0.15f, w * 0.5f, h * 0.05f)
                close()
            }

            // Outer glowing aura
            drawPath(
                path = eggPath,
                color = accentColor.copy(alpha = 0.25f * glowPulse),
                style = Stroke(width = 12.dp.toPx())
            )

            // Obsidian Glass Shell fill
            drawPath(
                path = eggPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD910241C),
                        Color(0xF008100C)
                    )
                )
            )

            // Inner Plasma Liquid Fluid depending on progress
            val fluidFillY = h * (1f - progress.coerceIn(0.1f, 1f))
            drawContext.canvas.save()
            drawContext.canvas.clipPath(eggPath)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.6f * glowPulse),
                        accentColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    startY = fluidFillY,
                    endY = h
                ),
                topLeft = Offset(0f, fluidFillY),
                size = androidx.compose.ui.geometry.Size(w, h - fluidFillY)
            )
            drawContext.canvas.restore()

            // Razor-sharp luminous shell border
            drawPath(
                path = eggPath,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White,
                        accentColor,
                        accentColor.copy(alpha = 0.5f)
                    )
                ),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Egg Core Emblem
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextLuminous
            )
        }
    }
}

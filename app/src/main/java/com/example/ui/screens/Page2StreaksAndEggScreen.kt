package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

/**
 * PAGE 2: Streak & Egg System
 * Features battery compliance streaks, 3 protective health shields,
 * and 100% incubation egg hatching mechanics in Light Neumorphism / Dark Slate style.
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
    val isDark = themeConfig.isDarkMode
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal

    var newlyHatchedCompanion by remember { mutableStateOf<HatchedCompanion?>(null) }
    var showHatchCelebration by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
            .graphicsLayer { clip = false }
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
                isDarkMode = isDark,
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                    .border(1.2.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LITHIUM HEALTH STREAK",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "20%-80% Charging & Thermal Compliance",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSlate
                                )
                            }
                        }

                        // Day counter
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${streakData.currentStreakDays}",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = accentColor
                            )
                            Text(
                                text = "DAYS ACTIVE",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = textColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Streak Metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StreakStatPill(
                            label = "TODAY COMPLIANCE",
                            value = "${(streakData.todayComplianceRate * 100).toInt()}%",
                            isDarkMode = isDark
                        )
                        StreakStatPill(
                            label = "HEALTHY CHARGE",
                            value = "${streakData.healthyChargeMinutesToday}m",
                            isDarkMode = isDark
                        )
                        StreakStatPill(
                            label = "RECORD BEST",
                            value = "${streakData.bestStreakDays} Days",
                            isDarkMode = isDark
                        )
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
                isDarkMode = isDark,
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
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = textColor
                            )
                        }

                        Text(
                            text = "${streakData.shieldsAvailable} / ${streakData.maxShields} READY",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFFFB800)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Shields automatically absorb sudden thermal spikes or accidental 0% dropouts without breaking your daily streak score.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = TextSlate,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Shield Slots (or Locked State if 0 unlocked)
                    if (streakData.maxShields == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                .border(1.dp, if (isDark) Color(0x33475569) else Color(0x33CBD5E1), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Shield Locked",
                                    tint = TextSlate,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "0 SHIELDS UNLOCKED • EARN AT LEVEL 5",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isDark) TextWhite else TextCharcoal
                                    )
                                    Text(
                                        text = "Shields unlock automatically on the Prize Road as you level up.",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 10.sp,
                                        color = TextSlate
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (i in 1..streakData.maxShields) {
                                val isActive = i <= streakData.shieldsAvailable
                                val cardBg = when {
                                    isActive -> Color(0xFFFFB800).copy(alpha = if (isDark) 0.22f else 0.12f)
                                    isDark -> Color(0xFF1E293B)
                                    else -> Color(0xFFFFFFFF)
                                }
                                val borderCol = if (isActive) Color(0xFFFFB800) else if (isDark) Color(0x33475569) else Color(0x4DE2E8F0)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cardBg)
                                        .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Shield $i",
                                            tint = if (isActive) Color(0xFFFFB800) else TextSlate,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isActive) "SHIELD $i: ACTIVE" else "EXPENDED",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isActive) textColor else TextSlate
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
                                    .background(Color(0xFFFFB800).copy(alpha = if (isDark) 0.25f else 0.15f))
                                    .border(1.dp, Color(0xFFFFB800), RoundedCornerShape(10.dp))
                                    .clickable { onRepairShield() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "RECHARGE 1 SHIELD",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFB800)
                                )
                            }
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
                isDarkMode = isDark,
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
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = textColor
                            )
                            Text(
                                text = eggData.eggName,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = accentColor
                            )
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Living Incubating Egg Graphic
                    CyberEggGraphic(
                        progress = eggData.incubationProgress,
                        accentColor = accentColor,
                        isDarkMode = isDark
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
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSlate
                        )
                        Text(
                            text = "$percentInt% (${eggData.currentXP} / ${eggData.xpToHatch} XP)",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (percentInt >= 100) TactileGreen else accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { eggData.incubationProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (percentInt >= 100) TactileGreen else accentColor,
                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hatch Action Button
                    if (eggData.incubationProgress >= 0.99f || percentInt >= 100) {
                        Button(
                            onClick = {
                                val companion = onHatchEgg()
                                newlyHatchedCompanion = companion
                                showHatchCelebration = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TactileGreen,
                                contentColor = Color.White
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
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Incubation matrix advances during healthy AC charging sessions.",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = TextSlate,
                                modifier = Modifier.weight(1f)
                            )
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
                        accentColor = TactileGreen,
                        isDarkMode = isDark,
                        glowIntensity = 1.0f
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✦ NEW COMPANION HATCHED! ✦",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = TactileGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(companion.color.copy(alpha = if (isDark) 0.25f else 0.15f))
                                    .border(2.dp, companion.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👾", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = companion.name,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Text(
                                text = "RARITY: ${companion.rarity.uppercase()}",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = companion.color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ACTIVE PERK: ${companion.perk}",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = TextSlate
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TactileGreen)
                                    .clickable { showHatchCelebration = false }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "COLLECT TO CREW",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
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
                isDarkMode = isDark,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HATCHED CYBER COMPANIONS (${eggData.hatchedList.size})",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (eggData.hatchedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                .border(1.dp, if (isDark) Color(0x33475569) else Color(0x33CBD5E1), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NO CYBER COMPANIONS YET",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDark) TextWhite else TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Incubate your Cyber Egg to 100% via healthy charging to hatch and collect your first legendary crew companion.",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSlate,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        eggData.hatchedList.forEach { comp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(comp.color.copy(alpha = if (isDark) 0.22f else 0.12f))
                                    .border(1.dp, comp.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👾", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.name,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = textColor
                                )
                                Text(
                                    text = comp.perk,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSlate
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(comp.color.copy(alpha = if (isDark) 0.2f else 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = comp.rarity,
                                    fontFamily = OutfitFontFamily,
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
}

@Composable
private fun StreakStatPill(
    label: String,
    value: String,
    isDarkMode: Boolean
) {
    Column {
        Text(
            text = label,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 9.sp,
            color = TextSlate
        )
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isDarkMode) TextWhite else TextCharcoal
        )
    }
}

/**
 * 90 FPS Floating Cyber Egg with Tactile Soft Glass Shell
 */
@Composable
private fun CyberEggGraphic(
    progress: Float,
    accentColor: Color,
    isDarkMode: Boolean
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

            val eggPath = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                cubicTo(w * 0.95f, h * 0.15f, w * 0.95f, h * 0.85f, w * 0.5f, h * 0.95f)
                cubicTo(w * 0.05f, h * 0.85f, w * 0.05f, h * 0.15f, w * 0.5f, h * 0.05f)
                close()
            }

            // Neumorphic Dual Drop Shadows for Egg
            val shadowColor = if (isDarkMode) Color(0x66000000) else Color(0x2B9AAEC4)
            drawPath(
                path = eggPath,
                color = shadowColor,
                style = Stroke(width = 6.dp.toPx())
            )

            // Shell Fill
            drawPath(
                path = eggPath,
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(Color(0xFF243348), Color(0xFF1A2433))
                    } else {
                        listOf(Color(0xFFFFFFFF), Color(0xFFF7FAFD), Color(0xFFEFF3F8))
                    }
                )
            )

            // Inner Fluid Wave Fill
            val fluidFillY = h * (1f - progress.coerceIn(0.1f, 1f))
            drawContext.canvas.save()
            drawContext.canvas.clipPath(eggPath)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isDarkMode) 0.55f * glowPulse else 0.35f * glowPulse),
                        accentColor.copy(alpha = if (isDarkMode) 0.2f else 0.1f),
                        Color.Transparent
                    ),
                    startY = fluidFillY,
                    endY = h
                ),
                topLeft = Offset(0f, fluidFillY),
                size = androidx.compose.ui.geometry.Size(w, h - fluidFillY)
            )
            drawContext.canvas.restore()

            // Outer Shell Border
            drawPath(
                path = eggPath,
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.White,
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
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isDarkMode) TextWhite else TextCharcoal
            )
        }
    }
}

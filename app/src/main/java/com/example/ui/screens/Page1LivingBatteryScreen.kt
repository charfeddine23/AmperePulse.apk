package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.LivingBatteryState
import com.example.data.PrizeRoadData
import com.example.data.TelemetryPoint
import com.example.ui.components.LevelLinkedBatteryShell
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MorphingEnergyCore
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileViolet
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

/**
 * PAGE 1: "Living Battery Matrix"
 *
 * Implements:
 * 1. Top State Header (Replacing Video Pulse Title) with 4 dynamic telemetry states:
 *    - ⚡ ENERGY STORING (Phosphor Green #00C853)
 *    - 🔥 OVERDRIVE / HEATING (Warning Pulse Red #FF3D00)
 *    - ❄️ CRYOGENIC COOLING (Ice Blue #00B0FF)
 *    - 💤 DEEP SLEEP / HIBERNATION (Night Purple #7C4DFF)
 * 2. Central Flame-to-Ice Morphing Core:
 *    Pulsing fluid energy flame with particle shaders, seamlessly morphing into
 *    a 3D-styled faceted Cryo Ice Cube during cooling state.
 * 3. Biological Breathing Semicircles:
 *    Multi-layered concentric arcs with organic breathing transition locked at 90 FPS,
 *    accelerating rate on high-wattage charging or thermal runaway.
 * 4. Level-Linked Luxury Battery Component:
 *    Tactile extruded neumorphic battery shell bound directly to the 1,000-level Prize Road
 *    with 10 segmented glowing energy cells.
 * 5. Overload Prevention Logic:
 *    Real-time monitoring (>42°C threshold) with immediate thermal alarm and emergency
 *    cryo cooling suppression action.
 */
@Composable
fun Page1LivingBatteryScreen(
    telemetry: BatteryTelemetry,
    voltageHistory: List<TelemetryPoint>,
    currentHistory: List<TelemetryPoint>,
    prizeRoadData: PrizeRoadData,
    themeConfig: GlassThemeConfig,
    onSelectLivingState: (LivingBatteryState) -> Unit,
    onTriggerEmergencyCooling: () -> Unit,
    onAddSimulatedMah: (Long) -> Unit,
    onNavigateToPage: (Int) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isDark = themeConfig.isDarkMode
    val state = telemetry.livingState
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal

    // Overload Detection Threshold (>42°C or explicit Overdrive State)
    val isOverload = telemetry.temperatureC >= 42.0f || (state == LivingBatteryState.OVERDRIVE_HEATING && telemetry.temperatureC >= 40.0f)
    val isWarningZone = telemetry.temperatureC >= 39.0f && !isOverload

    val primaryAccent = when {
        isOverload -> TactileCoralRed
        state == LivingBatteryState.COOLING -> TactileCyan
        state == LivingBatteryState.ENERGY_STORING -> TactileGreen
        state == LivingBatteryState.DEEP_SLEEP -> TactileViolet
        else -> state.neonPrimary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "overloadAlert")
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
            .graphicsLayer {
                // 90 FPS isolated rendering layer
                clip = false
            }
    ) {
        // 1. TOP STATE HEADER (Replacing Video Pulse Title)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = primaryAccent,
                isDarkMode = isDark,
                glowIntensity = if (isOverload) 1.0f else glow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(primaryAccent.copy(alpha = if (isDark) 0.25f else 0.15f))
                                    .border(1.2.dp, primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isOverload -> Icons.Default.Warning
                                        state == LivingBatteryState.COOLING -> Icons.Default.AcUnit
                                        state == LivingBatteryState.OVERDRIVE_HEATING -> Icons.Default.LocalFireDepartment
                                        state == LivingBatteryState.ENERGY_STORING -> Icons.Default.Bolt
                                        else -> Icons.Default.Bedtime
                                    },
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                val headerTitle = when {
                                    isOverload -> "🔥 CRITICAL THERMAL OVERLOAD"
                                    state == LivingBatteryState.ENERGY_STORING -> "⚡ ENERGY STORING"
                                    state == LivingBatteryState.OVERDRIVE_HEATING -> "🔥 OVERDRIVE / HEATING"
                                    state == LivingBatteryState.COOLING -> "❄️ CRYOGENIC COOLING"
                                    state == LivingBatteryState.DEEP_SLEEP -> "💤 DEEP SLEEP / HIBERNATION"
                                    else -> "⚡ ENERGY MATRIX"
                                }

                                Text(
                                    text = headerTitle,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isOverload) TactileCoralRed else textColor
                                )
                                Text(
                                    text = when {
                                        isOverload -> "Core thermistor exceeds 42°C. Thermal bleed active."
                                        state == LivingBatteryState.ENERGY_STORING -> "Active lithium saturation & ultra-fast energy influx"
                                        state == LivingBatteryState.OVERDRIVE_HEATING -> "High thermal load active (-1 XP/min bleed)"
                                        state == LivingBatteryState.COOLING -> "Vapor chamber active. Cell matrix normalizing (+2 XP/min)"
                                        state == LivingBatteryState.DEEP_SLEEP -> "Sub-milliamp quiescent draw. Cell longevity mode (+1 XP/min)"
                                        else -> "Cell matrix in nominal state"
                                    },
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSlate
                                )
                            }
                        }

                        // Live XP Rate Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryAccent.copy(alpha = if (isDark) 0.22f else 0.12f))
                                .border(1.dp, primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isOverload) "-1 XP/MIN BLEED" else state.xpRateLabel.uppercase(),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isOverload || state.isBleeding) TactileCoralRed else primaryAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Telemetry State Chips (Interactive Selector)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(LivingBatteryState.ENERGY_STORING, "STORING", Icons.Default.Bolt),
                            Triple(LivingBatteryState.OVERDRIVE_HEATING, "HEATING", Icons.Default.LocalFireDepartment),
                            Triple(LivingBatteryState.COOLING, "COOLING", Icons.Default.AcUnit),
                            Triple(LivingBatteryState.DEEP_SLEEP, "SLEEP", Icons.Default.Bedtime)
                        ).forEach { (st, label, icon) ->
                            val isSelected = st == state
                            val stColor = st.neonPrimary
                            val chipBg = when {
                                isSelected -> stColor.copy(alpha = if (isDark) 0.25f else 0.15f)
                                isDark -> Color(0xFF0F172A)
                                else -> Color(0xFFF1F5F9)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) stColor else if (isDark) Color(0x33475569) else Color(0x4DE2E8F0),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectLivingState(st) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) stColor else TextSlate,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = label,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isSelected) textColor else TextSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. OVERLOAD PREVENTION ALARM BANNER (When approaching or exceeding limits)
        if (isOverload || isWarningZone) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = TactileCoralRed,
                    isDarkMode = isDark,
                    glowIntensity = 1.0f
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(TactileCoralRed.copy(alpha = 0.2f * alertPulse)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Thermal Alert",
                                        tint = TactileCoralRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isOverload) "OVERLOAD PREVENTION INTERVENTION" else "ELEVATED THERMAL DETECTED",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TactileCoralRed
                                    )
                                    Text(
                                        text = "Thermistor register: ${telemetry.temperatureC}°C (Critical threshold 42.0°C)",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 10.sp,
                                        color = TextSlate
                                    )
                                }
                            }

                            // Hazard Blink Dot
                            PulsingNeonDot(color = TactileCoralRed, sizeDp = 7.dp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isOverload)
                                "Critical thermal runaway detected! Battery cell thermistor has elevated to ${telemetry.temperatureC}°C, triggering a -1 XP/min bleed. Deploy Emergency Cryo Cooling immediately."
                            else
                                "Battery core is approaching thermal limits (${telemetry.temperatureC}°C). High heat degrades lithium cycle life.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = textColor,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // EMERGENCY CRYO COOLING ACTION BUTTON
                        Button(
                            onClick = onTriggerEmergencyCooling,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverload) TactileCoralRed else TactileCyan,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_emergency_cryo_cooling")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "❄️ ACTIVATE EMERGENCY CRYO COOLING",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2 & 3. CENTRAL FLAME-TO-ICE MORPHING CORE & BIOLOGICAL BREATHING SEMICIRCLES
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            MorphingEnergyCore(
                livingState = state,
                percentage = telemetry.percentage,
                wattageW = telemetry.wattageW,
                temperatureC = telemetry.temperatureC,
                isOverload = isOverload,
                isDarkMode = isDark,
                sizeDp = 260.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. LEVEL-LINKED LUXURY BATTERY COMPONENT (Directly Bound to 1,000-Level Prize Road)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LevelLinkedBatteryShell(
                prizeRoadData = prizeRoadData,
                accentColor = primaryAccent,
                isDarkMode = isDark,
                glowIntensity = glow,
                onAddSimulatedMah = onAddSimulatedMah
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Telemetry Hardware Registers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickMetricNeumorph(
                modifier = Modifier.weight(1f),
                label = "TERMINAL VOLTAGE",
                value = "${telemetry.voltageMv} mV",
                color = primaryAccent,
                isDarkMode = isDark
            )
            QuickMetricNeumorph(
                modifier = Modifier.weight(1f),
                label = "CURRENT FLOW",
                value = "${if (telemetry.currentMa >= 0) "+" else ""}${telemetry.currentMa} mA",
                color = if (telemetry.currentMa >= 0) TactileGreen else TactileCoralRed,
                isDarkMode = isDark
            )
            QuickMetricNeumorph(
                modifier = Modifier.weight(1f),
                label = "THERMISTOR",
                value = "${telemetry.temperatureC} °C",
                color = if (isOverload) TactileCoralRed else primaryAccent,
                isDarkMode = isDark
            )
        }
    }
}

@Composable
private fun QuickMetricNeumorph(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    isDarkMode: Boolean
) {
    LiquidGlassCard(
        modifier = modifier,
        accentColor = color,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                color = TextSlate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDarkMode) TextWhite else TextCharcoal
            )
        }
    }
}

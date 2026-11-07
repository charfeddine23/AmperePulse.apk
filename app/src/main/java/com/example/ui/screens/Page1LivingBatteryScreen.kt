package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.LivingBatteryState
import com.example.data.TelemetryPoint
import com.example.ui.components.LiquidBatteryRing
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

/**
 * PAGE 1: Living Dynamic Battery
 * 4 Living States: Overdrive/Heating (Red-Orange, -1 XP/min bleed),
 * Cooling (Icy Blue), Energy Storing (Phosphorus Green), Deep Sleep (Night Purple).
 * Completely styled in Light Neumorphism with dual shadows & Dark Slate mode.
 */
@Composable
fun Page1LivingBatteryScreen(
    telemetry: BatteryTelemetry,
    voltageHistory: List<TelemetryPoint>,
    currentHistory: List<TelemetryPoint>,
    themeConfig: GlassThemeConfig,
    onSelectLivingState: (LivingBatteryState) -> Unit,
    onNavigateToPage: (Int) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isDark = themeConfig.isDarkMode
    val state = telemetry.livingState
    val primaryColor = state.neonPrimary
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
            .graphicsLayer { clip = false }
    ) {
        // Living State Status Aura Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = primaryColor,
                isDarkMode = isDark,
                glowIntensity = glow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingNeonDot(color = primaryColor, sizeDp = 7.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = state.title.uppercase(),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = textColor
                            )
                            Text(
                                text = state.auraDescription,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = TextSlate
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(primaryColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = state.xpRateLabel,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (state.isBleeding) Color(0xFFFF3D00) else primaryColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Prominent Circular Liquid Progress Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidBatteryRing(
                percentage = telemetry.percentage,
                status = telemetry.status,
                plugType = telemetry.plugType,
                wattageW = telemetry.wattageW,
                livingState = state,
                isDarkMode = isDark,
                sizeDp = 230.dp,
                glowIntensity = glow,
                ambientLiquidWaves = themeConfig.ambientLiquidWaves
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive 4-State Mode Selector (Tactile Neumorphic Buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LivingBatteryState.entries.forEach { st ->
                val isSelected = st == state
                val color = st.neonPrimary
                val btnBg = when {
                    isSelected -> color.copy(alpha = if (isDark) 0.24f else 0.14f)
                    isDark -> Color(0xFF1E293B)
                    else -> Color(0xFFFFFFFF)
                }
                val borderCol = when {
                    isSelected -> color
                    isDark -> Color(0x33475569)
                    else -> Color(0x4DE2E8F0)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(btnBg)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderCol,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectLivingState(st) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (st) {
                                LivingBatteryState.OVERDRIVE_HEATING -> Icons.Default.LocalFireDepartment
                                LivingBatteryState.COOLING -> Icons.Default.AcUnit
                                LivingBatteryState.ENERGY_STORING -> Icons.Default.Bolt
                                LivingBatteryState.DEEP_SLEEP -> Icons.Default.Bedtime
                            },
                            contentDescription = st.title,
                            tint = if (isSelected) color else TextSlate,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (st) {
                                LivingBatteryState.OVERDRIVE_HEATING -> "HEATING"
                                LivingBatteryState.COOLING -> "COOLING"
                                LivingBatteryState.ENERGY_STORING -> "STORING"
                                LivingBatteryState.DEEP_SLEEP -> "SLEEP"
                            },
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isSelected) (if (isDark) TextWhite else TextCharcoal) else TextSlate
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Overdrive Bleed Warning Card
        if (state == LivingBatteryState.OVERDRIVE_HEATING) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = Color(0xFFFF3D00),
                    isDarkMode = isDark,
                    glowIntensity = glow
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Thermal Bleed Active",
                                tint = Color(0xFFFF3D00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THERMAL STRESS: -1 XP/MIN BLEED ACTIVE",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFFFF3D00)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Battery cell thermistor has elevated to ${telemetry.temperatureC}°C. Deploy Tech Crew Cryo Firefighters (Page 4) or unplug device to halt XP bleed and restore electrochemical equilibrium.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = TextSlate,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Vital Metrics Preview Row (Tactile Neumorphic Cards)
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
                color = primaryColor,
                isDarkMode = isDark
            )
            QuickMetricNeumorph(
                modifier = Modifier.weight(1f),
                label = "CURRENT FLOW",
                value = "${if (telemetry.currentMa >= 0) "+" else ""}${telemetry.currentMa} mA",
                color = primaryColor,
                isDarkMode = isDark
            )
            QuickMetricNeumorph(
                modifier = Modifier.weight(1f),
                label = "CORE THERMAL",
                value = "${telemetry.temperatureC} °C",
                color = if (telemetry.temperatureC >= 40f) Color(0xFFFF3D00) else primaryColor,
                isDarkMode = isDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Ecosystem Navigation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = primaryColor,
                isDarkMode = isDark,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CYBER CORE ECOSYSTEM",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Swipe right-to-left across the floating nav bar to incubate Cyber Eggs (Page 2), advance through 1,000 Prize Road levels (Page 3), or deploy Tech Crew Cryo cooling (Page 4).",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = TextSlate,
                        lineHeight = 16.sp
                    )
                }
            }
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
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
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

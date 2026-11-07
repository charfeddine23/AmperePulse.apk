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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryStatus
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.LivingBatteryState
import com.example.data.TelemetryPoint
import com.example.ui.components.LiquidBatteryRing
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

/**
 * PAGE 1: Living Dynamic Battery
 * 4 Living States: Overdrive/Heating (Red-Orange, -1 XP/min bleed),
 * Cooling (Icy Blue), Energy Storing (Phosphorus Green), Deep Sleep (Night Purple).
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
    val state = telemetry.livingState
    val primaryColor = state.neonPrimary
    val glow = themeConfig.glowIntensity

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
        // Living State Status Aura Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = primaryColor,
                glowIntensity = glow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingNeonDot(color = primaryColor, sizeDp = 7.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = state.title.uppercase(),
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = state.auraDescription,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextMutedGreen
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.18f))
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.xpRateLabel,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (state.isBleeding) Color(0xFFFF453A) else primaryColor
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
                sizeDp = 230.dp,
                glowIntensity = glow,
                ambientLiquidWaves = themeConfig.ambientLiquidWaves
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive 4-State Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LivingBatteryState.entries.forEach { st ->
                val isSelected = st == state
                val color = st.neonPrimary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) color.copy(alpha = 0.22f) else Color(0x55111B17))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) color else Color(0x33446655),
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
                            tint = if (isSelected) color else TextMutedGreen,
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
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isSelected) TextLuminous else TextMutedGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Overdrive Bleed Warning Card (Shows details if state is Overdrive or explains dynamics)
        if (state == LivingBatteryState.OVERDRIVE_HEATING) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = Color(0xFFFF3B30),
                    glowIntensity = glow
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Thermal Bleed Active",
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THERMAL STRESS: -1 XP/MIN BLEED ACTIVE",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFFF453A)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Battery cell thermistor has elevated to ${telemetry.temperatureC}°C. Deploy Tech Crew Cryo Firefighters (Page 4) or unplug device to halt XP bleed and restore electrochemical equilibrium.",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = TextMutedGreen,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Vital Metrics Preview Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickMetricGlass(
                modifier = Modifier.weight(1f),
                label = "TERMINAL VOLTAGE",
                value = "${telemetry.voltageMv} mV",
                color = primaryColor
            )
            QuickMetricGlass(
                modifier = Modifier.weight(1f),
                label = "CURRENT FLOW",
                value = "${if (telemetry.currentMa >= 0) "+" else ""}${telemetry.currentMa} mA",
                color = primaryColor
            )
            QuickMetricGlass(
                modifier = Modifier.weight(1f),
                label = "CORE THERMAL",
                value = "${telemetry.temperatureC} °C",
                color = if (telemetry.temperatureC >= 40f) Color(0xFFFF3B30) else primaryColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Teaser Card to Streaks & Prize Road
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = primaryColor,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CYBER CORE ECOSYSTEM",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Swipe right-to-left across the floating nav bar to incubate Cyber Eggs (Page 2), advance through 1,000 Prize Road levels (Page 3), or deploy Tech Crew Cryo cooling (Page 4).",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextMutedGreen,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickMetricGlass(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x55111B17))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.sp,
                color = TextMutedGreen
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextLuminous
            )
        }
    }
}

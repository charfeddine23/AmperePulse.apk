package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryStatus
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidTopAppBar
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

@Composable
fun UsageScreen(
    telemetry: BatteryTelemetry,
    themeConfig: GlassThemeConfig
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
    ) {
        LiquidTopAppBar(
            title = "POWER EFFICIENCY",
            subtitle = "CONSUMPTION DYNAMICS & DEGRADATION",
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Time to Full / Time to Empty Hero Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (telemetry.status == BatteryStatus.CHARGING)
                                    Icons.Default.HourglassTop
                                else
                                    Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (telemetry.status == BatteryStatus.CHARGING)
                                    "ESTIMATED TIME TO FULL"
                                else
                                    "PROJECTED BATTERY ENDURANCE",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val hours = telemetry.estimatedMinutesRemaining / 60
                    val mins = telemetry.estimatedMinutesRemaining % 60
                    val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeStr,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            letterSpacing = 1.sp,
                            color = TextLuminous
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (telemetry.status == BatteryStatus.CHARGING) "REMAINING" else "RUN-TIME",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = accentColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (telemetry.status == BatteryStatus.CHARGING)
                            "Charging at ${telemetry.wattageW}W. Tapering curve programmed at 85% to mitigate cell stress."
                        else
                            "Current drain velocity averages ${telemetry.dischargeRatePerHour}% per active hour.",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextMutedGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Health Score & Degradation Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BATTERY CELL HEALTH INDEX",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                        }
                        Text(
                            text = "OPTIMAL",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "State of Health (SOH)",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                            Text(
                                text = "99.2%",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = TextLuminous
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Retained Capacity",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                            Text(
                                text = "4,960 / 5,000 mAh",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { 0.992f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = Color(0x3310241C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Subsystem Consumption Breakdown
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
                        text = "SUBSYSTEM POWER IMPACT",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = TextLuminous
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SubsystemRow(
                        icon = Icons.Default.Smartphone,
                        name = "OLED Display Subsystem",
                        pct = 42,
                        wattage = "2.1 W",
                        accentColor = accentColor
                    )
                    SubsystemRow(
                        icon = Icons.Default.Memory,
                        name = "SoC & GPU Core Engine",
                        pct = 31,
                        wattage = "1.5 W",
                        accentColor = accentColor
                    )
                    SubsystemRow(
                        icon = Icons.Default.NetworkCheck,
                        name = "Cellular & Wi-Fi Modems",
                        pct = 16,
                        wattage = "0.8 W",
                        accentColor = accentColor
                    )
                    SubsystemRow(
                        icon = Icons.Default.Speed,
                        name = "Background Daemons & Audio",
                        pct = 11,
                        wattage = "0.5 W",
                        accentColor = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Thermal Dissipation Zone Monitor
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THERMAL ZONE TELEMETRY",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                        }
                        Text(
                            text = "${telemetry.temperatureC}°C",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Vapor chamber heat dissipation is performing nominally. Safe operating headroom: +13.5°C before thermal throttle point (42°C).",
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
private fun SubsystemRow(
    icon: ImageVector,
    name: String,
    pct: Int,
    wattage: String,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMutedGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    color = TextLuminous
                )
            }
            Text(
                text = "$pct% ($wattage)",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = Color(0x2210241C)
        )
    }
}

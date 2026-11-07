package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryStatus
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.TemperatureUnit
import com.example.data.TelemetryPoint
import com.example.ui.components.LiquidBatteryRing
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidTopAppBar
import com.example.ui.components.MetricSparkline
import com.example.ui.components.NeonBadge
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun DetailsScreen(
    telemetry: BatteryTelemetry,
    voltageHistory: List<TelemetryPoint>,
    currentHistory: List<TelemetryPoint>,
    wattageHistory: List<TelemetryPoint>,
    temperatureHistory: List<TelemetryPoint>,
    themeConfig: GlassThemeConfig,
    onNavigateToHistory: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    var isPowerSaveActive by remember { mutableStateOf(false) }
    var selectedMetricInspect by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
    ) {
        // Top App Header
        LiquidTopAppBar(
            title = "AMPERE PULSE",
            subtitle = "LIQUID GLASS M5 TELEMETRY",
            accentColor = accentColor,
            rightAction = {
                NeonBadge(
                    text = if (telemetry.status == BatteryStatus.CHARGING) "CHARGING" else "BATTERY",
                    accentColor = accentColor
                )
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Center Prominent Circular Liquid Progress Ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidBatteryRing(
                percentage = telemetry.percentage,
                status = telemetry.status,
                plugType = telemetry.plugType,
                wattageW = telemetry.wattageW,
                livingState = telemetry.livingState,
                sizeDp = 225.dp,
                glowIntensity = glow,
                ambientLiquidWaves = themeConfig.ambientLiquidWaves
            )
        }

        // Quick Mode Capsule Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Power saver quick toggle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isPowerSaveActive) accentColor.copy(alpha = 0.2f) else Color(0x66121C18))
                    .border(
                        1.dp,
                        if (isPowerSaveActive) accentColor else Color(0x3300FF87),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { isPowerSaveActive = !isPowerSaveActive }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EnergySavingsLeaf,
                        contentDescription = "Eco Mode",
                        tint = if (isPowerSaveActive) accentColor else TextMutedGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPowerSaveActive) "ECO PULSE: ON" else "ECO PULSE: OFF",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isPowerSaveActive) TextLuminous else TextMutedGreen
                    )
                }
            }

            // Health status chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x66121C18))
                    .border(1.dp, Color(0x3300FF87), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Battery Health",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HEALTH: 100%",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextLuminous
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section Title: Live Telemetry Metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LIVE POWER METRICS",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.2.sp,
                color = TextMutedGreen
            )
            Text(
                text = "TAP CARD TO EXPAND",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = accentColor.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2x2 Grid of Floating Data Cards
        val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Voltage (mV) & Current (mA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // VOLTAGE CARD
                LiquidMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "VOLTAGE",
                    value = "${numberFormat.format(telemetry.voltageMv)}",
                    unit = "mV",
                    subtext = "Nominal 3.7V - 4.4V",
                    points = voltageHistory,
                    accentColor = accentColor,
                    glowIntensity = glow,
                    testTag = "metric_card_voltage",
                    onClick = {
                        selectedMetricInspect = if (selectedMetricInspect == "VOLTAGE") null else "VOLTAGE"
                    }
                )

                // CURRENT CARD
                val currentSign = if (telemetry.currentMa >= 0) "+" else ""
                LiquidMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CURRENT",
                    value = "$currentSign${numberFormat.format(telemetry.currentMa)}",
                    unit = "mA",
                    subtext = if (telemetry.status == BatteryStatus.CHARGING) "Inflow charge rate" else "Active discharge",
                    points = currentHistory,
                    accentColor = NeonCyan,
                    glowIntensity = glow,
                    testTag = "metric_card_current",
                    onClick = {
                        selectedMetricInspect = if (selectedMetricInspect == "CURRENT") null else "CURRENT"
                    }
                )
            }

            // Row 2: Wattage (W) & Temperature (°C / °F)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WATTAGE CARD
                LiquidMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "WATTAGE",
                    value = String.format(Locale.US, "%.2f", telemetry.wattageW),
                    unit = "W",
                    subtext = "P = V × I Instantaneous",
                    points = wattageHistory,
                    accentColor = accentColor,
                    glowIntensity = glow,
                    testTag = "metric_card_wattage",
                    onClick = {
                        selectedMetricInspect = if (selectedMetricInspect == "WATTAGE") null else "WATTAGE"
                    }
                )

                // TEMPERATURE CARD
                val displayTemp = if (themeConfig.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                    (telemetry.temperatureC * 9f / 5f) + 32f
                } else {
                    telemetry.temperatureC
                }
                val tempUnitStr = themeConfig.temperatureUnit.symbol

                LiquidMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "TEMPERATURE",
                    value = String.format(Locale.US, "%.1f", displayTemp),
                    unit = tempUnitStr,
                    subtext = if (telemetry.temperatureC < 38f) "Optimal Thermals" else "Warm (Monitored)",
                    points = temperatureHistory,
                    accentColor = if (telemetry.temperatureC > 39f) Color(0xFFFFB800) else accentColor,
                    glowIntensity = glow,
                    testTag = "metric_card_temperature",
                    onClick = {
                        selectedMetricInspect = if (selectedMetricInspect == "TEMPERATURE") null else "TEMPERATURE"
                    }
                )
            }
        }

        // Expanded Metric Detail Banner if a card is tapped
        AnimatedVisibility(visible = selectedMetricInspect != null) {
            selectedMetricInspect?.let { metricName ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = accentColor,
                        glowIntensity = glow
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$metricName ANALYSIS",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextLuminous
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .clickable { onNavigateToHistory() }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "VIEW FULL GRAPH →",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (metricName) {
                                    "VOLTAGE" -> "Real-time battery terminal potential is ${telemetry.voltageMv} mV. Operating across normal electrochemical threshold bounds (3600mV cutoff - 4400mV saturation)."
                                    "CURRENT" -> "Live power transfer: ${telemetry.currentMa} mA. Positive values signify rapid lithium intercalation; negative denotes load consumption."
                                    "WATTAGE" -> "Dynamic instantaneous power dissipation calculated at ${telemetry.wattageW} Watts directly using live voltage and current vectors."
                                    else -> "Thermistor reporting internal cell temperature at ${telemetry.temperatureC}°C. Well within safe operational thresholds."
                                },
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextMutedGreen,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Deep Hardware Telemetry Specs Card
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
                        Text(
                            text = "HARDWARE & BATTERY SPECS",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            color = TextLuminous
                        )
                        PulsingNeonDot(color = accentColor, sizeDp = 6.dp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SpecRow(label = "Cell Chemistry", value = telemetry.technology)
                    SpecRow(label = "Rated Capacity", value = "${telemetry.capacityMah} mAh (Solid-State Matrix)")
                    SpecRow(label = "Health Condition", value = telemetry.health.label)
                    SpecRow(label = "Plug Interface", value = telemetry.plugType.label)
                    SpecRow(label = "Charge Cycles", value = "${telemetry.cycleCount} Recorded Cycles")
                    SpecRow(
                        label = "Estimated Remaining",
                        value = if (telemetry.status == BatteryStatus.CHARGING)
                            "${telemetry.estimatedMinutesRemaining} mins until Full (100%)"
                        else
                            "${telemetry.estimatedMinutesRemaining} mins until Depletion"
                    )
                }
            }
        }
    }
}

/**
 * Floating frosted glass data card displaying live metric value,
 * unit, description, and smooth glowing waveform sparkline.
 */
@Composable
fun LiquidMetricCard(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonPhosphorusGreen,
    glowIntensity: Float = 0.85f,
    testTag: String? = null,
    onClick: (() -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier,
        accentColor = accentColor,
        glowIntensity = glowIntensity,
        testTag = testTag,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = TextMutedGreen
                )
                PulsingNeonDot(color = accentColor, sizeDp = 5.dp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main value readout
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TextLuminous,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtext
            Text(
                text = subtext,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                color = TextMutedGreen.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Real-time Glowing Waveform Sparkline
            MetricSparkline(
                points = points,
                lineColor = accentColor,
                glowIntensity = glowIntensity,
                heightDp = 38.dp
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = TextMutedGreen
        )
        Text(
            text = value,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextLuminous
        )
    }
}

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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.MetricType
import com.example.data.SessionLog
import com.example.data.TemperatureUnit
import com.example.data.TelemetryPoint
import com.example.ui.components.DetailedWaveformGraph
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MetricSparkline
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen
import java.text.NumberFormat
import java.util.Locale

/**
 * PAGE 5: My Space
 * Live metrics (Voltage, Current, Wattage, Temp, Health) and Vital Pulse Log.
 */
@Composable
fun Page5MySpaceScreen(
    telemetry: BatteryTelemetry,
    voltageHistory: List<TelemetryPoint>,
    currentHistory: List<TelemetryPoint>,
    wattageHistory: List<TelemetryPoint>,
    temperatureHistory: List<TelemetryPoint>,
    sessionLogs: List<SessionLog>,
    themeConfig: GlassThemeConfig,
    onAddVitalLog: (String, String, String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    var selectedMetric by remember { mutableStateOf(MetricType.VOLTAGE) }

    val activePoints = when (selectedMetric) {
        MetricType.VOLTAGE -> voltageHistory
        MetricType.CURRENT -> currentHistory
        MetricType.WATTAGE -> wattageHistory
        MetricType.TEMPERATURE -> temperatureHistory
    }

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
        // Space Header Banner
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
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "MY SPACE TELEMETRY CONSOLE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp,
                                    color = TextLuminous
                                )
                                Text(
                                    text = "Live Hardware Registers & Vital Pulse Log",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMutedGreen
                                )
                            }
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2x2 Floating Data Cards Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Voltage & Current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MySpaceMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "VOLTAGE",
                    value = "${numberFormat.format(telemetry.voltageMv)}",
                    unit = "mV",
                    subtext = "Nominal 3.7V - 4.4V",
                    points = voltageHistory,
                    accentColor = accentColor,
                    glowIntensity = glow,
                    onClick = { selectedMetric = MetricType.VOLTAGE }
                )

                val currentSign = if (telemetry.currentMa >= 0) "+" else ""
                MySpaceMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CURRENT",
                    value = "$currentSign${numberFormat.format(telemetry.currentMa)}",
                    unit = "mA",
                    subtext = if (telemetry.currentMa >= 0) "Inflow charge" else "Discharge draw",
                    points = currentHistory,
                    accentColor = NeonCyan,
                    glowIntensity = glow,
                    onClick = { selectedMetric = MetricType.CURRENT }
                )
            }

            // Row 2: Wattage & Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MySpaceMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "WATTAGE",
                    value = String.format(Locale.US, "%.2f", telemetry.wattageW),
                    unit = "W",
                    subtext = "P = V × I Instantaneous",
                    points = wattageHistory,
                    accentColor = accentColor,
                    glowIntensity = glow,
                    onClick = { selectedMetric = MetricType.WATTAGE }
                )

                val displayTemp = if (themeConfig.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                    (telemetry.temperatureC * 9f / 5f) + 32f
                } else {
                    telemetry.temperatureC
                }
                MySpaceMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "TEMPERATURE",
                    value = String.format(Locale.US, "%.1f", displayTemp),
                    unit = themeConfig.temperatureUnit.symbol,
                    subtext = if (telemetry.temperatureC >= 40f) "Thermal Warning" else "Nominal Heat Zone",
                    points = temperatureHistory,
                    accentColor = if (telemetry.temperatureC >= 40f) Color(0xFFFF3B30) else accentColor,
                    glowIntensity = glow,
                    onClick = { selectedMetric = MetricType.TEMPERATURE }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Oscilloscope Waveform Card for Selected Channel
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
                            text = "${selectedMetric.title.uppercase()} REAL-TIME OSCILLOSCOPE",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = TextLuminous
                        )
                        PulsingNeonDot(color = accentColor, sizeDp = 6.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DetailedWaveformGraph(
                        points = activePoints,
                        lineColor = accentColor,
                        glowIntensity = glow,
                        heightDp = 150.dp,
                        showGrid = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vital Pulse Log Header & Benchmark Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VITAL PULSE HARDWARE LOG",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = TextLuminous
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                    .clickable {
                        onAddVitalLog(
                            "Vital Pulse Diagnostics Sweep",
                            "All 12 battery registers verified OK: ${telemetry.voltageMv}mV, ${telemetry.currentMa}mA, ${telemetry.temperatureC}°C, 0 degradation.",
                            "PASS OK",
                            "check"
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BENCHMARK NOW",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Log Items List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sessionLogs.forEach { log ->
                VitalLogItem(log = log, accentColor = accentColor, glow = glow)
            }
        }
    }
}

@Composable
fun MySpaceMetricCard(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier,
    accentColor: Color,
    glowIntensity: Float,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        accentColor = accentColor,
        glowIntensity = glowIntensity,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = TextMutedGreen
                )
                PulsingNeonDot(color = accentColor, sizeDp = 5.dp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextLuminous
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Text(
                text = subtext,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.sp,
                color = TextMutedGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            MetricSparkline(
                points = points,
                lineColor = accentColor,
                glowIntensity = glowIntensity,
                heightDp = 30.dp
            )
        }
    }
}

@Composable
fun VitalLogItem(
    log: SessionLog,
    accentColor: Color,
    glow: Float
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        glowIntensity = glow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.type) {
                        "charge" -> Icons.Default.Bolt
                        "temp" -> Icons.Default.Thermostat
                        "discharge" -> Icons.Default.Timeline
                        else -> Icons.Default.History
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.title,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextLuminous
                    )
                    Text(
                        text = log.timestampFormatted,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 9.sp,
                        color = TextMutedGreen
                    )
                }
                Text(
                    text = log.description,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    color = TextMutedGreen,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x3310241C))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = log.statTag,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = accentColor
                )
            }
        }
    }
}

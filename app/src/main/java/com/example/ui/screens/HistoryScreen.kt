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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.MetricType
import com.example.data.SessionLog
import com.example.data.TelemetryPoint
import com.example.ui.components.DetailedWaveformGraph
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidTopAppBar
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    telemetry: BatteryTelemetry,
    voltageHistory: List<TelemetryPoint>,
    currentHistory: List<TelemetryPoint>,
    wattageHistory: List<TelemetryPoint>,
    temperatureHistory: List<TelemetryPoint>,
    sessionLogs: List<SessionLog>,
    themeConfig: GlassThemeConfig,
    onAddLog: (String, String, String, String) -> Unit = { _, _, _, _ -> }
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

    val minVal = if (activePoints.isNotEmpty()) activePoints.minOf { it.value } else 0f
    val maxVal = if (activePoints.isNotEmpty()) activePoints.maxOf { it.value } else 0f
    val avgVal = if (activePoints.isNotEmpty()) activePoints.map { it.value }.average().toFloat() else 0f
    val currentVal = if (activePoints.isNotEmpty()) activePoints.last().value else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 110.dp)
    ) {
        LiquidTopAppBar(
            title = "TELEMETRY ARCHIVE",
            subtitle = "REAL-TIME OSCILLOSCOPE & HISTORY",
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Metric Selector Channel Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricType.entries.forEach { metric ->
                val isSelected = metric == selectedMetric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0x66121C18))
                        .border(
                            1.dp,
                            if (isSelected) accentColor else Color(0x3300FF87),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedMetric = metric }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = metric.title.uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) TextLuminous else TextMutedGreen
                        )
                        Text(
                            text = metric.unit,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 9.sp,
                            color = if (isSelected) accentColor else TextMutedGreen.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Waveform Card with Oscilloscope Graph
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
                        Column {
                            Text(
                                text = "${selectedMetric.title.uppercase()} OSCILLOGRAM",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = "High-precision live hardware sampling",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                        }
                        PulsingNeonDot(color = accentColor, sizeDp = 7.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Waveform Canvas
                    DetailedWaveformGraph(
                        points = activePoints,
                        lineColor = accentColor,
                        glowIntensity = glow,
                        heightDp = 160.dp,
                        showGrid = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats summary grid: Min, Avg, Max, Current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(
                            label = "MINIMUM",
                            value = formatMetric(minVal, selectedMetric, numberFormat)
                        )
                        StatPill(
                            label = "AVERAGE",
                            value = formatMetric(avgVal, selectedMetric, numberFormat)
                        )
                        StatPill(
                            label = "MAXIMUM",
                            value = formatMetric(maxVal, selectedMetric, numberFormat)
                        )
                        StatPill(
                            label = "REAL-TIME",
                            value = formatMetric(currentVal, selectedMetric, numberFormat),
                            highlightColor = accentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Logs Header with Manual Checkpoint Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "POWER SESSION LOGS",
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = TextLuminous
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        onAddLog(
                            "Instant Telemetry Snapshot",
                            "Manual benchmark recorded: ${telemetry.percentage}% SOC, ${telemetry.voltageMv}mV, ${telemetry.wattageW}W.",
                            "${telemetry.wattageW}W",
                            "check"
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Log Snapshot",
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LOG SNAPSHOT",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // List of Session Logs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            sessionLogs.forEach { log ->
                SessionLogItem(
                    log = log,
                    accentColor = accentColor,
                    glow = glow
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    highlightColor: Color? = null
) {
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
            color = highlightColor ?: TextLuminous
        )
    }
}

@Composable
private fun SessionLogItem(
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
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
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.title,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextLuminous
                    )
                    Text(
                        text = log.timestampFormatted,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        color = TextMutedGreen
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.description,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = TextMutedGreen,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x3310241C))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.statTag,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = accentColor
                )
            }
        }
    }
}

private fun formatMetric(value: Float, metric: MetricType, numberFormat: NumberFormat): String {
    return when (metric) {
        MetricType.VOLTAGE -> "${numberFormat.format(value.toInt())} mV"
        MetricType.CURRENT -> "${if (value >= 0) "+" else ""}${numberFormat.format(value.toInt())} mA"
        MetricType.WATTAGE -> "${String.format(Locale.US, "%.2f", value)} W"
        MetricType.TEMPERATURE -> "${String.format(Locale.US, "%.1f", value)} °C"
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.local.HourlyBatteryStatEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileAmber
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TactileViolet
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale

enum class HourlyStatsFilter(val label: String) {
    ALL("All 24 Hours"),
    CHARGING("Charging Hours"),
    DISCHARGE("Drain & Idle"),
    HIGH_TEMP("Thermals >35°C")
}

/**
 * PAGE 2: Hourly Battery Statistics Dashboard.
 * Backed by Room Database with Write-Ahead Logging (WAL) mode for crash-resilient 24/7 logging.
 * Displays hourly voltage, current flow, net mAh throughput, and thermal distribution in a clean scrollable log.
 */
@Composable
fun Page2StatisticsScreen(
    telemetry: BatteryTelemetry,
    hourlyStats: List<HourlyBatteryStatEntity>,
    themeConfig: GlassThemeConfig,
    onResetOrSeedStats: () -> Unit = {}
) {
    val isDark = themeConfig.isDarkMode
    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedFilter by remember { mutableStateOf(HourlyStatsFilter.ALL) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Filter items according to user selection
    val filteredStats = remember(hourlyStats, selectedFilter) {
        when (selectedFilter) {
            HourlyStatsFilter.ALL -> hourlyStats
            HourlyStatsFilter.CHARGING -> hourlyStats.filter { it.isChargingHour || it.netMahFlow > 0 }
            HourlyStatsFilter.DISCHARGE -> hourlyStats.filter { !it.isChargingHour && it.netMahFlow <= 0 }
            HourlyStatsFilter.HIGH_TEMP -> hourlyStats.filter { it.maxTemperatureC >= 35.0f }
        }
    }

    // Calculate aggregated summary metrics from Room WAL records
    val totalHours = hourlyStats.size
    val totalChargeMah = hourlyStats.filter { it.netMahFlow > 0 }.sumOf { it.netMahFlow }
    val totalDrainMah = hourlyStats.filter { it.netMahFlow < 0 }.sumOf { -it.netMahFlow }
    val avgTemp = if (hourlyStats.isNotEmpty()) hourlyStats.map { it.avgTemperatureC }.average().toFloat() else telemetry.temperatureC
    val peakCurrent = if (hourlyStats.isNotEmpty()) hourlyStats.maxOf { it.maxCurrentMa } else telemetry.currentMa

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("page2_statistics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Room WAL Telemetry Engine Status Header Card
        item(key = "wal_engine_header") {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = accentColor,
                isDarkMode = isDark,
                shape = RoundedCornerShape(22.dp),
                glowIntensity = glow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title and Live DB Engine Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                    .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "HOURLY BATTERY STATS",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        letterSpacing = (-0.2).sp,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PulsingNeonDot(color = TactileGreen, sizeDp = 6.dp)
                                }
                                Text(
                                    text = "Room SQLite Database • WAL Mode Persistent Engine",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 10.sp,
                                    color = subTextColor
                                )
                            }
                        }

                        // DB Records Counter Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(TactileGreen.copy(alpha = if (isDark) 0.20f else 0.12f))
                                .border(1.dp, TactileGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$totalHours HRS LOGGED",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TactileGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Quadrant Metric Summary Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricMiniCell(
                            modifier = Modifier.weight(1f),
                            label = "CHARGE INFLUX",
                            value = "+${numberFormat.format(totalChargeMah)} mAh",
                            color = TactileGreen,
                            isDarkMode = isDark
                        )
                        StatMetricMiniCell(
                            modifier = Modifier.weight(1f),
                            label = "TOTAL DRAIN",
                            value = "-${numberFormat.format(totalDrainMah)} mAh",
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            isDarkMode = isDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricMiniCell(
                            modifier = Modifier.weight(1f),
                            label = "AVG CORE TEMP",
                            value = "${(avgTemp * 10).toInt() / 10f} °C",
                            color = if (avgTemp >= 35f) TactileCoralRed else TactileCyan,
                            isDarkMode = isDark
                        )
                        StatMetricMiniCell(
                            modifier = Modifier.weight(1f),
                            label = "PEAK INFLOW",
                            value = "+${numberFormat.format(peakCurrent)} mA",
                            color = accentColor,
                            isDarkMode = isDark
                        )
                    }
                }
            }
        }

        // 2. Interactive Filter Bar
        item(key = "filter_bar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HourlyStatsFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val chipBg = if (isSelected) {
                        accentColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                    } else {
                        if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                    }
                    val chipBorder = if (isSelected) accentColor else if (isDark) Color(0x33475569) else Color(0x4DE2E8F0)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.label,
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            color = if (isSelected) accentColor else subTextColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Section Header with Count
        item(key = "section_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HOURLY TELEMETRY LOGS (${filteredStats.size})",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                }

                // Refresh / Re-seed Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onResetOrSeedStats() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sync DB",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor
                    )
                }
            }
        }

        // 4. Scrollable Hourly Statistics Log Items
        if (filteredStats.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = subTextColor,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hourly records match this filter",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            color = subTextColor
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredStats,
                key = { it.id }
            ) { stat ->
                HourlyStatCard(
                    stat = stat,
                    isDarkMode = isDark,
                    numberFormat = numberFormat
                )
            }
        }
    }
}

/**
 * Individual Hourly Battery Statistics Card in Room WAL Log.
 */
@Composable
private fun HourlyStatCard(
    stat: HourlyBatteryStatEntity,
    isDarkMode: Boolean,
    numberFormat: NumberFormat
) {
    val stateColor = when (stat.dominantState) {
        "ENERGY_STORING" -> TactileGreen
        "COOLING" -> TactileCyan
        "OVERDRIVE_HEATING" -> TactileCoralRed
        "DEEP_SLEEP" -> TactileViolet
        else -> TactileRoyalBlue
    }

    val stateIcon: ImageVector = when (stat.dominantState) {
        "ENERGY_STORING" -> Icons.Default.Bolt
        "COOLING" -> Icons.Default.AcUnit
        "OVERDRIVE_HEATING" -> Icons.Default.LocalFireDepartment
        "DEEP_SLEEP" -> Icons.Default.Bedtime
        else -> Icons.Default.QueryStats
    }

    val textColor = if (isDarkMode) TextWhite else TextCharcoal
    val subTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = stateColor,
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(18.dp),
        glowIntensity = 0.65f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Time Window, Dominant State Badge & Net Flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time & Date Window
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(stateColor.copy(alpha = if (isDarkMode) 0.22f else 0.14f))
                            .border(1.dp, stateColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = stateIcon,
                            contentDescription = null,
                            tint = stateColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stat.formattedTime,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = textColor
                        )
                        Text(
                            text = "${stat.formattedDate} • ${stat.sampleCount} samples",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            color = subTextColor
                        )
                    }
                }

                // Net Throughput Badge
                val isPositive = stat.netMahFlow > 0
                val flowColor = if (isPositive) TactileGreen else Color(0xFFFF5252)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(flowColor.copy(alpha = if (isDarkMode) 0.20f else 0.12f))
                        .border(1.dp, flowColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${numberFormat.format(stat.netMahFlow)} mAh",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = flowColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Telemetry Metric Micro Cells
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Voltage
                TelemetryMiniPill(
                    modifier = Modifier.weight(1f),
                    label = "VOLTAGE",
                    value = "${stat.avgVoltageMv} mV",
                    sub = "Range: ${stat.minVoltageMv}-${stat.maxVoltageMv}",
                    accent = stateColor,
                    isDarkMode = isDarkMode
                )

                // Current Flow
                TelemetryMiniPill(
                    modifier = Modifier.weight(1f),
                    label = "FLOW",
                    value = "${if (stat.avgCurrentMa >= 0) "+" else ""}${stat.avgCurrentMa} mA",
                    sub = "Max: ${if (stat.maxCurrentMa >= 0) "+" else ""}${stat.maxCurrentMa} mA",
                    accent = stateColor,
                    isDarkMode = isDarkMode
                )

                // Temperature
                val isHot = stat.maxTemperatureC >= 35f
                TelemetryMiniPill(
                    modifier = Modifier.weight(1f),
                    label = "TEMP",
                    value = "${stat.avgTemperatureC}°C",
                    sub = "Peak: ${stat.maxTemperatureC}°C",
                    accent = if (isHot) TactileCoralRed else TactileCyan,
                    isDarkMode = isDarkMode
                )

                // Battery Delta
                val pctDelta = stat.endBatteryPct - stat.startBatteryPct
                val deltaSign = if (pctDelta > 0) "+" else ""
                TelemetryMiniPill(
                    modifier = Modifier.weight(1f),
                    label = "DELTA",
                    value = "${stat.endBatteryPct}%",
                    sub = "${stat.startBatteryPct}% ($deltaSign$pctDelta%)",
                    accent = if (pctDelta >= 0) TactileGreen else subTextColor,
                    isDarkMode = isDarkMode
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Battery Progression Fluid Progress Bar
            val pctFloat = (stat.endBatteryPct / 100f).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pctFloat)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(stateColor.copy(alpha = 0.5f), stateColor)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer State & Screen Time Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode: ${stat.dominantStateLabel}",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = stateColor
                )
                Text(
                    text = "Screen On: ~${stat.screenOnDurationMinutes}m",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 9.5.sp,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
private fun StatMetricMiniCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    isDarkMode: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .border(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
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
                fontSize = 13.sp,
                color = color
            )
        }
    }
}

@Composable
private fun TelemetryMiniPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    accent: Color,
    isDarkMode: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
            .border(1.dp, if (isDarkMode) Color(0x33475569) else Color(0x4DCBD5E1), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 7.5.sp,
                color = TextSlate,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = accent,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = sub,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 6.8.sp,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

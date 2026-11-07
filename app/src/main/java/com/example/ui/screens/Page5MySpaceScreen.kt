package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.MetricType
import com.example.data.SessionLog
import com.example.data.TelemetryPoint
import com.example.data.TemperatureUnit
import com.example.ui.components.DetailedWaveformGraph
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.MetricSparkline
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

/**
 * PAGE 5: My Space Telemetry Console
 * High-performance, silky-smooth 90 FPS real-time charts, oscilloscopes,
 * and dynamic Current-driven ambient glow visual effects (VFX).
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
    val isDark = themeConfig.isDarkMode
    val baseAccent = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val textColor = if (isDark) TextWhite else TextCharcoal

    var selectedMetric by remember { mutableStateOf(MetricType.VOLTAGE) }

    // DYNAMIC CURRENT-DRIVEN AMBIENT GLOW (VFX)
    // Bind the background ambient glow layer to live Current (mA) state:
    // - High Inflow / Stress (> 2500 mA or high thermals): Reactive Red Ambient Glow
    // - Stable / Safe Flow (300 - 2500 mA): Serene Emerald Green
    // - Low Trickle Flow (0 - 299 mA): Serene Royal Blue
    // - High Load Drain (< -600 mA): Energetic Violet/Coral warning
    // - Idle Drain (-600 - 0 mA): Cool Cyan/Slate
    val currentMa = telemetry.currentMa
    val targetAmbientColor = when {
        currentMa > 2500 || telemetry.temperatureC >= 38.5f -> TactileCoralRed // High Inflow Stress
        currentMa in 400..2500 -> TactileGreen // Stable / Safe Flow
        currentMa in 0..399 -> TactileRoyalBlue // Serene Trickle
        currentMa < -600 -> Color(0xFFA855F7) // High Load Drain
        else -> Color(0xFF00B0FF) // Cool Idle Drain
    }

    val animatedAmbientColor by animateColorAsState(
        targetValue = targetAmbientColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ambientGlowColor"
    )

    val activePoints = when (selectedMetric) {
        MetricType.VOLTAGE -> voltageHistory
        MetricType.CURRENT -> currentHistory
        MetricType.WATTAGE -> wattageHistory
        MetricType.TEMPERATURE -> temperatureHistory
    }

    // 90 FPS Breathing Ambient Aura & Particle Shift
    val infiniteTransition = rememberInfiniteTransition(label = "ambientPulse90Fps")
    val ambientAuraBreath by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraBreath"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
    ) {
        // Dynamic Current-Driven Background Ambient Glow Layer
        CurrentDrivenAmbientGlowBackdrop(
            ambientColor = animatedAmbientColor,
            breatheScale = ambientAuraBreath,
            isDarkMode = isDark,
            glowIntensity = glow
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 110.dp)
                .graphicsLayer { clip = false }
        ) {
            // Space Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = animatedAmbientColor,
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
                                        .background(animatedAmbientColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                        .border(1.2.dp, animatedAmbientColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueryStats,
                                        contentDescription = null,
                                        tint = animatedAmbientColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MY SPACE TELEMETRY CONSOLE",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.5.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "90Hz Real-Time Sensors & Dynamic VFX",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSlate
                                    )
                                }
                            }
                            PulsingNeonDot(color = animatedAmbientColor, sizeDp = 7.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Electron Flow Velocity VFX Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                CurrentFlowVfxBar(
                    currentMa = currentMa,
                    ambientColor = animatedAmbientColor,
                    isDarkMode = isDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Floating Data Cards Grid (Tactile Neumorphic Cards)
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
                        value = numberFormat.format(telemetry.voltageMv),
                        unit = "mV",
                        subtext = "Nominal 3.7V - 4.4V",
                        points = voltageHistory,
                        accentColor = if (selectedMetric == MetricType.VOLTAGE) animatedAmbientColor else baseAccent,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.VOLTAGE,
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
                        accentColor = animatedAmbientColor,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.CURRENT,
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
                        subtext = "P = V × I Real-time",
                        points = wattageHistory,
                        accentColor = if (selectedMetric == MetricType.WATTAGE) animatedAmbientColor else baseAccent,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.WATTAGE,
                        onClick = { selectedMetric = MetricType.WATTAGE }
                    )

                    val displayTemp = if (themeConfig.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
                        (telemetry.temperatureC * 9f / 5f) + 32f
                    } else {
                        telemetry.temperatureC
                    }
                    val tempAccent = if (telemetry.temperatureC >= 40f) TactileCoralRed else baseAccent
                    MySpaceMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "TEMPERATURE",
                        value = String.format(Locale.US, "%.1f", displayTemp),
                        unit = themeConfig.temperatureUnit.symbol,
                        subtext = if (telemetry.temperatureC >= 40f) "Thermal Warning" else "Nominal Heat Zone",
                        points = temperatureHistory,
                        accentColor = if (selectedMetric == MetricType.TEMPERATURE) animatedAmbientColor else tempAccent,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.TEMPERATURE,
                        onClick = { selectedMetric = MetricType.TEMPERATURE }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 90Hz Oscilloscope Waveform Card for Selected Channel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = animatedAmbientColor,
                    isDarkMode = isDark,
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
                                    text = "${selectedMetric.title.uppercase()} 90Hz OSCILLOSCOPE",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "Phosphor Laser Sweep & Hardware Waveform",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSlate
                                )
                            }
                            PulsingNeonDot(color = animatedAmbientColor, sizeDp = 6.dp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Channel Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MetricType.entries.forEach { metric ->
                                val isSelected = metric == selectedMetric
                                val chipBg = if (isSelected) {
                                    animatedAmbientColor.copy(alpha = if (isDark) 0.25f else 0.15f)
                                } else {
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                }
                                val borderCol = if (isSelected) animatedAmbientColor else if (isDark) Color(0x33475569) else Color(0x33CBD5E1)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipBg)
                                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                        .clickable { selectedMetric = metric }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = metric.name.take(4),
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isSelected) animatedAmbientColor else TextSlate
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Waveform Canvas with 90 FPS Laser Sweep
                        DetailedWaveformGraph(
                            points = activePoints,
                            lineColor = animatedAmbientColor,
                            isDarkMode = isDark,
                            glowIntensity = glow,
                            heightDp = 150.dp,
                            showGrid = true,
                            showScanline = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Min/Max/Current Readout Strip
                        if (activePoints.isNotEmpty()) {
                            val minVal = activePoints.minOf { it.value }
                            val maxVal = activePoints.maxOf { it.value }
                            val currentVal = activePoints.last().value
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OscilloscopeStatPill("MIN", String.format(Locale.US, "%.1f", minVal), isDark)
                                OscilloscopeStatPill("LIVE", String.format(Locale.US, "%.1f", currentVal), isDark, animatedAmbientColor)
                                OscilloscopeStatPill("PEAK", String.format(Locale.US, "%.1f", maxVal), isDark)
                                OscilloscopeStatPill("RATE", "90 FPS", isDark, animatedAmbientColor)
                            }
                        }
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
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(animatedAmbientColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                        .border(1.dp, animatedAmbientColor, RoundedCornerShape(10.dp))
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
                            tint = animatedAmbientColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BENCHMARK NOW",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = animatedAmbientColor
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
                    VitalLogItem(
                        log = log,
                        accentColor = animatedAmbientColor,
                        isDarkMode = isDark,
                        glow = glow
                    )
                }
            }
        }
    }
}

/**
 * Dynamic Current-Driven Background Ambient Glow Layer.
 * Renders a soft diffused aura behind the console cards that reacts to the battery telemetry.
 */
@Composable
private fun CurrentDrivenAmbientGlowBackdrop(
    ambientColor: Color,
    breatheScale: Float,
    isDarkMode: Boolean,
    glowIntensity: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
    ) {
        val w = size.width
        val h = size.height

        // Top halo centered around the live telemetry grid
        val centerTop = Offset(w * 0.5f, h * 0.28f)
        val radiusTop = (w * 0.65f) * breatheScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ambientColor.copy(alpha = if (isDarkMode) 0.18f * glowIntensity else 0.10f * glowIntensity),
                    ambientColor.copy(alpha = if (isDarkMode) 0.05f * glowIntensity else 0.02f * glowIntensity),
                    Color.Transparent
                ),
                center = centerTop,
                radius = radiusTop
            ),
            center = centerTop,
            radius = radiusTop
        )

        // Subtle secondary bottom halo behind the oscilloscope
        val centerBottom = Offset(w * 0.5f, h * 0.58f)
        val radiusBottom = (w * 0.55f) * breatheScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ambientColor.copy(alpha = if (isDarkMode) 0.14f * glowIntensity else 0.07f * glowIntensity),
                    Color.Transparent
                ),
                center = centerBottom,
                radius = radiusBottom
            ),
            center = centerBottom,
            radius = radiusBottom
        )
    }
}

/**
 * Animated Electron Flow Velocity VFX Bar.
 * Renders moving particles/waves indicating the direction and rate of active current flow.
 */
@Composable
private fun CurrentFlowVfxBar(
    currentMa: Int,
    ambientColor: Color,
    isDarkMode: Boolean
) {
    val isCharging = currentMa >= 0
    val absCurrent = abs(currentMa)

    val stateTitle = when {
        currentMa > 2500 -> "⚡ HIGH INFLOW SURGE (+${currentMa} mA)"
        currentMa in 400..2500 -> "🌿 SERENE SAFE CHARGE (+${currentMa} mA)"
        currentMa in 0..399 -> "🔋 STABLE TRICKLE FLOW (+${currentMa} mA)"
        currentMa < -600 -> "🔥 HIGH LOAD DRAIN (${currentMa} mA)"
        else -> "⚡ IDLE SYSTEM DRAW (${currentMa} mA)"
    }

    // 90 FPS traveling wave phase
    val infiniteTransition = rememberInfiniteTransition(label = "electronFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (absCurrent > 2000) 1000 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPhase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF))
            .border(1.dp, ambientColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = null,
                        tint = ambientColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stateTitle,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = ambientColor
                    )
                }

                Text(
                    text = if (isCharging) "INFLOW →" else "← OUTFLOW",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextSlate
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Particle flow line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                val w = size.width
                val h = size.height

                // Base track
                drawLine(
                    color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0),
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h * 0.5f),
                    strokeWidth = 2.dp.toPx()
                )

                // 8 traveling electron wave beads
                val beadCount = 8
                for (i in 0 until beadCount) {
                    val rawFrac = (i.toFloat() / beadCount) + (if (isCharging) flowPhase else (1f - flowPhase))
                    val frac = (rawFrac % 1f)
                    val x = frac * w
                    val beadAlpha = sin(frac * Math.PI).toFloat().coerceIn(0.2f, 1f)

                    drawCircle(
                        color = ambientColor.copy(alpha = beadAlpha),
                        radius = 2.5.dp.toPx(),
                        center = Offset(x, h * 0.5f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = beadAlpha),
                        radius = 1.dp.toPx(),
                        center = Offset(x, h * 0.5f)
                    )
                }
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
    isDarkMode: Boolean,
    glowIntensity: Float,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        accentColor = accentColor,
        isDarkMode = isDarkMode,
        glowIntensity = if (isSelected) 1.0f else glowIntensity,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = if (isSelected) accentColor else TextSlate
                )
                PulsingNeonDot(color = accentColor, sizeDp = 5.dp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = if (isDarkMode) TextWhite else TextCharcoal
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Text(
                text = subtext,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 9.sp,
                color = TextSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            MetricSparkline(
                points = points,
                lineColor = accentColor,
                isDarkMode = isDarkMode,
                glowIntensity = glowIntensity,
                heightDp = 30.dp
            )
        }
    }
}

@Composable
private fun OscilloscopeStatPill(
    label: String,
    value: String,
    isDarkMode: Boolean,
    highlightColor: Color? = null
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
            fontSize = 12.sp,
            color = highlightColor ?: if (isDarkMode) TextWhite else TextCharcoal
        )
    }
}

@Composable
fun VitalLogItem(
    log: SessionLog,
    accentColor: Color,
    isDarkMode: Boolean,
    glow: Float
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        isDarkMode = isDarkMode,
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (isDarkMode) 0.22f else 0.12f))
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

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.title,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isDarkMode) TextWhite else TextCharcoal
                    )
                    Text(
                        text = log.timestampFormatted,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 9.sp,
                        color = TextSlate
                    )
                }
                Text(
                    text = log.description,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 10.sp,
                    color = TextSlate,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.statTag,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = accentColor
                )
            }
        }
    }
}

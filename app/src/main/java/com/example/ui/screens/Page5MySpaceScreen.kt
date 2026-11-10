package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.GlassThemeConfig
import com.example.data.MetricType
import com.example.data.SessionLog
import com.example.data.TelemetryPoint
import com.example.data.TemperatureUnit
import com.example.data.UserAccount
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
 * PAGE 5: My Space Telemetry Console & Account Center
 * High-performance, silky-smooth 90 FPS real-time charts, oscilloscopes,
 * User Account Profile, and dynamic Current-driven ambient glow visual effects (VFX).
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
    userAccount: UserAccount = UserAccount(),
    onUpdateOwnerName: (String) -> Unit = {},
    onAddVitalLog: (String, String, String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = themeConfig.isDarkMode
    val baseAccent = themeConfig.accentColor
    val glow = themeConfig.glowIntensity
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val textColor = if (isDark) TextWhite else TextCharcoal

    var selectedMetric by remember { mutableStateOf(MetricType.VOLTAGE) }
    var showProfileEditDialog by remember { mutableStateOf(false) }
    val activeCurrentMa = telemetry.currentMa

    // DYNAMIC CURRENT-DRIVEN AMBIENT GLOW (VFX)
    // Bind the background ambient glow layer to live Current (mA) state:
    // - High Inflow / Stress (> 2500 mA or high thermals): Reactive Red Ambient Glow (#FF3D00)
    // - Stable / Safe Flow (400 - 2500 mA): Serene Emerald Green (#00C853)
    // - Low Trickle Flow (0 - 399 mA): Serene Royal Blue (#2962FF)
    // - High Load Drain (< -600 mA): Energetic Violet/Amber (#A855F7)
    // - Idle Drain (-600 - 0 mA): Cool Cyan/Slate (#00B0FF)
    val isHighStress = activeCurrentMa > 2500 || telemetry.temperatureC >= 38.5f
    val isSafeFlow = activeCurrentMa in 400..2500 && !isHighStress

    val targetAmbientColor = when {
        isHighStress -> TactileCoralRed // High Inflow Stress
        isSafeFlow -> TactileGreen // Stable / Safe Flow
        activeCurrentMa in 0..399 -> TactileRoyalBlue // Serene Trickle
        activeCurrentMa < -600 -> Color(0xFFA855F7) // High Load Drain
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

    // 90 FPS Breathing Ambient Aura & Particle Shift
    val infiniteTransition = rememberInfiniteTransition(label = "ambientPulse90Fps")
    val ambientAuraBreath by infiniteTransition.animateFloat(
        initialValue = if (isHighStress) 0.80f else 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isHighStress) 800 else 1600, easing = FastOutSlowInEasing),
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
            isStress = isHighStress,
            glowIntensity = glow
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 110.dp)
                .graphicsLayer { clip = false }
        ) {
            // USER ACCOUNT & GOOGLE LINK BANNER (Replaces MY SPACE TELEMETRY CONSOLE)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = animatedAmbientColor,
                    isDarkMode = isDark,
                    glowIntensity = glow,
                    onClick = { showProfileEditDialog = true }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Section: Account Avatar & User Name with Verification Badge on the Right
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ChargingStateNeonProfileAvatar(
                                    userAccount = userAccount,
                                    telemetry = telemetry,
                                    isDarkMode = isDark,
                                    glowIntensity = glow,
                                    onClick = { showProfileEditDialog = true }
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    // User Name with Verified Badge on its right
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = userAccount.ownerName,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            letterSpacing = 0.2.sp,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Account",
                                            tint = themeConfig.appTier.color,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Account Tier Label with dynamic glowing color
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(themeConfig.appTier.color)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Ampere Pulse • ${themeConfig.appTier.label}",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = themeConfig.appTier.color
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Right Section: Quick Edit Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(themeConfig.appTier.color.copy(alpha = if (isDark) 0.20f else 0.12f))
                                    .border(1.dp, themeConfig.appTier.color.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .clickable { showProfileEditDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "تعديل",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = themeConfig.appTier.color
                                )
                            }
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
                    currentMa = activeCurrentMa,
                    ambientColor = animatedAmbientColor,
                    isDarkMode = isDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Floating Data Cards Grid (Tactile Neumorphic Cards with Diffused Glow)
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
                        ambientGlowColor = animatedAmbientColor,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.VOLTAGE,
                        onClick = { selectedMetric = MetricType.VOLTAGE }
                    )

                    val currentSign = if (activeCurrentMa >= 0) "+" else ""
                    MySpaceMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "CURRENT",
                        value = "$currentSign${numberFormat.format(activeCurrentMa)}",
                        unit = "mA",
                        subtext = if (activeCurrentMa >= 0) "Inflow charge" else "Discharge draw",
                        points = currentHistory,
                        accentColor = animatedAmbientColor,
                        ambientGlowColor = animatedAmbientColor,
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
                    val instantaneousWattage = (telemetry.voltageMv / 1000f) * (abs(activeCurrentMa) / 1000f)
                    MySpaceMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "WATTAGE",
                        value = String.format(Locale.US, "%.2f", instantaneousWattage),
                        unit = "W",
                        subtext = "P = V × I Real-time",
                        points = wattageHistory,
                        accentColor = if (selectedMetric == MetricType.WATTAGE) animatedAmbientColor else baseAccent,
                        ambientGlowColor = animatedAmbientColor,
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
                        ambientGlowColor = animatedAmbientColor,
                        isDarkMode = isDark,
                        glowIntensity = glow,
                        isSelected = selectedMetric == MetricType.TEMPERATURE,
                        onClick = { selectedMetric = MetricType.TEMPERATURE }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                                "All 12 battery registers verified OK: ${telemetry.voltageMv}mV, ${activeCurrentMa}mA, ${telemetry.temperatureC}°C, 0 degradation.",
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

        if (showProfileEditDialog) {
            UserProfileEditDialog(
                userAccount = userAccount,
                isDarkMode = isDark,
                onDismiss = { showProfileEditDialog = false },
                onUpdateOwnerName = { name ->
                    onUpdateOwnerName(name)
                }
            )
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
    isStress: Boolean,
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
        val radiusTop = (w * (if (isStress) 0.75f else 0.65f)) * breatheScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ambientColor.copy(alpha = if (isDarkMode) (if (isStress) 0.26f else 0.18f) * glowIntensity else (if (isStress) 0.16f else 0.10f) * glowIntensity),
                    ambientColor.copy(alpha = if (isDarkMode) 0.06f * glowIntensity else 0.03f * glowIntensity),
                    Color.Transparent
                ),
                center = centerTop,
                radius = radiusTop
            ),
            center = centerTop,
            radius = radiusTop
        )

        // Secondary bottom halo behind the oscilloscope
        val centerBottom = Offset(w * 0.5f, h * 0.60f)
        val radiusBottom = (w * 0.58f) * breatheScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ambientColor.copy(alpha = if (isDarkMode) (if (isStress) 0.20f else 0.14f) * glowIntensity else (if (isStress) 0.12f else 0.07f) * glowIntensity),
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
        currentMa in 400..2500 -> "🌿 SERENE SAFE CHARGING (+${currentMa} mA)"
        currentMa in 0..399 -> "🔋 SERENE TRICKLE FLOW (+${currentMa} mA)"
        currentMa < -600 -> "🔥 HEAVY LOAD DRAIN (${currentMa} mA)"
        else -> "⚡ IDLE SYSTEM DRAW (${currentMa} mA)"
    }

    // 90 FPS traveling wave phase
    val infiniteTransition = rememberInfiniteTransition(label = "electronFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (absCurrent > 2000) 900 else 1800, easing = LinearEasing),
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
    ambientGlowColor: Color = accentColor,
    isDarkMode: Boolean,
    glowIntensity: Float,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            // Outer diffused ambient aura around each card
            .drawBehind {
                val auraRadius = size.width * 0.6f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ambientGlowColor.copy(alpha = if (isDarkMode) 0.12f * glowIntensity else 0.07f * glowIntensity),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = auraRadius
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = auraRadius
                )
            }
    ) {
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
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

/**
 * Interactive Neumorphic User Profile Dialog.
 */
@Composable
fun UserProfileEditDialog(
    userAccount: UserAccount,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onUpdateOwnerName: (String) -> Unit
) {
    var ownerNameInput by remember { mutableStateOf(userAccount.ownerName) }

    val cardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) TextWhite else TextCharcoal
    val subTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textColor,
        textContentColor = subTextColor,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TactileRoyalBlue.copy(alpha = if (isDarkMode) 0.22f else 0.12f))
                        .border(1.dp, TactileRoyalBlue.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TactileRoyalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ملف المستخدم",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = textColor
                    )
                    Text(
                        text = "Ampere Pulse Local Core",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        color = subTextColor
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Header Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TactileGreen.copy(alpha = if (isDarkMode) 0.16f else 0.10f))
                        .border(1.dp, TactileGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TactileGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "حساب عتاد محلي نشط 100%",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TactileGreen
                            )
                            Text(
                                text = "البيانات مخزنة ومحمية محلياً في جهازك (Room WAL)",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = subTextColor
                            )
                        }
                    }
                }

                // Name Input
                Column {
                    Text(
                        text = "اسم صاحب الحساب",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = {
                            ownerNameInput = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("أدخل اسم صاحب الحساب...") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TactileRoyalBlue)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TactileRoyalBlue,
                            unfocusedBorderColor = if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Account Details Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رتبة الحساب",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = subTextColor
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TactileRoyalBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = userAccount.accountTier,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TactileRoyalBlue
                                )
                            }
                        }

                        Text(
                            text = "الأحرف الأولى: ${userAccount.avatarInitials}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = textColor
                        )
                    }
                }

                // Privacy and Security Bullets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "مواصفات الأمان المحلي:",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = textColor
                    )
                    ProfileFeatureBullet(
                        icon = Icons.Default.Bolt,
                        tint = TactileGreen,
                        text = "قاعدة بيانات Room محلية مع وضع WAL لسرعة القراءة والكتابة",
                        isDarkMode = isDarkMode
                    )
                    ProfileFeatureBullet(
                        icon = Icons.Default.Verified,
                        tint = TactileRoyalBlue,
                        text = "خصوصية تامة 100% دون إرسال أو ربط أي بيانات بأي جهات خارجية",
                        isDarkMode = isDarkMode
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateOwnerName(ownerNameInput.ifBlank { "Charfeddine" })
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileRoyalBlue
                )
            ) {
                Text(
                    text = "حفظ الاسم",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "إلغاء",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor
                )
            }
        }
    )
}

@Composable
private fun ProfileFeatureBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String,
    isDarkMode: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(13.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = PlusJakartaSansFontFamily,
            fontSize = 10.sp,
            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
            lineHeight = 14.sp
        )
    }
}

private data class AvatarChargingStateConfig(
    val primaryNeon: Color,
    val secondaryNeon: Color,
    val stateBadgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val pulseDurationMs: Int
)

/**
 * Dynamic Pulsing Neon Avatar Ring for Google Account Profile.
 * Real-time responsive to Battery Charging States (Energy Storing, Overheating, Cooling, Deep Sleep),
 * creating an ultra-immersive Liquid Glass & Tactile Neumorphic visual effect.
 */
@Composable
fun ChargingStateNeonProfileAvatar(
    userAccount: UserAccount,
    telemetry: BatteryTelemetry,
    isDarkMode: Boolean,
    glowIntensity: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val livingState = telemetry.livingState
    val isCharging = telemetry.status == com.example.data.BatteryStatus.CHARGING || telemetry.status == com.example.data.BatteryStatus.FULL || telemetry.currentMa > 0
    val isFastCharging = telemetry.isFastCharging || telemetry.currentMa >= 2000
    val isOverheating = telemetry.temperatureC >= 38.5f || livingState == com.example.data.LivingBatteryState.OVERDRIVE_HEATING

    // Dynamic State Colors & Timing mapped to BatteryManager Telemetry
    val config = when {
        isOverheating -> AvatarChargingStateConfig(
            primaryNeon = TactileCoralRed, // #FF3D00 Warning Red
            secondaryNeon = Color(0xFFFF9100), // Electric Amber
            stateBadgeIcon = Icons.Default.LocalFireDepartment,
            pulseDurationMs = 650 // Rapid alarm heartbeat pulse
        )
        isFastCharging -> AvatarChargingStateConfig(
            primaryNeon = TactileGreen, // #00C853 Phosphor Green
            secondaryNeon = Color(0xFF00E5FF), // Cyber Cyan Inflow
            stateBadgeIcon = Icons.Default.Bolt,
            pulseDurationMs = 850 // Fast energetic rhythm
        )
        isCharging -> AvatarChargingStateConfig(
            primaryNeon = TactileGreen, // #00C853
            secondaryNeon = Color(0xFF69F0AE),
            stateBadgeIcon = Icons.Default.Bolt,
            pulseDurationMs = 1150 // Smooth charging rhythm
        )
        livingState == com.example.data.LivingBatteryState.COOLING -> AvatarChargingStateConfig(
            primaryNeon = Color(0xFF00B0FF), // Ice Cyan
            secondaryNeon = Color(0xFF80D8FF),
            stateBadgeIcon = Icons.Default.AcUnit,
            pulseDurationMs = 1400 // Serene cryo cooling wave
        )
        livingState == com.example.data.LivingBatteryState.DEEP_SLEEP -> AvatarChargingStateConfig(
            primaryNeon = Color(0xFF7C4DFF), // Night Purple
            secondaryNeon = Color(0xFFB388FF),
            stateBadgeIcon = Icons.Default.Bedtime,
            pulseDurationMs = 2000 // Deep tranquil breathing
        )
        else -> AvatarChargingStateConfig(
            primaryNeon = TactileRoyalBlue, // #2962FF
            secondaryNeon = Color(0xFF82B1FF),
            stateBadgeIcon = Icons.Default.Bolt,
            pulseDurationMs = 1600 // Nominal idle pulse
        )
    }

    val primaryNeon = config.primaryNeon
    val secondaryNeon = config.secondaryNeon
    val stateBadgeIcon = config.stateBadgeIcon
    val pulseDurationMs = config.pulseDurationMs

    val infiniteTransition = rememberInfiniteTransition(label = "avatarNeonTransition")

    // Pulsing Neon Glow Intensity & Outer Halo Scale
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulseFraction"
    )

    // Fluid Orbiting Electron Arc Rotation
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isFastCharging) 2400 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatarOrbitAngle"
    )

    Box(
        modifier = modifier
            .size(54.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 1. Dynamic Diffused Ambient Neon Aura & Orbiting Liquid Stroke (Canvas Glow)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.44f
            val dynamicRadius = baseRadius + (4.dp.toPx() * pulseFraction)

            // Outer soft ambient neon diffusion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryNeon.copy(alpha = if (isDarkMode) (0.42f * pulseFraction * glowIntensity) else (0.28f * pulseFraction * glowIntensity)),
                        primaryNeon.copy(alpha = if (isDarkMode) (0.12f * pulseFraction * glowIntensity) else (0.08f * pulseFraction * glowIntensity)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius + 8.dp.toPx()
                ),
                center = center,
                radius = dynamicRadius + 8.dp.toPx()
            )

            // Orbiting liquid neon sweep stroke
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryNeon.copy(alpha = 0.1f),
                        primaryNeon,
                        secondaryNeon,
                        primaryNeon.copy(alpha = 0.15f)
                    ),
                    center = center
                ),
                startAngle = orbitAngle,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(
                    width = (2.4.dp + (0.8.dp * pulseFraction)).toPx(),
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2f, baseRadius * 2f)
            )

            // Tactile specular liquid glass inner border
            drawCircle(
                color = if (isDarkMode) Color(0x33FFFFFF) else Color(0x80FFFFFF),
                radius = baseRadius - 1.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Liquid Glass Frosted Center Capsule
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (isDarkMode) {
                        Color(0xFF1E293B).copy(alpha = 0.90f)
                    } else {
                        Color(0xFFFFFFFF).copy(alpha = 0.95f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = primaryNeon.copy(alpha = 0.35f + (0.35f * pulseFraction)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userAccount.avatarInitials,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryNeon
            )
        }

        // 3. Micro Neon State Indicator Pill (Pinned at 4 o'clock position)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(17.dp)
                .clip(CircleShape)
                .background(
                    if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                )
                .border(
                    1.2.dp,
                    primaryNeon.copy(alpha = 0.85f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = stateBadgeIcon,
                contentDescription = null,
                tint = primaryNeon,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}



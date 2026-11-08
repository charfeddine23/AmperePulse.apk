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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
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
 * User Account with Google Linking, and dynamic Current-driven ambient glow visual effects (VFX).
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
    onLinkGoogle: (String, String) -> Unit = { _, _ -> },
    onUnlinkGoogle: () -> Unit = {},
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
    var showGoogleLinkDialog by remember { mutableStateOf(false) }
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
                    accentColor = if (userAccount.isGoogleLinked) TactileGreen else animatedAmbientColor,
                    isDarkMode = isDark,
                    glowIntensity = glow
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
                            // Left Section: Account Avatar, Status & Google Link Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (userAccount.isGoogleLinked) {
                                                TactileGreen.copy(alpha = if (isDark) 0.20f else 0.12f)
                                            } else {
                                                animatedAmbientColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                                            }
                                        )
                                        .border(
                                            1.5.dp,
                                            if (userAccount.isGoogleLinked) TactileGreen else animatedAmbientColor.copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (userAccount.isGoogleLinked) {
                                        GoogleLogo(sizeDp = 22.dp)
                                    } else {
                                        Text(
                                            text = userAccount.avatarInitials,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = animatedAmbientColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "حساب المستخدم",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        PulsingNeonDot(
                                            color = if (userAccount.isGoogleLinked) TactileGreen else animatedAmbientColor,
                                            sizeDp = 6.dp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Google Account Link Action Chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (userAccount.isGoogleLinked) {
                                                    TactileGreen.copy(alpha = if (isDark) 0.22f else 0.12f)
                                                } else {
                                                    Color(0xFF4285F4).copy(alpha = if (isDark) 0.22f else 0.12f)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (userAccount.isGoogleLinked) TactileGreen.copy(alpha = 0.6f) else Color(0xFF4285F4).copy(alpha = 0.5f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { showGoogleLinkDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            GoogleLogo(sizeDp = 12.dp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (userAccount.isGoogleLinked) "مرتبط بـ Google ✓" else "ربط بحساب Google",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (userAccount.isGoogleLinked) TactileGreen else Color(0xFF4285F4)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Right Section: اسم صاحب الحساب (Prominently on the Right Side)
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (userAccount.isGoogleLinked) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = TactileGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = userAccount.ownerName,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.3.sp,
                                        color = textColor
                                    )
                                }

                                Text(
                                    text = "صاحب الحساب",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSlate
                                )

                                Text(
                                    text = if (userAccount.isGoogleLinked) userAccount.email else "حساب عتاد محلي",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 9.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    maxLines = 1
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

        if (showGoogleLinkDialog) {
            GoogleAccountLinkDialog(
                userAccount = userAccount,
                isDarkMode = isDark,
                onDismiss = { showGoogleLinkDialog = false },
                onLinkGoogle = { name, email ->
                    onLinkGoogle(name, email)
                    showGoogleLinkDialog = false
                },
                onUnlinkGoogle = {
                    onUnlinkGoogle()
                    showGoogleLinkDialog = false
                },
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
 * High-Precision Vector Canvas Google Logo (Authentic 4-Color G Symbol).
 */
@Composable
fun GoogleLogo(modifier: Modifier = Modifier, sizeDp: Dp = 18.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.22f
        val inset = strokeWidth / 2f
        val arcRect = androidx.compose.ui.geometry.Rect(inset, inset, w - inset, h - inset)

        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(w * 0.48f, h * 0.5f),
            end = Offset(w - strokeWidth * 0.2f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )

        // Red arc (top / top-left)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 200f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Yellow arc (left / bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Green arc (bottom / bottom-right)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 25f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue arc (right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 310f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

/**
 * Interactive Neumorphic Google Account Link & Profile Sync Dialog.
 */
@Composable
fun GoogleAccountLinkDialog(
    userAccount: UserAccount,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onLinkGoogle: (String, String) -> Unit,
    onUnlinkGoogle: () -> Unit,
    onUpdateOwnerName: (String) -> Unit
) {
    var ownerNameInput by remember { mutableStateOf(userAccount.ownerName) }
    val googleEmail = userAccount.email

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
                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFF4285F4).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleLogo(sizeDp = 20.dp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (userAccount.isGoogleLinked) "إدارة حساب Google" else "ربط بحساب Google",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = textColor
                    )
                    Text(
                        text = "Ampere Pulse Cloud & Profile Sync",
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
                        .background(
                            if (userAccount.isGoogleLinked) {
                                TactileGreen.copy(alpha = if (isDarkMode) 0.16f else 0.10f)
                            } else {
                                Color(0xFF4285F4).copy(alpha = if (isDarkMode) 0.16f else 0.08f)
                            }
                        )
                        .border(
                            1.dp,
                            if (userAccount.isGoogleLinked) TactileGreen.copy(alpha = 0.4f) else Color(0xFF4285F4).copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (userAccount.isGoogleLinked) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (userAccount.isGoogleLinked) TactileGreen else Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (userAccount.isGoogleLinked) "الحساب مرتبط بنجاح ✓" else "حسابك جاهز للربط الفوري",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (userAccount.isGoogleLinked) TactileGreen else Color(0xFF4285F4)
                            )
                            Text(
                                text = if (userAccount.isGoogleLinked) "المزامنة السحابية 24/7 نشطة مع قاعدة البيانات" else "اربط حسابك لتفعيل النسخ الاحتياطي في السحابة",
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
                            if (userAccount.isGoogleLinked) {
                                onUpdateOwnerName(it)
                            }
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

                // Google Account Details Box
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
                                text = "حساب Google المرتبط",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = subTextColor
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (userAccount.isGoogleLinked) TactileGreen.copy(alpha = 0.15f) else Color(0x184285F4))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (userAccount.isGoogleLinked) "ACTIVE" else "READY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userAccount.isGoogleLinked) TactileGreen else Color(0xFF4285F4)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleLogo(sizeDp = 16.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = googleEmail,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }
                }

                // Cloud Sync Feature Highlights
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "مزايا ربط الحساب:",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = textColor
                    )
                    GoogleFeatureBullet(
                        icon = Icons.Default.Bolt,
                        tint = TactileGreen,
                        text = "مزامنة سحابية مستمرة لسجلات الجهد والتيار والحرارة (Room WAL)",
                        isDarkMode = isDarkMode
                    )
                    GoogleFeatureBullet(
                        icon = Icons.Default.Verified,
                        tint = TactileRoyalBlue,
                        text = "حفظ تقدم بيضة السايبر ومستويات طريق الجائزة في السحابة",
                        isDarkMode = isDarkMode
                    )
                    GoogleFeatureBullet(
                        icon = Icons.Default.Sync,
                        tint = Color(0xFFA855F7),
                        text = "إمكانية استرجاع البيانات فوراً عند تغيير الجهاز أو إعادة تثبيت التطبيق",
                        isDarkMode = isDarkMode
                    )
                }
            }
        },
        confirmButton = {
            if (!userAccount.isGoogleLinked) {
                Button(
                    onClick = {
                        onLinkGoogle(ownerNameInput.ifBlank { "Charfeddine" }, googleEmail)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4)
                    )
                ) {
                    GoogleLogo(sizeDp = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تأكيد الربط بحساب Google الآن",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onUnlinkGoogle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TactileCoralRed
                        )
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إلغاء الربط",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            onUpdateOwnerName(ownerNameInput)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TactileRoyalBlue
                        )
                    ) {
                        Text(
                            text = "حفظ وإغلاق",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (!userAccount.isGoogleLinked) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "إلغاء",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = subTextColor
                    )
                }
            }
        }
    )
}

@Composable
private fun GoogleFeatureBullet(
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


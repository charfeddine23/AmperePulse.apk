package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberHapticsEngine
import com.example.audio.CyberSoundscapeEngine
import com.example.data.BatteryTelemetry
import com.example.data.CableDiagnosticResult
import com.example.data.CableDnaTier
import com.example.data.GlassThemeConfig
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileAmber
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PAGE 4: Cable DNA & Charger Analyzer
 * Real-time diagnostic module triggered upon charger connection.
 * Measures cable resistance (mΩ), voltage sag, power throughput,
 * classifies charger tiers (Legendary Original vs. Dangerous Counterfeit),
 * and computes XP multiplier or thermal warning alerts.
 */
@Composable
fun Page4CableDnaScreen(
    telemetry: BatteryTelemetry,
    themeConfig: GlassThemeConfig,
    cableResult: CableDiagnosticResult,
    onRunDiagnosticSweep: (CableDnaTier?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isDark = themeConfig.isDarkMode
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var isScanning by remember { mutableStateOf(false) }
    val activeResult = cableResult
    val tier = activeResult.tier
    val tierColor = tier.badgeColor

    // 500ms Smooth Indicator Animation to Real-Time Measurement
    val targetResistance = activeResult.resistanceMilliOhms.toFloat()
    val animatedResistance by animateFloatAsState(
        targetValue = targetResistance,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "resistance500msGauge"
    )

    val targetVoltageDrop = activeResult.voltageDropMv.toFloat()
    val animatedVoltageDrop by animateFloatAsState(
        targetValue = targetVoltageDrop,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "voltageDrop500ms"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "cableDnaGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (tier.isDangerous) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scanSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanSweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
            .graphicsLayer { clip = false }
    ) {
        // 1. Header Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = tierColor,
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(tierColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                    .border(1.2.dp, tierColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cable,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "CABLE DNA & CHARGER ANALYZER",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "${telemetry.plugType.label} • Active Line Audit",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSlate
                                )
                            }
                        }
                        PulsingNeonDot(color = tierColor, sizeDp = 7.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Main Cable Resistance & Diagnostic Dial Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = tierColor,
                isDarkMode = isDark,
                glowIntensity = glow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INTERNAL CABLE RESISTANCE",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = TextSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Circular Resistance Gauge (mΩ Meter)
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val strokeWidth = 12.dp.toPx()
                            val center = Offset(w / 2f, h / 2f)
                            val radius = (w - strokeWidth) / 2f

                            // Background Track Arc (240 degrees arc from 150 to 390)
                            drawArc(
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFCFD9E6),
                                startAngle = 150f,
                                sweepAngle = 240f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Active Measured Resistance Arc - Smoothly rising over 500ms
                            val resistanceFraction = (animatedResistance / 600f).coerceIn(0f, 1f)
                            val activeSweep = 240f * resistanceFraction

                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        TactileGreen,
                                        TactileCyan,
                                        TactileAmber,
                                        TactileCoralRed
                                    ),
                                    center = center
                                ),
                                startAngle = 150f,
                                sweepAngle = activeSweep,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth + 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // Glowing Real-Time Needle / Pointer Orb at the leading tip of the gauge
                            if (animatedResistance > 1.5f) {
                                val currentAngle = 150f + activeSweep
                                val angleRad = Math.toRadians(currentAngle.toDouble())
                                val needleX = center.x + radius * Math.cos(angleRad).toFloat()
                                val needleY = center.y + radius * Math.sin(angleRad).toFloat()

                                // Outer neon aura
                                drawCircle(
                                    color = tierColor.copy(alpha = 0.50f * pulseAlpha),
                                    radius = strokeWidth * 0.95f,
                                    center = Offset(needleX, needleY)
                                )
                                // Solid needle core
                                drawCircle(
                                    color = Color.White,
                                    radius = strokeWidth * 0.55f,
                                    center = Offset(needleX, needleY)
                                )
                                drawCircle(
                                    color = tierColor,
                                    radius = strokeWidth * 0.35f,
                                    center = Offset(needleX, needleY)
                                )
                            }

                            // Dynamic Scan Needle Glow
                            if (isScanning) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.8f),
                                    startAngle = scanSweepAngle,
                                    sweepAngle = 40f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Central Resistance Readout - Smooth Synchronous 500ms counter
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${animatedResistance.toInt()}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "mΩ",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = tierColor,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = "ΔV Sag: -${animatedVoltageDrop.toInt()} mV",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (tier.isDangerous) TactileCoralRed else TextSlate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Classification Tier Badge & Status Banner (No XP)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(tierColor.copy(alpha = if (isDark) 0.20f else 0.12f))
                            .border(1.2.dp, tierColor.copy(alpha = pulseAlpha), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tier.isDangerous) Icons.Default.Warning else Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = tierColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "شاحن غير موصول (0 mΩ)" else tier.title,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "وصل الشاحن لبدء الفحص التلقائي الفوري" else tier.grade,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = tierColor
                                    )
                                }
                            }

                            // Active Sensor Auto Status Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tierColor)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "STANDBY" else "100% AUTO",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tier Description Box
                    Text(
                        text = if (telemetry.plugType == com.example.data.ChargePlug.NONE)
                            "المستشعرات في وضع الاستعداد بانتظار توصيل كابل الشحن لحساب المقاومة وهبوط الجهد فورياً بدقة متناهية."
                        else tier.description,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = subTextColor,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 2x2 Telemetry Metrics Grid (Voltage Drop, Thermal Watts, Protocol, Score)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voltage Drop Sag
                CableMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "VOLTAGE SAG",
                    value = if (animatedVoltageDrop.toInt() > 0) "-${animatedVoltageDrop.toInt()} mV" else "0 mV",
                    subtext = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "No load connected" else "Drop from 5.0V line",
                    accentColor = if (animatedVoltageDrop > 500f) TactileCoralRed else TactileRoyalBlue,
                    isDark = isDark
                )

                // Thermal Dissipation
                CableMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "HEAT DISSIPATION",
                    value = "${activeResult.thermalDissipationMilliWatts} mW",
                    subtext = "I² × R Line Loss",
                    accentColor = if (activeResult.thermalDissipationMilliWatts > 2500) TactileCoralRed else TactileAmber,
                    isDark = isDark
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Protocol
                CableMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "PROTOCOL MATRIX",
                    value = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "Disconnected" else activeResult.usbProtocol,
                    subtext = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "Line Idle (0 mA)" else "Handshake Active",
                    accentColor = TactileCyan,
                    isDark = isDark
                )

                // Quality Score
                CableMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CABLE SCORE",
                    value = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "-- / 100" else "${activeResult.qualityScore} / 100",
                    subtext = if (telemetry.plugType == com.example.data.ChargePlug.NONE) "Connect Charger" else "Pin Integrity ${activeResult.pinIntegrityPercent}%",
                    accentColor = tierColor,
                    isDark = isDark
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Auto Refresh Real-Time Sweep Button (No simulation chips, 100% automated)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isScanning = true
                        CyberSoundscapeEngine.playDiagnosticSweep()
                        CyberHapticsEngine.triggerTactileTick(context)
                        delay(900)
                        isScanning = false
                        if (tier.isDangerous) {
                            CyberHapticsEngine.triggerThermalWarning(context)
                            CyberSoundscapeEngine.playThermalAlert()
                        } else {
                            CyberHapticsEngine.triggerBenchmarkSuccess(context)
                            CyberSoundscapeEngine.playCableTierLegendary()
                        }
                        onRunDiagnosticSweep(null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tier.isDangerous) TactileCoralRed else TactileRoyalBlue
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isScanning) "SAMPLING HARDWARE REGISTERS..." else "RE-SCAN CABLE & CHARGER NOW",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CableMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    isDark: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE7ECF3))
            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0x99BECAD8), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = TextSlate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = if (isDark) Color(0xFF94A3B8) else TextSlate
            )
        }
    }
}

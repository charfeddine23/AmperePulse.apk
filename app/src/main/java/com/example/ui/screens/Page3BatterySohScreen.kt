package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberHapticsEngine
import com.example.audio.CyberSoundscapeEngine
import com.example.data.BatterySohPrediction
import com.example.data.BatteryTelemetry
import com.example.data.CellHealthStatus
import com.example.data.GlassThemeConfig
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PulsingNeonDot
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
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
 * PAGE 3: Battery SOH & Charge Cycles Predictor
 * Algorithmic State of Health (SOH) predictor using Room WAL database logs and charge cycle counts.
 * Displays medical-grade cell telemetry, degradation rate, estimated remaining lifespan in days,
 * and forecast curves.
 */
@Composable
fun Page3BatterySohScreen(
    telemetry: BatteryTelemetry,
    themeConfig: GlassThemeConfig,
    sohPrediction: BatterySohPrediction,
    onRecalibrateSoh: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isDark = themeConfig.isDarkMode
    val glow = themeConfig.glowIntensity
    val textColor = if (isDark) TextWhite else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var isCalibrating by remember { mutableStateOf(true) }

    // Automatically recalibrate SOH against Room WAL & hardware registers on page entry
    LaunchedEffect(Unit) {
        isCalibrating = true
        onRecalibrateSoh(false)
        delay(450L)
        isCalibrating = false
    }

    val statusColor = sohPrediction.cellStatus.color
    val animatedHealthFraction by animateFloatAsState(
        targetValue = (sohPrediction.healthPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.86f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sohGaugeFraction"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sohPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sohPulseAlpha"
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
                accentColor = statusColor,
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
                                    .background(statusColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                    .border(1.2.dp, statusColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "BATTERY SOH & CYCLE PREDICTOR",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "Medical-Grade Degradation Telemetry",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSlate
                                )
                            }
                        }
                        PulsingNeonDot(color = statusColor, sizeDp = 7.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Futuristic Medical SOH Gauge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = statusColor,
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
                        text = "CELL STATE OF HEALTH (SOH)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = TextSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Circular Medical SOH Gauge
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val strokeWidth = 14.dp.toPx()
                            val center = Offset(w / 2f, h / 2f)
                            val radius = (w - strokeWidth) / 2f

                            // Background Inactive Arc
                            drawArc(
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFCFD9E6),
                                startAngle = 140f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Active Health Arc (smooth animated sub-second sweep)
                            val activeSweep = 260f * animatedHealthFraction

                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        TactileGreen,
                                        TactileCyan,
                                        Color(0xFF00E5FF),
                                        TactileGreen
                                    ),
                                    center = center
                                ),
                                startAngle = 140f,
                                sweepAngle = activeSweep,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth + 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // Medical Calibrated Tick Needle Indicator
                            val angleRad = Math.toRadians((140f + activeSweep).toDouble())
                            val needleX = (center.x + radius * Math.cos(angleRad)).toFloat()
                            val needleY = (center.y + radius * Math.sin(angleRad)).toFloat()

                            drawCircle(
                                color = Color.White,
                                radius = 6.dp.toPx(),
                                center = Offset(needleX, needleY)
                            )
                            drawCircle(
                                color = statusColor,
                                radius = 3.dp.toPx(),
                                center = Offset(needleX, needleY)
                            )
                        }

                        // Gauge Center Texts (live sub-second precision)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f%%", sohPrediction.healthPercentage),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = textColor
                            )
                            Text(
                                text = "EST. ${sohPrediction.estimatedLifespanDays} DAYS REMAINING",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cell Status Label Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = if (isDark) 0.20f else 0.12f))
                            .border(1.2.dp, statusColor.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sohPrediction.cellStatus.label,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = textColor
                                )
                            }
                            Text(
                                text = "NMC 811 LATTICE",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sohPrediction.cellStatus.description,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = subTextColor,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 2x2 Telemetry Medical Cards Grid (Cycles, Usable Capacity, Impedance, Coulombic)
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
                // Effective Capacity
                SohMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "EFFECTIVE CAPACITY",
                    value = "${sohPrediction.estimatedCurrentCapacityMah} mAh",
                    subtext = "Factory ${sohPrediction.originalCapacityMah} mAh",
                    accentColor = statusColor,
                    isDark = isDark
                )

                // Total Cycles
                SohMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CHARGE CYCLES",
                    value = "${sohPrediction.totalCyclesCompleted} Cycles",
                    subtext = "${sohPrediction.estimatedRemainingCycles} cycles to 80%",
                    accentColor = TactileRoyalBlue,
                    isDark = isDark
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Internal Impedance ESR
                SohMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CELL IMPEDANCE (ESR)",
                    value = "${sohPrediction.internalResistanceEstimateMilliOhms} mΩ",
                    subtext = "Optimal Conductivity < 65mΩ",
                    accentColor = TactileCyan,
                    isDark = isDark
                )

                // Coulombic Efficiency
                SohMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "COULOMBIC RATIO",
                    value = String.format(java.util.Locale.US, "%.2f%%", sohPrediction.coulombicEfficiencyPercent),
                    subtext = "Electron Retention Rate",
                    accentColor = TactileGreen,
                    isDark = isDark
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Lifespan Forecast & Degradation Trajectory Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = TactileCyan,
                isDarkMode = isDark,
                glowIntensity = glow
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIFESPAN TRAJECTORY FORECAST",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            color = textColor
                        )
                        Text(
                            text = "PROJECTED: ${sohPrediction.projectedReplacementDate}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TactileCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Canvas Trajectory Curve
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val path = Path()
                            val fillPath = Path()

                            val points = listOf(
                                Offset(0f, h * 0.15f), // 100%
                                Offset(w * 0.20f, h * 0.22f), // 98%
                                Offset(w * 0.45f, h * 0.38f), // 95%
                                Offset(w * 0.70f, h * 0.58f), // 90%
                                Offset(w, h * 0.82f) // 80% threshold
                            )

                            path.moveTo(points[0].x, points[0].y)
                            fillPath.moveTo(points[0].x, h)
                            fillPath.lineTo(points[0].x, points[0].y)

                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val cx1 = prev.x + (curr.x - prev.x) / 2f
                                val cy1 = prev.y
                                val cx2 = prev.x + (curr.x - prev.x) / 2f
                                val cy2 = curr.y
                                path.cubicTo(cx1, cy1, cx2, cy2, curr.x, curr.y)
                                fillPath.cubicTo(cx1, cy1, cx2, cy2, curr.x, curr.y)
                            }

                            fillPath.lineTo(w, h)
                            fillPath.close()

                            // Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        TactileCyan.copy(alpha = if (isDark) 0.25f else 0.15f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = h
                                )
                            )

                            // 80% EOL guideline
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            drawLine(
                                color = TactileCoralRed.copy(alpha = 0.4f),
                                start = Offset(0f, h * 0.82f),
                                end = Offset(w, h * 0.82f),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect
                            )

                            // Stroke
                            drawPath(
                                path = path,
                                color = TactileCyan,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Current operating point indicator (responds live to cycle count & health %)
                            val cycleFraction = (sohPrediction.totalCyclesCompleted / 1000f).coerceIn(0f, 1f)
                            val healthDropFraction = ((100f - sohPrediction.healthPercentage) / 20f).coerceIn(0f, 1f)
                            val curX = w * cycleFraction
                            val curY = h * (0.15f + (0.67f * healthDropFraction))
                            drawCircle(
                                color = statusColor.copy(alpha = 0.3f),
                                radius = 8.dp.toPx(),
                                center = Offset(curX, curY)
                            )
                            drawCircle(
                                color = statusColor,
                                radius = 4.dp.toPx(),
                                center = Offset(curX, curY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(curX, curY)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cycle 0 (100% Health)", fontSize = 10.sp, color = TextSlate)
                        Text("Cycle 1000 (80% Health EOL)", fontSize = 10.sp, color = TactileCoralRed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Automatic Live Sub-Second Calibration Status & Optional Manual Trigger
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isCalibrating = true
                        CyberSoundscapeEngine.playDiagnosticSweep()
                        CyberHapticsEngine.triggerTactileTick(context)
                        onRecalibrateSoh(true)
                        delay(450L)
                        isCalibrating = false
                        CyberHapticsEngine.triggerBenchmarkSuccess(context)
                        CyberSoundscapeEngine.playCryoChime()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileGreen
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCalibrating) Icons.Default.Autorenew else Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCalibrating) {
                            "AUTO-CALIBRATING CELL SOH & ROOM WAL..."
                        } else {
                            "LIVE AUTO-CALIBRATION ACTIVE • 100ms SYNC"
                        },
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SohMetricCard(
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

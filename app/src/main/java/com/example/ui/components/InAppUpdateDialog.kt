package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.CyberHapticsEngine
import com.example.audio.CyberSoundscapeEngine
import com.example.data.GlassThemeConfig
import com.example.data.update.InAppUpdateUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileAmber
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import java.util.Locale

/**
 * Centered, Kinetic & Eye-Friendly In-App Auto-Update Modal Dialog
 * Appears strictly centered on screen with smooth spring entrance,
 * orbiting telemetry halos, dynamic rocket flight physics, and live download progress.
 */
@Composable
fun InAppUpdateDialog(
    state: InAppUpdateUiState,
    themeConfig: GlassThemeConfig,
    accentColor: Color,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit,
    onCancelDownload: () -> Unit = {}
) {
    if (!state.isDialogVisible) return

    BackHandler { onDismiss() }

    val context = LocalContext.current
    val isDark = themeConfig.isDarkMode

    // Eye-friendly soft palette
    val textColor = if (isDark) Color(0xFFE9EFF8) else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF9FB0C7) else TextSlate

    val statusTint by animateColorAsState(
        targetValue = when {
            state.isDownloadedReadyToInstall && !state.needsInstallPermission -> TactileGreen
            state.needsInstallPermission -> TactileAmber
            state.isDownloading -> accentColor
            else -> accentColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dialogStatusTint"
    )

    val cardBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xF7182235), Color(0xFA101726))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFCEDF1F7), Color(0xFCE0E7F0))
        )
    }

    // Spring entrance animation when dialog opens
    val entranceProgress = remember { Animatable(0f) }
    LaunchedEffect(state.isDialogVisible) {
        entranceProgress.snapTo(0f)
        entranceProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "kineticUpdateModal")

    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "modalBreath"
    )
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.isDownloading) 2800 else 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitAngle"
    )
    val shockwaveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.isDownloading) 1200 else 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwave"
    )
    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = if (state.isDownloading) 1.38f else 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.isDownloading) 160 else 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameFlicker"
    )
    val rocketBob by infiniteTransition.animateFloat(
        initialValue = if (state.isDownloading) -5f else -3f,
        targetValue = if (state.isDownloading) 5f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.isDownloading) 650 else 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketBob"
    )
    val rocketTilt by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketTilt"
    )
    val sheenSweep by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "modalSheen"
    )

    val breathingBorderBrush = Brush.linearGradient(
        colors = listOf(
            statusTint.copy(alpha = 0.30f + 0.38f * breathPhase),
            TactileCyan.copy(alpha = 0.16f + 0.25f * (1f - breathPhase)),
            statusTint.copy(alpha = 0.25f + 0.32f * breathPhase)
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Explicit full-screen centered container ensuring exact dead-center placement
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66060A14).copy(alpha = 0.55f * entranceProgress.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 376.dp)
                    .graphicsLayer {
                        val p = entranceProgress.value
                        val scale = 0.86f + 0.14f * p
                        scaleX = scale
                        scaleY = scale
                        alpha = p.coerceIn(0f, 1f)
                        translationY = (1f - p) * 28f
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume backdrop tap inside dialog card */ }
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = statusTint.copy(alpha = 0.22f),
                        spotColor = statusTint.copy(alpha = 0.28f)
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(cardBrush)
                    .drawBehind {
                        // Gentle ambient top radial aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    statusTint.copy(alpha = if (isDark) 0.15f else 0.09f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.18f),
                                radius = size.width * 0.65f
                            ),
                            radius = size.width * 0.65f,
                            center = Offset(size.width * 0.5f, size.height * 0.18f)
                        )
                        // Subtle moving glass specular reflection
                        val sStart = Offset(size.width * sheenSweep - size.width * 0.35f, 0f)
                        val sEnd = Offset(size.width * sheenSweep + size.width * 0.2f, size.height)
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = if (isDark) 0.04f else 0.22f),
                                    Color.Transparent
                                ),
                                start = sStart,
                                end = sEnd
                            ),
                            cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
                        )
                    }
                    .border(1.35.dp, breathingBorderBrush, RoundedCornerShape(30.dp))
                    .padding(horizontal = 22.dp, vertical = 20.dp)
                    .testTag("in_app_update_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.80f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Row: Close (X) & Version Indicator Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                CyberHapticsEngine.triggerTactileTick(context)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Color(0x1EFFFFFF) else Color(0x100F172A)
                                )
                                .testTag("update_dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = subTextColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        val targetVerText = state.availableVersionName?.let { "v$it" }
                            ?: "v${state.currentVersionName}"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusTint.copy(alpha = if (isDark) 0.16f else 0.10f))
                                .border(
                                    1.dp,
                                    statusTint.copy(alpha = 0.35f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = targetVerText,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = statusTint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Kinetic Rocket Emblem with Expanding Shockwave, Rotating Orbit & Particle Sparks
                    Box(
                        modifier = Modifier.size(134.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxRadius = size.minDimension / 2f
                            val innerRadius = maxRadius * 0.66f

                            // Expanding shockwave pulse ring
                            val waveRadius = innerRadius + (maxRadius - innerRadius) * shockwaveProgress
                            val waveAlpha = (1f - shockwaveProgress) * if (isDark) 0.42f else 0.28f
                            drawCircle(
                                color = statusTint.copy(alpha = waveAlpha),
                                radius = waveRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1.6.dp.toPx())
                            )

                            // Ambient pulsing aura behind rocket core
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        statusTint.copy(alpha = (if (isDark) 0.30f else 0.16f) * (0.7f + 0.3f * breathPhase)),
                                        statusTint.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = maxRadius * 0.96f
                                ),
                                radius = maxRadius * 0.96f,
                                center = Offset(cx, cy)
                            )

                            // Rotating segmented orbital ring & satellite dot
                            rotate(degrees = orbitAngle, pivot = Offset(cx, cy)) {
                                drawArc(
                                    color = TactileCyan.copy(alpha = 0.55f),
                                    startAngle = 0f,
                                    sweepAngle = 72f,
                                    useCenter = false,
                                    topLeft = Offset(cx - innerRadius * 1.16f, cy - innerRadius * 1.16f),
                                    size = Size(innerRadius * 2.32f, innerRadius * 2.32f),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = statusTint.copy(alpha = 0.55f),
                                    startAngle = 180f,
                                    sweepAngle = 68f,
                                    useCenter = false,
                                    topLeft = Offset(cx - innerRadius * 1.16f, cy - innerRadius * 1.16f),
                                    size = Size(innerRadius * 2.32f, innerRadius * 2.32f),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawCircle(
                                    color = TactileCyan,
                                    radius = 3.2.dp.toPx(),
                                    center = Offset(cx + innerRadius * 1.16f, cy)
                                )
                            }

                            // Inner solid glass circular core
                            drawCircle(
                                color = if (isDark) Color(0xFF111828) else Color(0xFFD7E0EC),
                                radius = innerRadius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = statusTint.copy(alpha = 0.78f),
                                radius = innerRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.4.dp.toPx())
                            )

                            // Twinkling floating star particles
                            val starPositions = listOf(
                                Triple(cx - innerRadius * 0.62f, cy - innerRadius * 0.34f, Color.White.copy(alpha = (0.45f + 0.4f * breathPhase))),
                                Triple(cx + innerRadius * 0.64f, cy - innerRadius * 0.42f, TactileCyan.copy(alpha = 0.85f)),
                                Triple(cx - innerRadius * 0.56f, cy + innerRadius * 0.44f, TactileAmber.copy(alpha = 0.85f)),
                                Triple(cx + innerRadius * 0.58f, cy + innerRadius * 0.36f, statusTint.copy(alpha = 0.9f))
                            )
                            starPositions.forEach { (sx, sy, sColor) ->
                                drawCircle(
                                    color = sColor,
                                    radius = 2.2.dp.toPx(),
                                    center = Offset(sx, sy)
                                )
                            }

                            // Rocket Geometry with smooth hover bob & gentle flight bank
                            val rY = cy + rocketBob.dp.toPx()
                            val bodyW = innerRadius * 0.40f
                            val bodyH = innerRadius * 0.78f

                            rotate(degrees = rocketTilt, pivot = Offset(cx, rY)) {
                                // Dual-layer Plasma Thruster Flame
                                val outerFlame = Path().apply {
                                    moveTo(cx - bodyW * 0.38f, rY + bodyH * 0.40f)
                                    lineTo(cx, rY + bodyH * (0.42f + 0.46f * flameFlicker))
                                    lineTo(cx + bodyW * 0.38f, rY + bodyH * 0.40f)
                                    close()
                                }
                                drawPath(
                                    path = outerFlame,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6D00))
                                    )
                                )
                                val innerFlame = Path().apply {
                                    moveTo(cx - bodyW * 0.20f, rY + bodyH * 0.40f)
                                    lineTo(cx, rY + bodyH * (0.42f + 0.25f * flameFlicker))
                                    lineTo(cx + bodyW * 0.20f, rY + bodyH * 0.40f)
                                    close()
                                }
                                drawPath(innerFlame, color = Color(0xFFFFF8E1))

                                // Swept Fins
                                val leftFin = Path().apply {
                                    moveTo(cx - bodyW * 0.45f, rY + bodyH * 0.05f)
                                    lineTo(cx - bodyW * 1.15f, rY + bodyH * 0.46f)
                                    lineTo(cx - bodyW * 0.38f, rY + bodyH * 0.38f)
                                    close()
                                }
                                val rightFin = Path().apply {
                                    moveTo(cx + bodyW * 0.45f, rY + bodyH * 0.05f)
                                    lineTo(cx + bodyW * 1.15f, rY + bodyH * 0.46f)
                                    lineTo(cx + bodyW * 0.38f, rY + bodyH * 0.38f)
                                    close()
                                }
                                drawPath(leftFin, color = statusTint)
                                drawPath(rightFin, color = statusTint)

                                // Main Fuselage
                                val fuselage = Path().apply {
                                    moveTo(cx, rY - bodyH * 0.62f)
                                    cubicTo(
                                        cx + bodyW * 0.72f, rY - bodyH * 0.22f,
                                        cx + bodyW * 0.62f, rY + bodyH * 0.26f,
                                        cx + bodyW * 0.36f, rY + bodyH * 0.42f
                                    )
                                    lineTo(cx - bodyW * 0.36f, rY + bodyH * 0.42f)
                                    cubicTo(
                                        cx - bodyW * 0.62f, rY + bodyH * 0.26f,
                                        cx - bodyW * 0.72f, rY - bodyH * 0.22f,
                                        cx, rY - bodyH * 0.62f
                                    )
                                    close()
                                }
                                drawPath(
                                    path = fuselage,
                                    color = if (isDark) Color(0xFFF2F6FA) else Color(0xFFF0F4FA)
                                )
                                if (!isDark) {
                                    drawPath(
                                        path = fuselage,
                                        color = Color(0x2E64748B),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                }

                                // Nose Cone
                                val noseCone = Path().apply {
                                    moveTo(cx, rY - bodyH * 0.62f)
                                    cubicTo(
                                        cx + bodyW * 0.36f, rY - bodyH * 0.44f,
                                        cx + bodyW * 0.42f, rY - bodyH * 0.30f,
                                        cx + bodyW * 0.44f, rY - bodyH * 0.22f
                                    )
                                    lineTo(cx - bodyW * 0.44f, rY - bodyH * 0.22f)
                                    cubicTo(
                                        cx - bodyW * 0.42f, rY - bodyH * 0.30f,
                                        cx - bodyW * 0.36f, rY - bodyH * 0.44f,
                                        cx, rY - bodyH * 0.62f
                                    )
                                    close()
                                }
                                drawPath(noseCone, color = statusTint)

                                // Luminous Porthole Window
                                val windowCenter = Offset(cx, rY - bodyH * 0.02f)
                                drawCircle(
                                    color = if (isDark) Color(0xFF0F172A) else Color(0xFF1E293B),
                                    radius = bodyW * 0.33f,
                                    center = windowCenter
                                )
                                drawCircle(
                                    color = TactileCyan,
                                    radius = bodyW * 0.23f,
                                    center = windowCenter
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Main Headline
                    AnimatedContent(
                        targetState = state.headlineText,
                        transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(180))) },
                        label = "dialogHeadlineAnim"
                    ) { headline ->
                        Text(
                            text = headline,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Breathing Status Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusTint.copy(alpha = if (isDark) 0.18f else 0.10f))
                            .border(
                                1.dp,
                                statusTint.copy(alpha = 0.28f + 0.22f * breathPhase),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = state.statusBadgeText,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = statusTint,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Soft Eye-Friendly Description
                    AnimatedContent(
                        targetState = state.descriptionText,
                        transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(180))) },
                        label = "dialogDescAnim"
                    ) { desc ->
                        Text(
                            text = desc,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 13.sp,
                            color = subTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    // Smooth Animated Progress Wave Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = state.downloadProgress.coerceIn(0f, 1f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "dialogDownloadProgress"
                    )

                    AnimatedVisibility(
                        visible = state.isDownloading,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = statusTint,
                                trackColor = if (isDark) Color(0xFF1F2A3F) else Color(0xFFCED8E5),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val pct = (animatedProgress * 100).toInt()
                            val sizeText = if (state.totalMb > 0f) {
                                String.format(
                                    Locale.US,
                                    "%.1f / %.1f MB  •  %d%%",
                                    state.downloadedMb,
                                    state.totalMb,
                                    pct
                                )
                            } else {
                                String.format(Locale.US, "%.1f MB  •  %d%%", state.downloadedMb, pct)
                            }
                            Text(
                                text = sizeText,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusTint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Breathing Action CTA Pill
                    val buttonLabel = when {
                        state.needsInstallPermission -> "منح إذن التثبيت والمتابعة"
                        state.isDownloadedReadyToInstall -> "تثبيت الآن"
                        state.isDownloading -> "جاري التنزيل داخل التطبيق..."
                        state.isChecking -> "جاري الفحص..."
                        else -> "تحديث"
                    }

                    val ctaScale = if (!state.isDownloading && !state.isChecking) {
                        1f + 0.015f * breathPhase
                    } else 1f

                    Button(
                        onClick = {
                            CyberHapticsEngine.triggerTactileTick(context)
                            CyberSoundscapeEngine.playElectricSpark()
                            onUpdateClick()
                        },
                        enabled = !state.isDownloading && !state.isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = ctaScale
                                scaleY = ctaScale
                            }
                            .shadow(
                                elevation = if (state.isDownloading) 0.dp else (8 + 6 * breathPhase).dp,
                                shape = RoundedCornerShape(26.dp),
                                ambientColor = statusTint.copy(alpha = 0.35f),
                                spotColor = statusTint.copy(alpha = 0.45f)
                            )
                            .testTag("update_dialog_action_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = statusTint,
                            contentColor = Color.White,
                            disabledContainerColor = statusTint.copy(alpha = 0.42f),
                            disabledContentColor = Color.White.copy(alpha = 0.88f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (state.isDownloadedReadyToInstall) {
                                    Icons.Default.DownloadDone
                                } else {
                                    Icons.Default.RocketLaunch
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = buttonLabel,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = Color.White
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.isDownloading,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TextButton(
                            onClick = {
                                CyberHapticsEngine.triggerTactileTick(context)
                                onCancelDownload()
                            },
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .heightIn(min = 48.dp)
                                .testTag("update_dialog_cancel_button")
                        ) {
                            Text(
                                text = "إلغاء التنزيل",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = subTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

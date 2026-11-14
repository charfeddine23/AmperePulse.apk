package com.example.ui.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite
import java.util.Locale

/**
 * Compact, Non-Fullscreen In-App Update Modal Dialog
 * Styled to harmonize with Ampere Pulse's Liquid Glass & Neumorphic themes
 * while preserving the iconic Rocket Emblem, Arabic typography, and Pill Update CTA.
 */
@Composable
fun InAppUpdateDialog(
    state: InAppUpdateUiState,
    themeConfig: GlassThemeConfig,
    accentColor: Color,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit
) {
    if (!state.isDialogVisible) return

    BackHandler { onDismiss() }

    val context = LocalContext.current
    val isDark = themeConfig.isDarkMode
    val textColor = if (isDark) TextWhite else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF94A3B8) else TextSlate

    val cardBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFA141B2D), Color(0xFC0B101D))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFCFFFFFF), Color(0xFAF2F6FC))
        )
    }

    val borderColor = accentColor.copy(alpha = if (isDark) 0.45f else 0.30f)

    val infiniteTransition = rememberInfiniteTransition(label = "rocketUpdateAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringPulse"
    )
    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameFlicker"
    )
    val rocketBob by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketBob"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 370.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = accentColor.copy(alpha = 0.25f),
                    spotColor = accentColor.copy(alpha = 0.30f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(cardBrush)
                .border(1.3.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .testTag("in_app_update_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Close (X) on Top-Left matching the reference layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
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
                                if (isDark) Color(0x26FFFFFF) else Color(0x140F172A)
                            )
                            .testTag("update_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = subTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Glowing Concentric Rings + Custom Rocket Vector Canvas Emblem
                Box(
                    modifier = Modifier.size(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val outerRadius = (size.minDimension / 2f) * 0.96f * pulseScale
                        val innerRadius = (size.minDimension / 2f) * 0.72f

                        // Outer soft ambient glow ring
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = if (isDark) 0.28f else 0.16f),
                                    accentColor.copy(alpha = 0.04f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = outerRadius
                            ),
                            radius = outerRadius,
                            center = Offset(cx, cy)
                        )

                        // Inner solid dark/tinted circular core
                        drawCircle(
                            color = if (isDark) Color(0xFF101524) else Color(0xFFF1F5FB),
                            radius = innerRadius,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.85f),
                            radius = innerRadius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.6.dp.toPx())
                        )

                        // Floating space sparks around the rocket
                        val starPositions = listOf(
                            Offset(cx - innerRadius * 0.62f, cy - innerRadius * 0.34f) to Color.White.copy(alpha = if (isDark) 0.8f else 0.45f),
                            Offset(cx + innerRadius * 0.64f, cy - innerRadius * 0.42f) to TactileCyan,
                            Offset(cx - innerRadius * 0.56f, cy + innerRadius * 0.44f) to TactileAmber,
                            Offset(cx + innerRadius * 0.58f, cy + innerRadius * 0.36f) to accentColor
                        )
                        starPositions.forEach { (pos, starColor) ->
                            drawRect(
                                color = starColor,
                                topLeft = pos,
                                size = Size(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Rocket Geometry centered with subtle bobbing motion
                        val rY = cy + rocketBob.dp.toPx()
                        val bodyW = innerRadius * 0.40f
                        val bodyH = innerRadius * 0.78f

                        // Thruster Flame
                        val flamePath = Path().apply {
                            moveTo(cx - bodyW * 0.36f, rY + bodyH * 0.42f)
                            lineTo(cx, rY + bodyH * (0.42f + 0.44f * flameFlicker))
                            lineTo(cx + bodyW * 0.36f, rY + bodyH * 0.42f)
                            close()
                        }
                        drawPath(
                            path = flamePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), Color(0xFFFF6D00))
                            )
                        )

                        // Left & Right Swept Fins
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
                        drawPath(leftFin, color = accentColor)
                        drawPath(rightFin, color = accentColor)

                        // Main Fuselage Body
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
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFFFFFFFF)
                        )
                        if (!isDark) {
                            drawPath(
                                path = fuselage,
                                color = Color(0x3364748B),
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }

                        // Nose Cone Accent Tip
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
                        drawPath(noseCone, color = accentColor)

                        // Glowing Porthole Window
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

                Spacer(modifier = Modifier.height(16.dp))

                // Main Title ("تحديث جديد متاح")
                Text(
                    text = state.headlineText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Accent Subtitle / Badge ("تحديث جديد")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.10f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.statusBadgeText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description text
                Text(
                    text = state.descriptionText,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    color = subTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                // Live Download Progress Bar (shown during Play Flexible Update download)
                if (state.isDownloading) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { state.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        strokeCap = StrokeCap.Round
                    )
                    if (state.totalMb > 0f) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.1f / %.1f MB (%d%%)",
                                state.downloadedMb,
                                state.totalMb,
                                (state.downloadProgress * 100).toInt()
                            ),
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = subTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Rounded Full-Width Pill CTA Button ("تحديث")
                val buttonLabel = when {
                    state.isDownloadedReadyToInstall -> "تثبيت وإعادة التشغيل"
                    state.isDownloading -> "جاري التنزيل..."
                    else -> "تحديث"
                }

                Button(
                    onClick = {
                        CyberHapticsEngine.triggerTactileTick(context)
                        CyberSoundscapeEngine.playElectricSpark()
                        onUpdateClick()
                    },
                    enabled = !state.isDownloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("update_dialog_action_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White,
                        disabledContainerColor = accentColor.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.85f)
                    )
                ) {
                    Text(
                        text = buttonLabel,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

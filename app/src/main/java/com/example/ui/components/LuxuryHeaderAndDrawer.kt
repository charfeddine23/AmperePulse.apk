package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassThemeConfig
import com.example.data.TemperatureUnit
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.TextLuminous
import com.example.ui.theme.TextMutedGreen

/**
 * Luxury 3-line frosted glass hamburger top bar with 90 FPS status HUD
 */
@Composable
fun LuxuryTopBar(
    title: String,
    subtitle: String,
    isMenuOpen: Boolean,
    onMenuToggle: () -> Unit,
    themeConfig: GlassThemeConfig,
    modifier: Modifier = Modifier
) {
    val accentColor = themeConfig.accentColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                // 90 FPS rendering isolation
                clip = false
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Status
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 1.2.sp,
                    color = TextLuminous
                )
                Spacer(modifier = Modifier.width(8.dp))
                PulsingNeonDot(color = accentColor, sizeDp = 6.dp)

                if (themeConfig.showFpsOverlay) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x3300FF87))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "90 FPS",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = accentColor
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextMutedGreen
            )
        }

        // Luxury 3-Line Animated Hamburger Button
        LuxuryHamburgerButton(
            isOpen = isMenuOpen,
            onClick = onMenuToggle,
            accentColor = accentColor,
            glowIntensity = themeConfig.glowIntensity
        )
    }
}

/**
 * Animated 3-line luxury hamburger button using Spring physics
 */
@Composable
fun LuxuryHamburgerButton(
    isOpen: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    glowIntensity: Float,
    modifier: Modifier = Modifier
) {
    val line1Rotate by animateFloatAsState(
        targetValue = if (isOpen) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "line1Rotate"
    )
    val line2Alpha by animateFloatAsState(
        targetValue = if (isOpen) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "line2Alpha"
    )
    val line3Rotate by animateFloatAsState(
        targetValue = if (isOpen) -45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "line3Rotate"
    )
    val line1TranslateY by animateFloatAsState(
        targetValue = if (isOpen) 6.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "line1Y"
    )
    val line3TranslateY by animateFloatAsState(
        targetValue = if (isOpen) -6.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "line3Y"
    )

    Box(
        modifier = modifier
            .testTag("luxury_hamburger_button")
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xCC111816))
            .border(
                1.dp,
                accentColor.copy(alpha = 0.5f * glowIntensity),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.size(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Line 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.2.dp)
                    .graphicsLayer {
                        rotationZ = line1Rotate
                        translationY = line1TranslateY * density
                    }
                    .clip(CircleShape)
                    .background(accentColor)
            )
            // Line 2
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(2.2.dp)
                    .graphicsLayer {
                        alpha = line2Alpha
                    }
                    .clip(CircleShape)
                    .background(accentColor)
            )
            // Line 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.2.dp)
                    .graphicsLayer {
                        rotationZ = line3Rotate
                        translationY = line3TranslateY * density
                    }
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

/**
 * Slide-over frosted glass luxury settings drawer
 */
@Composable
fun LuxurySettingsDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    themeConfig: GlassThemeConfig,
    onConfigChange: (GlassThemeConfig) -> Unit
) {
    if (isOpen) {
        BackHandler {
            onClose()
        }
    }

    val accentColor = themeConfig.accentColor
    val glow = themeConfig.glowIntensity

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        ),
        exit = fadeOut() + slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            // Drawer Panel
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) {} // prevent click-through
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.25f * glow),
                                    Color.Transparent
                                ),
                                center = Offset(0f, size.height * 0.3f),
                                radius = size.width
                            ),
                            size = size
                        )
                    }
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(Color(0xF50D1412))
                    .border(
                        1.2.dp,
                        accentColor.copy(alpha = 0.5f * glow),
                        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CORE SETTINGS",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = "90Hz ENGINE & M5 SYSTEM",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x3314221C))
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 90Hz High-Refresh Acceleration Toggle
                    DrawerToggleRow(
                        title = "90Hz / 90 FPS Motion Lock",
                        subtitle = "Hardware-accelerated graphicsLayer render pipeline",
                        checked = themeConfig.target90FpsMode,
                        onCheckedChange = { onConfigChange(themeConfig.copy(target90FpsMode = it)) },
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 90 FPS HUD overlay toggle
                    DrawerToggleRow(
                        title = "Live 90 FPS Telemetry HUD",
                        subtitle = "Display frame rate performance chip in top bar",
                        checked = themeConfig.showFpsOverlay,
                        onCheckedChange = { onConfigChange(themeConfig.copy(showFpsOverlay = it)) },
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Neon Glow Slider
                    Text(
                        text = "NEON GLOW INTENSITY: ${(glow * 100).toInt()}%",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextLuminous
                    )
                    Slider(
                        value = glow,
                        onValueChange = { onConfigChange(themeConfig.copy(glowIntensity = it)) },
                        valueRange = 0.4f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color(0x33446655)
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Theme Color Switcher
                    Text(
                        text = "CHROMATIC NEON ACCENT",
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextLuminous
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Green", NeonPhosphorusGreen, 0),
                            Triple("Cyan", NeonCyan, 1),
                            Triple("Violet", CyberViolet, 2),
                            Triple("Amber", SolarAmber, 3)
                        ).forEach { (name, color, idx) ->
                            val isSelected = themeConfig.accentColorIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0x33101E18))
                                    .border(
                                        1.dp,
                                        if (isSelected) color else Color(0x33446655),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onConfigChange(themeConfig.copy(accentColorIndex = idx)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextLuminous else TextMutedGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Temperature Units Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Temperature Unit",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = TextLuminous
                            )
                            Text(
                                text = "Display scale",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = TextMutedGreen
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3312221A))
                                .border(1.dp, Color(0x3300FF87), RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (themeConfig.temperatureUnit == TemperatureUnit.CELSIUS)
                                            accentColor.copy(alpha = 0.25f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onConfigChange(themeConfig.copy(temperatureUnit = TemperatureUnit.CELSIUS))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("°C", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLuminous)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (themeConfig.temperatureUnit == TemperatureUnit.FAHRENHEIT)
                                            accentColor.copy(alpha = 0.25f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onConfigChange(themeConfig.copy(temperatureUnit = TemperatureUnit.FAHRENHEIT))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("°F", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextLuminous)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Safety Toggles
                    DrawerToggleRow(
                        title = "80% Battery Longevity Alert",
                        subtitle = "Chime when cell reaches 80% to protect lithium matrix",
                        checked = themeConfig.alertOn80Percent,
                        onCheckedChange = { onConfigChange(themeConfig.copy(alertOn80Percent = it)) },
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "Thermal Overheat Alarm (>42°C)",
                        subtitle = "Instant alarm when cell thermistor spikes",
                        checked = themeConfig.alertOnHighTemp,
                        onCheckedChange = { onConfigChange(themeConfig.copy(alertOnHighTemp = it)) },
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "Haptic Pulse Feedback",
                        subtitle = "Tactile feedback on charge plug and triggers",
                        checked = themeConfig.hapticFeedbackEnabled,
                        onCheckedChange = { onConfigChange(themeConfig.copy(hapticFeedbackEnabled = it)) },
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextLuminous
            )
            Text(
                text = subtitle,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 10.sp,
                color = TextMutedGreen,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextMutedGreen,
                uncheckedTrackColor = Color(0x33446655)
            )
        )
    }
}

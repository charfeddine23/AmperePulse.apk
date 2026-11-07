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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlassThemeConfig
import com.example.data.TemperatureUnit
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TactileViolet
import com.example.ui.theme.TextCharcoal
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextWhite

/**
 * Top Header with Luxury 3-line frosted glass hamburger menu,
 * Quick Dark Mode / Light Mode toggle, and 90 FPS status HUD.
 */
@Composable
fun LuxuryTopBar(
    title: String,
    subtitle: String,
    isMenuOpen: Boolean,
    onMenuToggle: () -> Unit,
    themeConfig: GlassThemeConfig,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = themeConfig.isDarkMode
    val accentColor = themeConfig.accentColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer { clip = false },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title & Status Subtitle
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.3).sp,
                    color = if (isDark) TextWhite else TextCharcoal
                )
                Spacer(modifier = Modifier.width(8.dp))
                PulsingNeonDot(color = accentColor, sizeDp = 6.dp)

                if (themeConfig.showFpsOverlay) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0x33448AFF) else Color(0x1F2962FF))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "90 FPS",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = accentColor
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextSlate
            )
        }

        // Actions: Quick Dark/Light Toggle + Luxury 3-Line Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dark Mode Quick Toggle Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF))
                    .border(
                        1.dp,
                        if (isDark) Color(0x33475569) else Color(0x4DE2E8F0),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleDarkMode() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Luxury 3-Line Animated Hamburger Button
            LuxuryHamburgerButton(
                isOpen = isMenuOpen,
                onClick = onMenuToggle,
                accentColor = accentColor,
                isDarkMode = isDark
            )
        }
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
    isDarkMode: Boolean,
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

    val buttonBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val borderColor = if (isDarkMode) Color(0x33475569) else Color(0x66E2E8F0)
    val lineColor = if (isDarkMode) TextWhite else TextCharcoal

    Box(
        modifier = modifier
            .testTag("luxury_hamburger_button")
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.size(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.2.dp)
                    .graphicsLayer {
                        rotationZ = line1Rotate
                        translationY = line1TranslateY * density
                    }
                    .clip(CircleShape)
                    .background(if (isOpen) accentColor else lineColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(2.2.dp)
                    .graphicsLayer { alpha = line2Alpha }
                    .clip(CircleShape)
                    .background(lineColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.2.dp)
                    .graphicsLayer {
                        rotationZ = line3Rotate
                        translationY = line3TranslateY * density
                    }
                    .clip(CircleShape)
                    .background(if (isOpen) accentColor else lineColor)
            )
        }
    }
}

/**
 * Slide-over frosted glass settings drawer with Neumorphic styling
 */
@Composable
fun LuxurySettingsDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    themeConfig: GlassThemeConfig,
    onConfigChange: (GlassThemeConfig) -> Unit
) {
    if (isOpen) {
        BackHandler { onClose() }
    }

    val isDark = themeConfig.isDarkMode
    val accentColor = themeConfig.accentColor

    val drawerBg = if (isDark) Color(0xF71E293B) else Color(0xFBF8FAFD)
    val cardBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val textColor = if (isDark) TextWhite else TextCharcoal

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
                .background(Color(0x55000000))
                .clickable { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(drawerBg)
                    .border(
                        1.dp,
                        if (isDark) Color(0x33475569) else Color(0x4DE2E8F0),
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
                                text = "APPEARANCE & SYSTEM",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.2).sp,
                                color = textColor
                            )
                            Text(
                                text = "Neumorphic & Soft Glass Engine",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = TextSlate
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dark Mode Toggle Row
                    DrawerToggleRow(
                        title = "Dark Slate Mode",
                        subtitle = "Switch between Light Neumorphism and Dark Slate glass",
                        checked = themeConfig.isDarkMode,
                        onCheckedChange = { onConfigChange(themeConfig.copy(isDarkMode = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 90Hz Motion Acceleration Toggle
                    DrawerToggleRow(
                        title = "90Hz / 90 FPS Motion Lock",
                        subtitle = "Hardware-accelerated graphicsLayer render pipeline",
                        checked = themeConfig.target90FpsMode,
                        onCheckedChange = { onConfigChange(themeConfig.copy(target90FpsMode = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "Live 90 FPS Telemetry HUD",
                        subtitle = "Show real-time frame rate badge in top bar",
                        checked = themeConfig.showFpsOverlay,
                        onCheckedChange = { onConfigChange(themeConfig.copy(showFpsOverlay = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Accent Palette Picker
                    Text(
                        text = "ACCENT COLOR PALETTE",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Blue", TactileRoyalBlue, 0),
                            Triple("Green", TactileGreen, 1),
                            Triple("Red", TactileCoralRed, 2),
                            Triple("Violet", TactileViolet, 3)
                        ).forEach { (name, color, idx) ->
                            val isSelected = themeConfig.accentColorIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.2f) else cardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) color else Color(0x3394A3B8),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onConfigChange(themeConfig.copy(accentColorIndex = idx)) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) color else TextSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Temperature Scale Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Temperature Unit",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = textColor
                            )
                            Text(
                                text = "Thermal readout scale",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = TextSlate
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
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
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("°C", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (themeConfig.temperatureUnit == TemperatureUnit.CELSIUS) accentColor else TextSlate)
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
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("°F", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (themeConfig.temperatureUnit == TemperatureUnit.FAHRENHEIT) accentColor else TextSlate)
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
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "Thermal Warning (>42°C)",
                        subtitle = "Instant alert when cell thermistor spikes",
                        checked = themeConfig.alertOnHighTemp,
                        onCheckedChange = { onConfigChange(themeConfig.copy(alertOnHighTemp = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "Haptic Pulse Feedback",
                        subtitle = "Tactile clicks on charge plug and button taps",
                        checked = themeConfig.hapticFeedbackEnabled,
                        onCheckedChange = { onConfigChange(themeConfig.copy(hapticFeedbackEnabled = it)) },
                        accentColor = accentColor,
                        textColor = textColor
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
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = textColor
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = TextSlate,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextSlate,
                uncheckedTrackColor = Color(0x3394A3B8)
            )
        )
    }
}

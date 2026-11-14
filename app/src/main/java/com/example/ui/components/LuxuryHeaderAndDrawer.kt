package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberHapticsEngine
import com.example.data.GlassThemeConfig
import com.example.data.TemperatureUnit
import com.example.data.update.InAppUpdateUiState
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
import java.util.Locale

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
    onOpenSkinVault: () -> Unit = {},
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

                // Dynamic glowing AppTier badge (FREE / VIP / PREMIUM) with fluid neon aura
                Spacer(modifier = Modifier.width(8.dp))
                val tier = themeConfig.appTier
                val tierColor = tier.color
                val infiniteTransition = rememberInfiniteTransition(label = "tierGlow")
                val pulseGlow by infiniteTransition.animateFloat(
                    initialValue = 0.55f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "tierPulse"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    tierColor.copy(alpha = if (isDark) 0.28f else 0.16f),
                                    tierColor.copy(alpha = if (isDark) 0.14f else 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    tierColor.copy(alpha = 0.85f * pulseGlow),
                                    tierColor.copy(alpha = 0.40f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onMenuToggle() }
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        .testTag("app_tier_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(tierColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tier.label,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp,
                            color = tierColor
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

            Spacer(modifier = Modifier.width(8.dp))

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
    onConfigChange: (GlassThemeConfig) -> Unit,
    onOpenSkinVault: () -> Unit = {},
    updateUiState: InAppUpdateUiState = InAppUpdateUiState(),
    onCheckUpdates: () -> Unit = {},
    onOpenUpdateDialog: () -> Unit = {},
    onTriggerUpdate: () -> Unit = {},
    onCancelDownload: () -> Unit = {}
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cyber Skin Vault Promo Banner (متجر الأقنعة والثيمات السيبرانية)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.10f))
                            .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable {
                                onClose()
                                onOpenSkinVault()
                            }
                            .padding(14.dp)
                            .testTag("drawer_cyber_vault_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(accentColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "CYBER SKIN VAULT",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "متجر الأقنعة والثيمات السيبرانية",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        color = accentColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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

                    // 120Hz / 120 FPS Ultra Motion Acceleration Toggle
                    DrawerToggleRow(
                        title = "120Hz / 120 FPS Ultra Fluidity Lock",
                        subtitle = "Hardware-accelerated 120 FPS ProMotion render pipeline with ultra-low latency",
                        checked = themeConfig.target120FpsMode,
                        onCheckedChange = { onConfigChange(themeConfig.copy(target120FpsMode = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Liquid Droplet Visibility Toggle (إخفاء القطرة والاكتفاء بالسحب)
                    DrawerToggleRow(
                        title = "Apple Liquid Glass Droplet (إظهار القطرة)",
                        subtitle = "تفعيل أو إخفاء قطرة الزجاج السائل في الشريط السفلي والاكتفاء بالسحب والتنقل المباشر",
                        checked = themeConfig.showLiquidDroplet,
                        onCheckedChange = { onConfigChange(themeConfig.copy(showLiquidDroplet = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    // App Tier Selector (Free / VIP / Premium)
                    Text(
                        text = "APPLICATION TIER (نوع التطبيق)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اختر صنف الحساب لإعطاء توهج ولون نيون ديناميكي في الشريط العلوي",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.5.sp,
                        color = TextSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.data.AppTier.values().forEach { tierOption ->
                            val isSelected = themeConfig.appTier == tierOption
                            val tColor = tierOption.color
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) tColor.copy(alpha = if (isDark) 0.28f else 0.16f) else cardBg
                                    )
                                    .border(
                                        1.2.dp,
                                        if (isSelected) tColor else Color(0x3394A3B8),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onConfigChange(themeConfig.copy(appTier = tierOption))
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = tierOption.label,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.6.sp,
                                        color = if (isSelected) tColor else TextSlate
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) tColor else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }

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

                    // Safety & Audio Toggles
                    DrawerToggleRow(
                        title = "Futuristic Soundscape Effects",
                        subtitle = "Play procedural audio cues on cable connect, sweeps, and state changes",
                        checked = themeConfig.soundEffectsEnabled,
                        onCheckedChange = { onConfigChange(themeConfig.copy(soundEffectsEnabled = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "24/7 Background Telemetry Service",
                        subtitle = "Run persistent foreground service to sample battery data in background/Doze",
                        checked = themeConfig.backgroundServiceEnabled,
                        onCheckedChange = { onConfigChange(themeConfig.copy(backgroundServiceEnabled = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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

                    Spacer(modifier = Modifier.height(14.dp))

                    DrawerToggleRow(
                        title = "التحديث التلقائي من داخل التطبيق",
                        subtitle = "فحص التحديثات تلقائياً من GitHub Releases (Ampere)",
                        checked = themeConfig.autoUpdateEnabled,
                        onCheckedChange = { onConfigChange(themeConfig.copy(autoUpdateEnabled = it)) },
                        accentColor = accentColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Luxurious, Breathing & Dynamic Auto-Update Status Container
                    LuxuriousAutoUpdateStatusCard(
                        updateUiState = updateUiState,
                        accentColor = accentColor,
                        isDark = isDark,
                        textColor = textColor,
                        onCheckUpdates = onCheckUpdates,
                        onTriggerUpdate = onTriggerUpdate,
                        onCancelDownload = onCancelDownload,
                        onOpenUpdateDialog = {
                            onClose()
                            onOpenUpdateDialog()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // CURRENT SENSOR CALIBRATION & NOISE FILTER ENGINE
                    Text(
                        text = "CURRENT SENSOR CALIBRATION (معايرة مقياس التيار)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adaptive EMA Noise Filter & Zero-Offset drift neutralizer across OEM chipsets",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.5.sp,
                        color = TextSlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) Color(0x33475569) else Color(0x66CBD5E1), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            // Multiplier Slider Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gain Multiplier",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = textColor
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2fx", themeConfig.currentMultiplier),
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor
                                )
                            }
                            androidx.compose.material3.Slider(
                                value = themeConfig.currentMultiplier,
                                onValueChange = {
                                    onConfigChange(themeConfig.copy(currentMultiplier = ((it * 100).toInt() / 100f)))
                                },
                                valueRange = 0.70f..1.40f,
                                steps = 14,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Zero Offset Slider Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Zero Offset Drift (معايرة الصفر)",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "${if (themeConfig.currentZeroOffsetMa >= 0) "+" else ""}${themeConfig.currentZeroOffsetMa.toInt()} mA",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor
                                )
                            }
                            androidx.compose.material3.Slider(
                                value = themeConfig.currentZeroOffsetMa,
                                onValueChange = {
                                    onConfigChange(themeConfig.copy(currentZeroOffsetMa = it.toInt().toFloat()))
                                },
                                valueRange = -150f..150f,
                                steps = 15,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Reset to Factory Baseline
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = if (isDark) 0.22f else 0.12f))
                                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            onConfigChange(
                                                themeConfig.copy(
                                                    currentMultiplier = 1.0f,
                                                    currentZeroOffsetMa = 0.0f
                                                )
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "RESET TO BASELINE",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

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

/**
 * Luxurious, breathing, smooth & dynamic In-App Auto-Update Status container.
 * Features breathing glass aura, sweeping specular sheen, live status orb,
 * version pill, spring progress indicator, and direct in-app download/install actions.
 */
@Composable
private fun LuxuriousAutoUpdateStatusCard(
    updateUiState: InAppUpdateUiState,
    accentColor: Color,
    isDark: Boolean,
    textColor: Color,
    onCheckUpdates: () -> Unit,
    onTriggerUpdate: () -> Unit,
    onCancelDownload: () -> Unit,
    onOpenUpdateDialog: () -> Unit
) {
    val context = LocalContext.current

    val targetStatusColor = when {
        updateUiState.isDownloadedReadyToInstall && !updateUiState.needsInstallPermission -> TactileGreen
        updateUiState.needsInstallPermission -> TactileAmber
        updateUiState.isDownloading -> accentColor
        updateUiState.isChecking -> TactileCyan
        updateUiState.updateAvailable -> TactileAmber
        else -> TactileGreen
    }
    val statusColor by animateColorAsState(
        targetValue = targetStatusColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "updateCardStatusColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "luxuriousUpdateCardAnim")
    val breathPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardBreathPhase"
    )
    val sheenSweep by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cardSheenSweep"
    )
    val animatedDownloadProgress by animateFloatAsState(
        targetValue = updateUiState.downloadProgress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "drawerDownloadProgress"
    )

    val cardBaseBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF10192D),
                Color(0xFF0A1120)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF3F7FD)
            )
        )
    }

    val breathingBorderBrush = Brush.linearGradient(
        colors = listOf(
            statusColor.copy(alpha = 0.28f + 0.42f * breathPhase),
            accentColor.copy(alpha = 0.16f + 0.28f * (1f - breathPhase)),
            statusColor.copy(alpha = 0.22f + 0.34f * breathPhase)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auto_update_status_card")
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.80f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBaseBrush)
            .drawBehind {
                // Soft breathing ambient orb glow inside top-right corner
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            statusColor.copy(alpha = (if (isDark) 0.22f else 0.12f) * (0.55f + 0.45f * breathPhase)),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.86f, size.height * 0.20f),
                        radius = size.width * (0.44f + 0.14f * breathPhase)
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.86f, size.height * 0.20f)
                )

                // Fluid diagonal specular sheen sweep
                val sweepStart = Offset(size.width * sheenSweep - size.width * 0.3f, 0f)
                val sweepEnd = Offset(size.width * sheenSweep + size.width * 0.25f, size.height)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.045f else 0.22f),
                            Color.Transparent
                        ),
                        start = sweepStart,
                        end = sweepEnd
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            }
            .border(1.35.dp, breathingBorderBrush, RoundedCornerShape(20.dp))
            .padding(15.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Header: Breathing Status Orb + Title + Version Capsule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Breathing multi-ring status icon container
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .graphicsLayer {
                                    val s = 0.88f + 0.24f * breathPhase
                                    scaleX = s
                                    scaleY = s
                                    alpha = 0.55f - 0.30f * breathPhase
                                }
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.35f))
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = if (isDark) 0.24f else 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (updateUiState.isDownloadedReadyToInstall) {
                                    Icons.Default.DownloadDone
                                } else {
                                    Icons.Default.SystemUpdate
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "حالة التحديث التلقائي",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when {
                                updateUiState.isDownloading -> "تنزيل مباشر داخل التطبيق"
                                updateUiState.isDownloadedReadyToInstall -> "الحزمة جاهزة للتثبيت الفوري"
                                updateUiState.isChecking -> "جاري الفحص السحابي..."
                                updateUiState.updateAvailable -> "تحديث جديد متوفر للتحميل"
                                else -> "تحديث سحابي آمن ومباشر"
                            },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dynamic Version Pill
                val versionLabel = if (updateUiState.updateAvailable && !updateUiState.availableVersionName.isNullOrBlank()) {
                    "v${updateUiState.currentVersionName} → v${updateUiState.availableVersionName}"
                } else {
                    "v${updateUiState.currentVersionName}"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = if (isDark) 0.18f else 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = versionLabel,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fluidly animated summary text
            AnimatedContent(
                targetState = updateUiState.lastCheckSummary,
                transitionSpec = { fadeIn(tween(220)).togetherWith(fadeOut(tween(180))) },
                label = "updateSummaryTransition"
            ) { summary ->
                Text(
                    text = summary,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else TextSlate,
                    lineHeight = 16.sp
                )
            }

            // Live In-App Download Progress Section inside the Drawer Card
            AnimatedVisibility(
                visible = updateUiState.isDownloading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { animatedDownloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pct = (animatedDownloadProgress * 100).toInt()
                        Text(
                            text = if (updateUiState.totalMb > 0f) {
                                String.format(
                                    Locale.US,
                                    "%.1f / %.1f MB",
                                    updateUiState.downloadedMb,
                                    updateUiState.totalMb
                                )
                            } else {
                                String.format(Locale.US, "%.1f MB", updateUiState.downloadedMb)
                            },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.5.sp,
                            color = TextSlate
                        )
                        Text(
                            text = "$pct%",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row with >= 48dp accessible hit targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val primaryLabel = when {
                    updateUiState.needsInstallPermission -> "منح الإذن والتثبيت"
                    updateUiState.isDownloadedReadyToInstall -> "تثبيت الآن"
                    updateUiState.isDownloading -> "إلغاء التنزيل"
                    updateUiState.isChecking -> "جاري الفحص..."
                    updateUiState.updateAvailable -> "تحديث داخلي"
                    else -> "فحص التحديثات"
                }

                val primaryBg = when {
                    updateUiState.isDownloading -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    updateUiState.isDownloadedReadyToInstall -> TactileGreen
                    updateUiState.updateAvailable -> statusColor
                    else -> accentColor
                }

                val primaryTextColor = if (updateUiState.isDownloading) {
                    if (isDark) TextWhite else TextCharcoal
                } else {
                    Color.White
                }

                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .heightIn(min = 48.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryBg)
                        .clickable(enabled = !updateUiState.isChecking) {
                            CyberHapticsEngine.triggerTactileTick(context)
                            when {
                                updateUiState.isDownloading -> onCancelDownload()
                                updateUiState.isDownloadedReadyToInstall || updateUiState.updateAvailable -> onTriggerUpdate()
                                else -> onCheckUpdates()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 11.dp)
                        .testTag("check_updates_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = primaryLabel,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .heightIn(min = 48.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = if (isDark) 0.16f else 0.10f))
                        .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .clickable {
                            CyberHapticsEngine.triggerTactileTick(context)
                            onOpenUpdateDialog()
                        }
                        .padding(horizontal = 8.dp, vertical = 11.dp)
                        .testTag("open_update_dialog_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "نافذة التحديث",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

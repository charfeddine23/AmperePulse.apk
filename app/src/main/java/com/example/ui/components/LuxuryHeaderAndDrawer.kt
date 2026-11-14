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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Modern, Simple, Dynamic & Eye-Friendly Slide-Over Luxury Settings Drawer
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

    val context = LocalContext.current
    val isDark = themeConfig.isDarkMode
    val accentColor = themeConfig.accentColor

    // Eye-friendly soft Nordic twilight (Dark) & warm porcelain mist (Light) palette
    val drawerBgBrush = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xF8151D2C), Color(0xFA101622)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFCF7F9FD), Color(0xFCEFF4FA)))
    }
    val surfaceCardColor = if (isDark) Color(0xFF1B2436) else Color(0xFFFFFFFF)
    val softTileColor = if (isDark) Color(0xFF212C42) else Color(0xFFF1F5FA)
    val subtleBorder = if (isDark) Color(0xFF2A364F) else Color(0xFFE3EAF3)
    val textColor = if (isDark) Color(0xFFE9EFF8) else TextCharcoal
    val subTextColor = if (isDark) Color(0xFF93A5BF) else TextSlate

    var expandAppearance by remember { mutableStateOf(false) }
    var expandProtection by remember { mutableStateOf(false) }
    var expandCalibration by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(220)) + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow)
        ),
        exit = fadeOut(tween(180)) + slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66060B14))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.86f)
                    .widthIn(max = 420.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* consume touches inside drawer */ }
                    .clip(RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp))
                    .background(drawerBgBrush)
                    .border(
                        1.dp,
                        subtleBorder,
                        RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp)
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Minimal Luxury Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الإعدادات",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = textColor
                            )
                            Text(
                                text = "تخصيص بسيط وسلس لتجربة Ampere Pulse",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = subTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Eye-Comfort Theme Switch Pill
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(surfaceCardColor)
                                    .border(1.dp, subtleBorder, CircleShape)
                                    .clickable {
                                        CyberHapticsEngine.triggerTactileTick(context)
                                        onConfigChange(themeConfig.copy(isDarkMode = !isDark))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "تبديل المظهر",
                                    tint = accentColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Accessible Close Button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(surfaceCardColor)
                                    .border(1.dp, subtleBorder, CircleShape)
                                    .clickable {
                                        CyberHapticsEngine.triggerTactileTick(context)
                                        onClose()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق الإعدادات",
                                    tint = subTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 2. Cyber Skin Vault Sleek Showcase Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = if (isDark) 0.22f else 0.12f),
                                        surfaceCardColor
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                accentColor.copy(alpha = if (isDark) 0.42f else 0.28f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                CyberHapticsEngine.triggerTactileTick(context)
                                onClose()
                                onOpenSkinVault()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("drawer_cyber_vault_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = if (isDark) 0.24f else 0.14f)),
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
                                        text = "متجر الثيمات الزجاجية",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = "أقنعة ومؤثرات بصرية مريحة للعين",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 10.5.sp,
                                        color = subTextColor
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // 3. Dynamic 2x2 Quick Glass Control Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickControlTile(
                                title = if (isDark) "الوضع الليلي" else "الوضع النهاري",
                                status = if (isDark) "داكن هادئ" else "ناعم مريح",
                                icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                active = isDark,
                                accentColor = accentColor,
                                surfaceColor = surfaceCardColor,
                                softColor = softTileColor,
                                borderColor = subtleBorder,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onConfigChange(themeConfig.copy(isDarkMode = !isDark))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickControlTile(
                                title = "سلاسة 120Hz",
                                status = if (themeConfig.target120FpsMode) "مفعّل" else "قياسي",
                                icon = Icons.Default.Speed,
                                active = themeConfig.target120FpsMode,
                                accentColor = TactileCyan,
                                surfaceColor = surfaceCardColor,
                                softColor = softTileColor,
                                borderColor = subtleBorder,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onConfigChange(themeConfig.copy(target120FpsMode = !themeConfig.target120FpsMode))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickControlTile(
                                title = "الأصوات",
                                status = if (themeConfig.soundEffectsEnabled) "مفعّلة" else "صامت",
                                icon = Icons.Default.GraphicEq,
                                active = themeConfig.soundEffectsEnabled,
                                accentColor = TactileViolet,
                                surfaceColor = surfaceCardColor,
                                softColor = softTileColor,
                                borderColor = subtleBorder,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onConfigChange(themeConfig.copy(soundEffectsEnabled = !themeConfig.soundEffectsEnabled))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickControlTile(
                                title = "الاهتزاز",
                                status = if (themeConfig.hapticFeedbackEnabled) "تفاعلي" else "معطّل",
                                icon = Icons.Default.Vibration,
                                active = themeConfig.hapticFeedbackEnabled,
                                accentColor = TactileGreen,
                                surfaceColor = surfaceCardColor,
                                softColor = softTileColor,
                                borderColor = subtleBorder,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    CyberHapticsEngine.triggerTactileTick(context)
                                    onConfigChange(themeConfig.copy(hapticFeedbackEnabled = !themeConfig.hapticFeedbackEnabled))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Luxurious, Breathing & Dynamic Auto-Update Card (with Auto Toggle integrated cleanly)
                    LuxuriousAutoUpdateStatusCard(
                        updateUiState = updateUiState,
                        autoUpdateEnabled = themeConfig.autoUpdateEnabled,
                        onToggleAutoUpdate = { enabled ->
                            CyberHapticsEngine.triggerTactileTick(context)
                            onConfigChange(themeConfig.copy(autoUpdateEnabled = enabled))
                        },
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

                    // 5. Expandable Accordion: Appearance & Identity
                    SettingsAccordionCard(
                        title = "الألوان والهوية",
                        summary = "${themeConfig.appTier.label} • ${if (themeConfig.temperatureUnit == TemperatureUnit.CELSIUS) "°C" else "°F"}",
                        icon = Icons.Default.Palette,
                        expanded = expandAppearance,
                        onToggle = {
                            CyberHapticsEngine.triggerTactileTick(context)
                            expandAppearance = !expandAppearance
                        },
                        accentColor = accentColor,
                        surfaceColor = surfaceCardColor,
                        borderColor = subtleBorder,
                        textColor = textColor,
                        subTextColor = subTextColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Soft Eye-Friendly Color Orbs
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "لون التمييز المريح للعين",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = subTextColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(
                                        Triple("أزرق هادئ", Color(0xFF4F7DF3), 0),
                                        Triple("زمردي", Color(0xFF1BBF75), 1),
                                        Triple("مرجاني", Color(0xFFF06449), 2),
                                        Triple("بنفسجي", Color(0xFF8B6EF3), 3)
                                    ).forEach { (label, color, idx) ->
                                        val isSelected = themeConfig.accentColorIndex == idx && themeConfig.customAccentHex == null
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    CyberHapticsEngine.triggerTactileTick(context)
                                                    onConfigChange(
                                                        themeConfig.copy(
                                                            accentColorIndex = idx,
                                                            customAccentHex = null
                                                        )
                                                    )
                                                }
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) textColor else Color.White.copy(alpha = 0.25f),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = label,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 10.sp,
                                                color = if (isSelected) textColor else subTextColor,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            // Tier Pills
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "فئة الحساب والشارة",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = subTextColor
                                )
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
                                                .heightIn(min = 44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) tColor.copy(alpha = if (isDark) 0.22f else 0.14f)
                                                    else softTileColor
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) tColor.copy(alpha = 0.7f) else subtleBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    CyberHapticsEngine.triggerTactileTick(context)
                                                    onConfigChange(themeConfig.copy(appTier = tierOption))
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tierOption.label,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = if (isSelected) tColor else subTextColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Temperature Scale Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "وحدة الحرارة",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = textColor
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(softTileColor)
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        TemperatureUnit.CELSIUS to "°C",
                                        TemperatureUnit.FAHRENHEIT to "°F"
                                    ).forEach { (unit, label) ->
                                        val selected = themeConfig.temperatureUnit == unit
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) accentColor.copy(alpha = 0.22f) else Color.Transparent)
                                                .clickable {
                                                    CyberHapticsEngine.triggerTactileTick(context)
                                                    onConfigChange(themeConfig.copy(temperatureUnit = unit))
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (selected) accentColor else subTextColor
                                            )
                                        }
                                    }
                                }
                            }

                            // Liquid Droplet Toggle
                            DrawerToggleRow(
                                title = "قطرة الزجاج السائل",
                                subtitle = "إظهار المؤشر العائم في شريط التنقل السفلي",
                                checked = themeConfig.showLiquidDroplet,
                                onCheckedChange = { onConfigChange(themeConfig.copy(showLiquidDroplet = it)) },
                                accentColor = accentColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                        }
                    }

                    // 6. Expandable Accordion: Protection & Background Alerts
                    val activeProtectionCount = listOf(
                        themeConfig.backgroundServiceEnabled,
                        themeConfig.alertOn80Percent,
                        themeConfig.alertOnHighTemp
                    ).count { it }

                    SettingsAccordionCard(
                        title = "الحماية والتنبيهات",
                        summary = "$activeProtectionCount من 3 مفعّلة",
                        icon = Icons.Default.Shield,
                        expanded = expandProtection,
                        onToggle = {
                            CyberHapticsEngine.triggerTactileTick(context)
                            expandProtection = !expandProtection
                        },
                        accentColor = TactileGreen,
                        surfaceColor = surfaceCardColor,
                        borderColor = subtleBorder,
                        textColor = textColor,
                        subTextColor = subTextColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DrawerToggleRow(
                                title = "خدمة القياس في الخلفية 24/7",
                                subtitle = "رصد استقرار الشحن والبطارية باستمرار",
                                checked = themeConfig.backgroundServiceEnabled,
                                onCheckedChange = { onConfigChange(themeConfig.copy(backgroundServiceEnabled = it)) },
                                accentColor = accentColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                            DrawerToggleRow(
                                title = "تنبيه حماية البطارية 80%",
                                subtitle = "إشعار لطيف لإطالة عمر خلايا الليثيوم",
                                checked = themeConfig.alertOn80Percent,
                                onCheckedChange = { onConfigChange(themeConfig.copy(alertOn80Percent = it)) },
                                accentColor = accentColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                            DrawerToggleRow(
                                title = "تحذير الحرارة المرتفعة (>42°C)",
                                subtitle = "تنبيه فوري عند ارتفاع حرارة المستشعر",
                                checked = themeConfig.alertOnHighTemp,
                                onCheckedChange = { onConfigChange(themeConfig.copy(alertOnHighTemp = it)) },
                                accentColor = accentColor,
                                textColor = textColor,
                                subTextColor = subTextColor
                            )
                        }
                    }

                    // 7. Expandable Accordion: Sensor Calibration
                    SettingsAccordionCard(
                        title = "معايرة مقياس التيار",
                        summary = "${String.format(Locale.US, "%.2fx", themeConfig.currentMultiplier)} • ${themeConfig.currentZeroOffsetMa.toInt()} mA",
                        icon = Icons.Default.Tune,
                        expanded = expandCalibration,
                        onToggle = {
                            CyberHapticsEngine.triggerTactileTick(context)
                            expandCalibration = !expandCalibration
                        },
                        accentColor = TactileCyan,
                        surfaceColor = surfaceCardColor,
                        borderColor = subtleBorder,
                        textColor = textColor,
                        subTextColor = subTextColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "معامل الضبط (Gain)",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = textColor
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2fx", themeConfig.currentMultiplier),
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = accentColor
                                )
                            }
                            Slider(
                                value = themeConfig.currentMultiplier,
                                onValueChange = {
                                    onConfigChange(themeConfig.copy(currentMultiplier = ((it * 100).toInt() / 100f)))
                                },
                                valueRange = 0.70f..1.40f,
                                steps = 14,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = softTileColor
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "معايرة الصفر (Offset)",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontWeight = FontWeight.Medium,
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
                            Slider(
                                value = themeConfig.currentZeroOffsetMa,
                                onValueChange = {
                                    onConfigChange(themeConfig.copy(currentZeroOffsetMa = it.toInt().toFloat()))
                                },
                                valueRange = -150f..150f,
                                steps = 15,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = softTileColor
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.10f))
                                        .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            CyberHapticsEngine.triggerTactileTick(context)
                                            onConfigChange(
                                                themeConfig.copy(
                                                    currentMultiplier = 1.0f,
                                                    currentZeroOffsetMa = 0.0f
                                                )
                                            )
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "إعادة ضبط المصنع",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickControlTile(
    title: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    accentColor: Color,
    surfaceColor: Color,
    softColor: Color,
    borderColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) accentColor.copy(alpha = 0.14f) else surfaceColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "quickTileBg"
    )
    val strokeColor by animateColorAsState(
        targetValue = if (active) accentColor.copy(alpha = 0.55f) else borderColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "quickTileBorder"
    )

    Box(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.1.dp, strokeColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (active) accentColor.copy(alpha = 0.20f) else softColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (active) accentColor else subTextColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (active) accentColor else subTextColor.copy(alpha = 0.35f))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = status,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = if (active) accentColor else subTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsAccordionCard(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    surfaceColor: Color,
    borderColor: Color,
    textColor: Color,
    subTextColor: Color,
    content: @Composable () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "accordionArrow"
    )
    val activeBorder by animateColorAsState(
        targetValue = if (expanded) accentColor.copy(alpha = 0.45f) else borderColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "accordionBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.1.dp, activeBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = summary,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.5.sp,
                        color = subTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = subTextColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                content()
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
    textColor: Color,
    subTextColor: Color = TextSlate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp,
                color = textColor
            )
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = subTextColor,
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
                uncheckedThumbColor = subTextColor,
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
    autoUpdateEnabled: Boolean,
    onToggleAutoUpdate: (Boolean) -> Unit,
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
                Color(0xFF1B2436),
                Color(0xFF141C2B)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF4F7FC)
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

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact Auto-Update Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (autoUpdateEnabled) {
                                    accentColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                                } else {
                                    if (isDark) Color(0xFF232F45) else Color(0xFFEAF0F7)
                                }
                            )
                            .border(
                                1.dp,
                                if (autoUpdateEnabled) accentColor.copy(alpha = 0.50f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleAutoUpdate(!autoUpdateEnabled) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (autoUpdateEnabled) "تلقائي" else "يدوي",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (autoUpdateEnabled) accentColor else TextSlate
                        )
                    }

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

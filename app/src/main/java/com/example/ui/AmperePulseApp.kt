package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.AdaptiveLiquidDropletSwipeContainer
import com.example.ui.components.CyberSkinVaultDialog
import com.example.ui.components.FloatingGlassNavBar
import com.example.ui.components.LuxurySettingsDrawer
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.components.thermalLiquidGlassBackground
import com.example.ui.screens.Page1LivingBatteryScreen
import com.example.ui.screens.Page2StatisticsScreen
import com.example.ui.screens.Page3BatterySohScreen
import com.example.ui.screens.Page4CableDnaScreen
import com.example.ui.screens.Page5MySpaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeumorphBgDark
import com.example.ui.theme.NeumorphBgLight

@Composable
fun AmperePulseApp(viewModel: AmperePulseViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    val voltageHistory by viewModel.voltageHistory.collectAsState()
    val currentHistory by viewModel.currentHistory.collectAsState()
    val wattageHistory by viewModel.wattageHistory.collectAsState()
    val temperatureHistory by viewModel.temperatureHistory.collectAsState()
    val sessionLogs by viewModel.sessionLogs.collectAsState()

    val streakData by viewModel.streakData.collectAsState()
    val eggData by viewModel.eggData.collectAsState()
    val prizeRoadData by viewModel.prizeRoadData.collectAsState()
    val techCrew by viewModel.techCrew.collectAsState()
    val cableResult by viewModel.cableResult.collectAsState()
    val sohPrediction by viewModel.sohPrediction.collectAsState()
    val hourlyStats by viewModel.hourlyStats.collectAsState()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val themeConfig by viewModel.themeConfig.collectAsState()
    val isSettingsOpen by viewModel.isSettingsMenuOpen.collectAsState()
    val isSkinVaultOpen by viewModel.isSkinVaultOpen.collectAsState()
    val isAutoSensorMode by viewModel.isAutoSensorMode.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()

    val isDark = themeConfig.isDarkMode

    // Handle back button: first close settings drawer, then return to BATTERY page
    BackHandler(enabled = isSettingsOpen) {
        viewModel.toggleSettingsMenu(false)
    }

    if (!isSettingsOpen && selectedTab != NavigationTab.BATTERY) {
        BackHandler {
            viewModel.selectTab(NavigationTab.BATTERY)
        }
    }

    // Dynamic state-based primary accent color
    val activeAccent = when (selectedTab) {
        NavigationTab.BATTERY -> {
            when {
                telemetry.temperatureC >= 40f -> Color(0xFFFF3D00)
                telemetry.temperatureC >= 38f -> Color(0xFFFF9100)
                telemetry.temperatureC <= 34f -> Color(0xFF00B0FF)
                else -> telemetry.livingState.neonPrimary
            }
        }
        else -> themeConfig.accentColor
    }

    MyApplicationTheme(
        isDarkMode = isDark,
        accentColor = activeAccent
    ) {
        val rootBg = if (isDark) NeumorphBgDark else NeumorphBgLight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (selectedTab == NavigationTab.BATTERY) {
                        Modifier.thermalLiquidGlassBackground(
                            temperatureC = telemetry.temperatureC,
                            isDarkMode = isDark,
                            glowIntensity = themeConfig.glowIntensity,
                            criticalTempThreshold = 40.0f,
                            normalTempThreshold = 34.0f
                        )
                    } else {
                        Modifier
                            .background(rootBg)
                            .graphicsLayer { clip = false }
                            .drawBehind {
                                // Subtle ambient aura tint
                                val auraColor = activeAccent
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = if (isDark) {
                                            listOf(
                                                auraColor.copy(alpha = 0.08f * themeConfig.glowIntensity),
                                                auraColor.copy(alpha = 0.02f),
                                                Color.Transparent
                                            )
                                        } else {
                                            listOf(
                                                auraColor.copy(alpha = 0.05f * themeConfig.glowIntensity),
                                                Color.Transparent
                                            )
                                        },
                                        center = Offset(size.width * 0.5f, size.height * 0.16f),
                                        radius = size.width * 0.85f
                                    )
                                )
                            }
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Luxury 3-Line Top Settings Header Bar with Dark Mode Toggle & Cyber Vault Button
                LuxuryTopBar(
                    title = "AMPERE PULSE",
                    subtitle = when (selectedTab) {
                        NavigationTab.BATTERY -> "PAGE 1: LIVING BATTERY MATRIX"
                        NavigationTab.STATISTICS -> "PAGE 2: HOURLY BATTERY STATISTICS (ROOM WAL)"
                        NavigationTab.BATTERY_SOH -> "PAGE 3: BATTERY SOH & CYCLE PREDICTOR"
                        NavigationTab.CABLE_DNA -> "PAGE 4: CABLE DNA & CHARGER ANALYZER"
                        NavigationTab.MY_SPACE -> "PAGE 5: TELEMETRY SPACE & LOG"
                    },
                    isMenuOpen = isSettingsOpen,
                    onMenuToggle = { viewModel.toggleSettingsMenu(!isSettingsOpen) },
                    themeConfig = themeConfig,
                    onToggleDarkMode = {
                        viewModel.updateThemeConfig(themeConfig.copy(isDarkMode = !themeConfig.isDarkMode))
                    }
                )

                // 5-Page Content with High-Performance Spring Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AdaptiveLiquidDropletSwipeContainer(
                        modifier = Modifier.fillMaxSize(),
                        accentColor = activeAccent,
                        isDarkMode = isDark,
                        onSwipeNext = {
                            val tabs = NavigationTab.values()
                            val nextIndex = (selectedTab.ordinal + 1).coerceAtMost(tabs.size - 1)
                            viewModel.selectTab(tabs[nextIndex])
                        },
                        onSwipePrevious = {
                            val tabs = NavigationTab.values()
                            val prevIndex = (selectedTab.ordinal - 1).coerceAtLeast(0)
                            viewModel.selectTab(tabs[prevIndex])
                        }
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                val isMovingForward = targetState.ordinal > initialState.ordinal
                                (slideInHorizontally(
                                    initialOffsetX = { if (isMovingForward) it else -it },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))).togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { if (isMovingForward) -it else it },
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    ) + scaleOut(
                                        targetScale = 0.96f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    ) + fadeOut()
                                )
                            },
                            label = "90HzAdaptiveDropletPageTransition"
                        ) { tab ->
                            when (tab) {
                                NavigationTab.BATTERY -> Page1LivingBatteryScreen(
                                    telemetry = telemetry,
                                    voltageHistory = voltageHistory,
                                    currentHistory = currentHistory,
                                    themeConfig = themeConfig,
                                    prizeRoadData = prizeRoadData,
                                    isAutoSensorMode = isAutoSensorMode,
                                    onSelectLivingState = { viewModel.selectLivingState(it) },
                                    onNavigateToPrizeRoad = { viewModel.selectTab(NavigationTab.BATTERY_SOH) },
                                    onToggleThermalTest = { viewModel.toggleThermalSimulation() }
                                )
                                NavigationTab.STATISTICS -> Page2StatisticsScreen(
                                    telemetry = telemetry,
                                    hourlyStats = hourlyStats,
                                    themeConfig = themeConfig,
                                    onResetOrSeedStats = { viewModel.seedOrResetHourlyStats() }
                                )
                                NavigationTab.BATTERY_SOH -> Page3BatterySohScreen(
                                    telemetry = telemetry,
                                    themeConfig = themeConfig,
                                    sohPrediction = sohPrediction,
                                    onRecalibrateSoh = { viewModel.recalibrateSohPrediction() }
                                )
                                NavigationTab.CABLE_DNA -> Page4CableDnaScreen(
                                    telemetry = telemetry,
                                    themeConfig = themeConfig,
                                    cableResult = cableResult,
                                    onRunDiagnosticSweep = { viewModel.runCableDiagnosticSweep(it) }
                                )
                                NavigationTab.MY_SPACE -> Page5MySpaceScreen(
                                    telemetry = telemetry,
                                    voltageHistory = voltageHistory,
                                    currentHistory = currentHistory,
                                    wattageHistory = wattageHistory,
                                    temperatureHistory = temperatureHistory,
                                    sessionLogs = sessionLogs,
                                    themeConfig = themeConfig,
                                    userAccount = userAccount,
                                    onUpdateOwnerName = { viewModel.updateOwnerName(it) },
                                    onAddVitalLog = { title, desc, tag, type ->
                                        viewModel.addManualLog(title, desc, tag, type)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Floating Navigation Bar anchored at bottom
            FloatingGlassNavBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                accentColor = activeAccent,
                isDarkMode = isDark,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Luxury Slide-Over Settings Drawer
            LuxurySettingsDrawer(
                isOpen = isSettingsOpen,
                onClose = { viewModel.toggleSettingsMenu(false) },
                themeConfig = themeConfig,
                onConfigChange = { viewModel.updateThemeConfig(it) },
                onOpenSkinVault = { viewModel.toggleSkinVault(true) }
            )

            // Cyber Skin (Liquid Glass Vault) Modal Dialog - متجر الأقنعة والثيمات السيبرانية
            CyberSkinVaultDialog(
                isOpen = isSkinVaultOpen,
                onClose = { viewModel.toggleSkinVault(false) },
                themeConfig = themeConfig,
                onEquipSkin = { skin ->
                    viewModel.equipCyberSkin(skin)
                }
            )
        }
    }
}

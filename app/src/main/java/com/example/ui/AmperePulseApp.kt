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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import com.example.data.LivingBatteryState
import com.example.ui.components.FloatingGlassNavBar
import com.example.ui.components.LuxurySettingsDrawer
import com.example.ui.components.LuxuryTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.screens.Page1LivingBatteryScreen
import com.example.ui.screens.Page2StreaksAndEggScreen
import com.example.ui.screens.Page3PrizeRoadScreen
import com.example.ui.screens.Page4TechCrewScreen
import com.example.ui.screens.Page5MySpaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianOled

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

    val selectedTab by viewModel.selectedTab.collectAsState()
    val themeConfig by viewModel.themeConfig.collectAsState()
    val isSettingsOpen by viewModel.isSettingsMenuOpen.collectAsState()

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
        NavigationTab.BATTERY -> telemetry.livingState.neonPrimary
        else -> themeConfig.accentColor
    }

    MyApplicationTheme(accentColor = activeAccent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianOled)
                // 90 FPS Hardware Render Pipeline
                .graphicsLayer {
                    clip = false
                }
                .drawBehind {
                    // Luminous ambient aura
                    val auraColor = activeAccent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = 0.09f * themeConfig.glowIntensity),
                                auraColor.copy(alpha = 0.02f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.22f),
                            radius = size.width * 0.85f
                        )
                    )
                    // Secondary subtle cyan aura at bottom right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1200E5FF),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.8f),
                            radius = size.width * 0.6f
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Luxury 3-Line Top Settings Header Bar
                LuxuryTopBar(
                    title = "AMPERE PULSE",
                    subtitle = when (selectedTab) {
                        NavigationTab.BATTERY -> "PAGE 1: LIVING BATTERY MATRIX"
                        NavigationTab.STREAKS -> "PAGE 2: STREAKS & CYBER INCUBATOR"
                        NavigationTab.PRIZE_ROAD -> "PAGE 3: 1,000-LEVEL PRIZE ROAD"
                        NavigationTab.TECH_CREW -> "PAGE 4: TECH CREW DECK"
                        NavigationTab.MY_SPACE -> "PAGE 5: TELEMETRY SPACE & LOG"
                    },
                    isMenuOpen = isSettingsOpen,
                    onMenuToggle = { viewModel.toggleSettingsMenu(!isSettingsOpen) },
                    themeConfig = themeConfig
                )

                // 5-Page Content with High-Performance Spring Transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                            ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { if (isMovingForward) -it else it },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ) + fadeOut()
                            )
                        },
                        label = "90HzPageTransition"
                    ) { tab ->
                        when (tab) {
                            NavigationTab.BATTERY -> Page1LivingBatteryScreen(
                                telemetry = telemetry,
                                voltageHistory = voltageHistory,
                                currentHistory = currentHistory,
                                themeConfig = themeConfig,
                                onSelectLivingState = { viewModel.selectLivingState(it) },
                                onNavigateToPage = { /* handled by tab selection */ }
                            )
                            NavigationTab.STREAKS -> Page2StreaksAndEggScreen(
                                streakData = streakData,
                                eggData = eggData,
                                themeConfig = themeConfig,
                                onHatchEgg = { viewModel.hatchEgg() },
                                onRepairShield = { viewModel.repairShield() }
                            )
                            NavigationTab.PRIZE_ROAD -> Page3PrizeRoadScreen(
                                prizeRoadData = prizeRoadData,
                                themeConfig = themeConfig,
                                onClaimMilestone = { viewModel.claimMilestone(it) },
                                onAddSimulatedMah = { viewModel.addCumulativeMah(it) }
                            )
                            NavigationTab.TECH_CREW -> Page4TechCrewScreen(
                                crewMembers = techCrew,
                                themeConfig = themeConfig,
                                onTriggerAbility = { viewModel.triggerCrewAbility(it) }
                            )
                            NavigationTab.MY_SPACE -> Page5MySpaceScreen(
                                telemetry = telemetry,
                                voltageHistory = voltageHistory,
                                currentHistory = currentHistory,
                                wattageHistory = wattageHistory,
                                temperatureHistory = temperatureHistory,
                                sessionLogs = sessionLogs,
                                themeConfig = themeConfig,
                                onAddVitalLog = { title, desc, tag, type ->
                                    viewModel.addManualLog(title, desc, tag, type)
                                }
                            )
                        }
                    }
                }
            }

            // Sleek Floating Navigation Bar anchored at bottom
            FloatingGlassNavBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                accentColor = activeAccent,
                glowIntensity = themeConfig.glowIntensity,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Luxury Slide-Over Settings Drawer
            LuxurySettingsDrawer(
                isOpen = isSettingsOpen,
                onClose = { viewModel.toggleSettingsMenu(false) },
                themeConfig = themeConfig,
                onConfigChange = { viewModel.updateThemeConfig(it) }
            )
        }
    }
}

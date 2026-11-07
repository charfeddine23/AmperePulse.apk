package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BatteryMonitorRepository
import com.example.data.BatteryTelemetry
import com.example.data.CyberEggData
import com.example.data.GlassThemeConfig
import com.example.data.HatchedCompanion
import com.example.data.LivingBatteryState
import com.example.data.PrizeRoadData
import com.example.data.SessionLog
import com.example.data.StreakData
import com.example.data.TechCrewMember
import com.example.data.TelemetryPoint
import com.example.ui.components.NavigationTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AmperePulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BatteryMonitorRepository(
        context = application.applicationContext,
        scope = viewModelScope
    )

    val telemetry: StateFlow<BatteryTelemetry> = repository.telemetry
    val voltageHistory: StateFlow<List<TelemetryPoint>> = repository.voltageHistory
    val currentHistory: StateFlow<List<TelemetryPoint>> = repository.currentHistory
    val wattageHistory: StateFlow<List<TelemetryPoint>> = repository.wattageHistory
    val temperatureHistory: StateFlow<List<TelemetryPoint>> = repository.temperatureHistory
    val sessionLogs: StateFlow<List<SessionLog>> = repository.sessionLogs

    // 5-Page Architecture Flows
    val streakData: StateFlow<StreakData> = repository.streakData
    val eggData: StateFlow<CyberEggData> = repository.eggData
    val prizeRoadData: StateFlow<PrizeRoadData> = repository.prizeRoadData
    val techCrew: StateFlow<List<TechCrewMember>> = repository.techCrew

    private val _selectedTab = MutableStateFlow(NavigationTab.BATTERY)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _themeConfig = MutableStateFlow(GlassThemeConfig())
    val themeConfig: StateFlow<GlassThemeConfig> = _themeConfig.asStateFlow()

    private val _isSettingsMenuOpen = MutableStateFlow(false)
    val isSettingsMenuOpen: StateFlow<Boolean> = _isSettingsMenuOpen.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun updateThemeConfig(config: GlassThemeConfig) {
        _themeConfig.value = config
    }

    fun toggleSettingsMenu(open: Boolean) {
        _isSettingsMenuOpen.value = open
    }

    fun selectLivingState(state: LivingBatteryState) {
        repository.setLivingBatteryState(state)
    }

    fun triggerCrewAbility(crewId: String) {
        repository.triggerCrewAbility(crewId)
    }

    fun hatchEgg(): HatchedCompanion {
        return repository.hatchEgg()
    }

    fun addCumulativeMah(addedMah: Long) {
        repository.addCumulativeMah(addedMah)
    }

    fun claimMilestone(level: Int) {
        repository.claimMilestone(level)
    }

    fun repairShield() {
        repository.repairShield()
    }

    fun triggerEmergencyCooling() {
        repository.setLivingBatteryState(LivingBatteryState.COOLING)
        repository.addManualSessionLog(
            title = "CRYO COOLANT ENGAGED",
            desc = "Thermal runaway suppressed. Active liquid nitrogen cooling deployed to protect lithium matrix.",
            statTag = "-8°C Shock",
            type = "temp"
        )
    }

    fun addManualLog(title: String, desc: String, statTag: String, type: String) {
        repository.addManualSessionLog(title, desc, statTag, type)
    }
}


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
    val isAutoSensorMode: StateFlow<Boolean> = repository.isAutoSensorMode
    val userAccount: StateFlow<com.example.data.UserAccount> = repository.userAccount
    val cableResult: StateFlow<com.example.data.CableDiagnosticResult> = repository.cableResult
    val sohPrediction: StateFlow<com.example.data.BatterySohPrediction> = repository.sohPrediction
    val hourlyStats: StateFlow<List<com.example.data.local.HourlyBatteryStatEntity>> = repository.hourlyStats

    private val _selectedTab = MutableStateFlow(NavigationTab.BATTERY)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val prefsManager = com.example.data.local.SettingsPreferencesManager(application.applicationContext)

    private val _themeConfig = MutableStateFlow(prefsManager.loadThemeConfig())
    val themeConfig: StateFlow<GlassThemeConfig> = _themeConfig.asStateFlow()

    private val _isSettingsMenuOpen = MutableStateFlow(false)
    val isSettingsMenuOpen: StateFlow<Boolean> = _isSettingsMenuOpen.asStateFlow()

    private val _isSkinVaultOpen = MutableStateFlow(false)
    val isSkinVaultOpen: StateFlow<Boolean> = _isSkinVaultOpen.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    init {
        // Apply persisted sound preferences on startup
        com.example.audio.CyberSoundscapeEngine.isEnabled = _themeConfig.value.soundEffectsEnabled
        repository.updateCurrentCalibration(
            multiplier = _themeConfig.value.currentMultiplier,
            zeroOffsetMa = _themeConfig.value.currentZeroOffsetMa
        )
    }

    fun updateThemeConfig(config: GlassThemeConfig) {
        _themeConfig.value = config
        prefsManager.saveThemeConfig(config)
        com.example.audio.CyberSoundscapeEngine.isEnabled = config.soundEffectsEnabled
        repository.updateCurrentCalibration(
            multiplier = config.currentMultiplier,
            zeroOffsetMa = config.currentZeroOffsetMa
        )
        if (config.backgroundServiceEnabled) {
            com.example.service.TelemetryForegroundService.startService(getApplication())
        } else {
            com.example.service.TelemetryForegroundService.stopService(getApplication())
        }
    }

    fun toggleSettingsMenu(open: Boolean) {
        _isSettingsMenuOpen.value = open
    }

    fun toggleSkinVault(open: Boolean) {
        _isSkinVaultOpen.value = open
    }

    fun equipCyberSkin(skin: com.example.data.CyberSkin) {
        val argb = (skin.primaryColor.value shr 32).toLong()
        // Retain the current user's dark/light mode preference unchanged
        val updatedConfig = _themeConfig.value.copy(
            activeSkinId = skin.id,
            customAccentHex = argb,
            glowIntensity = skin.glowIntensity
            // isDarkMode remains completely unchanged as requested
        )
        _themeConfig.value = updatedConfig
        prefsManager.saveThemeConfig(updatedConfig)

        repository.addManualSessionLog(
            title = "Cyber Skin Equipped: ${skin.name}",
            desc = "Liquid Glass holographic aesthetic applied. Aura: ${skin.tag} (${skin.codename}).",
            statTag = skin.tag,
            type = "check"
        )
    }

    fun seedOrResetHourlyStats() {
        repository.seedOrResetHourlyStats()
    }

    fun selectLivingState(state: LivingBatteryState?) {
        repository.setLivingBatteryState(state)
    }

    fun setAutoSensorMode(auto: Boolean) {
        repository.setAutoSensorMode(auto)
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

    fun addManualLog(title: String, desc: String, statTag: String, type: String) {
        repository.addManualSessionLog(title, desc, statTag, type)
    }

    fun updateOwnerName(name: String) {
        repository.updateOwnerName(name)
    }

    fun toggleCloudSync(enabled: Boolean) {
        repository.toggleCloudSync(enabled)
    }

    fun runCableDiagnosticSweep(tierOverride: com.example.data.CableDnaTier? = null) {
        repository.runCableDiagnosticSweep(tierOverride)
    }

    fun recalibrateSohPrediction() {
        repository.recalibrateSohPrediction()
    }

    fun toggleThermalSimulation(forceOverheat: Boolean? = null) {
        repository.toggleThermalSimulation(forceOverheat)
    }
}

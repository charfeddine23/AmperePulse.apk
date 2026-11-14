package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.data.AppTier
import com.example.data.GlassThemeConfig
import com.example.data.TemperatureUnit

/**
 * SharedPreferences storage for user settings & theme preferences.
 * Ensures that dark mode, active cyber skin, app tier, accent color,
 * alert triggers, and preferences persist across app swipe-away and restarts.
 */
class SettingsPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadThemeConfig(): GlassThemeConfig {
        val isDarkMode = prefs.getBoolean(KEY_IS_DARK_MODE, false) // default Light Neumorphism
        val glowIntensity = prefs.getFloat(KEY_GLOW_INTENSITY, 0.75f)
        val accentColorIndex = prefs.getInt(KEY_ACCENT_COLOR_INDEX, 0)
        val activeSkinId = prefs.getString(KEY_ACTIVE_SKIN_ID, "obsidian_cyber") ?: "obsidian_cyber"
        val customAccentHex = if (prefs.contains(KEY_CUSTOM_ACCENT_HEX)) prefs.getLong(KEY_CUSTOM_ACCENT_HEX, 0L) else null
        
        val isPremiumUnlocked = prefs.getBoolean(KEY_IS_PREMIUM_UNLOCKED, false)
        val appTierString = prefs.getString(KEY_APP_TIER, AppTier.FREE.name) ?: AppTier.FREE.name
        val parsedTier = try {
            AppTier.valueOf(appTierString)
        } catch (_: Exception) {
            AppTier.FREE
        }
        val appTier = if (parsedTier == AppTier.PREMIUM && !isPremiumUnlocked) {
            AppTier.FREE
        } else {
            parsedTier
        }

        val glassBlurAmount = prefs.getFloat(KEY_GLASS_BLUR, 0.85f)
        val ambientLiquidWaves = prefs.getBoolean(KEY_AMBIENT_WAVES, true)
        val tempUnitStr = prefs.getString(KEY_TEMP_UNIT, TemperatureUnit.CELSIUS.name) ?: TemperatureUnit.CELSIUS.name
        val temperatureUnit = try {
            TemperatureUnit.valueOf(tempUnitStr)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }

        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val bgServiceEnabled = prefs.getBoolean(KEY_BG_SERVICE_ENABLED, true)
        val alertOn80 = prefs.getBoolean(KEY_ALERT_80, true)
        val alertOnHighTemp = prefs.getBoolean(KEY_ALERT_HIGH_TEMP, true)
        val highTempThreshold = prefs.getFloat(KEY_HIGH_TEMP_THRESHOLD, 42.0f)
        val hapticEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        val target90Fps = prefs.getBoolean(KEY_TARGET_90FPS, true)
        val showFpsOverlay = prefs.getBoolean(KEY_SHOW_FPS, true)

        return GlassThemeConfig(
            isDarkMode = isDarkMode,
            glowIntensity = glowIntensity,
            accentColorIndex = accentColorIndex,
            activeSkinId = activeSkinId,
            customAccentHex = customAccentHex,
            appTier = appTier,
            isPremiumUnlocked = isPremiumUnlocked,
            glassBlurAmount = glassBlurAmount,
            ambientLiquidWaves = ambientLiquidWaves,
            temperatureUnit = temperatureUnit,
            samplingIntervalMs = 1000L,
            soundEffectsEnabled = soundEnabled,
            backgroundServiceEnabled = bgServiceEnabled,
            alertOn80Percent = alertOn80,
            alertOnHighTemp = alertOnHighTemp,
            highTempThresholdC = highTempThreshold,
            hapticFeedbackEnabled = hapticEnabled,
            target120FpsMode = prefs.getBoolean(KEY_TARGET_120FPS, true),
            showFpsOverlay = showFpsOverlay,
            showLiquidDroplet = prefs.getBoolean(KEY_SHOW_LIQUID_DROPLET, true),
            autoUpdateEnabled = prefs.getBoolean(KEY_AUTO_UPDATE_ENABLED, true),
            currentMultiplier = prefs.getFloat(KEY_CURRENT_MULTIPLIER, 1.0f),
            currentZeroOffsetMa = prefs.getFloat(KEY_CURRENT_ZERO_OFFSET, 0.0f)
        )
    }

    fun saveThemeConfig(config: GlassThemeConfig) {
        prefs.edit().apply {
            putBoolean(KEY_IS_DARK_MODE, config.isDarkMode)
            putFloat(KEY_GLOW_INTENSITY, config.glowIntensity)
            putInt(KEY_ACCENT_COLOR_INDEX, config.accentColorIndex)
            putString(KEY_ACTIVE_SKIN_ID, config.activeSkinId)
            if (config.customAccentHex != null) {
                putLong(KEY_CUSTOM_ACCENT_HEX, config.customAccentHex)
            } else {
                remove(KEY_CUSTOM_ACCENT_HEX)
            }
            val safeTier = if (config.appTier == AppTier.PREMIUM && !config.isPremiumUnlocked) {
                AppTier.FREE
            } else {
                config.appTier
            }
            putString(KEY_APP_TIER, safeTier.name)
            putBoolean(KEY_IS_PREMIUM_UNLOCKED, config.isPremiumUnlocked)
            putFloat(KEY_GLASS_BLUR, config.glassBlurAmount)
            putBoolean(KEY_AMBIENT_WAVES, config.ambientLiquidWaves)
            putString(KEY_TEMP_UNIT, config.temperatureUnit.name)
            putBoolean(KEY_SOUND_ENABLED, config.soundEffectsEnabled)
            putBoolean(KEY_BG_SERVICE_ENABLED, config.backgroundServiceEnabled)
            putBoolean(KEY_ALERT_80, config.alertOn80Percent)
            putBoolean(KEY_ALERT_HIGH_TEMP, config.alertOnHighTemp)
            putFloat(KEY_HIGH_TEMP_THRESHOLD, config.highTempThresholdC)
            putBoolean(KEY_HAPTIC_ENABLED, config.hapticFeedbackEnabled)
            putBoolean(KEY_TARGET_90FPS, config.target120FpsMode)
            putBoolean(KEY_TARGET_120FPS, config.target120FpsMode)
            putBoolean(KEY_SHOW_FPS, config.showFpsOverlay)
            putBoolean(KEY_SHOW_LIQUID_DROPLET, config.showLiquidDroplet)
            putBoolean(KEY_AUTO_UPDATE_ENABLED, config.autoUpdateEnabled)
            putFloat(KEY_CURRENT_MULTIPLIER, config.currentMultiplier)
            putFloat(KEY_CURRENT_ZERO_OFFSET, config.currentZeroOffsetMa)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "ampere_pulse_preferences"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_GLOW_INTENSITY = "glow_intensity"
        private const val KEY_ACCENT_COLOR_INDEX = "accent_color_index"
        private const val KEY_ACTIVE_SKIN_ID = "active_skin_id"
        private const val KEY_CUSTOM_ACCENT_HEX = "custom_accent_hex"
        private const val KEY_APP_TIER = "app_tier"
        private const val KEY_IS_PREMIUM_UNLOCKED = "is_premium_unlocked"
        private const val KEY_GLASS_BLUR = "glass_blur"
        private const val KEY_AMBIENT_WAVES = "ambient_waves"
        private const val KEY_TEMP_UNIT = "temp_unit"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_BG_SERVICE_ENABLED = "bg_service_enabled"
        private const val KEY_ALERT_80 = "alert_80"
        private const val KEY_ALERT_HIGH_TEMP = "alert_high_temp"
        private const val KEY_HIGH_TEMP_THRESHOLD = "high_temp_threshold"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_TARGET_90FPS = "target_90fps"
        private const val KEY_TARGET_120FPS = "target_120fps"
        private const val KEY_SHOW_FPS = "show_fps"
        private const val KEY_SHOW_LIQUID_DROPLET = "show_liquid_droplet"
        private const val KEY_AUTO_UPDATE_ENABLED = "auto_update_enabled"
        private const val KEY_CURRENT_MULTIPLIER = "current_multiplier"
        private const val KEY_CURRENT_ZERO_OFFSET = "current_zero_offset"
    }
}

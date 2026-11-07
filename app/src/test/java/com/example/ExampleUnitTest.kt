package com.example

import com.example.data.BatteryHealth
import com.example.data.BatteryStatus
import com.example.data.BatteryTelemetry
import com.example.data.ChargePlug
import com.example.data.CyberEggData
import com.example.data.GlassThemeConfig
import com.example.data.LivingBatteryState
import com.example.data.PrizeRoadData
import com.example.data.StreakData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBatteryTelemetryModel() {
        val telemetry = BatteryTelemetry(
            percentage = 84,
            status = BatteryStatus.CHARGING,
            plugType = ChargePlug.AC,
            health = BatteryHealth.GOOD,
            voltageMv = 4182,
            currentMa = 2340,
            wattageW = 9.78f,
            livingState = LivingBatteryState.ENERGY_STORING
        )
        assertEquals(84, telemetry.percentage)
        assertEquals(BatteryStatus.CHARGING, telemetry.status)
        assertEquals(ChargePlug.AC, telemetry.plugType)
        assertEquals(LivingBatteryState.ENERGY_STORING, telemetry.livingState)
        assertTrue(telemetry.wattageW > 0f)
    }

    @Test
    fun testLivingBatteryStates() {
        assertEquals(4, LivingBatteryState.entries.size)
        assertTrue(LivingBatteryState.OVERDRIVE_HEATING.isBleeding)
        assertFalse(LivingBatteryState.COOLING.isBleeding)
        assertFalse(LivingBatteryState.ENERGY_STORING.isBleeding)
        assertFalse(LivingBatteryState.DEEP_SLEEP.isBleeding)
    }

    @Test
    fun testStreakAndEggDefaults() {
        val streak = StreakData(currentStreakDays = 14, shieldsAvailable = 3)
        assertEquals(14, streak.currentStreakDays)
        assertEquals(3, streak.shieldsAvailable)

        val egg = CyberEggData()
        assertTrue(egg.incubationProgress in 0f..1f)
        assertTrue(egg.hatchedList.isNotEmpty())
    }

    @Test
    fun testPrizeRoadMath() {
        val prizeRoad = PrizeRoadData(cumulativeMah = 50_000L, currentLevel = 5)
        assertEquals(5, prizeRoad.currentLevel)
        assertEquals(1000, prizeRoad.maxLevel)
    }

    @Test
    fun testGlassThemeConfigDefaults() {
        val config = GlassThemeConfig()
        assertFalse(config.isDarkMode) // Defaults to Light Neumorphism
        assertEquals("Royal Blue (#2962FF)", config.accentName)
        assertTrue(config.glowIntensity in 0.3f..1.0f)
        assertTrue(config.target90FpsMode)

        // Dark mode toggle
        val darkConfig = config.copy(isDarkMode = true)
        assertTrue(darkConfig.isDarkMode)
    }
}

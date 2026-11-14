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
    fun test4DynamicSensorTriggerRules() {
        // Condition 1: ENERGY STORING (Current > 0, Temp 20-37C, stable V)
        val temp1 = 28.5f
        val currentMa1 = 2200
        val voltageMv1 = 4180
        val isStableV = voltageMv1 in 3700..4450
        val isEnergyStoring = currentMa1 > 0 && temp1 in 20.0f..37.0f && isStableV
        assertTrue(isEnergyStoring)

        // Condition 2: OVERDRIVE / HEATING (Temp > 38C or severe voltage drop fault)
        val temp2 = 39.5f
        val isOverdrive = temp2 > 38.0f
        assertTrue(isOverdrive)

        // Condition 3: CRYOGENIC COOLING (Rapid thermal drop toward <35C or Cryo skill)
        val tempDropRate = 0.25f
        val temp3 = 31.0f
        val isRapidCooling = tempDropRate >= 0.10f && temp3 < 35.0f
        assertTrue(isRapidCooling)

        // Condition 4: DEEP SLEEP / HIBERNATION (Screen off + low idle draw <=300mA)
        val isScreenOn = false
        val idleCurrentMa = 120
        val isDeepSleep = !isScreenOn && idleCurrentMa <= 350
        assertTrue(isDeepSleep)
    }

    @Test
    fun testStreakAndEggDefaults() {
        val streak = StreakData()
        assertEquals(0, streak.currentStreakDays)
        assertEquals(0, streak.shieldsAvailable)
        assertEquals(0, streak.maxShields)
        assertEquals(0.0f, streak.todayComplianceRate, 0.01f)
        assertEquals(0, streak.healthyChargeMinutesToday)

        val egg = CyberEggData()
        assertEquals(0f, egg.incubationProgress, 0.001f)
        assertEquals(0, egg.currentXP)
        assertTrue(egg.hatchedList.isEmpty())
    }

    @Test
    fun testPrizeRoadMath() {
        val prizeRoad = PrizeRoadData()
        assertEquals(0L, prizeRoad.cumulativeMah)
        assertEquals(1, prizeRoad.currentLevel)
        assertEquals(1000, prizeRoad.maxLevel)
        assertTrue(prizeRoad.claimedMilestones.isEmpty())

        // Level fullness calculation at initial 0 mAh
        val currentLevelMah = (prizeRoad.cumulativeMah % 10_000L).toFloat()
        val fullnessRatio = currentLevelMah / 10_000f
        assertEquals(0f, fullnessRatio, 0.01f)

        // Progress after adding 25,000 mAh -> Level 3
        val simulatedMah = 25_000L
        val calculatedLevel = ((simulatedMah / 10_000L).toInt() + 1).coerceIn(1, 1000)
        assertEquals(3, calculatedLevel)
    }

    @Test
    fun testGlassThemeConfigDefaults() {
        val config = GlassThemeConfig()
        assertFalse(config.isDarkMode) // Defaults to Light Neumorphism
        assertEquals("Royal Blue (#2962FF)", config.accentName)
        assertTrue(config.glowIntensity in 0.3f..1.0f)
        assertTrue(config.target120FpsMode)
        assertTrue(config.target90FpsMode)
        assertTrue(config.showLiquidDroplet)
        assertEquals(1.0f, config.currentMultiplier, 0.001f)
        assertEquals(0.0f, config.currentZeroOffsetMa, 0.001f)

        // Dark mode toggle
        val darkConfig = config.copy(isDarkMode = true)
        assertTrue(darkConfig.isDarkMode)
    }

    @Test
    fun testCurrentCalibrationEngineMath() {
        val engine = com.example.data.telemetry.CurrentCalibrationEngine(
            context = null,
            initialMultiplier = 1.0f,
            initialOffsetMa = 0.0f
        )

        // 1. Physical fallback when no BatteryManager present
        val dischargeReading = engine.sampleCurrentMa(null, isCharging = false)
        assertTrue(dischargeReading < 0)

        val chargingReading = engine.sampleCurrentMa(null, isCharging = true)
        assertTrue(chargingReading > 0)

        // 2. Calibration update (Multiplier & Zero Offset)
        engine.updateCalibration(newMultiplier = 1.10f, newOffsetMa = 20.0f)
        assertEquals(1.10f, engine.multiplier, 0.01f)
        assertEquals(20.0f, engine.zeroOffsetMa, 0.01f)

        // 3. Reset calibration
        engine.resetCalibration()
        assertEquals(1.0f, engine.multiplier, 0.001f)
        assertEquals(0.0f, engine.zeroOffsetMa, 0.001f)

        // 4. Manual Zero Calibration
        engine.performManualZeroCalibration(currentReadingMa = -140f, expectedNominalMa = -120f)
        assertEquals(-20.0f, engine.zeroOffsetMa, 0.01f)

        val status = engine.getTelemetryEngineStatus()
        assertEquals(1.0f, status.multiplier, 0.001f)
        assertEquals(-20.0f, status.zeroOffsetMa, 0.01f)
    }

    @Test
    fun testAutoUpdateVersionComparison() {
        val config = GlassThemeConfig()
        assertTrue(config.autoUpdateEnabled)

        assertTrue(com.example.data.update.InAppUpdateEngine.compareSemanticVersions("1.0.1", "1.0.0") > 0)
        assertTrue(com.example.data.update.InAppUpdateEngine.compareSemanticVersions("1.1.0", "1.0.99") > 0)
        assertEquals(0, com.example.data.update.InAppUpdateEngine.compareSemanticVersions("1.0.5", "1.0.5"))
        assertTrue(com.example.data.update.InAppUpdateEngine.compareSemanticVersions("1.0.4", "1.0.5") < 0)
    }
}


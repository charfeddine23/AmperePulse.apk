package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class BatteryMonitorRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private var previousTempC: Float = 28.5f
    private val _isAutoSensorMode = MutableStateFlow(true)
    val isAutoSensorMode: StateFlow<Boolean> = _isAutoSensorMode.asStateFlow()

    private val _telemetry = MutableStateFlow(BatteryTelemetry())
    val telemetry: StateFlow<BatteryTelemetry> = _telemetry.asStateFlow()

    private val _voltageHistory = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val voltageHistory: StateFlow<List<TelemetryPoint>> = _voltageHistory.asStateFlow()

    private val _currentHistory = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val currentHistory: StateFlow<List<TelemetryPoint>> = _currentHistory.asStateFlow()

    private val _wattageHistory = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val wattageHistory: StateFlow<List<TelemetryPoint>> = _wattageHistory.asStateFlow()

    private val _temperatureHistory = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val temperatureHistory: StateFlow<List<TelemetryPoint>> = _temperatureHistory.asStateFlow()

    private val _sessionLogs = MutableStateFlow<List<SessionLog>>(emptyList())
    val sessionLogs: StateFlow<List<SessionLog>> = _sessionLogs.asStateFlow()

    // 5-Page System States
    private val _streakData = MutableStateFlow(StreakData())
    val streakData: StateFlow<StreakData> = _streakData.asStateFlow()

    private val _eggData = MutableStateFlow(CyberEggData())
    val eggData: StateFlow<CyberEggData> = _eggData.asStateFlow()

    private val _prizeRoadData = MutableStateFlow(PrizeRoadData())
    val prizeRoadData: StateFlow<PrizeRoadData> = _prizeRoadData.asStateFlow()

    private val _techCrew = MutableStateFlow<List<TechCrewMember>>(emptyList())
    val techCrew: StateFlow<List<TechCrewMember>> = _techCrew.asStateFlow()

    private var manualStateOverride: LivingBatteryState? = null
    private var simulatedTempDelta = 0.0f
    private var simTick = 0L

    init {
        initHistorySeed()
        initSessionLogsSeed()
        initTechCrewSeed()
        startPolling()
    }

    private fun initTechCrewSeed() {
        _techCrew.value = listOf(
            TechCrewMember(
                id = "crew_1",
                name = "Kael Vance",
                title = "Cryo Firefighter & Thermal Chief",
                avatarLetter = "❄",
                primaryColor = Color(0xFF00F0FF),
                abilityName = "Cryo Nitrogen Blast",
                abilityDescription = "Discharges active liquid nitrogen to suppress battery core thermals by -4°C.",
                cooldownTotalSec = 45,
                unlockLevel = 2,
                isUnlocked = false // Earned at Level 2!
            ),
            TechCrewMember(
                id = "crew_2",
                name = "Dr. Sophia Reed",
                title = "Volt Architect & Quantum Tuner",
                avatarLetter = "⚡",
                primaryColor = Color(0xFF00FF87),
                abilityName = "Harmonic Voltage Lock",
                abilityDescription = "Eliminates terminal voltage ripple and injects +250 XP into battery matrix.",
                cooldownTotalSec = 60,
                unlockLevel = 10,
                isUnlocked = false // Earned at Level 10!
            ),
            TechCrewMember(
                id = "crew_3",
                name = "Ghost Unit Siphon",
                title = "Zombie Drain Purger",
                avatarLetter = "⚙",
                primaryColor = Color(0xFFA855F7),
                abilityName = "Background Siphon Flush",
                abilityDescription = "Suppresses phantom background daemon draws, reducing active current load.",
                cooldownTotalSec = 50,
                unlockLevel = 25,
                isUnlocked = false // Earned at Level 25!
            ),
            TechCrewMember(
                id = "crew_4",
                name = "Aegis Matrix",
                title = "Cyber Shield Guardian",
                avatarLetter = "🛡",
                primaryColor = Color(0xFFFFB800),
                abilityName = "Restore Health Shield",
                abilityDescription = "Rebuilds depleted battery health shields to guard against thermal spikes.",
                cooldownTotalSec = 90,
                unlockLevel = 50,
                isUnlocked = false // Earned at Level 50!
            )
        )
    }

    private fun initHistorySeed() {
        val now = System.currentTimeMillis()
        val vList = mutableListOf<TelemetryPoint>()
        val cList = mutableListOf<TelemetryPoint>()
        val wList = mutableListOf<TelemetryPoint>()
        val tList = mutableListOf<TelemetryPoint>()

        for (i in 30 downTo 0) {
            val t = now - (i * 1500L)
            val v = 4120f + (sin(i * 0.4) * 45).toFloat() + Random.nextFloat() * 12f
            val c = 2150f + (sin(i * 0.3) * 180).toFloat() + Random.nextFloat() * 40f
            val w = (v / 1000f) * (c / 1000f)
            val temp = 28.2f + (i * 0.03f) + (sin(i * 0.2) * 0.3).toFloat()

            vList.add(TelemetryPoint(t, v))
            cList.add(TelemetryPoint(t, c))
            wList.add(TelemetryPoint(t, w))
            tList.add(TelemetryPoint(t, temp))
        }

        _voltageHistory.value = vList
        _currentHistory.value = cList
        _wattageHistory.value = wList
        _temperatureHistory.value = tList
    }

    private fun initSessionLogsSeed() {
        _sessionLogs.value = listOf(
            SessionLog(
                id = "init_0",
                title = "Hardware Monitor Initialized",
                timestampFormatted = "Just now",
                description = "Sensors calibrated from 0. Level 1 Cadet profile active. Advance on the Prize Road to earn shields and specialist crew.",
                statTag = "LVL 1 • 0 mAh",
                type = "check"
            )
        )
    }

    private fun startPolling() {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                readBatteryState()
                updateEggAndPrizeProgress()
                updateCrewCooldowns()
                delay(1000L)
            }
        }
    }

    private fun readBatteryState() {
        simTick++
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val rawLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel / scale.toFloat()) * 100).toInt()
        } else {
            84
        }

        val statusInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL

        val status = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            else -> if (isCharging) BatteryStatus.CHARGING else BatteryStatus.DISCHARGING
        }

        val chargePlugInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val plugType = when (chargePlugInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargePlug.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargePlug.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargePlug.WIRELESS
            else -> if (isCharging) ChargePlug.AC else ChargePlug.NONE
        }

        val rawVoltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4150) ?: 4150
        val baseVoltageMv = if (rawVoltage in 1..15) rawVoltage * 1000 else rawVoltage

        val voltageMv = if (baseVoltageMv > 1000) {
            (baseVoltageMv + (sin(simTick * 0.3) * 15).toInt())
        } else {
            4180 + (sin(simTick * 0.3) * 20).toInt()
        }

        val rawTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285) ?: 285
        val baseTempC = (rawTemp / 10.0f) + simulatedTempDelta
        val temperatureC = if (baseTempC in 10f..65f) {
            baseTempC + (sin(simTick * 0.15) * 0.15f).toFloat()
        } else {
            (28.5f + simulatedTempDelta + (sin(simTick * 0.15) * 0.2f)).toFloat()
        }

        var currentNowUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        var currentMa = currentNowUa / 1000

        if (abs(currentMa) < 10) {
            val baseCurrent = if (isCharging) 2250 else -430
            val oscillation = (sin(simTick * 0.45) * 120).toInt() + Random.nextInt(-25, 25)
            currentMa = baseCurrent + oscillation
        } else if (!isCharging && currentMa > 0) {
            currentMa = -currentMa
        }

        val wattageW = (voltageMv / 1000.0f) * (abs(currentMa) / 1000.0f)

        // Real-Time Sensor Telemetry Checks
        val isScreenInteractive = powerManager?.isInteractive ?: true
        val tempDropRate = previousTempC - temperatureC
        previousTempC = temperatureC

        // 1. ENERGY STORING (Phosphor Green #00C853)
        // Condition: Charging active (Current > 0 mA) AND Temp between 20°C and 37°C AND stable voltage (3700 - 4450 mV).
        val isStableVoltage = voltageMv in 3700..4450
        val isEnergyStoringCondition = (isCharging || currentMa > 0) &&
                (temperatureC in 20.0f..37.0f) &&
                isStableVoltage

        // 2. OVERDRIVE / HEATING (Warning Red #FF3D00)
        // Condition: Temp > 38°C OR high internal resistance/voltage drop (poor cable/charger).
        val isVoltageDropFault = isCharging && (voltageMv < 3700 || (currentMa > 2000 && voltageMv < 3800))
        val isOverdriveHeatingCondition = temperatureC > 38.0f || isVoltageDropFault

        // 3. CRYOGENIC COOLING (Ice Blue #00B0FF)
        // Condition: Rapid thermal drop (Temp decreasing toward <35°C) OR Cryo skill deployed.
        val isCryoSkillActive = _techCrew.value.any { it.id == "crew_1" && it.isActiveAbilityRunning } || simulatedTempDelta < -1.0f
        val isRapidThermalDrop = (tempDropRate >= 0.10f && temperatureC < 35.0f)
        val isCoolingCondition = isRapidThermalDrop || isCryoSkillActive

        // 4. DEEP SLEEP / HIBERNATION (Night Purple #7C4DFF)
        // Condition: Screen OFF + Low idle current draw (Current <= 300 mA) OR Overnight 80-85% trickling.
        val isLowIdleCurrent = abs(currentMa) <= 350
        val isOvernightTrickle = isCharging && percent in 80..85 && abs(currentMa) <= 400
        val isDeepSleepCondition = (!isScreenInteractive && isLowIdleCurrent) || isOvernightTrickle || (!isCharging && isLowIdleCurrent && temperatureC < 32f)

        // Auto-Trigger Resolution based on real-time sensor hierarchy
        val sensorEvaluatedState = when {
            isOverdriveHeatingCondition -> LivingBatteryState.OVERDRIVE_HEATING
            isCoolingCondition -> LivingBatteryState.COOLING
            isDeepSleepCondition -> LivingBatteryState.DEEP_SLEEP
            isEnergyStoringCondition -> LivingBatteryState.ENERGY_STORING
            isCharging -> LivingBatteryState.ENERGY_STORING
            abs(currentMa) <= 350 -> LivingBatteryState.DEEP_SLEEP
            temperatureC < 30.0f -> LivingBatteryState.COOLING
            else -> LivingBatteryState.ENERGY_STORING
        }

        // Apply automatic real-time sensor detection (or manual preview when explicitly toggled)
        val livingState = if (_isAutoSensorMode.value) {
            sensorEvaluatedState
        } else {
            manualStateOverride ?: sensorEvaluatedState
        }

        val estimatedMin = if (isCharging) {
            val remainingPercent = (100 - percent).coerceAtLeast(1)
            (remainingPercent * 1.4f).toInt().coerceAtLeast(4)
        } else {
            val remainingPercent = percent.coerceAtLeast(1)
            (remainingPercent * 11.5f).toInt().coerceAtLeast(15)
        }

        val telemetry = BatteryTelemetry(
            percentage = percent.coerceIn(0, 100),
            status = status,
            plugType = plugType,
            health = if (temperatureC >= 42f) BatteryHealth.OVERHEAT else BatteryHealth.GOOD,
            voltageMv = voltageMv,
            currentMa = currentMa,
            wattageW = (wattageW * 100).toInt() / 100f,
            temperatureC = (temperatureC * 10).toInt() / 10f,
            technology = "Li-poly (Solid Matrix)",
            capacityMah = 5000,
            cycleCount = 142,
            estimatedMinutesRemaining = estimatedMin,
            isFastCharging = abs(currentMa) > 1800,
            dischargeRatePerHour = 4.2f,
            livingState = livingState,
            lastUpdated = System.currentTimeMillis()
        )

        _telemetry.value = telemetry
        appendHistoryPoint(telemetry)
    }

    private fun appendHistoryPoint(t: BatteryTelemetry) {
        val now = System.currentTimeMillis()
        val maxPoints = 40

        fun append(currentList: List<TelemetryPoint>, newVal: Float): List<TelemetryPoint> {
            val list = currentList.toMutableList()
            list.add(TelemetryPoint(now, newVal))
            if (list.size > maxPoints) {
                list.removeAt(0)
            }
            return list
        }

        _voltageHistory.value = append(_voltageHistory.value, t.voltageMv.toFloat())
        _currentHistory.value = append(_currentHistory.value, t.currentMa.toFloat())
        _wattageHistory.value = append(_wattageHistory.value, t.wattageW)
        _temperatureHistory.value = append(_temperatureHistory.value, t.temperatureC)
    }

    private fun updateEggAndPrizeProgress() {
        val isCharging = _telemetry.value.status == BatteryStatus.CHARGING
        val state = _telemetry.value.livingState

        // XP delta
        val xpGain = when (state) {
            LivingBatteryState.OVERDRIVE_HEATING -> -1
            LivingBatteryState.COOLING -> 2
            LivingBatteryState.ENERGY_STORING -> if (isCharging) 5 else 2
            LivingBatteryState.DEEP_SLEEP -> 1
        }

        val currentEgg = _eggData.value
        val newXp = (currentEgg.currentXP + xpGain).coerceAtLeast(0)
        val progress = (newXp.toFloat() / currentEgg.xpToHatch).coerceIn(0f, 1f)

        _eggData.value = currentEgg.copy(
            currentXP = newXp,
            incubationProgress = progress
        )

        // Increment Prize Road cumulative mAh continuously while charging
        if (isCharging) {
            val addedMah = (abs(_telemetry.value.currentMa) / 3600f * 1.5f).toLong().coerceAtLeast(1L)
            addCumulativeMah(addedMah)
        }
    }

    private fun updateCrewCooldowns() {
        _techCrew.value = _techCrew.value.map { member ->
            val cd = if (member.cooldownRemainingSec > 0) member.cooldownRemainingSec - 1 else 0
            val activeSec = if (member.activeSecondsRemaining > 0) member.activeSecondsRemaining - 1 else 0
            val isRunning = activeSec > 0

            member.copy(
                cooldownRemainingSec = cd,
                activeSecondsRemaining = activeSec,
                isActiveAbilityRunning = isRunning
            )
        }

        // Slowly recover simulated temp delta back to normal
        if (simulatedTempDelta != 0f) {
            if (simulatedTempDelta < 0f) simulatedTempDelta += 0.05f
            if (simulatedTempDelta > 0f) simulatedTempDelta -= 0.05f
            if (abs(simulatedTempDelta) < 0.05f) simulatedTempDelta = 0f
        }
    }

    fun setLivingBatteryState(state: LivingBatteryState?) {
        if (state == null) {
            _isAutoSensorMode.value = true
            manualStateOverride = null
        } else {
            _isAutoSensorMode.value = false
            manualStateOverride = state
            if (state == LivingBatteryState.OVERDRIVE_HEATING) {
                simulatedTempDelta = 12.5f
            } else if (state == LivingBatteryState.COOLING) {
                simulatedTempDelta = -6.0f
            }
        }
    }

    fun setAutoSensorMode(auto: Boolean) {
        _isAutoSensorMode.value = auto
        if (auto) {
            manualStateOverride = null
        }
    }

    fun triggerCrewAbility(crewId: String) {
        val crewList = _techCrew.value.toMutableList()
        val index = crewList.indexOfFirst { it.id == crewId }
        if (index == -1) return

        val member = crewList[index]
        if (!member.isUnlocked || member.cooldownRemainingSec > 0) return

        // Dispatch ability effect
        when (member.id) {
            "crew_1" -> { // Cryo Blast
                simulatedTempDelta = -8.0f
                manualStateOverride = LivingBatteryState.COOLING
                addManualSessionLog("Cryo Coolant Deployed", "Kael Vance injected liquid nitrogen. Core temp dropped.", "-8°C Suppression", "temp")
            }
            "crew_2" -> { // Volt Lock
                val currentEgg = _eggData.value
                _eggData.value = currentEgg.copy(currentXP = currentEgg.currentXP + 350)
                addManualSessionLog("Harmonic Voltage Tuned", "Dr. Reed locked cell balance and injected +350 XP.", "+350 XP", "charge")
            }
            "crew_3" -> { // Siphon Flush
                addManualSessionLog("Background Siphon Purged", "Ghost Unit eliminated 14 zombie threads.", "-180mA Load", "check")
            }
            "crew_4" -> { // Shield Restore
                val currentShields = _streakData.value.shieldsAvailable
                if (currentShields < _streakData.value.maxShields) {
                    _streakData.value = _streakData.value.copy(shieldsAvailable = currentShields + 1)
                }
                addManualSessionLog("Health Shield Recharged", "Aegis Matrix reinforced electrochemical shield.", "+1 Shield Active", "check")
            }
        }

        crewList[index] = member.copy(
            cooldownRemainingSec = member.cooldownTotalSec,
            activeSecondsRemaining = 12,
            isActiveAbilityRunning = true
        )
        _techCrew.value = crewList
    }

    fun hatchEgg(): HatchedCompanion {
        val companion = HatchedCompanion(
            id = System.currentTimeMillis().toString(),
            name = "Quantum Matrix Drake Mk II",
            rarity = "Mythic",
            perk = "+25% Superfast Charge Absorption",
            color = Color(0xFF00FF87)
        )
        val currentEgg = _eggData.value
        _eggData.value = currentEgg.copy(
            incubationProgress = 0.05f,
            currentXP = 50,
            hatchedList = listOf(companion) + currentEgg.hatchedList
        )
        addManualSessionLog("Cyber Egg Hatched!", "Discovered: ${companion.name} (${companion.rarity}).", companion.perk, "charge")
        return companion
    }

    fun addCumulativeMah(addedMah: Long) {
        val current = _prizeRoadData.value
        val newMah = (current.cumulativeMah + addedMah).coerceAtMost(10_000_000L)
        val newLevel = ((newMah / 10_000L).toInt() + 1).coerceIn(1, 1000)
        val previousLevel = current.currentLevel

        val newTitle = when {
            newLevel >= 500 -> "Quantum Matrix Overlord"
            newLevel >= 250 -> "Cryo Superconductor Master"
            newLevel >= 100 -> "Volt Grandmaster"
            newLevel >= 50 -> "High-Voltage Specialist"
            newLevel >= 25 -> "Lithium Engineer"
            newLevel >= 10 -> "Electrolyte Guardian"
            newLevel >= 5 -> "Energy Technician"
            else -> "Cadet Recruit"
        }

        _prizeRoadData.value = current.copy(
            cumulativeMah = newMah,
            currentLevel = newLevel,
            activeTitle = newTitle
        )

        // Level up rewards: unlock shields and recruit specialist crew!
        if (newLevel > previousLevel) {
            checkLevelProgressionRewards(previousLevel, newLevel)
        }
    }

    private fun checkLevelProgressionRewards(oldLevel: Int, newLevel: Int) {
        // Unlock Shields as player levels up
        val currentStreak = _streakData.value
        val newMaxShields = when {
            newLevel >= 50 -> 4
            newLevel >= 25 -> 3
            newLevel >= 10 -> 2
            newLevel >= 5 -> 1
            else -> 0
        }
        if (newMaxShields > currentStreak.maxShields) {
            val added = newMaxShields - currentStreak.maxShields
            _streakData.value = currentStreak.copy(
                maxShields = newMaxShields,
                shieldsAvailable = currentStreak.shieldsAvailable + added
            )
            addManualSessionLog("Shield Capacity Unlocked!", "Earned +$added Battery Health Shield by reaching Level $newLevel.", "+$added Shield", "check")
        }

        // Unlock Tech Crew Specialists as player levels up
        _techCrew.value = _techCrew.value.map { member ->
            if (!member.isUnlocked && newLevel >= member.unlockLevel) {
                addManualSessionLog("Specialist Recruited!", "${member.name} (${member.title}) unlocked at Level $newLevel.", "Recruited", "check")
                member.copy(isUnlocked = true)
            } else {
                member
            }
        }
    }

    fun claimMilestone(level: Int) {
        val current = _prizeRoadData.value
        if (current.claimedMilestones.contains(level)) return

        _prizeRoadData.value = current.copy(
            claimedMilestones = current.claimedMilestones + level
        )

        if (level % 10 == 0) {
            val s = _streakData.value
            _streakData.value = s.copy(
                maxShields = (s.maxShields + 1).coerceAtMost(5),
                shieldsAvailable = (s.shieldsAvailable + 1).coerceAtMost(5)
            )
        }

        val egg = _eggData.value
        val newXp = egg.currentXP + 500
        val progress = (newXp.toFloat() / egg.xpToHatch).coerceIn(0f, 1f)
        _eggData.value = egg.copy(currentXP = newXp, incubationProgress = progress)

        addManualSessionLog("Milestone Tier #$level Claimed", "Claimed Level $level Milestone Cache: +500 Egg XP.", "Level $level", "check")
    }

    fun repairShield() {
        val s = _streakData.value
        if (s.shieldsAvailable < s.maxShields) {
            _streakData.value = s.copy(shieldsAvailable = s.shieldsAvailable + 1)
        }
    }

    fun addManualSessionLog(title: String, desc: String, statTag: String, type: String) {
        val newLog = SessionLog(
            id = System.currentTimeMillis().toString(),
            title = title,
            timestampFormatted = "Just now",
            description = desc,
            statTag = statTag,
            type = type
        )
        _sessionLogs.value = listOf(newLog) + _sessionLogs.value
    }
}

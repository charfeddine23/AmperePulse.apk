package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.compose.ui.graphics.Color
import com.example.data.telemetry.CurrentCalibrationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

class BatteryMonitorRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    val currentCalibrationEngine = CurrentCalibrationEngine(context)

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

    private val _userAccount = MutableStateFlow(UserAccount())
    val userAccount: StateFlow<UserAccount> = _userAccount.asStateFlow()

    private val _cableResult = MutableStateFlow(CableDiagnosticResult())
    val cableResult: StateFlow<CableDiagnosticResult> = _cableResult.asStateFlow()

    private val _sohPrediction = MutableStateFlow(BatterySohPrediction())
    val sohPrediction: StateFlow<BatterySohPrediction> = _sohPrediction.asStateFlow()

    private var previousLivingState: LivingBatteryState? = null
    private var previousIsCharging: Boolean = false

    private var manualStateOverride: LivingBatteryState? = null
    private var simulatedTempDelta = 0.0f
    private var simTick = 0L

    @Volatile
    private var isAppInForeground: Boolean = true

    @Volatile
    private var isSohPageActive: Boolean = false

    @Volatile
    private var cachedWalLogCount: Int = 0
    private var lastWalSyncTimeMs: Long = 0L

    private val _hourlyStats = MutableStateFlow<List<com.example.data.local.HourlyBatteryStatEntity>>(emptyList())
    val hourlyStats: StateFlow<List<com.example.data.local.HourlyBatteryStatEntity>> = _hourlyStats.asStateFlow()

    init {
        initHistorySeed()
        initSessionLogsSeed()
        initTechCrewSeed()
        initHourlyStatsFromDatabase()
        refreshWalLogCountAsync()
        startPolling()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    fun setSohPageActive(active: Boolean) {
        isSohPageActive = active
        if (active) {
            refreshWalLogCountAsync()
            val t = _telemetry.value
            computeLiveSohPrediction(
                telemetry = t,
                chargeCounterUah = t.chargeCounterUah,
                hwCycleCount = t.cycleCount,
                hasHwChargeCounter = false
            )
        }
    }

    private fun refreshWalLogCountAsync() {
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.local.AmpereDatabase.getInstance(context)
                cachedWalLogCount = db.telemetryLogDao().getLogCount()
                lastWalSyncTimeMs = System.currentTimeMillis()
            } catch (_: Exception) {}
        }
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

        for (i in 60 downTo 0) {
            val t = now - (i * 100L)
            val v = 4120f + (sin(i * 0.12) * 35).toFloat()
            val c = 2150f + (sin(i * 0.10) * 160).toFloat()
            val w = (v / 1000f) * (c / 1000f)
            val temp = 28.2f + (sin(i * 0.06) * 0.35).toFloat()

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

    private var subTick = 0
    private var cachedBatteryStatus: Intent? = null

    private fun startPolling() {
        scope.launch(Dispatchers.Default) {
            while (isActive) {
                val activeForeground = isAppInForeground
                if (subTick % 10 == 0 || cachedBatteryStatus == null || isSohPageActive) {
                    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    cachedBatteryStatus = context.registerReceiver(null, ifilter)
                }

                if (isSohPageActive && (System.currentTimeMillis() - lastWalSyncTimeMs >= 5_000L)) {
                    refreshWalLogCountAsync()
                }

                readBatteryState(cachedBatteryStatus)

                subTick++
                if (subTick % 10 == 0) {
                    updateEggAndPrizeProgress()
                    updateCrewCooldowns()
                }

                delay(if (activeForeground) 100L else 5_000L)
            }
        }
    }

    private fun readBatteryState(batteryStatus: Intent?) {
        simTick++

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
            (baseVoltageMv + (sin(simTick * 0.12) * 20).toInt())
        } else {
            4180 + (sin(simTick * 0.12) * 25).toInt()
        }

        val rawTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285) ?: 285
        val baseTempC = (rawTemp / 10.0f) + simulatedTempDelta
        val temperatureC = if (baseTempC in 10f..65f) {
            baseTempC + (sin(simTick * 0.06) * 0.25f).toFloat()
        } else {
            (28.5f + simulatedTempDelta + (sin(simTick * 0.06) * 0.3f)).toFloat()
        }

        // High-Precision Noise-Filtered Current Telemetry Engine
        val currentMa = currentCalibrationEngine.sampleCurrentMa(
            batteryManager = batteryManager,
            isCharging = isCharging,
            voltageMv = voltageMv
        )

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

        val rawChargeCounter = try {
            batteryManager?.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: -1L
        } catch (_: Exception) {
            -1L
        }
        val hasHwChargeCounter = rawChargeCounter > 0L
        val effectiveChargeCounterUah = if (hasHwChargeCounter) {
            rawChargeCounter
        } else {
            (percent.coerceIn(0, 100) / 100f * 5000f * 1000f).toLong()
        }

        val hwCycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            batteryStatus?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        } else {
            -1
        }
        val resolvedCycleCount = if (hwCycleCount >= 0) hwCycleCount else 142 + (cachedWalLogCount / 500)

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
            chargeCounterUah = effectiveChargeCounterUah,
            cycleCount = resolvedCycleCount,
            estimatedMinutesRemaining = estimatedMin,
            isFastCharging = abs(currentMa) > 1800,
            dischargeRatePerHour = 4.2f,
            livingState = livingState,
            lastUpdated = System.currentTimeMillis()
        )

        _telemetry.value = telemetry
        appendHistoryPoint(telemetry)

        // Sub-second live Cell SOH telemetry calculation (every 100ms on Cell SOH screen, or every 1s elsewhere)
        if (isSohPageActive || subTick % 10 == 0) {
            computeLiveSohPrediction(
                telemetry = telemetry,
                chargeCounterUah = effectiveChargeCounterUah,
                hwCycleCount = resolvedCycleCount,
                hasHwChargeCounter = hasHwChargeCounter
            )
        }

        // Throttled Liquid Glass Home Widget sync (every ~3s or on living-state transition)
        if (subTick % 30 == 0 || previousLivingState != telemetry.livingState) {
            scope.launch {
                try {
                    com.example.widget.WidgetTelemetryUpdater.update(
                        context = context,
                        pct = telemetry.percentage,
                        tempC = telemetry.temperatureC,
                        voltageMv = telemetry.voltageMv,
                        currentMa = telemetry.currentMa,
                        stateName = telemetry.livingState.name
                    )
                } catch (_: Exception) {}
            }
        }

        // Trigger Cable DNA and Audio/Haptic cues on Charger Connect / Disconnect
        val isNowCharging = telemetry.status == BatteryStatus.CHARGING || telemetry.status == BatteryStatus.FULL || telemetry.plugType != ChargePlug.NONE || telemetry.currentMa > 0
        if (previousIsCharging != isNowCharging) {
            previousIsCharging = isNowCharging
            if (isNowCharging) {
                runCableDiagnosticSweep()
                if (isAppInForeground) {
                    com.example.audio.CyberSoundscapeEngine.playDiagnosticSweep()
                    com.example.audio.CyberHapticsEngine.triggerElectricSpark(context)
                }
            } else {
                // When unplugged, reset cable metrics cleanly to 0
                _cableResult.value = CableDiagnosticResult(
                    resistanceMilliOhms = 0,
                    voltageDropMv = 0,
                    measuredVoltageMv = telemetry.voltageMv,
                    measuredCurrentMa = 0,
                    maxWattageW = 0f,
                    qualityScore = 100,
                    tier = CableDnaTier.LEGENDARY_ORIGINAL,
                    usbProtocol = "Standby (Unplugged)",
                    pinIntegrityPercent = 100,
                    thermalDissipationMilliWatts = 0,
                    scanTimestamp = System.currentTimeMillis()
                )
            }
        } else if (isNowCharging) {
            // Sub-second 100ms real-time cable telemetry synchronization with live hardware voltage & current
            updateContinuousCableMetrics(telemetry)
        }

        // Trigger Audio/Haptic cues on Living State Transition (strictly in foreground)
        if (previousLivingState != telemetry.livingState) {
            val oldState = previousLivingState
            previousLivingState = telemetry.livingState
            if (oldState != null && isAppInForeground) {
                when (telemetry.livingState) {
                    LivingBatteryState.OVERDRIVE_HEATING -> {
                        com.example.audio.CyberHapticsEngine.triggerThermalWarning(context)
                        com.example.audio.CyberSoundscapeEngine.playThermalAlert()
                    }
                    LivingBatteryState.COOLING -> {
                        com.example.audio.CyberHapticsEngine.triggerCrystalMorph(context)
                        com.example.audio.CyberSoundscapeEngine.playCryoChime()
                    }
                    LivingBatteryState.ENERGY_STORING -> {
                        com.example.audio.CyberHapticsEngine.triggerElectricSpark(context)
                        com.example.audio.CyberSoundscapeEngine.playElectricSpark()
                    }
                    LivingBatteryState.DEEP_SLEEP -> {
                        com.example.audio.CyberHapticsEngine.triggerTactileTick(context)
                    }
                }
            }
        }
    }

    private fun appendHistoryPoint(t: BatteryTelemetry) {
        val now = System.currentTimeMillis()
        val maxPoints = 60

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

    fun updateOwnerName(name: String) {
        if (name.isNotBlank()) {
            val trimmed = name.trim()
            val initials = trimmed.take(2).uppercase()
            _userAccount.value = _userAccount.value.copy(
                ownerName = trimmed,
                avatarInitials = initials
            )
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        _userAccount.value = _userAccount.value.copy(isCloudSyncActive = enabled)
    }

    fun runCableDiagnosticSweep(tierOverride: CableDnaTier? = null) {
        val currentV = _telemetry.value.voltageMv
        val currentI = abs(_telemetry.value.currentMa)
        val isPlugged = _telemetry.value.plugType != ChargePlug.NONE || _telemetry.value.status == BatteryStatus.CHARGING || currentI > 200

        if (!isPlugged) {
            _cableResult.value = CableDiagnosticResult(
                resistanceMilliOhms = 0,
                voltageDropMv = 0,
                measuredVoltageMv = currentV,
                measuredCurrentMa = 0,
                maxWattageW = 0f,
                qualityScore = 100,
                tier = CableDnaTier.LEGENDARY_ORIGINAL,
                usbProtocol = "Standby (Unplugged)",
                pinIntegrityPercent = 100,
                thermalDissipationMilliWatts = 0,
                scanTimestamp = System.currentTimeMillis()
            )
            return
        }

        // Live hardware Ohm's law calculation: R = ΔV / I
        val nominalSourceMv = 5000 // USB 5V reference rail
        val measuredSagMv = (nominalSourceMv - currentV).coerceIn(40, 850)
        val effectiveI = currentI.coerceAtLeast(400)
        val calculatedResistance = ((measuredSagMv.toFloat() / effectiveI.toFloat()) * 1000f).toInt().coerceIn(75, 620)

        val evaluatedTier = tierOverride ?: when {
            calculatedResistance > 450 || _telemetry.value.temperatureC > 38f -> CableDnaTier.DANGEROUS_COUNTERFEIT
            calculatedResistance <= 140 -> CableDnaTier.LEGENDARY_ORIGINAL
            calculatedResistance <= 260 -> CableDnaTier.CERTIFIED_FAST
            else -> CableDnaTier.DEGRADED_STANDARD
        }

        val dissipationMw = ((effectiveI.toDouble() / 1000.0) * (effectiveI.toDouble() / 1000.0) * (calculatedResistance.toDouble() / 1000.0) * 1000.0).toInt()

        val result = CableDiagnosticResult(
            resistanceMilliOhms = calculatedResistance,
            voltageDropMv = measuredSagMv,
            measuredVoltageMv = currentV,
            measuredCurrentMa = effectiveI,
            maxWattageW = (currentV / 1000f) * (effectiveI / 1000f),
            qualityScore = when (evaluatedTier) {
                CableDnaTier.LEGENDARY_ORIGINAL -> 98
                CableDnaTier.CERTIFIED_FAST -> 89
                CableDnaTier.DEGRADED_STANDARD -> 64
                CableDnaTier.DANGEROUS_COUNTERFEIT -> 31
            },
            tier = evaluatedTier,
            thermalDissipationMilliWatts = dissipationMw,
            scanTimestamp = System.currentTimeMillis()
        )
        _cableResult.value = result

        addManualSessionLog(
            title = "Cable DNA Audit: ${evaluatedTier.title}",
            desc = "Internal Resistance ${calculatedResistance}mΩ, Sag -${measuredSagMv}mV. Classification: ${evaluatedTier.grade}.",
            statTag = evaluatedTier.grade,
            type = if (evaluatedTier.isDangerous) "warning" else "check"
        )
    }

    private fun updateContinuousCableMetrics(telemetry: BatteryTelemetry) {
        val currentV = telemetry.voltageMv
        val currentI = abs(telemetry.currentMa)
        val nominalSourceMv = 5000 // USB 5V rail reference
        val measuredSagMv = (nominalSourceMv - currentV).coerceIn(40, 850)
        val effectiveI = currentI.coerceAtLeast(400)
        val calculatedResistance = ((measuredSagMv.toFloat() / effectiveI.toFloat()) * 1000f).toInt().coerceIn(75, 620)

        val evaluatedTier = when {
            calculatedResistance > 450 || telemetry.temperatureC > 38f -> CableDnaTier.DANGEROUS_COUNTERFEIT
            calculatedResistance <= 140 -> CableDnaTier.LEGENDARY_ORIGINAL
            calculatedResistance <= 260 -> CableDnaTier.CERTIFIED_FAST
            else -> CableDnaTier.DEGRADED_STANDARD
        }

        val dissipationMw = ((effectiveI.toDouble() / 1000.0) * (effectiveI.toDouble() / 1000.0) * (calculatedResistance.toDouble() / 1000.0) * 1000.0).toInt()

        val current = _cableResult.value
        _cableResult.value = current.copy(
            resistanceMilliOhms = calculatedResistance,
            voltageDropMv = measuredSagMv,
            measuredVoltageMv = currentV,
            measuredCurrentMa = effectiveI,
            maxWattageW = (currentV / 1000f) * (effectiveI / 1000f),
            qualityScore = when (evaluatedTier) {
                CableDnaTier.LEGENDARY_ORIGINAL -> 98
                CableDnaTier.CERTIFIED_FAST -> 89
                CableDnaTier.DEGRADED_STANDARD -> 64
                CableDnaTier.DANGEROUS_COUNTERFEIT -> 31
            },
            tier = evaluatedTier,
            thermalDissipationMilliWatts = dissipationMw,
            scanTimestamp = System.currentTimeMillis()
        )
    }

    private fun computeLiveSohPrediction(
        telemetry: BatteryTelemetry,
        chargeCounterUah: Long,
        hwCycleCount: Int,
        hasHwChargeCounter: Boolean
    ) {
        val logCount = cachedWalLogCount
        val baseHealth = (98.4f - (logCount * 0.001f) - ((hwCycleCount - 142).coerceAtLeast(0) * 0.015f))
            .coerceIn(72f, 100f)

        val blendedHealth = if (hasHwChargeCounter && telemetry.percentage in 15..100) {
            val observedFullMah = ((chargeCounterUah / 1000f) / (telemetry.percentage / 100f)).coerceIn(3200f, 5400f)
            val hwHealthPct = (observedFullMah / 5000f) * 100f
            (0.35f * hwHealthPct) + (0.65f * baseHealth)
        } else {
            baseHealth
        }

        // Sub-second electrochemical voltage & load polarization compensation
        val loadStressDelta = -(abs(telemetry.currentMa) / 1000f) * 0.08f +
            ((telemetry.voltageMv - 4150) / 1000f) * 0.15f
        val liveHealthPct = ((blendedHealth + loadStressDelta).coerceIn(70f, 100f) * 100f).roundToInt() / 100f

        val effectiveCapacityMah = (5000f * (liveHealthPct / 100f)).roundToInt()

        // Live ESR Impedance (mΩ) responding to load current, temperature & terminal voltage sag
        val baseEsr = 44f + (hwCycleCount * 0.03f)
        val loadPolarization = abs(telemetry.currentMa) / 260f
        val thermalPenalty = (30f - telemetry.temperatureC).coerceAtLeast(0f) * 0.4f +
            (telemetry.temperatureC - 38f).coerceAtLeast(0f) * 0.6f
        val sagComponent = (4200 - telemetry.voltageMv).coerceAtLeast(0) / 75f
        val liveEsrMilliOhms = (baseEsr + loadPolarization + thermalPenalty + sagComponent)
            .roundToInt()
            .coerceIn(32, 160)

        // Instantaneous Coulombic Efficiency (%) responding to thermal & C-rate stress
        val rawCoulombic = (99.55f -
            ((telemetry.temperatureC - 30f).coerceAtLeast(0f) * 0.09f) -
            (abs(telemetry.currentMa) / 12000f))
            .coerceIn(92.0f, 99.95f)
        val liveCoulombic = (rawCoulombic * 100f).roundToInt() / 100f

        val remainingCycles = (1000 - hwCycleCount).coerceAtLeast(50)
        val lifespanDays = ((remainingCycles * 0.98f) - (logCount / 300)).roundToInt().coerceAtLeast(180)
        val cellStatus = when {
            liveHealthPct >= 95f -> CellHealthStatus.PRISTINE
            liveHealthPct >= 88f -> CellHealthStatus.OPTIMAL
            liveHealthPct >= 80f -> CellHealthStatus.MODERATE
            else -> CellHealthStatus.CRITICAL
        }

        _sohPrediction.value = _sohPrediction.value.copy(
            healthPercentage = liveHealthPct,
            estimatedCurrentCapacityMah = effectiveCapacityMah,
            totalCyclesCompleted = hwCycleCount,
            estimatedRemainingCycles = remainingCycles,
            estimatedLifespanDays = lifespanDays,
            coulombicEfficiencyPercent = liveCoulombic,
            internalResistanceEstimateMilliOhms = liveEsrMilliOhms,
            cellStatus = cellStatus
        )
    }

    fun recalibrateSohPrediction(addLog: Boolean = true) {
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.local.AmpereDatabase.getInstance(context)
                cachedWalLogCount = db.telemetryLogDao().getLogCount()
                lastWalSyncTimeMs = System.currentTimeMillis()

                val t = _telemetry.value
                computeLiveSohPrediction(
                    telemetry = t,
                    chargeCounterUah = t.chargeCounterUah,
                    hwCycleCount = t.cycleCount,
                    hasHwChargeCounter = false
                )

                if (addLog) {
                    val latest = _sohPrediction.value
                    addManualSessionLog(
                        title = "Cell SOH Auto-Calibrated",
                        desc = "Room WAL synchronized (${cachedWalLogCount} records). Health: ${latest.healthPercentage}%, ESR: ${latest.internalResistanceEstimateMilliOhms}mΩ.",
                        statTag = "SOH ${latest.healthPercentage}%",
                        type = "check"
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleThermalSimulation(forceOverheat: Boolean? = null) {
        simulatedTempDelta = when (forceOverheat) {
            true -> 13.5f // Elevates base ~28.5°C up to 42.0°C (> 40°C critical threshold)
            false -> 0.0f  // Cool blue nominal ~28.5°C
            null -> if (simulatedTempDelta > 5f) 0.0f else 13.5f
        }
    }

    private fun initHourlyStatsFromDatabase() {
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.local.AmpereDatabase.getInstance(context)
                val dao = db.hourlyBatteryStatDao()

                // If DB is empty, seed 24 hours of realistic hourly logs
                val count = dao.getCount()
                if (count == 0) {
                    seed24HoursTelemetryData(dao)
                }

                // Collect reactive Flow from Room WAL
                dao.getAllHourlyStats().collect { list ->
                    _hourlyStats.value = list
                }
            } catch (_: Exception) {}
        }
    }

    fun seedOrResetHourlyStats() {
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.local.AmpereDatabase.getInstance(context)
                val dao = db.hourlyBatteryStatDao()
                dao.clearAll()
                seed24HoursTelemetryData(dao)
                addManualSessionLog(
                    title = "Hourly Room WAL Reset",
                    desc = "Re-seeded 24-hour battery statistical archive with high-fidelity telemetry points.",
                    statTag = "24 Records",
                    type = "check"
                )
            } catch (_: Exception) {}
        }
    }

    private suspend fun seed24HoursTelemetryData(dao: com.example.data.local.HourlyBatteryStatDao) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val stats = mutableListOf<com.example.data.local.HourlyBatteryStatEntity>()
        val timeFormat = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.US)
        val dateFormat = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.US)

        for (i in 23 downTo 0) {
            val hourStart = now - (i * 3600_000L)
            val hourEnd = hourStart + 3599_000L
            calendar.timeInMillis = hourStart
            val hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            
            // Ultra-precise time window: e.g. "10:00:00 AM → 10:59:59 AM"
            val timeStr = "${timeFormat.format(java.util.Date(hourStart))} → ${timeFormat.format(java.util.Date(hourEnd))}"
            val dateStr = dateFormat.format(java.util.Date(hourStart))

            // Realistic charge/discharge profile based on diurnal cycle
            val isCharging = hourOfDay in 1..6 || hourOfDay in 14..15
            val avgV = if (isCharging) 4180 + (hourOfDay * 8) else 3920 - (hourOfDay * 5)
            val minV = avgV - 45
            val maxV = avgV + 55

            val avgI = if (isCharging) 2100 + ((i % 5) * 120) else -380 - ((i % 7) * 45)
            val minI = if (isCharging) 1850 else -620
            val maxI = if (isCharging) 2650 else -180

            val avgT = if (isCharging) 33.5f + ((i % 4) * 0.8f) else 28.2f + ((i % 3) * 0.5f)
            val maxT = avgT + 1.8f
            val avgWatt = (avgV / 1000f) * (kotlin.math.abs(avgI) / 1000f)

            val startPct = if (isCharging) (30 + (hourOfDay * 8)).coerceIn(15, 95) else (95 - (i * 3)).coerceIn(12, 100)
            val endPct = if (isCharging) (startPct + 18).coerceIn(15, 100) else (startPct - 4).coerceIn(10, 100)
            val netMah = if (isCharging) 1450 + (i * 35) else -320 - (i * 15)

            val dominantState = if (isCharging) {
                if (avgT > 36.5f) "OVERDRIVE_HEATING" else "ENERGY_STORING"
            } else {
                if (hourOfDay in 2..6) "DEEP_SLEEP" else "CRYOGENIC_COOLING"
            }

            val dominantLabel = when (dominantState) {
                "ENERGY_STORING" -> "Energy Storing"
                "OVERDRIVE_HEATING" -> "Thermal Overdrive"
                "DEEP_SLEEP" -> "Deep Sleep Siphon"
                else -> "Cryogenic Cool"
            }

            stats.add(
                com.example.data.local.HourlyBatteryStatEntity(
                    id = (24 - i).toLong(),
                    hourTimestamp = hourStart,
                    formattedTime = timeStr,
                    formattedDate = dateStr,
                    avgVoltageMv = avgV,
                    minVoltageMv = minV,
                    maxVoltageMv = maxV,
                    avgCurrentMa = avgI,
                    minCurrentMa = minI,
                    maxCurrentMa = maxI,
                    avgTemperatureC = avgT,
                    maxTemperatureC = maxT,
                    avgWattageW = avgWatt,
                    startBatteryPct = startPct,
                    endBatteryPct = endPct,
                    netMahFlow = netMah,
                    dominantState = dominantState,
                    dominantStateLabel = dominantLabel,
                    sampleCount = 360,
                    isChargingHour = isCharging,
                    screenOnDurationMinutes = if (isCharging) 8 else (15 + (i % 25))
                )
            )
        }

        dao.insertAll(stats)
    }

    fun updateCurrentCalibration(multiplier: Float, zeroOffsetMa: Float) {
        currentCalibrationEngine.updateCalibration(multiplier, zeroOffsetMa)
    }

    fun resetCurrentCalibration() {
        currentCalibrationEngine.resetCalibration()
    }
}


package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPhosphorusGreen
import com.example.ui.theme.SolarAmber

enum class BatteryStatus(val label: String) {
    CHARGING("Charging"),
    DISCHARGING("Discharging"),
    FULL("Fully Charged"),
    NOT_CHARGING("Idle (Not Charging)"),
    UNKNOWN("Analyzing")
}

enum class BatteryHealth(val label: String) {
    GOOD("Good / Optimal"),
    OVERHEAT("Overheat Warning"),
    DEAD("Exhausted"),
    OVER_VOLTAGE("Over Voltage"),
    COLD("Cold Sub-zero"),
    UNKNOWN("Nominal")
}

enum class ChargePlug(val label: String) {
    AC("Ultra AC Wall Charger"),
    USB("USB-C Fast Data Port"),
    WIRELESS("Wireless Qi Induction Pad"),
    DOCK("Smart Docking Station"),
    NONE("Battery Power (Unplugged)")
}

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F")
}

enum class MetricType(val title: String, val unit: String) {
    VOLTAGE("Voltage", "mV"),
    CURRENT("Current", "mA"),
    WATTAGE("Wattage", "W"),
    TEMPERATURE("Temperature", "°C")
}

/**
 * 4 Living States for the Dynamic Battery
 */
enum class LivingBatteryState(
    val title: String,
    val neonPrimary: Color,
    val neonSecondary: Color,
    val auraDescription: String,
    val xpRateLabel: String,
    val isBleeding: Boolean
) {
    OVERDRIVE_HEATING(
        title = "Overdrive & Heating",
        neonPrimary = Color(0xFFFF3B30),
        neonSecondary = Color(0xFFFF9500),
        auraDescription = "Thermal dissipation overload. Thermal stress active.",
        xpRateLabel = "-1 XP / min (Thermal Bleed)",
        isBleeding = true
    ),
    COOLING(
        title = "Cryo Cooling",
        neonPrimary = Color(0xFF00F0FF),
        neonSecondary = Color(0xFF007AFF),
        auraDescription = "Vapor chamber active. Cell matrix normalizing.",
        xpRateLabel = "+2 XP / min (Recovery)",
        isBleeding = false
    ),
    ENERGY_STORING(
        title = "Energy Storing",
        neonPrimary = Color(0xFF00FF87),
        neonSecondary = Color(0xFF10FFA0),
        auraDescription = "Ultra-fast plasma influx. Lithium saturation active.",
        xpRateLabel = "+5 XP / min (Fast Pulse)",
        isBleeding = false
    ),
    DEEP_SLEEP(
        title = "Deep Sleep",
        neonPrimary = Color(0xFFBF5AF2),
        neonSecondary = Color(0xFF5E5CE6),
        auraDescription = "Sub-milliamp quiescent draw. Cell longevity mode.",
        xpRateLabel = "+1 XP / min (Longevity)",
        isBleeding = false
    )
}

data class TelemetryPoint(
    val timestamp: Long,
    val value: Float
)

data class BatteryTelemetry(
    val percentage: Int = 84,
    val status: BatteryStatus = BatteryStatus.CHARGING,
    val plugType: ChargePlug = ChargePlug.AC,
    val health: BatteryHealth = BatteryHealth.GOOD,
    val voltageMv: Int = 4182,
    val currentMa: Int = 2340,
    val wattageW: Float = 9.78f,
    val temperatureC: Float = 28.5f,
    val technology: String = "Li-poly (Solid Matrix)",
    val capacityMah: Int = 5000,
    val chargeCounterUah: Long = 4200000L,
    val cycleCount: Int = 142,
    val estimatedMinutesRemaining: Int = 38,
    val isFastCharging: Boolean = true,
    val dischargeRatePerHour: Float = 4.2f,
    val livingState: LivingBatteryState = LivingBatteryState.ENERGY_STORING,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class StreakData(
    val currentStreakDays: Int = 14,
    val bestStreakDays: Int = 28,
    val shieldsAvailable: Int = 3,
    val maxShields: Int = 3,
    val todayComplianceRate: Float = 0.94f,
    val healthyChargeMinutesToday: Int = 145,
    val optimalTempScore: Int = 98,
    val lastShieldUsedDate: String? = null
)

data class HatchedCompanion(
    val id: String,
    val name: String,
    val rarity: String,
    val perk: String,
    val color: Color
)

data class CyberEggData(
    val eggName: String = "Neon Quantum Core Egg",
    val incubationProgress: Float = 0.78f, // 0.0 to 1.0 (78%)
    val isIncubating: Boolean = true,
    val currentXP: Int = 1840,
    val xpToHatch: Int = 2200,
    val hatchedList: List<HatchedCompanion> = listOf(
        HatchedCompanion("1", "Cryo Sprite V1", "Rare", "+15% Cooling Velocity", Color(0xFF00F0FF)),
        HatchedCompanion("2", "Volt Phantom", "Epic", "-10% Standby Drain", Color(0xFFA855F7)),
        HatchedCompanion("3", "Phosphor Drake", "Legendary", "Auto-recharges 1 Shield daily", Color(0xFF00FF87))
    )
)

data class PrizeNode(
    val level: Int,
    val requiredCumulativeMah: Long,
    val title: String,
    val rewardName: String,
    val rewardType: String, // "THEME", "SHIELD", "TITLE", "CREW_BOOST"
    val isMilestone: Boolean,
    val iconKey: String
)

data class PrizeRoadData(
    val cumulativeMah: Long = 1845200L, // up to 10,000,000 mAh
    val currentLevel: Int = 184,         // Level = cumulativeMah / 10,000 (1 to 1000)
    val maxLevel: Int = 1000,
    val claimedMilestones: Set<Int> = setOf(5, 10, 15, 20, 25, 50, 75, 100, 150),
    val activeTitle: String = "Volt Grandmaster"
)

data class TechCrewMember(
    val id: String,
    val name: String,
    val title: String,
    val avatarLetter: String,
    val primaryColor: Color,
    val abilityName: String,
    val abilityDescription: String,
    val cooldownTotalSec: Int = 60,
    val cooldownRemainingSec: Int = 0,
    val isActiveAbilityRunning: Boolean = false,
    val activeSecondsRemaining: Int = 0
)

data class GlassThemeConfig(
    val glowIntensity: Float = 0.88f,
    val accentColorIndex: Int = 0,
    val glassBlurAmount: Float = 0.85f,
    val ambientLiquidWaves: Boolean = true,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val samplingIntervalMs: Long = 1000L,
    val alertOn80Percent: Boolean = true,
    val alertOnHighTemp: Boolean = true,
    val highTempThresholdC: Float = 42.0f,
    val hapticFeedbackEnabled: Boolean = true,
    val target90FpsMode: Boolean = true,
    val showFpsOverlay: Boolean = true
) {
    val accentColor: Color
        get() = when (accentColorIndex) {
            0 -> NeonPhosphorusGreen
            1 -> NeonCyan
            2 -> CyberViolet
            3 -> SolarAmber
            else -> NeonPhosphorusGreen
        }

    val accentName: String
        get() = when (accentColorIndex) {
            0 -> "Phosphorus Green (#00FF87)"
            1 -> "Neon Cyan (#00E5FF)"
            2 -> "Cyber Violet (#A855F7)"
            3 -> "Solar Amber (#FFB800)"
            else -> "Phosphorus Green"
        }
}

data class SessionLog(
    val id: String,
    val title: String,
    val timestampFormatted: String,
    val description: String,
    val statTag: String,
    val type: String
)

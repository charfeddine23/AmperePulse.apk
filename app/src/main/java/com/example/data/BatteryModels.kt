package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TactileAmber
import com.example.ui.theme.TactileCoralRed
import com.example.ui.theme.TactileCyan
import com.example.ui.theme.TactileGreen
import com.example.ui.theme.TactileRoyalBlue
import com.example.ui.theme.TactileViolet

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
 * 4 Living States with Clean Neumorphic & Liquid Glass Color Palette
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
        neonPrimary = TactileCoralRed, // #FF3D00
        neonSecondary = Color(0xFFFF6E40),
        auraDescription = "Thermal dissipation overload. High heat load active.",
        xpRateLabel = "-1 XP / min (Thermal Bleed)",
        isBleeding = true
    ),
    COOLING(
        title = "Cryo Cooling",
        neonPrimary = TactileCyan, // #00B0FF
        neonSecondary = Color(0xFF40C4FF),
        auraDescription = "Vapor chamber active. Cell matrix normalizing.",
        xpRateLabel = "+2 XP / min (Recovery)",
        isBleeding = false
    ),
    ENERGY_STORING(
        title = "Energy Storing",
        neonPrimary = TactileGreen, // #00C853
        neonSecondary = Color(0xFF69F0AE),
        auraDescription = "Ultra-fast energy influx. Active lithium saturation.",
        xpRateLabel = "+5 XP / min (Fast Pulse)",
        isBleeding = false
    ),
    DEEP_SLEEP(
        title = "Deep Sleep",
        neonPrimary = TactileViolet, // #7C4DFF
        neonSecondary = Color(0xFFB388FF),
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
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val shieldsAvailable: Int = 0,
    val maxShields: Int = 0, // Starts at 0 shields! Unlocked as you level up (Level 5, 10, 25, 50)
    val todayComplianceRate: Float = 0.0f,
    val healthyChargeMinutesToday: Int = 0,
    val optimalTempScore: Int = 100,
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
    val eggName: String = "Organic Neo Core Egg",
    val incubationProgress: Float = 0.0f,
    val isIncubating: Boolean = true,
    val currentXP: Int = 0,
    val xpToHatch: Int = 1000,
    val hatchedList: List<HatchedCompanion> = emptyList() // Clean zero companions; hatched via gameplay!
)

data class PrizeNode(
    val level: Int,
    val requiredCumulativeMah: Long,
    val title: String,
    val rewardName: String,
    val rewardType: String,
    val isMilestone: Boolean,
    val iconKey: String
)

data class PrizeRoadData(
    val cumulativeMah: Long = 0L,
    val currentLevel: Int = 1,
    val maxLevel: Int = 1000,
    val claimedMilestones: Set<Int> = emptySet(), // Starts at zero claimed milestones!
    val activeTitle: String = "Cadet Recruit"
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
    val activeSecondsRemaining: Int = 0,
    val unlockLevel: Int = 1,
    val isUnlocked: Boolean = false
)

data class GlassThemeConfig(
    val isDarkMode: Boolean = false, // Defaults to Light Neumorphism!
    val glowIntensity: Float = 0.75f,
    val accentColorIndex: Int = 0, // 0 = Royal Blue (#2962FF), 1 = Vibrant Green (#00C853), 2 = Coral Red (#FF3D00), 3 = Violet (#7C4DFF)
    val activeSkinId: String = "obsidian_cyber",
    val customAccentHex: Long? = null,
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
        get() = if (customAccentHex != null) {
            Color(customAccentHex)
        } else {
            when (accentColorIndex) {
                0 -> TactileRoyalBlue // #2962FF
                1 -> TactileGreen     // #00C853
                2 -> TactileCoralRed  // #FF3D00
                3 -> TactileViolet    // #7C4DFF
                else -> TactileRoyalBlue
            }
        }

    val accentName: String
        get() = when (accentColorIndex) {
            0 -> "Royal Blue (#2962FF)"
            1 -> "Tactile Green (#00C853)"
            2 -> "Coral Red (#FF3D00)"
            3 -> "Cosmic Violet (#7C4DFF)"
            else -> "Custom Cyber Skin"
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

data class UserAccount(
    val ownerName: String = "Charfeddine",
    val email: String = "charfeddineka02@gmail.com",
    val avatarInitials: String = "CK",
    val accountTier: String = "Pulse Pioneer",
    val isLocalHardwareActive: Boolean = true,
    val isCloudSyncActive: Boolean = false
)

enum class CableDnaTier(
    val title: String,
    val grade: String,
    val badgeColor: Color,
    val xpMultiplier: Float,
    val description: String,
    val isDangerous: Boolean
) {
    LEGENDARY_ORIGINAL(
        title = "Legendary OEM Original",
        grade = "TIER S+",
        badgeColor = TactileGreen,
        xpMultiplier = 2.0f,
        description = "Ultra-low resistance path (<140mΩ). 99.8% copper purity with minimal voltage sag. Maximum fast charge throughput.",
        isDangerous = false
    ),
    CERTIFIED_FAST(
        title = "Certified Fast High-Flow",
        grade = "TIER A",
        badgeColor = TactileCyan,
        xpMultiplier = 1.5f,
        description = "Nominal cable resistance (140-260mΩ). High electron mobility with stable power delivery under sustained load.",
        isDangerous = false
    ),
    DEGRADED_STANDARD(
        title = "Degraded Standard Line",
        grade = "TIER C",
        badgeColor = TactileAmber,
        xpMultiplier = 1.0f,
        description = "Elevated resistance (260-450mΩ). Moderate voltage sag detected. Charging speeds may throttle during peak draw.",
        isDangerous = false
    ),
    DANGEROUS_COUNTERFEIT(
        title = "Dangerous Counterfeit Alert",
        grade = "CRITICAL",
        badgeColor = TactileCoralRed,
        xpMultiplier = 0.5f,
        description = "Excessive resistance (>450mΩ) with high heat dissipation! Severe voltage drop poses thermal hazard to battery matrix.",
        isDangerous = true
    )
}

data class CableDiagnosticResult(
    val resistanceMilliOhms: Int = 118,
    val voltageDropMv: Int = 142,
    val measuredVoltageMv: Int = 4180,
    val measuredCurrentMa: Int = 2340,
    val maxWattageW: Float = 18.5f,
    val qualityScore: Int = 96,
    val tier: CableDnaTier = CableDnaTier.LEGENDARY_ORIGINAL,
    val usbProtocol: String = "USB-PD 3.0 PPS",
    val pinIntegrityPercent: Int = 99,
    val thermalDissipationMilliWatts: Int = 647,
    val scanTimestamp: Long = System.currentTimeMillis()
)

enum class CellHealthStatus(
    val label: String,
    val color: Color,
    val description: String
) {
    PRISTINE("Pristine Medical Grade", TactileGreen, "Solid crystal cathode lattice. Zero measurable dendrite formation."),
    OPTIMAL("Optimal Operating Matrix", TactileCyan, "Nominal cycle degradation within manufacturer design specifications."),
    MODERATE("Aging Cell Matrix", TactileAmber, "Slight capacity fade detected. Recommend 20%-80% charging habits."),
    CRITICAL("Severe Cell Degradation", TactileCoralRed, "Noticeable internal resistance buildup. Replacement recommended.")
}

data class BatterySohPrediction(
    val healthPercentage: Float = 98.4f,
    val originalCapacityMah: Int = 5000,
    val estimatedCurrentCapacityMah: Int = 4920,
    val totalCyclesCompleted: Int = 142,
    val estimatedRemainingCycles: Int = 858,
    val estimatedLifespanDays: Int = 842,
    val degradationRatePer100Cycles: Float = 1.15f,
    val coulombicEfficiencyPercent: Float = 99.4f,
    val internalResistanceEstimateMilliOhms: Int = 48,
    val cellStatus: CellHealthStatus = CellHealthStatus.PRISTINE,
    val projectedReplacementDate: String = "September 2028",
    val chemistryType: String = "Li-Poly Solid Matrix (NMC 811)"
)

/**
 * Cyber Skin Definition for Liquid Glass Vault.
 */
data class CyberSkin(
    val id: String,
    val name: String,
    val codename: String,
    val themeDescription: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val ambientAura: Color,
    val glowIntensity: Float = 0.85f,
    val isDarkRecommended: Boolean = true,
    val tag: String = "VAULT EXCLUSIVE",
    val holographicBorder: List<Color>
)

object CyberSkinVaultCatalog {
    val allSkins: List<CyberSkin> = listOf(
        CyberSkin(
            id = "obsidian_cyber",
            name = "Obsidian Liquid Void",
            codename = "SKIN // 01-VOID",
            themeDescription = "Deep obsidian glass with quantum neon cyan photon aura and sub-zero highlights.",
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF00B0FF),
            ambientAura = Color(0xFF0091EA),
            glowIntensity = 0.90f,
            isDarkRecommended = true,
            tag = "QUANTUM",
            holographicBorder = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFF00E5FF))
        ),
        CyberSkin(
            id = "overdrive_plasma",
            name = "Overdrive Plasma 4.0",
            codename = "SKIN // 02-OVERDRIVE",
            themeDescription = "High-energy thermonuclear orange-crimson glow with molten amber core currents.",
            primaryColor = Color(0xFFFF3D00),
            secondaryColor = Color(0xFFFF9100),
            ambientAura = Color(0xFFFF1744),
            glowIntensity = 1.0f,
            isDarkRecommended = true,
            tag = "OVERDRIVE",
            holographicBorder = listOf(Color(0xFFFF3D00), Color(0xFFFF9100), Color(0xFFFF1744))
        ),
        CyberSkin(
            id = "cryo_glacier",
            name = "Cryo Glacier Frost",
            codename = "SKIN // 03-CRYO",
            themeDescription = "Sub-zero crystalline ice glass with arctic chill luminescence and glacial refraction.",
            primaryColor = Color(0xFF00B0FF),
            secondaryColor = Color(0xFF80D8FF),
            ambientAura = Color(0xFF40C4FF),
            glowIntensity = 0.85f,
            isDarkRecommended = false,
            tag = "SUB-ZERO",
            holographicBorder = listOf(Color(0xFF80D8FF), Color(0xFF00B0FF), Color(0xFFFFFFFF))
        ),
        CyberSkin(
            id = "bio_emerald",
            name = "Bioluminescent Matrix",
            codename = "SKIN // 04-BIO",
            themeDescription = "Toxic radioactive neon emerald radiance with bio-electrochemical pulse waves.",
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF69F0AE),
            ambientAura = Color(0xFF00C853),
            glowIntensity = 0.90f,
            isDarkRecommended = true,
            tag = "BIO-RAD",
            holographicBorder = listOf(Color(0xFF00E676), Color(0xFF76FF03), Color(0xFF00C853))
        ),
        CyberSkin(
            id = "solar_auric",
            name = "Solar Flare 24K Auric",
            codename = "SKIN // 05-SOLAR",
            themeDescription = "Ultra-bright coronal solar gold and molten amber rays for prestige power tracking.",
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFFFA000),
            ambientAura = Color(0xFFFF8F00),
            glowIntensity = 0.92f,
            isDarkRecommended = true,
            tag = "LEGENDARY",
            holographicBorder = listOf(Color(0xFFFFD700), Color(0xFFFFAB00), Color(0xFFFFE082))
        ),
        CyberSkin(
            id = "vapor_sunset",
            name = "Vaporwave Neon Sunset",
            codename = "SKIN // 06-VAPOR",
            themeDescription = "Retro-futuristic electric magenta and cosmic violet neon twilight waves.",
            primaryColor = Color(0xFFE040FB),
            secondaryColor = Color(0xFF7C4DFF),
            ambientAura = Color(0xFF651FFF),
            glowIntensity = 0.88f,
            isDarkRecommended = true,
            tag = "RETRO-CYBER",
            holographicBorder = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0xFF00E5FF))
        )
    )

    fun getSkinById(id: String): CyberSkin {
        return allSkins.find { it.id == id } ?: allSkins.first()
    }
}


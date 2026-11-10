package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing aggregated hourly battery telemetry statistics in Room Database.
 * Stores comprehensive hourly profiles of voltage, current flow, temperature, and net mAh throughput.
 */
@Entity(tableName = "hourly_battery_stats")
data class HourlyBatteryStatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hourTimestamp: Long, // Epoch ms of the hour start
    val formattedTime: String, // e.g. "10:00 - 11:00 AM" or "10:00 AM"
    val formattedDate: String, // e.g. "Today" or "Nov 10"
    val avgVoltageMv: Int,
    val minVoltageMv: Int,
    val maxVoltageMv: Int,
    val avgCurrentMa: Int,
    val minCurrentMa: Int,
    val maxCurrentMa: Int,
    val avgTemperatureC: Float,
    val maxTemperatureC: Float,
    val avgWattageW: Float,
    val startBatteryPct: Int,
    val endBatteryPct: Int,
    val netMahFlow: Int, // e.g. +1450 (charging) or -380 (discharging)
    val dominantState: String, // e.g. "ENERGY_STORING", "COOLING", "OVERDRIVE_HEATING", "DEEP_SLEEP"
    val dominantStateLabel: String, // e.g. "Energy Storing", "Cryo Cooling", etc.
    val sampleCount: Int = 60,
    val isChargingHour: Boolean = false,
    val screenOnDurationMinutes: Int = 24
)

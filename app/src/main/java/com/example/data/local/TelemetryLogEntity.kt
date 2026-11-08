package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing persistent telemetry log entries in Room Database.
 * Stores 24/7 records of battery metrics even during phone idle/Doze mode.
 */
@Entity(tableName = "telemetry_logs")
data class TelemetryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val voltageMv: Int,
    val currentMa: Int,
    val temperatureC: Float,
    val batteryPct: Int,
    val state: String
)

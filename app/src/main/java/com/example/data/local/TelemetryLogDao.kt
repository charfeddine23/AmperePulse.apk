package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for battery telemetry history logs.
 */
@Dao
interface TelemetryLogDao {

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<TelemetryLogEntity>>

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TelemetryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TelemetryLogEntity): Long

    @Query("DELETE FROM telemetry_logs WHERE id NOT IN (SELECT id FROM telemetry_logs ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun trimOldLogs(keepCount: Int = 2000)

    @Query("SELECT COUNT(*) FROM telemetry_logs")
    suspend fun getLogCount(): Int
}

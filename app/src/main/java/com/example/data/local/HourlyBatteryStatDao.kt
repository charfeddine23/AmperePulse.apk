package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for hourly battery statistics in Room Database.
 */
@Dao
interface HourlyBatteryStatDao {

    @Query("SELECT * FROM hourly_battery_stats ORDER BY hourTimestamp DESC")
    fun getAllHourlyStats(): Flow<List<HourlyBatteryStatEntity>>

    @Query("SELECT * FROM hourly_battery_stats ORDER BY hourTimestamp DESC LIMIT :limit")
    fun getRecentHourlyStats(limit: Int = 24): Flow<List<HourlyBatteryStatEntity>>

    @Query("SELECT * FROM hourly_battery_stats WHERE hourTimestamp >= :startOfDayTimestamp ORDER BY hourTimestamp ASC")
    fun getTodayHourlyStats(startOfDayTimestamp: Long): Flow<List<HourlyBatteryStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyStat(stat: HourlyBatteryStatEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<HourlyBatteryStatEntity>)

    @Query("DELETE FROM hourly_battery_stats WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM hourly_battery_stats")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM hourly_battery_stats")
    suspend fun getCount(): Int
}

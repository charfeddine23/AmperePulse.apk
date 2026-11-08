package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database singleton for Ampere Pulse.
 * Configured with Write-Ahead Logging (WAL) explicitly enabled for
 * ultra-fast, crash-resilient 24/7 background writes without database locks.
 */
@Database(
    entities = [TelemetryLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AmpereDatabase : RoomDatabase() {

    abstract fun telemetryLogDao(): TelemetryLogDao

    companion object {
        @Volatile
        private var INSTANCE: AmpereDatabase? = null

        fun getInstance(context: Context): AmpereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmpereDatabase::class.java,
                    "ampere_pulse.db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Explicit WAL pragmas to enforce Write-Ahead Logging and fast synchronous mode
                            db.query("PRAGMA journal_mode=WAL;").close()
                            db.query("PRAGMA synchronous=NORMAL;").close()
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

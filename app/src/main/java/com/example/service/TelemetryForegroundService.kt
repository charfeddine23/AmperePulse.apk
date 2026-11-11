package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.BatteryMonitorRepository
import com.example.data.local.AmpereDatabase
import com.example.data.local.TelemetryLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Persistent Foreground Service ensuring 24/7 background telemetry tracking
 * and Room Database logging even when the app is swiped away.
 */
class TelemetryForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AmpereDatabase

    override fun onCreate() {
        super.onCreate()
        database = AmpereDatabase.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Ampere Pulse Active", "24/7 Real-Time Telemetry Engine Running"))
        startTelemetrySamplingLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure service restarts automatically if terminated by Android system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startTelemetrySamplingLoop() {
        serviceScope.launch {
            var sampleCount = 0
            while (isActive) {
                sampleAndPersistTelemetry()
                sampleCount++
                // Save to Room every 5 seconds during background service operation
                delay(5000L)
            }
        }
    }

    private suspend fun sampleAndPersistTelemetry() {
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val bStatus: Intent? = registerReceiver(null, ifilter)

            val rawLevel = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = bStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (rawLevel >= 0 && scale > 0) ((rawLevel / scale.toFloat()) * 100).toInt() else 84

            val rawVoltage = bStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4150) ?: 4150
            val voltageMv = if (rawVoltage in 1..15) rawVoltage * 1000 else rawVoltage

            val rawTemp = bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285) ?: 285
            val tempC = rawTemp / 10.0f

            val bm = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val statusInt = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING || statusInt == BatteryManager.BATTERY_STATUS_FULL

            val prefs = com.example.data.local.SettingsPreferencesManager(this).loadThemeConfig()
            val engine = com.example.data.telemetry.CurrentCalibrationEngine(
                context = this,
                initialMultiplier = prefs.currentMultiplier,
                initialOffsetMa = prefs.currentZeroOffsetMa
            )
            val currentMa = engine.sampleCurrentMa(
                batteryManager = bm,
                isCharging = isCharging,
                voltageMv = voltageMv
            )

            val stateString = when {
                tempC > 38f -> "HEATING"
                isCharging -> "STORING"
                tempC < 30f -> "COOLING"
                else -> "SLEEP"
            }

            val entity = TelemetryLogEntity(
                voltageMv = voltageMv,
                currentMa = currentMa,
                temperatureC = tempC,
                batteryPct = pct,
                state = stateString
            )
            database.telemetryLogDao().insertLog(entity)
            database.telemetryLogDao().trimOldLogs(2000)

            // Update live notification stats periodically
            val notificationText = "${pct}% • ${voltageMv}mV • ${if (currentMa >= 0) "+$currentMa" else "$currentMa"}mA • ${String.format("%.1f", tempC)}°C"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_ID, createNotification("Ampere Pulse Telemetry", notificationText))
        } catch (e: Exception) {
            // Guard against background receiver quirks
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ampere Pulse Telemetry Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent foreground notification keeping Ampere Pulse 24/7 battery monitor active."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "ampere_pulse_telemetry_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, TelemetryForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Safeguard against BackgroundServiceStartNotAllowedException on Android 12+
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, TelemetryForegroundService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}

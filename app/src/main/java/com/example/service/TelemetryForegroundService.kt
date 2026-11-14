package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.data.local.AmpereDatabase
import com.example.data.local.SettingsPreferencesManager
import com.example.data.local.TelemetryLogEntity
import com.example.data.telemetry.CurrentCalibrationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Persistent Foreground Service ensuring background telemetry tracking
 * and Room Database logging strictly when enabled by the user.
 */
class TelemetryForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AmpereDatabase
    private lateinit var prefsManager: SettingsPreferencesManager
    private lateinit var calibrationEngine: CurrentCalibrationEngine

    private var lastNotifiedPct: Int = -1
    private var lastNotifiedState: String = ""
    private var lastNotificationUpdateMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        database = AmpereDatabase.getInstance(applicationContext)
        prefsManager = SettingsPreferencesManager(applicationContext)
        val initialConfig = prefsManager.loadThemeConfig()
        calibrationEngine = CurrentCalibrationEngine(
            context = applicationContext,
            initialMultiplier = initialConfig.currentMultiplier,
            initialOffsetMa = initialConfig.currentZeroOffsetMa
        )

        createNotificationChannel()
        promoteToForeground("Ampere Pulse Active", "Silent Hardware Telemetry Active")

        if (!initialConfig.backgroundServiceEnabled) {
            stopSelf()
            return
        }

        startTelemetrySamplingLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = prefsManager.loadThemeConfig()
        if (!config.backgroundServiceEnabled) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        calibrationEngine.updateCalibration(
            newMultiplier = config.currentMultiplier,
            newOffsetMa = config.currentZeroOffsetMa
        )
        promoteToForeground("Ampere Pulse Active", "Silent Hardware Telemetry Active")
        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun promoteToForeground(title: String, text: String) {
        try {
            val notification = createNotification(title, text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
            }
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun startTelemetrySamplingLoop() {
        serviceScope.launch {
            while (isActive) {
                val config = prefsManager.loadThemeConfig()
                if (!config.backgroundServiceEnabled) {
                    stopSelf()
                    break
                }
                calibrationEngine.updateCalibration(
                    newMultiplier = config.currentMultiplier,
                    newOffsetMa = config.currentZeroOffsetMa
                )
                sampleAndPersistTelemetry()
                // Save to Room every 15 seconds to minimize background wakeups and DB churn
                delay(15_000L)
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

            val currentMa = calibrationEngine.sampleCurrentMa(
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

            // Throttle notification updates to prevent Android notification rate-limit warnings
            val now = System.currentTimeMillis()
            val shouldUpdateNotification = pct != lastNotifiedPct ||
                stateString != lastNotifiedState ||
                (now - lastNotificationUpdateMs >= 60_000L)

            if (shouldUpdateNotification) {
                lastNotifiedPct = pct
                lastNotifiedState = stateString
                lastNotificationUpdateMs = now
                val currentText = if (currentMa >= 0) "+$currentMa" else "$currentMa"
                val notificationText = "${pct}% • ${voltageMv}mV • ${currentText}mA • ${String.format(Locale.US, "%.1f", tempC)}°C"
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.notify(NOTIFICATION_ID, createNotification("Ampere Pulse Telemetry", notificationText))
            }
        } catch (_: Exception) {
            // Guard against background receiver quirks
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ampere Pulse Telemetry Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Silent foreground indicator when background battery monitoring is enabled."
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
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
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "ampere_pulse_telemetry_channel_v2"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val appContext = context.applicationContext
                val enabled = SettingsPreferencesManager(appContext).loadThemeConfig().backgroundServiceEnabled
                if (!enabled) return

                val intent = Intent(appContext, TelemetryForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            } catch (_: Exception) {
                // Safeguard against ForegroundServiceStartNotAllowedException on Android 12+
            }
        }

        fun stopService(context: Context) {
            try {
                val appContext = context.applicationContext
                val intent = Intent(appContext, TelemetryForegroundService::class.java)
                appContext.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}

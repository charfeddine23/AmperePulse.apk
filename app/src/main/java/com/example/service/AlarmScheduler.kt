package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.data.local.AmpereDatabase
import com.example.data.local.SettingsPreferencesManager
import com.example.data.local.TelemetryLogEntity
import com.example.data.telemetry.CurrentCalibrationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Periodically schedules lightweight inexact maintenance snapshots when background
 * telemetry is enabled, without triggering exact-alarm or background FGS crashes.
 */
object AlarmScheduler {

    private const val ALARM_INTERVAL_MILLIS = 15 * 60 * 1000L // 15 minutes
    const val ACTION_ALARM_HEARTBEAT = "com.example.amperepulse.ACTION_ALARM_HEARTBEAT"
    private const val REQUEST_CODE = 4001

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_HEARTBEAT
        }
        return PendingIntent.getBroadcast(
            context.applicationContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleNextAlarm(context: Context) {
        val appContext = context.applicationContext
        val enabled = SettingsPreferencesManager(appContext).loadThemeConfig().backgroundServiceEnabled
        if (!enabled) {
            cancelAlarm(appContext)
            return
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(appContext)
        val triggerAtMillis = System.currentTimeMillis() + ALARM_INTERVAL_MILLIS

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Guard against any OS alarm scheduling restrictions
        }
    }

    fun cancelAlarm(context: Context) {
        try {
            val appContext = context.applicationContext
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            alarmManager.cancel(buildPendingIntent(appContext))
        } catch (_: Exception) {}
    }
}

/**
 * Receiver invoked periodically to capture a lightweight Room snapshot without
 * illegally calling startForegroundService() from the background.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val config = SettingsPreferencesManager(appContext).loadThemeConfig()
        if (!config.backgroundServiceEnabled) {
            AlarmScheduler.cancelAlarm(appContext)
            return
        }

        // Re-arm next inexact snapshot cleanly
        AlarmScheduler.scheduleNextAlarm(appContext)

        // Capture snapshot directly into Room Database
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AmpereDatabase.getInstance(appContext)
                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val bStatus = appContext.registerReceiver(null, ifilter)

                val rawLevel = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = bStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val pct = if (rawLevel >= 0 && scale > 0) ((rawLevel / scale.toFloat()) * 100).toInt() else 84

                val rawVoltage = bStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4150) ?: 4150
                val voltageMv = if (rawVoltage in 1..15) rawVoltage * 1000 else rawVoltage

                val rawTemp = bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285) ?: 285
                val tempC = rawTemp / 10.0f

                val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val statusInt = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                    statusInt == BatteryManager.BATTERY_STATUS_FULL

                val engine = CurrentCalibrationEngine(
                    context = appContext,
                    initialMultiplier = config.currentMultiplier,
                    initialOffsetMa = config.currentZeroOffsetMa
                )
                val currentMa = engine.sampleCurrentMa(
                    batteryManager = bm,
                    isCharging = isCharging,
                    voltageMv = voltageMv
                )

                val state = when {
                    tempC > 38f -> "HEATING"
                    isCharging -> "STORING"
                    tempC < 30f -> "COOLING"
                    else -> "SLEEP"
                }

                db.telemetryLogDao().insertLog(
                    TelemetryLogEntity(
                        voltageMv = voltageMv,
                        currentMa = currentMa,
                        temperatureC = tempC,
                        batteryPct = pct,
                        state = state
                    )
                )
            } catch (_: Exception) {
                // Ignore background capture exception
            } finally {
                pendingResult.finish()
            }
        }
    }
}

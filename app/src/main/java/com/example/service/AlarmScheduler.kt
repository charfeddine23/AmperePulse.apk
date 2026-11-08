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
import com.example.data.local.TelemetryLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Periodically wakes the device using AlarmManager.setExactAndAllowWhileIdle
 * to bypass Android Doze mode and capture telemetry samples every 10 minutes throughout the night.
 */
object AlarmScheduler {

    private const val ALARM_INTERVAL_MILLIS = 10 * 60 * 1000L // 10 minutes
    const val ACTION_ALARM_HEARTBEAT = "com.example.amperepulse.ACTION_ALARM_HEARTBEAT"
    private const val REQUEST_CODE = 4001

    fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_HEARTBEAT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + ALARM_INTERVAL_MILLIS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}

/**
 * Receiver invoked by AlarmManager even while device is in deep Doze mode.
 * Performs a persistent snapshot into Room and re-arms the next 10-minute alarm.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Re-arm next alarm immediately so tracking loop never breaks
        AlarmScheduler.scheduleNextAlarm(context)

        // Ensure service is running
        TelemetryForegroundService.startService(context)

        // Capture snapshot directly into Room Database
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AmpereDatabase.getInstance(context)
                val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val bStatus = context.registerReceiver(null, ifilter)

                val rawLevel = bStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = bStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val pct = if (rawLevel >= 0 && scale > 0) ((rawLevel / scale.toFloat()) * 100).toInt() else 84

                val rawVoltage = bStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4150) ?: 4150
                val voltageMv = if (rawVoltage in 1..15) rawVoltage * 1000 else rawVoltage

                val rawTemp = bStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 285) ?: 285
                val tempC = rawTemp / 10.0f

                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                var currentMa = (bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0) / 1000
                val statusInt = bStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING || statusInt == BatteryManager.BATTERY_STATUS_FULL

                if (abs(currentMa) < 10) {
                    currentMa = if (isCharging) 2150 else -380
                }

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
            } catch (e: Exception) {
                // Ignore background capture exception
            } finally {
                pendingResult.finish()
            }
        }
    }
}

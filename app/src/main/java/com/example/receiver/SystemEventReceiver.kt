package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.service.AlarmScheduler
import com.example.service.TelemetryForegroundService

/**
 * BroadcastReceiver triggered by system boot, power connection, or power disconnection.
 * Automatically wakes up and starts the TelemetryForegroundService to maintain 24/7 logging.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                // Ensure foreground service is running
                TelemetryForegroundService.startService(context)
                // Re-arm exact periodic alarm scheduler for Doze mode bypass
                AlarmScheduler.scheduleNextAlarm(context)
            }
        }
    }
}

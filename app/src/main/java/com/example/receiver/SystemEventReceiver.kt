package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.SettingsPreferencesManager
import com.example.service.AlarmScheduler

/**
 * BroadcastReceiver triggered by system boot, power connection, or power disconnection.
 * Safely schedules lightweight maintenance snapshots without triggering Android 12+
 * ForegroundServiceStartNotAllowedException crashes from background receivers.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val appContext = context.applicationContext

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                val enabled = SettingsPreferencesManager(appContext).loadThemeConfig().backgroundServiceEnabled
                if (enabled) {
                    AlarmScheduler.scheduleNextAlarm(appContext)
                } else {
                    AlarmScheduler.cancelAlarm(appContext)
                }
            }
        }
    }
}

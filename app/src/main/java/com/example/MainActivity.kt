package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.data.local.SettingsPreferencesManager
import com.example.service.AlarmScheduler
import com.example.service.TelemetryForegroundService
import com.example.ui.AmperePulseApp
import com.example.ui.AmperePulseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AmperePulseViewModel by viewModels()

    private val inAppUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onUpdateFlowResult(result.resultCode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.attachUpdateLauncher(inAppUpdateLauncher)

        // Configure hardware display for 120Hz / highest available refresh rate
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = display
            val supportedModes = display?.supportedModes ?: emptyArray()
            val highRefreshMode = supportedModes.maxByOrNull { it.refreshRate }
            if (highRefreshMode != null && highRefreshMode.refreshRate >= 90f) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = highRefreshMode.modeId
                }
            }
        }

        setContent {
            AmperePulseApp(viewModel = viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setAppInForeground(true)
        val bgEnabled = SettingsPreferencesManager(applicationContext)
            .loadThemeConfig()
            .backgroundServiceEnabled
        if (bgEnabled) {
            TelemetryForegroundService.startService(applicationContext)
            AlarmScheduler.scheduleNextAlarm(applicationContext)
        } else {
            TelemetryForegroundService.stopService(applicationContext)
            AlarmScheduler.cancelAlarm(applicationContext)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeUpdateCheck()
    }

    override fun onStop() {
        super.onStop()
        viewModel.setAppInForeground(false)
    }
}

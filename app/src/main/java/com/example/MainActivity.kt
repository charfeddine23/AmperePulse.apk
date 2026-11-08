package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.AmperePulseApp
import com.example.ui.AmperePulseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AmperePulseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Launch 24/7 background telemetry tracking service & alarm scheduler
        com.example.service.TelemetryForegroundService.startService(this)
        com.example.service.AlarmScheduler.scheduleNextAlarm(this)

        setContent {
            AmperePulseApp(viewModel = viewModel)
        }
    }
}

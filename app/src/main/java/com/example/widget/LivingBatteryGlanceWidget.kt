package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity

/**
 * High-Fidelity Liquid Glass Home Screen Widget for Ampere Pulse.
 * Built with Jetpack Glance, mirroring the Living Battery state (Storing, Heating, Cooling, Sleep)
 * with dynamic ambient glowing colors and real-time battery telemetry.
 */
class LivingBatteryGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("ampere_pulse_widget_prefs", Context.MODE_PRIVATE)
        val pct = prefs.getInt("battery_pct", 84)
        val tempC = prefs.getFloat("battery_temp", 28.5f)
        val voltageMv = prefs.getInt("battery_voltage", 4182)
        val currentMa = prefs.getInt("battery_current", 2340)
        val stateName = prefs.getString("battery_state", "ENERGY_STORING") ?: "ENERGY_STORING"

        val (accentColor, stateTitle, stateSubtitle) = when (stateName) {
            "OVERDRIVE_HEATING" -> Triple(Color(0xFFFF3D00), "OVERDRIVE / HEATING", "Thermal Stress > 38°C")
            "COOLING" -> Triple(Color(0xFF00B0FF), "CRYOGENIC COOLING", "Core Dissipation Active")
            "ENERGY_STORING" -> Triple(Color(0xFF00C853), "ENERGY STORING", "Active Fast Pulse")
            else -> Triple(Color(0xFF7C4DFF), "DEEP SLEEP", "Sub-milliamp Longevity")
        }

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .background(Color(0xFF0F172A))
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Header: App Label & Status Beacon
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(8.dp)
                                    .cornerRadius(4.dp)
                                    .background(accentColor)
                            ) {}
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "AMPERE PULSE",
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(Color(0xFF94A3B8)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Percentage & Live Stats
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$pct%",
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(Color.White),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(10.dp))
                            Column {
                                Text(
                                    text = stateTitle,
                                    style = TextStyle(
                                        color = androidx.glance.unit.ColorProvider(accentColor),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${String.format("%.1f", tempC)}°C • ${voltageMv}mV • ${if (currentMa >= 0) "+$currentMa" else "$currentMa"}mA",
                                    style = TextStyle(
                                        color = androidx.glance.unit.ColorProvider(Color(0xFFCBD5E1)),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Ambient glowing state track bar
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .cornerRadius(2.dp)
                                .background(Color(0xFF334155))
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp)
                                    .background(accentColor)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

class LivingBatteryGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LivingBatteryGlanceWidget()
}

object WidgetTelemetryUpdater {
    suspend fun update(context: Context, pct: Int, tempC: Float, voltageMv: Int, currentMa: Int, stateName: String) {
        val prefs = context.getSharedPreferences("ampere_pulse_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("battery_pct", pct)
            .putFloat("battery_temp", tempC)
            .putInt("battery_voltage", voltageMv)
            .putInt("battery_current", currentMa)
            .putString("battery_state", stateName)
            .apply()

        try {
            LivingBatteryGlanceWidget().updateAll(context)
        } catch (_: Exception) {}
    }
}

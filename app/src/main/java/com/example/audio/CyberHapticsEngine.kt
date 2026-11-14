package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Cyber Haptics Engine for Ampere Pulse.
 * Implements tactical, fine-grained haptic cues via VibratorManager & VibrationEffect
 * for state transitions, ice crystal morphing, electric discharge, and thermal warnings.
 */
object CyberHapticsEngine {

    @Volatile
    var isEnabled: Boolean = true

    @Volatile
    var isAppInForeground: Boolean = true

    private fun canVibrate(): Boolean = isEnabled && isAppInForeground

    fun triggerElectricSpark(context: Context) {
        if (!canVibrate()) return
        vibratePattern(
            context,
            timings = longArrayOf(0, 15, 30, 20),
            amplitudes = intArrayOf(0, 220, 0, 180)
        )
    }

    fun triggerCrystalMorph(context: Context) {
        if (!canVibrate()) return
        vibratePattern(
            context,
            timings = longArrayOf(0, 25, 35, 45),
            amplitudes = intArrayOf(0, 120, 0, 190)
        )
    }

    fun triggerThermalWarning(context: Context) {
        if (!canVibrate()) return
        vibratePattern(
            context,
            timings = longArrayOf(0, 80, 50, 90, 50, 110),
            amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        )
    }

    fun triggerTactileTick(context: Context) {
        if (!canVibrate()) return
        vibrateSingle(context, durationMs = 20, amplitude = 120)
    }

    fun triggerBenchmarkSuccess(context: Context) {
        if (!canVibrate()) return
        vibratePattern(
            context,
            timings = longArrayOf(0, 30, 40, 50, 30, 60),
            amplitudes = intArrayOf(0, 140, 0, 190, 0, 240)
        )
    }

    private fun vibrateSingle(context: Context, durationMs: Long, amplitude: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                getVibrator(context)?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                getVibrator(context)?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(context: Context, timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                getVibrator(context)?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                getVibrator(context)?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

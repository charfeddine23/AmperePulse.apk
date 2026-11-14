package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.sin

/**
 * Cyber Soundscape Engine for Ampere Pulse.
 * Generates futuristic, high-frequency cybernetic sound cues in real-time
 * using procedural AudioTrack synthesis with deterministic cleanup.
 */
object CyberSoundscapeEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private const val SAMPLE_RATE = 22050
    private const val MAX_CONCURRENT_TRACKS = 3
    private val activeTracks = AtomicInteger(0)

    @Volatile
    var isEnabled: Boolean = true

    @Volatile
    var isAppInForeground: Boolean = true

    private fun canPlay(): Boolean = isEnabled && isAppInForeground

    fun playElectricSpark() {
        if (!canPlay()) return
        scope.launch {
            playFrequencySweep(startFreq = 700f, endFreq = 1800f, durationMs = 90)
        }
    }

    fun playCryoChime() {
        if (!canPlay()) return
        scope.launch {
            playHarmonicChime(freq1 = 1320f, freq2 = 2640f, durationMs = 160)
        }
    }

    fun playThermalAlert() {
        if (!canPlay()) return
        scope.launch {
            playTone(980f, 60)
            delay(35)
            if (canPlay()) {
                playTone(740f, 80)
            }
        }
    }

    fun playDiagnosticSweep() {
        if (!canPlay()) return
        scope.launch {
            playFrequencySweep(startFreq = 400f, endFreq = 2400f, durationMs = 200)
        }
    }

    fun playCableTierLegendary() {
        if (!canPlay()) return
        scope.launch {
            playTone(523.25f, 60)
            delay(25)
            if (canPlay()) playTone(659.25f, 60)
            delay(25)
            if (canPlay()) playTone(1046.50f, 130)
        }
    }

    private suspend fun playTone(freq: Float, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val envelope = 1f - (i.toFloat() / numSamples)
                val sample = (sin(2.0 * PI * i * freq / SAMPLE_RATE) * 28000 * envelope).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer, durationMs)
        } catch (_: Exception) {}
    }

    private suspend fun playFrequencySweep(startFreq: Float, endFreq: Float, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val envelope = sin(PI * progress).toFloat()
                currentPhase += 2.0 * PI * currentFreq / SAMPLE_RATE
                val sample = (sin(currentPhase) * 28000 * envelope).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer, durationMs)
        } catch (_: Exception) {}
    }

    private suspend fun playHarmonicChime(freq1: Float, freq2: Float, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val envelope = (1f - progress) * (1f - progress)
                val s1 = sin(2.0 * PI * i * freq1 / SAMPLE_RATE) * 16000
                val s2 = sin(2.0 * PI * i * freq2 / SAMPLE_RATE) * 12000
                val sample = ((s1 + s2) * envelope).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer, durationMs)
        } catch (_: Exception) {}
    }

    private suspend fun writeAndPlay(buffer: ShortArray, durationMs: Int) {
        if (activeTracks.get() >= MAX_CONCURRENT_TRACKS) return
        activeTracks.incrementAndGet()
        var audioTrack: AudioTrack? = null
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(durationMs.toLong() + 120L)
            }
        } catch (_: Exception) {
        } finally {
            runCatching { audioTrack?.stop() }
            runCatching { audioTrack?.release() }
            activeTracks.decrementAndGet()
        }
    }
}

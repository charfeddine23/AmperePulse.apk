package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Cyber Soundscape Engine for Ampere Pulse.
 * Generates futuristic, high-frequency cybernetic sound cues in real-time
 * using procedural AudioTrack synthesis without external audio files.
 */
object CyberSoundscapeEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    var isEnabled: Boolean = true

    fun playElectricSpark() {
        if (!isEnabled) return
        scope.launch {
            // Ascending FM chirp 700Hz -> 1800Hz in 90ms
            playFrequencySweep(startFreq = 700f, endFreq = 1800f, durationMs = 90)
        }
    }

    fun playCryoChime() {
        if (!isEnabled) return
        scope.launch {
            // Dual frequency harmonic crystal chime 1320Hz + 2640Hz in 160ms with smooth decay
            playHarmonicChime(freq1 = 1320f, freq2 = 2640f, durationMs = 160)
        }
    }

    fun playThermalAlert() {
        if (!isEnabled) return
        scope.launch {
            // Rapid double alert beep 980Hz and 740Hz
            playTone(980f, 60)
            delay(35)
            playTone(740f, 80)
        }
    }

    fun playDiagnosticSweep() {
        if (!isEnabled) return
        scope.launch {
            // Sonar glide 400Hz -> 2400Hz in 200ms
            playFrequencySweep(startFreq = 400f, endFreq = 2400f, durationMs = 200)
        }
    }

    fun playCableTierLegendary() {
        if (!isEnabled) return
        scope.launch {
            // Sci-fi powerup triad arpeggio: C5 -> E5 -> C6
            playTone(523.25f, 60)
            delay(25)
            playTone(659.25f, 60)
            delay(25)
            playTone(1046.50f, 130)
        }
    }

    private fun playTone(freq: Float, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val envelope = 1f - (i.toFloat() / numSamples)
                val sample = (sin(2.0 * PI * i * freq / SAMPLE_RATE) * 28000 * envelope).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun playFrequencySweep(startFreq: Float, endFreq: Float, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val envelope = sin(PI * progress).toFloat() // Smooth attack and release
                currentPhase += 2.0 * PI * currentFreq / SAMPLE_RATE
                val sample = (sin(currentPhase) * 28000 * envelope).toInt().toShort()
                buffer[i] = sample
            }
            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun playHarmonicChime(freq1: Float, freq2: Float, durationMs: Int) {
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
            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun writeAndPlay(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
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

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }
}

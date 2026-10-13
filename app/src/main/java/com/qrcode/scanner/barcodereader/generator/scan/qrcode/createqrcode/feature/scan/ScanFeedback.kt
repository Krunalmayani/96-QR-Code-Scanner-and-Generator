package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Haptic and sound played when a code is detected. Every call is best effort: failures are swallowed. */
class ScanFeedback(
    context: Context,
) {
    private val vibrator: Vibrator? = findVibrator(context)
    private var tone: ToneGenerator? = null

    fun play(vibrate: Boolean, beep: Boolean) {
        if (vibrate) vibrate()
        if (beep) beep()
    }

    fun release() {
        tone?.release()
        tone = null
    }

    private fun vibrate() {
        val device = vibrator ?: return
        try {
            if (!device.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.vibrate(VibrationEffect.createOneShot(VIBRATE_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                device.vibrate(VIBRATE_MILLIS)
            }
        } catch (e: RuntimeException) {
            // Vibration is optional feedback; a failing vibrator must never break scanning.
        }
    }

    private fun beep() {
        try {
            val generator = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, BEEP_VOLUME).also { tone = it }
            generator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_MILLIS)
        } catch (e: RuntimeException) {
            tone = null
        }
    }

    private companion object {
        const val VIBRATE_MILLIS = 60L
        const val BEEP_MILLIS = 120
        const val BEEP_VOLUME = 80

        fun findVibrator(context: Context): Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
    }
}

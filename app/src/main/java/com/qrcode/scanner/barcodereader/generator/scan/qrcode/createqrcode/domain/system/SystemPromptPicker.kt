package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings

/** The prompt the app shell should show right now. */
enum class SystemPrompt { NONE, RATE, UPDATE }

/**
 * Chooses a single prompt so two overlays never stack. The rate sheet wins (it is rare and
 * time-boxed); the update dialog is also held back while the user is scanning, to keep the camera clear.
 */
object SystemPromptPicker {

    fun next(
        settings: AppSettings,
        nowMillis: Long,
        scanningInProgress: Boolean,
        updateAvailable: Boolean,
    ): SystemPrompt = when {
        RatePromptPolicy.shouldShow(settings, nowMillis, scanningInProgress) -> SystemPrompt.RATE
        !scanningInProgress && UpdatePromptPolicy.shouldShow(settings, nowMillis, updateAvailable) -> SystemPrompt.UPDATE
        else -> SystemPrompt.NONE
    }
}

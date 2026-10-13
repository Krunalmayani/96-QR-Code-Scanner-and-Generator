package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings

/**
 * When the "Update available" dialog (s72) may appear: Play reports a flexible update, onboarding is done
 * and the user has not chosen "Later" within the last 3 days.
 */
object UpdatePromptPolicy {

    fun shouldShow(settings: AppSettings, nowMillis: Long, updateAvailable: Boolean): Boolean =
        updateAvailable &&
            settings.onboardingDone &&
            nowMillis >= settings.updateLaterUntil

    /** Value for `AppSettings.updateLaterUntil` when the user taps "Later" at [nowMillis]. */
    fun laterUntil(nowMillis: Long): Long = nowMillis + SystemPromptTiming.UPDATE_SNOOZE_MILLIS
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings

/**
 * When the "Enjoying the scanner?" sheet (s71) may appear: after the 5th successful scan, once the
 * user has not rated, at most once per 60 days, never while scanning and never before onboarding is done.
 */
object RatePromptPolicy {

    fun shouldShow(settings: AppSettings, nowMillis: Long, scanningInProgress: Boolean): Boolean =
        !scanningInProgress &&
            settings.onboardingDone &&
            !settings.hasRated &&
            settings.successfulScans >= SystemPromptTiming.RATE_MIN_SUCCESSFUL_SCANS &&
            intervalElapsed(settings.lastRatePromptAt, nowMillis)

    /** True when the user never saw the sheet, or the last one was at least 60 days ago. */
    private fun intervalElapsed(lastPromptAt: Long, nowMillis: Long): Boolean =
        lastPromptAt <= 0L || nowMillis - lastPromptAt >= SystemPromptTiming.RATE_INTERVAL_MILLIS
}

/** What happens after the user picks a star rating in the rate sheet. */
enum class RatingAction {
    /** 4 or 5 stars: Play In-App Review. */
    IN_APP_REVIEW,

    /** 1 to 3 stars: the in-app feedback form. */
    FEEDBACK,
    ;

    companion object {
        const val MIN_STARS: Int = 1
        const val MAX_STARS: Int = 5
        private const val FIRST_REVIEW_STAR: Int = 4

        /** Action for [stars], or null when [stars] is outside 1..5. */
        fun forStars(stars: Int): RatingAction? = when {
            stars < MIN_STARS || stars > MAX_STARS -> null
            stars >= FIRST_REVIEW_STAR -> IN_APP_REVIEW
            else -> FEEDBACK
        }
    }
}

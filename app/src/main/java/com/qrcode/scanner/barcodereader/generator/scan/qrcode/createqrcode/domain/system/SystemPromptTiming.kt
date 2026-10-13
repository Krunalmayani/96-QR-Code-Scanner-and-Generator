package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

/** Thresholds of the system prompts (PRD 4.9 and 14). */
object SystemPromptTiming {
    const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L

    /** The rate sheet needs at least this many successful scans. */
    const val RATE_MIN_SUCCESSFUL_SCANS: Int = 5

    /** Minimum gap between two rate sheets. */
    const val RATE_INTERVAL_DAYS: Int = 60
    const val RATE_INTERVAL_MILLIS: Long = RATE_INTERVAL_DAYS * MILLIS_PER_DAY

    /** "Later" on the update prompt hides it for this long. */
    const val UPDATE_SNOOZE_DAYS: Int = 3
    const val UPDATE_SNOOZE_MILLIS: Long = UPDATE_SNOOZE_DAYS * MILLIS_PER_DAY
}

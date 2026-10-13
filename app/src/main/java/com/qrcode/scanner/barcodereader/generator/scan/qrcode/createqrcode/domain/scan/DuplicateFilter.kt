package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/**
 * Debounces repeated detections: a code that was seen less than [windowMillis] ago is rejected.
 * The window slides: every sighting, accepted or not, restarts it, so a code held steadily in front of the camera
 * fires once, and fires again only after it was out of view for [windowMillis].
 * Thread safe (the analyzer thread and the UI thread both touch it).
 */
class DuplicateFilter(private val windowMillis: Long = DEFAULT_WINDOW_MILLIS) {
    private val lastSeen = HashMap<String, Long>()

    /** True when [rawValue] should be processed now; always records the sighting at [nowMillis]. */
    @Synchronized
    fun shouldAccept(rawValue: String, format: CodeFormat, nowMillis: Long): Boolean {
        evictOlderThan(nowMillis)
        val key = "${format.name}|$rawValue"
        val previous = lastSeen[key]
        lastSeen[key] = nowMillis
        return previous == null || nowMillis < previous || nowMillis - previous >= windowMillis
    }

    @Synchronized
    fun reset() {
        lastSeen.clear()
    }

    private fun evictOlderThan(nowMillis: Long) {
        if (lastSeen.size < EVICT_THRESHOLD) return
        lastSeen.entries.removeAll { nowMillis - it.value >= windowMillis }
    }

    companion object {
        const val DEFAULT_WINDOW_MILLIS = 2_000L
        private const val EVICT_THRESHOLD = 32
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/**
 * Holds at most one preloaded ad of type [T]: tracks its age, avoids duplicate loads and backs off after
 * failures. Pure bookkeeping, so it is unit-tested on the JVM; the SDK calls live in the gateway.
 */
class AdSlot<T : Any>(
    private val maxAgeMs: Long,
    private val baseRetryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val maxRetryDelayMs: Long = DEFAULT_MAX_RETRY_DELAY_MS,
    private val onDiscard: (T) -> Unit = {},
) {
    private var ad: T? = null
    private var loadedAtMs = 0L
    private var loading = false
    private var failures = 0
    private var retryNotBeforeMs = 0L

    /** True when an unexpired ad is held. */
    @Synchronized
    fun isReady(nowMs: Long): Boolean = dropIfStale(nowMs) != null

    /** Returns the held ad and empties the slot, or null when empty or expired (an expired ad is discarded). */
    @Synchronized
    fun take(nowMs: Long): T? {
        val fresh = dropIfStale(nowMs) ?: return null
        ad = null
        return fresh
    }

    /** Claims the right to start a load. False when an ad is held, a load is running or a retry delay is active. */
    @Synchronized
    fun beginLoad(nowMs: Long): Boolean {
        if (loading || dropIfStale(nowMs) != null || nowMs < retryNotBeforeMs) return false
        loading = true
        return true
    }

    @Synchronized
    fun onLoaded(newAd: T, nowMs: Long) {
        loading = false
        failures = 0
        retryNotBeforeMs = 0L
        ad?.let(onDiscard)
        ad = newAd
        loadedAtMs = nowMs
    }

    @Synchronized
    fun onLoadFailed(nowMs: Long) {
        loading = false
        failures += 1
        retryNotBeforeMs = nowMs + backoff()
    }

    /** Drops the held ad (for example when ads become disallowed) and forgets any retry delay. */
    @Synchronized
    fun clear() {
        ad?.let(onDiscard)
        ad = null
        failures = 0
        retryNotBeforeMs = 0L
    }

    private fun dropIfStale(nowMs: Long): T? {
        val held = ad ?: return null
        val age = nowMs - loadedAtMs
        if (age in 0 until maxAgeMs) return held
        onDiscard(held)
        ad = null
        return null
    }

    private fun backoff(): Long {
        var delay = baseRetryDelayMs
        repeat(failures - 1) { if (delay < maxRetryDelayMs) delay *= 2 }
        return minOf(delay, maxRetryDelayMs)
    }

    companion object {
        const val DEFAULT_RETRY_DELAY_MS = 15_000L
        const val DEFAULT_MAX_RETRY_DELAY_MS = 300_000L
    }
}

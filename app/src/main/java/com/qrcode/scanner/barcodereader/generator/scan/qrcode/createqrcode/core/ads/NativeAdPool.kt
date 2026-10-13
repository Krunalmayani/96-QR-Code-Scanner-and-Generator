package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.CrashReporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdDefaults
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Keeps one native ad preloaded for the history list. Each [NativeAdCard] takes the ad and the pool starts
 * loading the next one, so scrolling a card in and out of view never fires a request per frame.
 */
internal class NativeAdPool(
    private val analytics: Analytics,
    private val crashReporter: CrashReporter,
    private val scope: CoroutineScope,
    private val adsAvailable: () -> Boolean,
) {
    private val slot =
        AdSlot<LoadedNative>(AdDefaults.NATIVE_MAX_AGE_MS) { stale ->
            scope.launch(Dispatchers.Main) { stale.destroy() }
        }

    /** Bumped on every load result so waiters in [take] never miss one. */
    private val version = MutableStateFlow(0)

    fun clear() {
        slot.clear()
    }

    fun preload() {
        if (!adsAvailable() || !slot.beginLoad(nowMs())) return
        scope.launch(Dispatchers.Main) {
            guardedAds(crashReporter, "ads_load_native", onError = { loadFinished(failed = true) }) {
                NextGenAdsGateway.loadNative(
                    BuildConfig.AD_NATIVE_ID,
                    onLoaded = { loaded -> loadSucceeded(loaded) },
                    onFailed = { message -> onLoadFailed(message) },
                )
            }
        }
    }

    /** Returns a preloaded ad, waiting up to a few seconds for one, or null. */
    suspend fun take(): LoadedNative? {
        repeat(MAX_ATTEMPTS) {
            val seen = version.value
            val ready = slot.take(nowMs())
            preload()
            if (ready != null) return ready
            withTimeoutOrNull(WAIT_MS) { version.first { it != seen } }
        }
        return null
    }

    private fun loadSucceeded(loaded: LoadedNative) {
        slot.onLoaded(loaded, nowMs())
        version.update { it + 1 }
    }

    private fun onLoadFailed(message: String) {
        Timber.w("native ad failed to load: %s", message)
        analytics.log(Events.AD_LOAD_FAILED, mapOf(Params.AD_FORMAT to "native"))
        loadFinished(failed = true)
    }

    private fun loadFinished(failed: Boolean) {
        if (failed) slot.onLoadFailed(nowMs())
        version.update { it + 1 }
    }

    private companion object {
        const val MAX_ATTEMPTS = 2
        const val WAIT_MS = 6_000L
    }
}

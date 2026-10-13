package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.CrashReporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.config.RemoteFlags
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdDefaults
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdSlot
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AppOpenInput
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AppOpenPolicy
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.InterstitialInput
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.InterstitialPolicy
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Interstitial and app open ads: preloading, frequency rules (via the pure policies in domain/ads) and
 * showing. The in-memory result counter lives here; `lastInterstitialAt` and `lastBackgroundAt` are
 * persisted through [SettingsRepository].
 */
internal class FullScreenAds(
    private val settingsRepository: SettingsRepository,
    private val remoteFlags: RemoteFlags,
    private val analytics: Analytics,
    private val crashReporter: CrashReporter,
    private val tracker: ActivityTracker,
    private val scope: CoroutineScope,
    private val adsAvailable: () -> Boolean,
) {
    private val interstitials = AdSlot<LoadedInterstitial>(AdDefaults.INTERSTITIAL_MAX_AGE_MS)
    private val appOpens = AdSlot<LoadedAppOpen>(AdDefaults.APP_OPEN_MAX_AGE_MS)
    private val resultsSinceInterstitial = AtomicInteger(0)

    /** Start time of the full-screen ad on screen, 0 = none. A stale value (SDK never called back) expires. */
    private val fullScreenSince = AtomicLong(0L)

    fun preloadAll() {
        preloadInterstitial()
        preloadAppOpen()
    }

    fun clear() {
        interstitials.clear()
        appOpens.clear()
    }

    /** Called when the app is backgrounded; remembered for the app open rule and survives process death. */
    fun recordBackgrounded() {
        val at = nowMs()
        scope.launch { settingsRepository.update { it.copy(lastBackgroundAt = at) } }
    }

    // ---- Interstitial ------------------------------------------------------------------------------------

    fun onResultShown(activity: Activity?) {
        if (!adsAvailable()) return
        resultsSinceInterstitial.incrementAndGet()
        scope.launch { evaluateInterstitial(activity) }
    }

    private suspend fun evaluateInterstitial(activity: Activity?) {
        val settings = settingsRepository.current()
        val policy = InterstitialPolicy(remoteFlags.config.value)
        val now = nowMs()
        if (policy.isFirstSession(now, settings.firstLaunchAt)) {
            resultsSinceInterstitial.set(0)
            return
        }
        val input = InterstitialInput(
            adsAllowed = adsAvailable(),
            onboardingDone = settings.onboardingDone,
            resultsSinceLastInterstitial = resultsSinceInterstitial.get(),
            nowMs = now,
            lastInterstitialAtMs = settings.lastInterstitialAt,
            firstLaunchAtMs = settings.firstLaunchAt,
            adReady = interstitials.isReady(now),
            fullScreenAdShowing = isFullScreenShowing(),
        )
        if (policy.shouldShow(input)) showInterstitial(activity) else preloadInterstitial()
    }

    private suspend fun showInterstitial(activity: Activity?) {
        val target = usableActivity(activity) ?: return
        if (!tryBeginFullScreen()) return
        val ad = interstitials.take(nowMs())
        if (ad == null) {
            endFullScreen()
            preloadInterstitial()
            return
        }
        withContext(Dispatchers.Main) {
            guardedAds(crashReporter, "ads_show_interstitial", onError = { interstitialShowFailed() }) {
                NextGenAdsGateway.showInterstitial(ad, target, interstitialCallbacks())
            }
        }
    }

    private fun interstitialCallbacks() = object : FullScreenCallbacks {
        override fun onShown() {
            resultsSinceInterstitial.set(0)
            val at = nowMs()
            scope.launch { settingsRepository.update { it.copy(lastInterstitialAt = at) } }
            analytics.log(Events.AD_INTERSTITIAL_SHOWN)
        }

        override fun onDismissed() = interstitialShowFailed()

        override fun onFailedToShow(message: String) {
            Timber.w("Interstitial failed to show: %s", message)
            interstitialShowFailed()
        }
    }

    /** Also used for "dismissed": the screen is free again and the next ad loads. */
    private fun interstitialShowFailed() {
        endFullScreen()
        preloadInterstitial()
    }

    private fun preloadInterstitial() {
        if (!adsAvailable() || !interstitials.beginLoad(nowMs())) return
        scope.launch(Dispatchers.Main) {
            guardedAds(crashReporter, "ads_load_interstitial", onError = { interstitials.onLoadFailed(nowMs()) }) {
                NextGenAdsGateway.loadInterstitial(
                    BuildConfig.AD_INTERSTITIAL_ID,
                    onLoaded = { interstitials.onLoaded(it, nowMs()) },
                    onFailed = { loadFailed("interstitial", it) { interstitials.onLoadFailed(nowMs()) } },
                )
            }
        }
    }

    // ---- App open ----------------------------------------------------------------------------------------

    /** Always evaluates, even while ads are unavailable, so the background mark is consumed exactly once. */
    fun onAppForegrounded(activity: Activity?) {
        scope.launch { evaluateAppOpen(activity) }
    }

    private suspend fun evaluateAppOpen(activity: Activity?) {
        val settings = settingsRepository.current()
        // MainActivity reports every STARTED, also after an ad or a dialog; consume the background mark once.
        if (settings.lastBackgroundAt != 0L) consumeBackgroundMark(settings.lastBackgroundAt)
        val now = nowMs()
        val input = AppOpenInput(
            adsAllowed = adsAvailable(),
            onboardingDone = settings.onboardingDone,
            nowMs = now,
            lastBackgroundAtMs = settings.lastBackgroundAt,
            adReady = appOpens.isReady(now),
            fullScreenAdShowing = isFullScreenShowing(),
        )
        if (AppOpenPolicy(remoteFlags.config.value).shouldShow(input)) showAppOpen(activity) else preloadAppOpen()
    }

    /** Clears the mark only if it was not replaced by a newer background event in the meantime. */
    private suspend fun consumeBackgroundMark(seenAt: Long) {
        settingsRepository.update { if (it.lastBackgroundAt == seenAt) it.copy(lastBackgroundAt = 0L) else it }
    }

    private suspend fun showAppOpen(activity: Activity?) {
        val target = usableActivity(activity) ?: return
        if (!tryBeginFullScreen()) return
        val ad = appOpens.take(nowMs())
        if (ad == null) {
            endFullScreen()
            preloadAppOpen()
            return
        }
        withContext(Dispatchers.Main) {
            guardedAds(crashReporter, "ads_show_app_open", onError = { appOpenShowFailed() }) {
                NextGenAdsGateway.showAppOpen(ad, target, appOpenCallbacks())
            }
        }
    }

    private fun appOpenCallbacks() = object : FullScreenCallbacks {
        override fun onShown() {
            analytics.log(Events.AD_APP_OPEN_SHOWN)
        }

        override fun onDismissed() = appOpenShowFailed()

        override fun onFailedToShow(message: String) {
            Timber.w("App open ad failed to show: %s", message)
            appOpenShowFailed()
        }
    }

    private fun appOpenShowFailed() {
        endFullScreen()
        preloadAppOpen()
    }

    private fun preloadAppOpen() {
        if (!adsAvailable() || !appOpens.beginLoad(nowMs())) return
        scope.launch(Dispatchers.Main) {
            guardedAds(crashReporter, "ads_load_app_open", onError = { appOpens.onLoadFailed(nowMs()) }) {
                NextGenAdsGateway.loadAppOpen(
                    BuildConfig.AD_APP_OPEN_ID,
                    onLoaded = { appOpens.onLoaded(it, nowMs()) },
                    onFailed = { loadFailed("app_open", it) { appOpens.onLoadFailed(nowMs()) } },
                )
            }
        }
    }

    // ---- Shared ------------------------------------------------------------------------------------------

    private fun loadFailed(format: String, message: String, record: () -> Unit) {
        record()
        Timber.w("%s ad failed to load: %s", format, message)
        analytics.log(Events.AD_LOAD_FAILED, mapOf(Params.AD_FORMAT to format))
    }

    /** The activity ads may be shown on: still alive and the app is in the foreground. */
    private fun usableActivity(activity: Activity?): Activity? {
        val candidate = activity ?: tracker.currentActivity ?: return null
        val alive = !candidate.isFinishing && !candidate.isDestroyed
        return if (alive && tracker.isForeground) candidate else null
    }

    private fun isFullScreenShowing(): Boolean {
        val since = fullScreenSince.get()
        return since != 0L && (nowMs() - since) in 0 until FULL_SCREEN_STUCK_MS
    }

    private fun tryBeginFullScreen(): Boolean {
        val previous = fullScreenSince.get()
        if (isFullScreenShowing()) return false
        return fullScreenSince.compareAndSet(previous, nowMs())
    }

    private fun endFullScreen() {
        fullScreenSince.set(0L)
    }

    private companion object {
        /** If the SDK never reports dismissal, treat the screen as free again after this long. */
        const val FULL_SCREEN_STUCK_MS = 5L * 60_000L
    }
}

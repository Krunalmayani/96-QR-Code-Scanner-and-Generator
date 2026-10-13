package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.CrashReporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.billing.BillingManager
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.config.RemoteFlags
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.ApplicationScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdsGate
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Google Mobile Ads Next Gen implementation of [AdManager].
 *
 * Ads run only when ALL hold: `BuildConfig.ADS_ENABLED`, Remote Config `ads_enabled`, UMP consent allows
 * ads, the user is not ad-free (see [BillingManager]). The Mobile Ads SDK is not initialised before consent
 * allows ads. Consent is gathered when the first [controller] is created, so MainActivity needs no changes.
 * Frequency rules live in domain/ads; SDK calls live in [NextGenAdsGateway].
 */
@Singleton
class GoogleAdManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    settingsRepository: SettingsRepository,
    private val remoteFlags: RemoteFlags,
    private val billing: BillingManager,
    analytics: Analytics,
    private val crashReporter: CrashReporter,
    @ApplicationScope private val scope: CoroutineScope,
) : AdManager, AdContentSource {

    private val started = AtomicBoolean(false)
    private val sdkStarting = AtomicBoolean(false)
    private val consentGathering = AtomicBoolean(false)
    private val consentGathered = AtomicBoolean(false)

    private val adFree = MutableStateFlow(false)
    private val sdkReady = MutableStateFlow(false)

    private val gateOpen: StateFlow<Boolean> =
        combine(UmpConsent.canRequestAds, remoteFlags.config, adFree) { consent, config, free ->
            AdsGate.canServeAds(
                buildEnabled = BuildConfig.ADS_ENABLED,
                remoteEnabled = config.adsEnabled,
                consentAllowsAds = consent,
                adFree = free,
            )
        }.stateIn(scope, SharingStarted.Eagerly, false)

    override val adsAvailable: StateFlow<Boolean> =
        combine(gateOpen, sdkReady) { open, ready -> open && ready }.stateIn(scope, SharingStarted.Eagerly, false)

    private val tracker = ActivityTracker(onBackgrounded = { recordBackgrounded() })
    private val fullScreen = FullScreenAds(
        settingsRepository, remoteFlags, analytics, crashReporter, tracker, scope, { adsAvailable.value },
    )
    private val natives = NativeAdPool(analytics, crashReporter, scope, { adsAvailable.value })

    override fun initialize(application: Application) {
        if (!started.compareAndSet(false, true)) return
        application.registerActivityLifecycleCallbacks(tracker)
        scope.launch { remoteFlags.fetchAndActivate() }
        scope.launch { billing.isAdFree.collect { adFree.value = it } }
        if (!BuildConfig.ADS_ENABLED) return
        UmpConsent.refresh(application)
        scope.launch { gateOpen.collect { open -> if (open) startSdk() } }
        scope.launch { adsAvailable.collect { available -> onAvailabilityChanged(available) } }
    }

    override fun controller(activity: Activity): AdController {
        if (!BuildConfig.ADS_ENABLED) return NoOpAdController
        gatherConsent(activity)
        return ActivityAdController(fullScreen, WeakReference(activity))
    }

    override suspend fun loadBanner(context: Context, widthDp: Int): LoadedBanner? {
        if (!adsAvailable.value || widthDp <= 0) return null
        return withContext(Dispatchers.Main) {
            NextGenAdsGateway.loadBanner(context, BuildConfig.AD_BANNER_ID, widthDp)
        }
    }

    override suspend fun takeNative(): LoadedNative? = if (adsAvailable.value) natives.take() else null

    /** Consent is asked on every app launch (UMP rule), not on every activity re-creation. */
    private fun gatherConsent(activity: Activity) {
        if (consentGathered.get() || !consentGathering.compareAndSet(false, true)) return
        guardedAds(crashReporter, "ads_consent", onError = { consentGathering.set(false) }) {
            UmpConsent.gather(activity) { errorMessage ->
                consentGathering.set(false)
                if (errorMessage == null) consentGathered.set(true) else Timber.w("UMP consent: %s", errorMessage)
            }
        }
    }

    private fun recordBackgrounded() {
        fullScreen.recordBackgrounded()
    }

    private fun startSdk() {
        if (!sdkStarting.compareAndSet(false, true)) return
        scope.launch(Dispatchers.IO) {
            guardedAds(crashReporter, "ads_init", onError = { sdkStarting.set(false) }) {
                val testDevices = if (BuildConfig.DEBUG) UmpConsent.testDeviceIds(appContext) else emptyList()
                NextGenAdsGateway.initialize(appContext, appContext.getString(R.string.admob_app_id), testDevices) {
                    sdkReady.value = true
                }
            }
        }
    }

    private fun onAvailabilityChanged(available: Boolean) {
        if (available) {
            fullScreen.preloadAll()
            natives.preload()
        } else {
            fullScreen.clear()
            natives.clear()
        }
    }
}

/** Controller bound to one activity; the activity is held weakly so a re-created activity cannot leak. */
internal class ActivityAdController(
    private val fullScreen: FullScreenAds,
    private val activity: WeakReference<Activity>,
) : AdController {
    override fun onResultShown() {
        fullScreen.onResultShown(activity.get())
    }

    override fun onAppForegrounded() {
        fullScreen.onAppForegrounded(activity.get())
    }
}

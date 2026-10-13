package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads.AdsConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Typed access to Firebase Remote Config. Defaults live in code ([AdsConfig.DEFAULT]) so everything works
 * offline and before the first fetch; fetched numbers are clamped by [AdsConfig.fromRaw].
 *
 * Call [fetchAndActivate] once at app start (the ad manager does). Minimum fetch interval is 12 h
 * (1 h in debug builds).
 */
@Singleton
class RemoteFlags
    @Inject
    constructor() {
        private val state = MutableStateFlow(AdsConfig.DEFAULT)

        /** Current ads rules; emits again after every successful fetch. */
        val config: StateFlow<AdsConfig> = state.asStateFlow()

        val adsEnabled: Boolean get() = state.value.adsEnabled
        val interstitialEveryN: Int get() = state.value.interstitialEveryN
        val interstitialMinIntervalSec: Int get() = state.value.interstitialMinIntervalSec
        val nativeEveryNItems: Int get() = state.value.nativeEveryNItems
        val appOpenMinAwayMin: Int get() = state.value.appOpenMinAwayMin

        private val remote: FirebaseRemoteConfig? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { createRemoteConfig() }

        /** Fetches and activates new values. Returns false when offline, throttled or Firebase is unavailable. */
        suspend fun fetchAndActivate(): Boolean {
            val rc = remote ?: return false
            return suspendCancellableCoroutine { continuation ->
                rc.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) publish(rc) else Timber.d("Remote Config fetch skipped or failed")
                    if (continuation.isActive) continuation.resume(task.isSuccessful)
                }
            }
        }

        private fun createRemoteConfig(): FirebaseRemoteConfig? =
            try {
                val rc = FirebaseRemoteConfig.getInstance()
                val settings =
                    FirebaseRemoteConfigSettings
                        .Builder()
                        .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) DEBUG_FETCH_INTERVAL_SEC else RELEASE_FETCH_INTERVAL_SEC)
                        .build()
                rc.setConfigSettingsAsync(settings)
                // Values are read only after the defaults are in place, so getLong never reports a transient 0.
                rc.setDefaultsAsync(defaults()).addOnCompleteListener { publish(rc) }
                rc
            } catch (error: IllegalStateException) {
                Timber.w(error, "Remote Config unavailable, using built-in defaults")
                null
            }

        private fun publish(rc: FirebaseRemoteConfig) {
            state.value =
                AdsConfig.fromRaw(
                    adsEnabled = rc.getBoolean(KEY_ADS_ENABLED),
                    interstitialEveryN = rc.getLong(KEY_INTERSTITIAL_EVERY_N),
                    interstitialMinIntervalSec = rc.getLong(KEY_INTERSTITIAL_MIN_INTERVAL_SEC),
                    nativeEveryNItems = rc.getLong(KEY_NATIVE_EVERY_N_ITEMS),
                    appOpenMinAwayMin = rc.getLong(KEY_APP_OPEN_MIN_AWAY_MIN),
                )
        }

        private fun defaults(): Map<String, Any> =
            mapOf(
                KEY_ADS_ENABLED to AdsConfig.DEFAULT.adsEnabled,
                KEY_INTERSTITIAL_EVERY_N to AdsConfig.DEFAULT.interstitialEveryN.toLong(),
                KEY_INTERSTITIAL_MIN_INTERVAL_SEC to AdsConfig.DEFAULT.interstitialMinIntervalSec.toLong(),
                KEY_NATIVE_EVERY_N_ITEMS to AdsConfig.DEFAULT.nativeEveryNItems.toLong(),
                KEY_APP_OPEN_MIN_AWAY_MIN to AdsConfig.DEFAULT.appOpenMinAwayMin.toLong(),
            )

        companion object {
            const val KEY_ADS_ENABLED = "ads_enabled"
            const val KEY_INTERSTITIAL_EVERY_N = "interstitial_every_n"
            const val KEY_INTERSTITIAL_MIN_INTERVAL_SEC = "interstitial_min_interval_sec"
            const val KEY_NATIVE_EVERY_N_ITEMS = "native_every_n_items"
            const val KEY_APP_OPEN_MIN_AWAY_MIN = "app_open_min_away_min"

            private const val RELEASE_FETCH_INTERVAL_SEC = 12L * 60L * 60L
            private const val DEBUG_FETCH_INTERVAL_SEC = 60L * 60L
        }
    }

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/** Fixed rules that are not remotely configurable. */
object AdDefaults {
    /** No interstitial within this long after the first launch (the "first session"). */
    const val FIRST_SESSION_MS: Long = 10L * 60_000L

    /** Interstitials expire one hour after loading; reload a little earlier. */
    const val INTERSTITIAL_MAX_AGE_MS: Long = 55L * 60_000L

    /** Native ads expire one hour after loading; reload a little earlier. */
    const val NATIVE_MAX_AGE_MS: Long = 55L * 60_000L

    /** App open ads expire four hours after loading. */
    const val APP_OPEN_MAX_AGE_MS: Long = 4L * 60L * 60_000L
}

/**
 * Remotely tunable ad rules (Firebase Remote Config keys: ads_enabled, interstitial_every_n,
 * interstitial_min_interval_sec, native_every_n_items, app_open_min_away_min).
 * Build one with [fromRaw] so a bad remote value can never produce an ad flood.
 */
data class AdsConfig(
    val adsEnabled: Boolean = true,
    val interstitialEveryN: Int = DEFAULT_INTERSTITIAL_EVERY_N,
    val interstitialMinIntervalSec: Int = DEFAULT_INTERSTITIAL_MIN_INTERVAL_SEC,
    val nativeEveryNItems: Int = DEFAULT_NATIVE_EVERY_N_ITEMS,
    val appOpenMinAwayMin: Int = DEFAULT_APP_OPEN_MIN_AWAY_MIN,
) {
    val interstitialMinIntervalMs: Long get() = interstitialMinIntervalSec * MS_PER_SECOND
    val appOpenMinAwayMs: Long get() = appOpenMinAwayMin * MS_PER_MINUTE

    companion object {
        const val DEFAULT_INTERSTITIAL_EVERY_N = 4
        const val DEFAULT_INTERSTITIAL_MIN_INTERVAL_SEC = 90
        const val DEFAULT_NATIVE_EVERY_N_ITEMS = 8
        const val DEFAULT_APP_OPEN_MIN_AWAY_MIN = 30

        val INTERSTITIAL_EVERY_N_RANGE = 2..50
        val INTERSTITIAL_MIN_INTERVAL_SEC_RANGE = 30..3600
        val NATIVE_EVERY_N_ITEMS_RANGE = 4..50
        val APP_OPEN_MIN_AWAY_MIN_RANGE = 5..1440

        private const val MS_PER_SECOND = 1000L
        private const val MS_PER_MINUTE = 60_000L

        val DEFAULT = AdsConfig()

        /** Clamps raw remote numbers into their safe ranges. */
        fun fromRaw(
            adsEnabled: Boolean,
            interstitialEveryN: Long,
            interstitialMinIntervalSec: Long,
            nativeEveryNItems: Long,
            appOpenMinAwayMin: Long,
        ): AdsConfig = AdsConfig(
            adsEnabled = adsEnabled,
            interstitialEveryN = interstitialEveryN.clampTo(INTERSTITIAL_EVERY_N_RANGE),
            interstitialMinIntervalSec = interstitialMinIntervalSec.clampTo(INTERSTITIAL_MIN_INTERVAL_SEC_RANGE),
            nativeEveryNItems = nativeEveryNItems.clampTo(NATIVE_EVERY_N_ITEMS_RANGE),
            appOpenMinAwayMin = appOpenMinAwayMin.clampTo(APP_OPEN_MIN_AWAY_MIN_RANGE),
        )

        private fun Long.clampTo(range: IntRange): Int =
            coerceIn(range.first.toLong(), range.last.toLong()).toInt()
    }
}

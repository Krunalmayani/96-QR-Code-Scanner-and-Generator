package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/** Everything [AppOpenPolicy] needs to decide; timestamps are epoch ms. */
data class AppOpenInput(
    /** [AdsGate.canServeAds] result. */
    val adsAllowed: Boolean,
    val onboardingDone: Boolean,
    val nowMs: Long,
    /** When the app last went to the background; 0 = it never did (a true first cold start). */
    val lastBackgroundAtMs: Long,
    /** A preloaded, unexpired app open ad is available. */
    val adReady: Boolean,
    /** Another full-screen ad (interstitial or app open) is on screen right now. */
    val fullScreenAdShowing: Boolean = false,
)

/**
 * PRD 9: an app open ad when the user returns after being away for at least [minAwayMs] (30 minutes by
 * default), never before onboarding is done, never when the app was not backgrounded.
 */
class AppOpenPolicy(
    private val minAwayMs: Long = AdsConfig.DEFAULT_APP_OPEN_MIN_AWAY_MIN * 60_000L,
) {
    constructor(config: AdsConfig) : this(config.appOpenMinAwayMs)

    fun shouldShow(input: AppOpenInput): Boolean = when {
        !input.adsAllowed || !input.onboardingDone -> false
        input.fullScreenAdShowing || !input.adReady -> false
        else -> awayLongEnough(input.nowMs, input.lastBackgroundAtMs)
    }

    /** A background time of 0 (never) or in the future (clock moved back) never qualifies. */
    private fun awayLongEnough(nowMs: Long, lastBackgroundAtMs: Long): Boolean =
        lastBackgroundAtMs in 1..nowMs && nowMs - lastBackgroundAtMs >= minAwayMs
}

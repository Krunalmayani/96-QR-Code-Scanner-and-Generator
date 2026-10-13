package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/** Everything [InterstitialPolicy] needs to decide; plain counters, timestamps (epoch ms) and flags. */
data class InterstitialInput(
    /** [AdsGate.canServeAds] result. */
    val adsAllowed: Boolean,
    val onboardingDone: Boolean,
    /** Completed scans/creates since the last interstitial was shown, including the one just shown. */
    val resultsSinceLastInterstitial: Int,
    val nowMs: Long,
    /** 0 = never shown. */
    val lastInterstitialAtMs: Long,
    /** 0 = unknown, treated as "first session". */
    val firstLaunchAtMs: Long,
    /** A preloaded, unexpired interstitial is available. */
    val adReady: Boolean,
    /** Another full-screen ad (interstitial or app open) is on screen right now. */
    val fullScreenAdShowing: Boolean = false,
)

/**
 * PRD 9: an interstitial after every Nth completed scan/create, at least a minimum interval apart,
 * never during the first session, never before the result is visible (the caller only asks after the
 * result screen is shown).
 */
class InterstitialPolicy(
    private val everyN: Int = AdsConfig.DEFAULT_INTERSTITIAL_EVERY_N,
    private val minIntervalMs: Long = AdsConfig.DEFAULT_INTERSTITIAL_MIN_INTERVAL_SEC * 1000L,
    private val firstSessionMs: Long = AdDefaults.FIRST_SESSION_MS,
) {
    constructor(config: AdsConfig) : this(config.interstitialEveryN, config.interstitialMinIntervalMs)

    /**
     * True while the first session lasts. An unknown or future first-launch time (clock moved back) counts as
     * first session, the safe side of "never in the first session".
     */
    fun isFirstSession(nowMs: Long, firstLaunchAtMs: Long): Boolean =
        firstLaunchAtMs <= 0L || nowMs - firstLaunchAtMs < firstSessionMs

    fun shouldShow(input: InterstitialInput): Boolean =
        when {
            !input.adsAllowed || !input.onboardingDone -> false
            input.fullScreenAdShowing || !input.adReady -> false
            input.resultsSinceLastInterstitial < everyN -> false
            isFirstSession(input.nowMs, input.firstLaunchAtMs) -> false
            else -> minIntervalElapsed(input.nowMs, input.lastInterstitialAtMs)
        }

    /** A last-shown time in the future is corrupt (clock moved back) and must not block ads for good. */
    private fun minIntervalElapsed(nowMs: Long, lastAtMs: Long): Boolean =
        lastAtMs <= 0L || lastAtMs > nowMs || nowMs - lastAtMs >= minIntervalMs
}

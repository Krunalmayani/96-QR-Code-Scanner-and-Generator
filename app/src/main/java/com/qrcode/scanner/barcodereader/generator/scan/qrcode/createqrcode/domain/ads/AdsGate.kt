package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/** The single place that decides whether any ad may be requested or shown at all. */
object AdsGate {
    /**
     * @param buildEnabled `BuildConfig.ADS_ENABLED`.
     * @param remoteEnabled Remote Config flag `ads_enabled`.
     * @param consentAllowsAds UMP `canRequestAds()`; ads are never initialised before this is true.
     * @param adFree the user bought "remove ads".
     */
    fun canServeAds(
        buildEnabled: Boolean,
        remoteEnabled: Boolean,
        consentAllowsAds: Boolean,
        adFree: Boolean,
    ): Boolean = buildEnabled && remoteEnabled && consentAllowsAds && !adFree
}

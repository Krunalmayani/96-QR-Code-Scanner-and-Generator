package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

/** Google Play links for "Rate" and "Share app". Debug builds point at the release listing. */
object StoreLinks {
    private const val DEBUG_SUFFIX = ".debug"

    fun releaseAppId(applicationId: String): String = applicationId.removeSuffix(DEBUG_SUFFIX)

    fun marketUrl(applicationId: String): String = "market://details?id=${releaseAppId(applicationId)}"

    fun webUrl(applicationId: String): String =
        "https://play.google.com/store/apps/details?id=${releaseAppId(applicationId)}"
}

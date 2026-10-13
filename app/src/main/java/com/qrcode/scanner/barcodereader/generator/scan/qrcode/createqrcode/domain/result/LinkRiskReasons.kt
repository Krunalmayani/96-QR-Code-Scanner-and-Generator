package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkRisk
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult

/** Stable keys for the reasons listed in the "This link may be unsafe" sheet (s69); the UI maps each to string resources. */
enum class RiskKind {
    NOT_HTTPS,
    LOOKALIKE_BRAND,
    PUNYCODE,
    SUSPICIOUS_TLD,
    URL_SHORTENER,
    IP_ADDRESS,
    USER_INFO,
    EXCESSIVE_SUBDOMAINS,
    NON_WEB_SCHEME,
    VERY_LONG_URL,
}

/** One row of the sheet. [detail] fills the placeholder of the reason text (brand, host, tld, scheme, length, ...). */
data class RiskReason(
    val kind: RiskKind,
    val detail: String? = null,
    val severe: Boolean = false,
)

object LinkRiskReasons {
    /** The sheet lists at most this many reasons; more would not fit above the three buttons. */
    const val MAX_REASONS = 5

    /** Distinct reasons for [result], severe ones first (original order otherwise), capped at [MAX_REASONS]. */
    fun of(result: LinkSafetyResult): List<RiskReason> =
        result.risks
            .map(::reasonOf)
            .distinct()
            .sortedByDescending { it.severe }
            .take(MAX_REASONS)

    fun reasonOf(risk: LinkRisk): RiskReason =
        when (risk) {
            LinkRisk.NotHttps -> RiskReason(RiskKind.NOT_HTTPS, severe = risk.severe)
            is LinkRisk.LookalikeBrand -> RiskReason(RiskKind.LOOKALIKE_BRAND, risk.brand, risk.severe)
            is LinkRisk.Punycode -> RiskReason(RiskKind.PUNYCODE, risk.host, risk.severe)
            is LinkRisk.SuspiciousTld -> RiskReason(RiskKind.SUSPICIOUS_TLD, risk.tld, risk.severe)
            is LinkRisk.UrlShortener -> RiskReason(RiskKind.URL_SHORTENER, risk.host, risk.severe)
            is LinkRisk.IpAddressHost -> RiskReason(RiskKind.IP_ADDRESS, risk.host, risk.severe)
            LinkRisk.UserInfoInUrl -> RiskReason(RiskKind.USER_INFO, severe = risk.severe)
            is LinkRisk.ExcessiveSubdomains -> RiskReason(RiskKind.EXCESSIVE_SUBDOMAINS, risk.labelCount.toString(), risk.severe)
            is LinkRisk.NonWebScheme -> RiskReason(RiskKind.NON_WEB_SCHEME, risk.scheme, risk.severe)
            is LinkRisk.VeryLongUrl -> RiskReason(RiskKind.VERY_LONG_URL, risk.length.toString(), risk.severe)
        }

    /** True when [url] uses `https` (drives the "Secure connection" badge). */
    fun isSecure(url: String): Boolean = url.trimStart().startsWith("https://", ignoreCase = true)

    /** True only for http(s) URLs: the only schemes the app ever opens in a Custom Tab. */
    fun isWebUrl(url: String): Boolean {
        val t = url.trimStart()
        return t.startsWith("https://", ignoreCase = true) || t.startsWith("http://", ignoreCase = true)
    }
}

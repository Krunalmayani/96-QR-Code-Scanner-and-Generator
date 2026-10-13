package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

/**
 * Offline heuristics for the "Unsafe link" sheet. No network, no reputation database: it can only say a link
 * *looks* risky, never that it is safe.
 *
 * Level: SAFE when there are no risks; UNSAFE when any severe risk is present ([LinkRisk.LookalikeBrand],
 * [LinkRisk.Punycode], [LinkRisk.UserInfoInUrl], [LinkRisk.NonWebScheme]) or an IP-address host is used over plain
 * http; otherwise CAUTION.
 *
 * Lookalike rules: a host is official when its registrable domain label is a known brand on a non-suspicious TLD
 * (so `mail.google.com`, `www.amazon.co.uk` and `paypal.me` are fine). Otherwise it is flagged when the label is a brand
 * after undoing look-alike characters (0/o, 1/l/i, rn/m, vv/w), within the allowed typos of a brand (0 for names of
 * up to 3 letters, 1 up to 6 letters, 2 beyond; the first letter must match), a brand glued to other words, or a brand
 * appearing as a sub-domain of an unrelated domain (`paypal.com.evil.xyz`). Free hosting suffixes such as
 * `github.io` never count as official.
 */
class LinkSafetyChecker {
    fun check(url: String): LinkSafetyResult {
        val input = url.trim()
        if (input.isEmpty()) return LinkSafetyResult(Level.SAFE, emptyList())
        val parts = UrlParts.parse(input)
        val scheme = parts.scheme
        val risks = ArrayList<LinkRisk>()
        if (scheme != null && scheme != "http" && scheme != "https") {
            risks += LinkRisk.NonWebScheme(scheme)
        } else {
            if (scheme == "http") risks += LinkRisk.NotHttps
            if (parts.hasUserInfo) risks += LinkRisk.UserInfoInUrl
            risks += hostRisks(parts)
        }
        if (input.length > VERY_LONG_URL_LENGTH) risks += LinkRisk.VeryLongUrl(input.length)
        return LinkSafetyResult(levelOf(risks), risks)
    }

    private fun hostRisks(parts: UrlParts): List<LinkRisk> {
        val host = parts.host
        if (host.isEmpty()) return emptyList()
        if (parts.isBracketedIp || IPV4.matches(host) || NUMERIC_IP.matches(host)) return listOf(LinkRisk.IpAddressHost(host))
        val out = ArrayList<LinkRisk>()
        val labels = HostParts.of(host)
        if (labels.labels.any { it.startsWith("xn--") } || host.any { it.code > 127 }) out += LinkRisk.Punycode(host)
        BrandMatcher.lookalikes(labels).forEach { out += LinkRisk.LookalikeBrand(it, host) }
        val tld = labels.labels.lastOrNull().orEmpty()
        if (tld in BrandData.SUSPICIOUS_TLDS) out += LinkRisk.SuspiciousTld(tld)
        if (BrandData.SHORTENERS.any { host == it || host.endsWith(".$it") }) out += LinkRisk.UrlShortener(host)
        if (labels.effectiveLabelCount >= EXCESSIVE_LABELS) out += LinkRisk.ExcessiveSubdomains(labels.effectiveLabelCount)
        return out
    }

    private fun levelOf(risks: List<LinkRisk>): Level =
        when {
            risks.isEmpty() -> Level.SAFE
            risks.any { it.severe } -> Level.UNSAFE
            risks.any { it is LinkRisk.IpAddressHost } && risks.any { it == LinkRisk.NotHttps } -> Level.UNSAFE
            else -> Level.CAUTION
        }

    private companion object {
        const val VERY_LONG_URL_LENGTH = 500
        const val EXCESSIVE_LABELS = 5
        val IPV4 = Regex("\\d{1,3}(?:\\.\\d{1,3}){3}")
        val NUMERIC_IP = Regex("\\d{7,10}|0x[0-9a-f]{4,8}")
    }
}

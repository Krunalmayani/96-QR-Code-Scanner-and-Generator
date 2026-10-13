package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

enum class Level { SAFE, CAUTION, UNSAFE }

/** One reason a link looks risky. The UI maps each type to a localised string; the fields are data for the placeholders. */
sealed interface LinkRisk {
    /** True when this risk alone makes the link UNSAFE. */
    val severe: Boolean

    /** Plain `http://` (no TLS). */
    data object NotHttps : LinkRisk {
        override val severe get() = false
    }

    /** Host imitates [brand] (edit distance, digit/letter swaps, brand-in-subdomain or brand-with-suffix tricks). */
    data class LookalikeBrand(
        val brand: String,
        val host: String,
    ) : LinkRisk {
        override val severe get() = true
    }

    /** Host contains `xn--` labels or non-ASCII characters (possible homograph attack). */
    data class Punycode(
        val host: String,
    ) : LinkRisk {
        override val severe get() = true
    }

    data class SuspiciousTld(
        val tld: String,
    ) : LinkRisk {
        override val severe get() = false
    }

    data class UrlShortener(
        val host: String,
    ) : LinkRisk {
        override val severe get() = false
    }

    /** Host is an IP address. Severe only together with [NotHttps]; the checker decides. */
    data class IpAddressHost(
        val host: String,
    ) : LinkRisk {
        override val severe get() = false
    }

    /** `https://google.com@evil.com`: everything before the `@` is a decoy. */
    data object UserInfoInUrl : LinkRisk {
        override val severe get() = true
    }

    data class ExcessiveSubdomains(
        val labelCount: Int,
    ) : LinkRisk {
        override val severe get() = false
    }

    /** `javascript:`, `data:`, `file:`, `intent:`, `vbscript:` and any other non-http(s) scheme. */
    data class NonWebScheme(
        val scheme: String,
    ) : LinkRisk {
        override val severe get() = true
    }

    data class VeryLongUrl(
        val length: Int,
    ) : LinkRisk {
        override val severe get() = false
    }
}

data class LinkSafetyResult(
    val level: Level,
    val risks: List<LinkRisk>,
) {
    val isSafe: Boolean get() = level == Level.SAFE
}

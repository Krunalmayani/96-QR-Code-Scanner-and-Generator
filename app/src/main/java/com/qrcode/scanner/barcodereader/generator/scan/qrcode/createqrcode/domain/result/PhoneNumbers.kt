package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

/**
 * Turns the free-form number printed in a code ("+1 (206) 555-0198") into something a dialer or messaging app
 * accepts. Keeps digits, a leading `+`, and the dialer symbols `*`, `#`, `,` (pause) and `;` (wait).
 */
object PhoneNumbers {
    private const val DIALER_SYMBOLS = "*#,;"

    fun dialable(raw: String): String {
        val out = StringBuilder(raw.length)
        for (ch in raw) {
            val keep = ch in '0'..'9' || ch in DIALER_SYMBOLS || (ch == '+' && out.isEmpty())
            if (keep) out.append(ch)
        }
        return out.toString()
    }

    /** True when [raw] contains at least one digit, so there is something to dial. */
    fun isDialable(raw: String): Boolean = dialable(raw).any { it in '0'..'9' }

    /** `tel:` URI with `#` percent-encoded (an unencoded `#` would start a URI fragment). */
    fun telUri(raw: String): String = "tel:" + UriEncoding.encode(dialable(raw), extraSafe = "+*,;")

    /** `smsto:` URI used with ACTION_SENDTO. */
    fun smsUri(raw: String): String = "smsto:" + UriEncoding.encode(dialable(raw), extraSafe = "+*,;")
}

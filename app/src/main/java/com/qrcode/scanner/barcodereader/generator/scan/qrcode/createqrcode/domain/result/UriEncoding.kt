package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

private const val HEX_DIGITS = "0123456789ABCDEF"
private const val ASCII_LIMIT = 128
private const val BYTE_MASK = 0xFF
private const val NIBBLE_BITS = 4
private const val NIBBLE_MASK = 0x0F

/** Minimal percent-encoder for URI components (UTF-8). Pure Kotlin so URIs can be unit tested without android.net.Uri. */
internal object UriEncoding {
    /** Encodes [value]; letters, digits and `-._~` are kept, as are the characters of [extraSafe]. */
    fun encode(value: String, extraSafe: String = ""): String {
        val out = StringBuilder(value.length + value.length / 2)
        for (byte in value.toByteArray(Charsets.UTF_8)) {
            val code = byte.toInt() and BYTE_MASK
            val ch = code.toChar()
            val plain = code < ASCII_LIMIT && (ch.isAsciiLetterOrDigit() || ch in "-._~" || ch in extraSafe)
            if (plain) {
                out.append(ch)
            } else {
                out.append('%').append(HEX_DIGITS[code shr NIBBLE_BITS]).append(HEX_DIGITS[code and NIBBLE_MASK])
            }
        }
        return out.toString()
    }

    private fun Char.isAsciiLetterOrDigit(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'
}

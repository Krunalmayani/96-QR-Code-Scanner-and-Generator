package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import java.time.ZoneId

/**
 * Turns the raw text of a scanned (or created) code into a typed [ScanContent]. Pure Kotlin, never throws.
 *
 * Detection order (first match wins; anything that fails to parse falls through to the next rule and finally to Text):
 *  1. blank payload, or a payload with control/binary characters (or U+FFFD) -> [ScanContent.Unknown]
 *  2. Product: digits only on EAN/UPC formats, or 8/12/13 digits on any other 1D format
 *  3. `WIFI:` -> Wifi (needs an SSID)
 *  4. `BEGIN:VCARD` -> Contact (vCard 2.1/3.0/4.0), `MECARD:` -> Contact
 *  5. `BEGIN:VEVENT` / `BEGIN:VCALENDAR` -> Event
 *  6. `MATMSG:` / `mailto:` -> Email (needs an `@`)
 *  7. `smsto:` `sms:` `mmsto:` `mms:` -> Sms (`number:message` or `number?body=message`)
 *  8. `tel:` -> Phone
 *  9. `geo:` -> Location
 * 10. `URLTO:` / `MEBKM:` wrappers -> Website
 * 11. `http://` / `https://` with a host -> Website, except map links that are only a coordinate pair -> Location
 *     (see [MapsUrls]: place names, `/maps/place/...` and short links stay Website)
 * 12. any other `scheme:` payload (javascript:, data:, file:, intent:, ...) -> Text, so there is never an "open" action
 * 13. a bare e-mail address -> Email
 * 14. a plain phone number (digits, `+` first, spaces, dashes, dots-free, parentheses; 7..15 digits) -> Phone.
 *     Only for 2D formats and UNKNOWN: a 1D barcode full of digits is a product/text, never a phone number.
 *     Dates such as `2024-05-01` are not phone numbers.
 * 15. `www.example.com` or a bare `domain.tld/path` (no whitespace) -> Website with `https://` added when no scheme
 *     was given. The user's scheme is always kept.
 * 16. everything else -> Text
 *
 * @param defaultZone zone used for floating (zone-less) iCalendar times.
 */
class ScanContentParser(
    private val defaultZone: ZoneId = ZoneId.systemDefault(),
) {
    private val events = EventParser(defaultZone)

    fun parse(rawValue: String, format: CodeFormat): ScanContent =
        try {
            classify(rawValue, format)
        } catch (e: RuntimeException) {
            if (rawValue.isBlank()) ScanContent.Unknown(rawValue, format) else ScanContent.Text(rawValue, format)
        }

    private fun classify(raw: String, format: CodeFormat): ScanContent {
        val text = raw.removePrefix("\uFEFF").trim()
        if (text.isEmpty() || hasBinary(text)) return ScanContent.Unknown(raw, format)
        return product(text, raw, format)
            ?: structured(text, raw, format)
            ?: unstructured(text, raw, format)
            ?: ScanContent.Text(raw, format)
    }

    private fun hasBinary(text: String): Boolean =
        text.any { (Character.isISOControl(it) && it != '\n' && it != '\r' && it != '\t') || it == '�' }

    // ---- 2. product -------------------------------------------------------------------------------------------

    private fun product(text: String, raw: String, format: CodeFormat): ScanContent? {
        if (!text.all { it in '0'..'9' }) return null
        val ok = format.isProductFormat || (format.isLinear() && text.length in PRODUCT_LENGTHS)
        return if (ok) ScanContent.Product(raw, format, text) else null
    }

    // ---- 3..12 prefix based -----------------------------------------------------------------------------------

    private fun structured(text: String, raw: String, format: CodeFormat): ScanContent? =
        when {
            text.startsWith("WIFI:", ignoreCase = true) -> WifiParser.parse(text, raw, format)
            text.startsWith("BEGIN:VCARD", ignoreCase = true) -> ContactParser.parseVCard(text, raw, format)
            text.startsWith("MECARD:", ignoreCase = true) -> ContactParser.parseMeCard(text, raw, format)
            text.startsWith("BEGIN:VEVENT", ignoreCase = true) || text.startsWith("BEGIN:VCALENDAR", ignoreCase = true) ->
                events.parse(text, raw, format)
            text.startsWith("MATMSG:", ignoreCase = true) -> matmsg(text, raw, format)
            text.startsWith("mailto:", ignoreCase = true) -> mailto(text, raw, format)
            SMS_PREFIXES.any { text.startsWith(it, ignoreCase = true) } -> sms(text, raw, format)
            text.startsWith("tel:", ignoreCase = true) -> tel(text, raw, format)
            text.startsWith("geo:", ignoreCase = true) -> geo(text, raw, format)
            text.startsWith("URLTO:", ignoreCase = true) -> urlTo(text, raw, format)
            text.startsWith("MEBKM:", ignoreCase = true) -> mebkm(text, raw, format)
            text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) -> web(text, raw, format)
            else -> null
        }.let { parsed ->
            // A recognised-but-malformed prefix (e.g. WIFI: without S:) must not fall into the URL/domain heuristics.
            if (parsed == null && hasKnownPrefix(text)) ScanContent.Text(raw, format) else parsed
        }

    private fun hasKnownPrefix(text: String): Boolean =
        KNOWN_PREFIXES.any { text.startsWith(it, ignoreCase = true) } || SCHEME.containsMatchIn(text.take(40))

    // ---- 13..15 -----------------------------------------------------------------------------------------------

    private fun unstructured(text: String, raw: String, format: CodeFormat): ScanContent? {
        if (text.length <= MAX_EMAIL_LENGTH && EMAIL.matches(text)) return ScanContent.Email(raw, format, text)
        if (!format.isLinear()) plainPhone(text)?.let { return ScanContent.Phone(raw, format, it) }
        return WebUrls.toWebsiteUrl(text)?.let { ScanContent.Website(raw, format, it) }
    }

    private fun plainPhone(text: String): String? {
        if (text.length > MAX_PHONE_LENGTH || text.any { it.isWhitespace() && it != ' ' }) return null
        if (DATE_LIKE.matches(text)) return null
        val digits = text.count { it in '0'..'9' }
        if (digits !in 7..15) return null
        if (!text.all { it in '0'..'9' || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' }) return null
        if (text.lastIndexOf('+') > 0) return null
        return if (text[0] == '+' || text[0] == '(' || text[0] in '0'..'9') text else null
    }

    // ---- e-mail -----------------------------------------------------------------------------------------------

    private fun matmsg(text: String, raw: String, format: CodeFormat): ScanContent? {
        val f = FieldParsing.parseKeyValue(text.substring("MATMSG:".length))
        val to = f["TO"]?.firstOrNull()?.trim().orEmpty()
        if (!isMailbox(to)) return null
        return ScanContent.Email(raw, format, to, f["SUB"]?.firstOrNull().orEmpty(), f["BODY"]?.firstOrNull().orEmpty())
    }

    private fun mailto(text: String, raw: String, format: CodeFormat): ScanContent? {
        val rest = text.substring("mailto:".length)
        val query = FieldParsing.parseQuery(rest.substringAfter('?', ""))
        var address = FieldParsing.percentDecode(rest.substringBefore('?')).trim().removePrefix("//")
        if (address.isEmpty()) address = query["to"].orEmpty().trim()
        if (!isMailbox(address)) return null
        return ScanContent.Email(raw, format, address, query["subject"].orEmpty(), query["body"].orEmpty())
    }

    private fun isMailbox(address: String): Boolean {
        val at = address.indexOf('@')
        return at > 0 && at < address.length - 1 && address.none { it.isWhitespace() && it != ' ' }
    }

    // ---- SMS / phone ------------------------------------------------------------------------------------------

    private fun sms(text: String, raw: String, format: CodeFormat): ScanContent? {
        val prefix = SMS_PREFIXES.first { text.startsWith(it, ignoreCase = true) }
        val rest = text.substring(prefix.length).removePrefix("//")
        val q = rest.indexOf('?')
        val colon = rest.indexOf(':')
        val amp = rest.indexOf('&')
        val queryAt =
            when {
                q >= 0 && (colon < 0 || q < colon) -> q
                colon < 0 && amp >= 0 -> amp
                else -> -1
            }
        val number: String
        val message: String
        if (queryAt >= 0) {
            number = rest.substring(0, queryAt)
            message = FieldParsing.parseQuery(rest.substring(queryAt + 1))["body"].orEmpty()
        } else {
            number = rest.substringBefore(':')
            message = rest.substringAfter(':', "")
        }
        val cleaned = FieldParsing.percentDecode(number).substringBefore(';').trim()
        return if (isDialable(cleaned)) ScanContent.Sms(raw, format, cleaned, message) else null
    }

    private fun tel(text: String, raw: String, format: CodeFormat): ScanContent? {
        val number = FieldParsing.percentDecode(text.substring("tel:".length)).trim().removePrefix("//")
        return if (isDialable(number)) ScanContent.Phone(raw, format, number) else null
    }

    private fun isDialable(number: String): Boolean =
        number.length <= MAX_PHONE_LENGTH &&
            number.any { it in '0'..'9' } &&
            number.all { it in '0'..'9' || it in "+-().*#,; " }

    // ---- geo --------------------------------------------------------------------------------------------------

    private fun geo(text: String, raw: String, format: CodeFormat): ScanContent? {
        val rest = text.substring("geo:".length)
        val coordPart = rest.substringBefore('?').substringBefore(';')
        val query = FieldParsing.parseQuery(rest.substringAfter('?', ""), plusAsSpace = true)
        var coords = MapsUrls.parseCoords(coordPart)
        var label = ""
        query["q"]?.let { q ->
            val paren = LABEL_IN_PARENS.find(q)
            val coordText = if (paren != null) q.substring(0, paren.range.first) else q
            val qCoords = MapsUrls.parseCoords(coordText)
            if (qCoords != null && (coords == null || coords == 0.0 to 0.0)) coords = qCoords
            label = paren?.groupValues?.get(1)?.trim() ?: if (qCoords == null) q.trim() else ""
        }
        val (lat, lng) = coords ?: return null
        return ScanContent.Location(raw, format, lat, lng, label)
    }

    // ---- web --------------------------------------------------------------------------------------------------

    private fun web(text: String, raw: String, format: CodeFormat): ScanContent? {
        val url = WebUrls.httpUrlOrNull(text) ?: return null
        val coords = MapsUrls.coordinatesOrNull(url)
        return if (coords != null) ScanContent.Location(raw, format, coords.first, coords.second) else ScanContent.Website(raw, format, url)
    }

    private fun urlTo(text: String, raw: String, format: CodeFormat): ScanContent? {
        val rest = text.substring("URLTO:".length)
        val schemeAt = HTTP_START.find(rest)?.range?.first
        val candidate = if (schemeAt != null) rest.substring(schemeAt) else rest.substringAfterLast(':')
        return WebUrls.toWebsiteUrl(candidate)?.let { ScanContent.Website(raw, format, it) }
    }

    private fun mebkm(text: String, raw: String, format: CodeFormat): ScanContent? {
        val url = FieldParsing.parseKeyValue(text.substring("MEBKM:".length))["URL"]?.firstOrNull().orEmpty()
        return WebUrls.toWebsiteUrl(url)?.let { ScanContent.Website(raw, format, it) }
    }

    private fun CodeFormat.isLinear(): Boolean = this in LINEAR_FORMATS

    private companion object {
        val PRODUCT_LENGTHS = setOf(8, 12, 13)
        val LINEAR_FORMATS =
            setOf(
                CodeFormat.EAN_13,
                CodeFormat.EAN_8,
                CodeFormat.UPC_A,
                CodeFormat.UPC_E,
                CodeFormat.CODE_128,
                CodeFormat.CODE_39,
                CodeFormat.CODE_93,
                CodeFormat.ITF,
                CodeFormat.CODABAR,
            )
        val SMS_PREFIXES = listOf("smsto:", "mmsto:", "sms:", "mms:")
        val KNOWN_PREFIXES =
            listOf(
                "WIFI:",
                "BEGIN:VCARD",
                "MECARD:",
                "BEGIN:VEVENT",
                "BEGIN:VCALENDAR",
                "MATMSG:",
                "mailto:",
                "smsto:",
                "mmsto:",
                "sms:",
                "mms:",
                "tel:",
                "geo:",
                "URLTO:",
                "MEBKM:",
                "http://",
                "https://",
            )
        const val MAX_EMAIL_LENGTH = 254
        const val MAX_PHONE_LENGTH = 40
        val EMAIL = Regex("[A-Za-z0-9._%+'-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}")
        val DATE_LIKE = Regex("\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}-\\d{1,2}-\\d{4}")
        val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*:(?:[^0-9\\s]|//)")
        val LABEL_IN_PARENS = Regex("\\((.*)\\)\\s*$")
        val HTTP_START = Regex("https?://", RegexOption.IGNORE_CASE)
    }
}

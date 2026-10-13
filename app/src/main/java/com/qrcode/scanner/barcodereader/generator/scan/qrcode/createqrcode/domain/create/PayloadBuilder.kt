package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

/**
 * Turns form inputs into the exact text that is encoded in the code. Output follows the standard
 * formats the scan-side parser reads back: URL, vCard 3.0, WIFI:, mailto:, tel:, SMSTO:, geo:, iCalendar.
 * Multi-line payloads use "\n" line endings. Does not validate: run [Validators] first.
 */
object PayloadBuilder {
    fun formatOf(spec: CreateSpec): CodeFormat = if (spec is CreateSpec.Barcode) spec.format else CodeFormat.QR_CODE

    fun build(spec: CreateSpec): String = when (spec) {
        is CreateSpec.Website -> normalizeUrl(spec.url)
        is CreateSpec.Text -> spec.text
        is CreateSpec.Contact -> contact(spec)
        is CreateSpec.Wifi -> wifi(spec)
        is CreateSpec.Email -> email(spec)
        is CreateSpec.Phone -> "tel:" + normalizePhone(spec.number)
        is CreateSpec.Sms -> sms(spec)
        is CreateSpec.Location -> location(spec)
        is CreateSpec.Event -> event(spec)
        is CreateSpec.Social -> socialUrl(spec.platform, spec.handle)
        is CreateSpec.AppStore -> normalizeUrl(spec.url)
        is CreateSpec.Barcode -> BarcodeCheckDigit.normalize(spec.format, spec.data)
    }

    // ---- shared helpers (public: forms use them for live previews) ----

    private val schemeRegex = Regex("^[A-Za-z][A-Za-z0-9+.-]*://")

    /** Trims and adds `https://` when the input has no `scheme://`. */
    fun normalizeUrl(input: String): String {
        val t = input.trim()
        return if (schemeRegex.containsMatchIn(t)) t else "https://$t"
    }

    /** Keeps a leading `+` and the digits: "+1 (415) 555-0142" becomes "+14155550142". */
    fun normalizePhone(input: String): String {
        val t = input.trim()
        val digits = t.filter { it in '0'..'9' }
        return if (t.startsWith("+")) "+$digits" else digits
    }

    /** The profile link a social code opens, e.g. `https://instagram.com/lumen.studio`. */
    fun socialUrl(platform: SocialPlatform, handle: String): String {
        val h = cleanHandle(handle)
        return when (platform) {
            SocialPlatform.INSTAGRAM -> "https://instagram.com/$h"
            SocialPlatform.FACEBOOK -> "https://facebook.com/$h"
            SocialPlatform.X -> "https://x.com/$h"
            SocialPlatform.LINKEDIN -> "https://linkedin.com/in/$h"
            SocialPlatform.YOUTUBE -> "https://youtube.com/@$h"
            SocialPlatform.WHATSAPP -> "https://wa.me/" + handle.filter { it in '0'..'9' }
            SocialPlatform.TELEGRAM -> "https://t.me/$h"
        }
    }

    /** Trims and drops one leading `@`. */
    fun cleanHandle(handle: String): String = handle.trim().removePrefix("@").trim()

    // ---- per type ----

    private fun wifi(s: CreateSpec.Wifi): String {
        val type = when (s.security) {
            WifiSecurity.NONE -> "nopass"
            WifiSecurity.WEP -> "WEP"
            WifiSecurity.WPA -> "WPA"
        }
        val sb = StringBuilder("WIFI:T:").append(type).append(";S:").append(escapeWifi(s.ssid)).append(';')
        if (s.security != WifiSecurity.NONE && s.password.isNotEmpty()) {
            sb.append("P:").append(escapeWifi(s.password)).append(';')
        }
        if (s.hidden) sb.append("H:true;")
        return sb.append(';').toString()
    }

    /** Escapes `\ ; , : "` with a backslash as the WIFI: format requires. */
    fun escapeWifi(value: String): String {
        val sb = StringBuilder(value.length + 4)
        for (ch in value) {
            if (ch == '\\' || ch == ';' || ch == ',' || ch == ':' || ch == '"') sb.append('\\')
            sb.append(ch)
        }
        return sb.toString()
    }

    private fun email(s: CreateSpec.Email): String {
        val params = ArrayList<String>(2)
        if (s.subject.isNotEmpty()) params += "subject=" + percentEncode(s.subject)
        if (s.body.isNotEmpty()) params += "body=" + percentEncode(s.body)
        val query = if (params.isEmpty()) "" else "?" + params.joinToString("&")
        return "mailto:" + s.to.trim() + query
    }

    private fun sms(s: CreateSpec.Sms): String {
        val number = normalizePhone(s.number)
        return if (s.message.isEmpty()) "SMSTO:$number" else "SMSTO:$number:${s.message}"
    }

    private fun location(s: CreateSpec.Location): String {
        val label = s.label.trim()
        val base = "geo:${normalizeCoordinate(s.latitude)},${normalizeCoordinate(s.longitude)}"
        return if (label.isEmpty()) base else base + "?q=" + percentEncode(label)
    }

    /** "37,7749" and " 37.7749 " both become "37.7749"; no exponent, no trailing zeros. */
    fun normalizeCoordinate(input: String): String {
        val t = input.trim().replace(',', '.')
        val parsed = t.toBigDecimalOrNull() ?: return t
        val plain = parsed.stripTrailingZeros().toPlainString()
        return if (plain == "-0") "0" else plain
    }

    /** Percent-encodes UTF-8 bytes; everything except `A-Za-z0-9-_.~` is encoded (space is %20). */
    fun percentEncode(value: String): String {
        val sb = StringBuilder(value.length + 8)
        for (b in value.toByteArray(Charsets.UTF_8)) {
            val c = b.toInt() and 0xFF
            val ch = c.toChar()
            if (c < 128 && (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' || ch == '-' || ch == '_' || ch == '.' || ch == '~')) {
                sb.append(ch)
            } else {
                sb.append('%').append(HEX[c shr 4]).append(HEX[c and 0xF])
            }
        }
        return sb.toString()
    }

    private const val HEX = "0123456789ABCDEF"

    // ---- vCard 3.0 ----

    private fun contact(s: CreateSpec.Contact): String {
        val name = s.name.trim()
        val org = s.organization.trim()
        val lines = ArrayList<String>()
        lines += "BEGIN:VCARD"
        lines += "VERSION:3.0"
        val displayName = name.ifEmpty { org }
        lines += "N:" + structuredName(name)
        lines += "FN:" + escapeText(displayName)
        if (org.isNotEmpty()) lines += "ORG:" + escapeText(org)
        if (s.jobTitle.isNotBlank()) lines += "TITLE:" + escapeText(s.jobTitle.trim())
        if (s.phone.isNotBlank()) lines += "TEL;TYPE=CELL:" + normalizePhone(s.phone)
        if (s.email.isNotBlank()) lines += "EMAIL:" + s.email.trim()
        if (s.address.isNotBlank()) lines += "ADR;TYPE=WORK:;;" + escapeText(s.address.trim()) + ";;;;"
        if (s.website.isNotBlank()) lines += "URL:" + normalizeUrl(s.website)
        if (s.note.isNotBlank()) lines += "NOTE:" + escapeText(s.note.trim())
        lines += "END:VCARD"
        return lines.joinToString("\n")
    }

    /** "Sophie Lambert" becomes "Lambert;Sophie;;;" (last word is the family name). */
    private fun structuredName(name: String): String {
        if (name.isEmpty()) return ";;;;"
        val parts = name.split(Regex("\\s+"))
        val family = if (parts.size > 1) parts.last() else ""
        val given = if (parts.size > 1) parts.dropLast(1).joinToString(" ") else parts.first()
        return "${escapeText(family)};${escapeText(given)};;;"
    }

    /** vCard / iCalendar TEXT escaping: `\ ; ,` and newlines. */
    fun escapeText(value: String): String {
        val sb = StringBuilder(value.length + 4)
        for (ch in value) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                ';' -> sb.append("\\;")
                ',' -> sb.append("\\,")
                '\n' -> sb.append("\\n")
                '\r' -> Unit
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    // ---- iCalendar ----

    private fun event(s: CreateSpec.Event): String {
        val lines = ArrayList<String>()
        lines += "BEGIN:VCALENDAR"
        lines += "VERSION:2.0"
        lines += "PRODID:-//QR Code Scanner and Generator//EN"
        lines += "BEGIN:VEVENT"
        lines += "SUMMARY:" + escapeText(s.title.trim())
        val start = s.startMillis
        if (start != null) {
            if (s.allDay) {
                val lastDay = s.endMillis ?: start
                lines += "DTSTART;VALUE=DATE:" + IcsDates.date(start)
                // DTEND is exclusive for all-day events (RFC 5545), so it is the day after the last day.
                lines += "DTEND;VALUE=DATE:" + IcsDates.date(lastDay + IcsDates.DAY_MILLIS)
            } else {
                lines += "DTSTART:" + IcsDates.utc(start)
                s.endMillis?.let { lines += "DTEND:" + IcsDates.utc(it) }
            }
        }
        if (s.location.isNotBlank()) lines += "LOCATION:" + escapeText(s.location.trim())
        if (s.description.isNotBlank()) lines += "DESCRIPTION:" + escapeText(s.description.trim())
        lines += "END:VEVENT"
        lines += "END:VCALENDAR"
        return lines.joinToString("\n")
    }
}

/** UTC date formatting without java.time (minSdk 24). */
internal object IcsDates {
    const val DAY_MILLIS = 86_400_000L

    /** `YYYYMMDD` of the UTC calendar day containing [millis]. */
    fun date(millis: Long): String {
        val (y, m, d) = civil(Math.floorDiv(millis, DAY_MILLIS))
        return pad(y, 4) + pad(m, 2) + pad(d, 2)
    }

    /** `YYYYMMDDTHHMMSSZ` in UTC. */
    fun utc(millis: Long): String {
        val secondOfDay = (Math.floorMod(millis, DAY_MILLIS) / 1000).toInt()
        val h = secondOfDay / 3600
        val mi = secondOfDay % 3600 / 60
        val s = secondOfDay % 60
        return date(millis) + "T" + pad(h, 2) + pad(mi, 2) + pad(s, 2) + "Z"
    }

    private fun pad(v: Int, n: Int): String = v.toString().padStart(n, '0')

    /** Days since 1970-01-01 to (year, month, day), proleptic Gregorian (Hinnant's algorithm). */
    private fun civil(daysSinceEpoch: Long): Triple<Int, Int, Int> {
        val z = daysSinceEpoch + 719_468
        val era = Math.floorDiv(z, 146_097L)
        val doe = z - era * 146_097
        val yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
        val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
        val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
        return Triple(y, m, d)
    }
}

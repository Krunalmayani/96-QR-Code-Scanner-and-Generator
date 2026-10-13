package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import java.util.Locale
import java.util.TimeZone

/** Host and the rest of a URL, split for the big-host / small-path layout of the website card (s15). */
data class UrlParts(val host: String, val rest: String)

object UrlDisplay {
    /** `https://www.brewhouse-coffee.com/menu?table=12` -> host `brewhouse-coffee.com`, rest `/menu?table=12`. */
    fun split(url: String): UrlParts {
        val withoutScheme = url.trim().substringAfter("://")
        val authorityEnd = withoutScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val authority = if (authorityEnd < 0) withoutScheme else withoutScheme.substring(0, authorityEnd)
        val rest = if (authorityEnd < 0) "" else withoutScheme.substring(authorityEnd)
        val host = authority.substringAfterLast('@').removePrefix("www.")
        val path = if (rest == "/") "" else rest
        return UrlParts(host.ifEmpty { url.trim() }, path)
    }
}

/** How a code is described in the header caption ("QR code", "Barcode" or the symbology name). */
enum class CodeKind { QR, BARCODE, OTHER }

object CodeKinds {
    private val LINEAR = setOf(
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

    fun of(format: CodeFormat): CodeKind = when {
        format.isQr -> CodeKind.QR
        format in LINEAR -> CodeKind.BARCODE
        else -> CodeKind.OTHER
    }
}

enum class DayBucket { TODAY, YESTERDAY, EARLIER }

object ResultTime {
    /** Which calendar day [timestamp] falls on relative to [now], in [zone]. */
    fun bucket(timestamp: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): DayBucket {
        val diff = dayNumber(now, zone) - dayNumber(timestamp, zone)
        return when (diff) {
            0L -> DayBucket.TODAY
            1L -> DayBucket.YESTERDAY
            else -> DayBucket.EARLIER
        }
    }

    private fun dayNumber(millis: Long, zone: TimeZone): Long =
        Math.floorDiv(millis + zone.getOffset(millis), DAY_MILLIS)

    private const val DAY_MILLIS = 86_400_000L
}

/** "5449000000996" -> "5 449000 000996" as printed on packaging. Other lengths are returned unchanged. */
object BarcodeNumber {
    fun group(number: String, format: CodeFormat): String {
        val digits = number.filter { it in '0'..'9' }
        if (digits.length != number.length) return number
        return when {
            format == CodeFormat.EAN_13 && digits.length == 13 -> "${digits.take(1)} ${digits.substring(1, 7)} ${digits.substring(7)}"
            format == CodeFormat.UPC_A && digits.length == 12 ->
                "${digits.take(1)} ${digits.substring(1, 6)} ${digits.substring(6, 11)} ${digits.substring(11)}"
            format == CodeFormat.EAN_8 && digits.length == 8 -> "${digits.take(4)} ${digits.substring(4)}"
            else -> number
        }
    }
}

/** Hex view of the raw payload for the "Unknown" result. */
object HexDump {
    const val BYTES_PER_LINE = 8
    const val MAX_BYTES = 256

    data class Dump(val lines: List<String>, val truncated: Boolean, val totalBytes: Int)

    /** Lines like `0000  48 65 6C 6C 6F 20 77 6F`; at most [MAX_BYTES] bytes of the UTF-8 encoding are shown. */
    fun of(text: String): Dump {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val shown = minOf(bytes.size, MAX_BYTES)
        val lines = (0 until shown step BYTES_PER_LINE).map { offset ->
            val end = minOf(offset + BYTES_PER_LINE, shown)
            val hex = (offset until end).joinToString(" ") { String.format(Locale.ROOT, "%02X", bytes[it].toInt() and 0xFF) }
            String.format(Locale.ROOT, "%04X  %s", offset, hex)
        }
        return Dump(lines, truncated = bytes.size > MAX_BYTES, totalBytes = bytes.size)
    }
}

/** Initials for the contact avatar (s17): first letters of the first two words, upper case. */
object Initials {
    fun of(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val letters = words.take(2).mapNotNull { word -> word.firstOrNull { it.isLetterOrDigit() } }
        return letters.joinToString("").uppercase()
    }
}

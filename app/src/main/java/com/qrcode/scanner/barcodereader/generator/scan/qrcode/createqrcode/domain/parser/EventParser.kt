package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Parses the first VEVENT of an iCalendar payload.
 *
 * Time rules: a bare date (`20240501`, or `VALUE=DATE`) is an all-day event at UTC midnight (the Android calendar
 * convention); a `...Z` time is UTC; a floating time (no Z) is interpreted in [defaultZone], and so is a time
 * whose `TZID=` is not a valid IANA zone id. A valid IANA `TZID` (for example `America/New_York`) is honoured.
 */
internal class EventParser(private val defaultZone: ZoneId) {
    private class Stamp(val millis: Long, val allDay: Boolean)

    fun parse(text: String, raw: String, format: CodeFormat): ScanContent.Event? {
        val lines = FieldParsing.unfoldLines(text)
        val begin = lines.indexOfFirst { it.trim().equals("BEGIN:VEVENT", ignoreCase = true) }
        if (begin < 0) return null
        var summary = ""
        var location = ""
        var description = ""
        var start: Stamp? = null
        var end: Stamp? = null
        var duration: String? = null
        for (i in begin + 1 until lines.size) {
            if (lines[i].trim().equals("END:VEVENT", ignoreCase = true)) break
            val cl = FieldParsing.parseContentLine(lines[i]) ?: continue
            when (cl.name) {
                "SUMMARY" -> if (summary.isEmpty()) summary = FieldParsing.unescapeText(cl.value).trim()
                "LOCATION" -> if (location.isEmpty()) location = FieldParsing.unescapeText(cl.value).trim()
                "DESCRIPTION" -> if (description.isEmpty()) description = FieldParsing.unescapeText(cl.value).trim()
                "DTSTART" -> if (start == null) start = parseTime(cl.value, cl.params)
                "DTEND" -> if (end == null) end = parseTime(cl.value, cl.params)
                "DURATION" -> if (duration == null) duration = cl.value.trim()
            }
        }
        val endMillis = end?.millis ?: addDuration(start, duration)
        if (summary.isEmpty() && location.isEmpty() && description.isEmpty() && start == null) return null
        return ScanContent.Event(
            rawValue = raw,
            format = format,
            summary = summary,
            startMillis = start?.millis,
            endMillis = endMillis,
            allDay = start?.allDay == true,
            location = location,
            description = description,
        )
    }

    private fun parseTime(value: String, params: Map<String, String>): Stamp? {
        val v = value.trim().replace("-", "").replace(":", "")
        val m = DATE_TIME.matchEntire(v) ?: return null
        return try {
            val date = LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
            val hh = m.groupValues[4]
            if (hh.isEmpty()) {
                return Stamp(date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), true)
            }
            val mm = m.groupValues[5]
            val ss = m.groupValues[6].ifEmpty { "0" }
            val time = LocalTime.of(hh.toInt(), mm.toInt(), ss.toInt())
            val zone = if (m.groupValues[7].equals("Z", ignoreCase = true)) ZoneOffset.UTC else zoneFor(params["TZID"])
            Stamp(LocalDateTime.of(date, time).atZone(zone).toInstant().toEpochMilli(), false)
        } catch (e: DateTimeException) {
            null
        }
    }

    private fun zoneFor(tzid: String?): ZoneId {
        if (tzid.isNullOrBlank()) return defaultZone
        return try {
            ZoneId.of(tzid.trim())
        } catch (e: DateTimeException) {
            defaultZone
        }
    }

    private fun addDuration(start: Stamp?, duration: String?): Long? {
        if (start == null || duration == null) return null
        val m = DURATION.matchEntire(duration) ?: return null
        val g = m.groupValues
        fun n(i: Int) = g[i].ifEmpty { "0" }.toLong()
        val seconds = n(2) * 604_800 + n(3) * 86_400 + n(4) * 3_600 + n(5) * 60 + n(6)
        val signed = if (g[1] == "-") -seconds else seconds
        return start.millis + signed * 1000
    }

    private companion object {
        val DATE_TIME = Regex("(\\d{4})(\\d{2})(\\d{2})(?:T(\\d{2})(\\d{2})(\\d{2})?(Z)?)?", RegexOption.IGNORE_CASE)
        val DURATION = Regex("([+-])?P(?:(\\d+)W)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?", RegexOption.IGNORE_CASE)
    }
}

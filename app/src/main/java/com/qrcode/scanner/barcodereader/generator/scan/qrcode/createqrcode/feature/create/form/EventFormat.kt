package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.text.format.DateFormat as AndroidDateFormat

/** Display text of event dates and times. java.util only: java.time needs API 26. */
object EventFormat {
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    private const val DATE_SKELETON = "yMMMEd"

    /** "Sat, Nov 14, 2026". All-day dates are UTC midnights, so they are formatted in UTC; timed ones in the device zone. */
    fun date(millis: Long, allDay: Boolean, locale: Locale = Locale.getDefault()): String {
        val pattern = AndroidDateFormat.getBestDateTimePattern(locale, DATE_SKELETON)
        val format = SimpleDateFormat(pattern, locale)
        format.timeZone = if (allDay) UTC else TimeZone.getDefault()
        return format.format(Date(millis))
    }

    /** "6:00 PM" or "18:00", following the device locale. */
    fun time(millis: Long, locale: Locale = Locale.getDefault()): String {
        val format = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
        format.timeZone = TimeZone.getDefault()
        return format.format(Date(millis))
    }
}

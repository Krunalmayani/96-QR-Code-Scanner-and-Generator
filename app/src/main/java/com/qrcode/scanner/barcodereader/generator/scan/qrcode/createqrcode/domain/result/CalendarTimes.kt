package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

/** Begin and end (epoch millis) handed to the calendar insert intent; null means "let the calendar choose". */
data class CalendarWindow(val beginMillis: Long?, val endMillis: Long?)

/**
 * Normalises the times of a scanned event for `CalendarContract.Events` inserts.
 *
 * - No start: nothing is passed and the calendar app uses its own default.
 * - All-day: begin is floored to UTC midnight (the calendar provider's convention) and end is the exclusive
 *   UTC midnight after the last day, at least one day after begin.
 * - Timed: end defaults to one hour after the start when missing or not after the start.
 */
object CalendarTimes {
    const val DAY_MILLIS = 86_400_000L
    const val HOUR_MILLIS = 3_600_000L

    fun window(startMillis: Long?, endMillis: Long?, allDay: Boolean): CalendarWindow {
        if (startMillis == null) return CalendarWindow(null, null)
        if (allDay) {
            val begin = floorToUtcDay(startMillis)
            val end = endMillis?.let { floorToUtcDay(it) }?.takeIf { it > begin } ?: (begin + DAY_MILLIS)
            return CalendarWindow(begin, end)
        }
        val end = endMillis?.takeIf { it > startMillis } ?: (startMillis + HOUR_MILLIS)
        return CalendarWindow(startMillis, end)
    }

    private fun floorToUtcDay(millis: Long): Long = Math.floorDiv(millis, DAY_MILLIS) * DAY_MILLIS
}

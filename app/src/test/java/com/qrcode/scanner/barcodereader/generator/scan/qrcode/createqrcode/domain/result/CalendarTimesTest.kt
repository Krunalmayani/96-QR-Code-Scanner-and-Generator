package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalendarTimesTest {
    private val day = CalendarTimes.DAY_MILLIS
    private val hour = CalendarTimes.HOUR_MILLIS
    private val midnight = 1_700_000_000_000L - (1_700_000_000_000L % 86_400_000L)

    @Test
    fun `no start leaves everything to the calendar`() {
        assertEquals(CalendarWindow(null, null), CalendarTimes.window(null, 5L, allDay = false))
        assertEquals(CalendarWindow(null, null), CalendarTimes.window(null, null, allDay = true))
    }

    @Test
    fun `timed event keeps its own end`() {
        val start = midnight + 18 * hour
        assertEquals(CalendarWindow(start, start + 150 * 60_000L), CalendarTimes.window(start, start + 150 * 60_000L, false))
    }

    @Test
    fun `timed event without a usable end lasts one hour`() {
        val start = midnight + 9 * hour
        assertEquals(CalendarWindow(start, start + hour), CalendarTimes.window(start, null, false))
        assertEquals(CalendarWindow(start, start + hour), CalendarTimes.window(start, start, false))
        assertEquals(CalendarWindow(start, start + hour), CalendarTimes.window(start, start - 1, false))
    }

    @Test
    fun `all day event is floored to utc midnight and spans at least one day`() {
        assertEquals(CalendarWindow(midnight, midnight + day), CalendarTimes.window(midnight + 5 * hour, null, true))
        assertEquals(CalendarWindow(midnight, midnight + day), CalendarTimes.window(midnight, midnight, true))
    }

    @Test
    fun `all day event keeps a later end as the exclusive end`() {
        assertEquals(CalendarWindow(midnight, midnight + 3 * day), CalendarTimes.window(midnight, midnight + 3 * day, true))
    }

    @Test
    fun `negative epoch values floor towards the earlier day`() {
        assertEquals(CalendarWindow(-day, 0L), CalendarTimes.window(-1L, null, true))
    }
}

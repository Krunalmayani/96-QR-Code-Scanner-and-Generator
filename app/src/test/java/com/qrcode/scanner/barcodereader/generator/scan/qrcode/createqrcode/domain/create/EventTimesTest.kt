package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.TimeZone

class EventTimesTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val berlin = TimeZone.getTimeZone("Europe/Berlin") // UTC+1 in November
    private val newYork = TimeZone.getTimeZone("America/New_York") // UTC-5 in November

    @Test fun combineBuildsTheLocalInstant() {
        val date = utcMillis(2026, 11, 14)
        assertEquals(utcMillis(2026, 11, 14, 18, 0), EventTimes.combine(date, TimeOfDay(18, 0), utc))
        assertEquals(utcMillis(2026, 11, 14, 17, 0), EventTimes.combine(date, TimeOfDay(18, 0), berlin))
        assertEquals(utcMillis(2026, 11, 14, 23, 30), EventTimes.combine(date, TimeOfDay(18, 30), newYork))
    }

    @Test fun combineIgnoresTheTimeOfDayInThePickedDate() {
        val noisy = utcMillis(2026, 11, 14, 13, 45, 12)
        assertEquals(utcMillis(2026, 11, 14, 6, 0), EventTimes.combine(noisy, TimeOfDay(6, 0), utc))
    }

    @Test fun dateOfIsTheLocalCalendarDayAsUtcMidnight() {
        val instant = utcMillis(2026, 11, 14, 23, 30)
        assertEquals(utcMillis(2026, 11, 14), EventTimes.dateOf(instant, utc))
        assertEquals(utcMillis(2026, 11, 15), EventTimes.dateOf(instant, berlin))
        assertEquals(utcMillis(2026, 11, 14), EventTimes.dateOf(instant, newYork))
        val early = utcMillis(2026, 11, 14, 2, 0)
        assertEquals(utcMillis(2026, 11, 13), EventTimes.dateOf(early, newYork))
    }

    @Test fun timeOfReadsLocalWallClock() {
        val instant = utcMillis(2026, 11, 14, 23, 30)
        assertEquals(TimeOfDay(23, 30), EventTimes.timeOf(instant, utc))
        assertEquals(TimeOfDay(0, 30), EventTimes.timeOf(instant, berlin))
        assertEquals(TimeOfDay(18, 30), EventTimes.timeOf(instant, newYork))
    }

    @Test fun combineAndSplitAreInverse() {
        val instant = utcMillis(2026, 11, 14, 20, 15)
        for (zone in listOf(utc, berlin, newYork)) {
            val back = EventTimes.combine(EventTimes.dateOf(instant, zone), EventTimes.timeOf(instant, zone), zone)
            assertEquals(instant, back, zone.id)
        }
    }

    @Test fun defaultsStartAtTheNextFullHour() {
        val now = utcMillis(2026, 11, 14, 17, 42, 30)
        assertEquals(utcMillis(2026, 11, 14, 18, 0), EventTimes.defaultStart(now, utc))
        assertEquals(utcMillis(2026, 11, 14, 19, 0), EventTimes.defaultEnd(EventTimes.defaultStart(now, utc)))
        val exact = utcMillis(2026, 11, 14, 17, 0)
        assertEquals(utcMillis(2026, 11, 14, 18, 0), EventTimes.defaultStart(exact, utc))
    }

    @Test fun blankEventIsValidOnceTitled() {
        val blank = EventTimes.blankEvent(utcMillis(2026, 11, 14, 17, 42), utc)
        assertFalse(Validators.isValid(blank))
        assertTrue(Validators.isValid(blank.copy(title = "Launch")))
        assertEquals(3_600_000L, blank.endMillis!! - blank.startMillis!!)
    }

    @Test fun switchingToAllDayKeepsTheCalendarDays() {
        val timed =
            CreateSpec.Event(
                title = "Launch",
                startMillis = utcMillis(2026, 11, 14, 18, 0),
                endMillis = utcMillis(2026, 11, 15, 8, 30),
            )
        val allDay = EventTimes.withAllDay(timed, true, utc)
        assertTrue(allDay.allDay)
        assertEquals(utcMillis(2026, 11, 14), allDay.startMillis)
        assertEquals(utcMillis(2026, 11, 15), allDay.endMillis)
        assertTrue(Validators.isValid(allDay))
    }

    @Test fun switchingToAllDayInALocalZoneUsesTheLocalDay() {
        val timed = CreateSpec.Event(title = "Late", startMillis = utcMillis(2026, 11, 14, 23, 30), endMillis = null)
        val allDay = EventTimes.withAllDay(timed, true, berlin)
        assertEquals(utcMillis(2026, 11, 15), allDay.startMillis)
        assertEquals(allDay.startMillis, allDay.endMillis)
    }

    @Test fun switchingBackGivesAMorningHourAndOneHourLength() {
        val allDay =
            CreateSpec.Event(
                title = "Day",
                allDay = true,
                startMillis = utcMillis(2026, 11, 14),
                endMillis = utcMillis(2026, 11, 14),
            )
        val timed = EventTimes.withAllDay(allDay, false, utc)
        assertFalse(timed.allDay)
        assertEquals(utcMillis(2026, 11, 14, 9, 0), timed.startMillis)
        assertEquals(utcMillis(2026, 11, 14, 10, 0), timed.endMillis)
    }

    @Test fun movingTheStartKeepsTheDuration() {
        val e = CreateSpec.Event(title = "x", startMillis = utcMillis(2026, 11, 14, 18, 0), endMillis = utcMillis(2026, 11, 14, 20, 30))
        val moved = EventTimes.withStart(e, utcMillis(2026, 11, 16, 9, 0))
        assertEquals(utcMillis(2026, 11, 16, 9, 0), moved.startMillis)
        assertEquals(utcMillis(2026, 11, 16, 11, 30), moved.endMillis)
        assertTrue(Validators.isValid(moved))
    }

    @Test fun movingTheStartWithoutEndOrOldStart() {
        val noEnd = EventTimes.withStart(CreateSpec.Event(title = "x", startMillis = 100L), 500L)
        assertNull(noEnd.endMillis)
        val noStart = EventTimes.withStart(CreateSpec.Event(title = "x", endMillis = 300L), 500L)
        assertEquals(500L, noStart.endMillis)
    }

    @Test fun sameModeIsANoOp() {
        val e = CreateSpec.Event(title = "x", startMillis = 5L, endMillis = 6L)
        assertEquals(e, EventTimes.withAllDay(e, false, utc))
    }

    @Test fun nullTimesStayNull() {
        val e = CreateSpec.Event(title = "x")
        val allDay = EventTimes.withAllDay(e, true, utc)
        assertNull(allDay.startMillis)
        assertNull(allDay.endMillis)
    }
}

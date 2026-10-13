package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import java.util.Calendar
import java.util.TimeZone

/** Hour and minute of a wall-clock time. */
data class TimeOfDay(val hour: Int, val minute: Int)

/**
 * Converts between what the Material3 pickers hand over (a date as UTC midnight millis, an hour and a minute) and the
 * values stored in [CreateSpec.Event]: an instant for timed events, a UTC-midnight date for all-day events.
 * Uses java.util.Calendar only (java.time needs API 26).
 */
object EventTimes {
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    private const val HOUR_MILLIS = 3_600_000L
    private const val DEFAULT_ALL_DAY_HOUR = 9

    private fun calendar(zone: TimeZone, millis: Long): Calendar =
        Calendar.getInstance(zone).apply { timeInMillis = millis }

    /** The instant at [time] on the calendar day that [utcDateMillis] (UTC midnight from a date picker) names, in [zone]. */
    fun combine(utcDateMillis: Long, time: TimeOfDay, zone: TimeZone): Long {
        val day = calendar(UTC, utcDateMillis)
        return Calendar.getInstance(zone).apply {
            clear()
            set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), time.hour, time.minute, 0)
        }.timeInMillis
    }

    /** UTC midnight of the calendar day that [instantMillis] falls on in [zone] (initial selection of a date picker). */
    fun dateOf(instantMillis: Long, zone: TimeZone): Long {
        val local = calendar(zone, instantMillis)
        return Calendar.getInstance(UTC).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        }.timeInMillis
    }

    fun timeOf(instantMillis: Long, zone: TimeZone): TimeOfDay {
        val local = calendar(zone, instantMillis)
        return TimeOfDay(local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE))
    }

    /** Next full hour after [nowMillis]. */
    fun defaultStart(nowMillis: Long, zone: TimeZone): Long {
        val local = calendar(zone, nowMillis).apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return local.timeInMillis + HOUR_MILLIS
    }

    /** One hour after [startMillis]. */
    fun defaultEnd(startMillis: Long): Long = startMillis + HOUR_MILLIS

    /** Timed to all-day: the calendar day of the instant, as UTC midnight. */
    fun toAllDay(instantMillis: Long, zone: TimeZone): Long = dateOf(instantMillis, zone)

    /** All-day to timed: [utcDateMillis] at [hour]:00 local time. */
    fun fromAllDay(utcDateMillis: Long, zone: TimeZone, hour: Int = DEFAULT_ALL_DAY_HOUR): Long =
        combine(utcDateMillis, TimeOfDay(hour, 0), zone)

    /** Switches an event between timed and all-day, converting both ends. */
    fun withAllDay(event: CreateSpec.Event, allDay: Boolean, zone: TimeZone): CreateSpec.Event {
        if (event.allDay == allDay) return event
        val start = event.startMillis
        val end = event.endMillis
        return if (allDay) {
            event.copy(
                allDay = true,
                startMillis = start?.let { toAllDay(it, zone) },
                endMillis = end?.let { toAllDay(it, zone) } ?: start?.let { toAllDay(it, zone) },
            )
        } else {
            val newStart = start?.let { fromAllDay(it, zone) }
            event.copy(
                allDay = false,
                startMillis = newStart,
                endMillis = end?.let { fromAllDay(it, zone, DEFAULT_ALL_DAY_HOUR + 1) } ?: newStart?.let { defaultEnd(it) },
            )
        }
    }

    /** Moves the start to [newStart] and the end by the same distance, so the duration is kept. */
    fun withStart(event: CreateSpec.Event, newStart: Long): CreateSpec.Event {
        val oldStart = event.startMillis
        val end = event.endMillis
        val newEnd = when {
            end == null -> null
            oldStart == null -> maxOf(end, newStart)
            else -> end + (newStart - oldStart)
        }
        return event.copy(startMillis = newStart, endMillis = newEnd)
    }

    /** A new blank event starting at the next full hour and lasting one hour. */
    fun blankEvent(nowMillis: Long, zone: TimeZone): CreateSpec.Event {
        val start = defaultStart(nowMillis, zone)
        return CreateSpec.Event(startMillis = start, endMillis = defaultEnd(start))
    }
}

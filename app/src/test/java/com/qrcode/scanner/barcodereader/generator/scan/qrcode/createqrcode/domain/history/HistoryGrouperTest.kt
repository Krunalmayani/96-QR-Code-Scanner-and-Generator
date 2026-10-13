package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryGrouperTest {
    private val utc = ZoneId.of("UTC")
    private val now: Instant = at(2024, 5, 10, 12, 0, utc)

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int, zone: ZoneId): Instant =
        LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant()

    private fun record(id: Long, instant: Instant): ScanRecord =
        Fixtures.record(id = id, createdAt = instant.toEpochMilli())

    @Test
    fun `empty input gives no groups`() {
        assertTrue(HistoryGrouper.group(emptyList(), now, utc).isEmpty())
    }

    @Test
    fun `groups into today yesterday and dated days newest first`() {
        val records = listOf(
            record(1, at(2024, 5, 10, 11, 0, utc)),
            record(2, at(2024, 5, 10, 9, 0, utc)),
            record(3, at(2024, 5, 9, 23, 59, utc)),
            record(4, at(2024, 5, 8, 8, 0, utc)),
            record(5, at(2023, 12, 31, 8, 0, utc)),
        )
        val groups = HistoryGrouper.group(records, now, utc)
        assertEquals(
            listOf(
                DayLabel.Today,
                DayLabel.Yesterday,
                DayLabel.Date(LocalDate.of(2024, 5, 8)),
                DayLabel.Date(LocalDate.of(2023, 12, 31)),
            ),
            groups.map { it.label },
        )
        assertEquals(listOf(listOf(1L, 2L), listOf(3L), listOf(4L), listOf(5L)), groups.map { g -> g.records.map { it.id } })
    }

    @Test
    fun `midnight boundaries belong to the new day`() {
        val records = listOf(
            record(1, at(2024, 5, 10, 0, 0, utc)),
            record(2, at(2024, 5, 9, 23, 59, utc)),
            record(3, at(2024, 5, 9, 0, 0, utc)),
            record(4, at(2024, 5, 8, 23, 59, utc)),
        )
        val groups = HistoryGrouper.group(records, now, utc)
        assertEquals(listOf<DayLabel>(DayLabel.Today, DayLabel.Yesterday, DayLabel.Date(LocalDate.of(2024, 5, 8))), groups.map { it.label })
        assertEquals(listOf(1L), groups[0].records.map { it.id })
        assertEquals(listOf(2L, 3L), groups[1].records.map { it.id })
    }

    @Test
    fun `the zone decides the calendar day`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        // 2024-05-09 20:00 UTC is already 2024-05-10 05:00 in Tokyo.
        val instant = at(2024, 5, 9, 20, 0, utc)
        val nowInTokyo = at(2024, 5, 10, 12, 0, tokyo)
        assertEquals(DayLabel.Today, HistoryGrouper.group(listOf(record(1, instant)), nowInTokyo, tokyo).single().label)
        assertEquals(DayLabel.Yesterday, HistoryGrouper.group(listOf(record(1, instant)), nowInTokyo, utc).single().label)
    }

    @Test
    fun `month and year boundaries are handled`() {
        val newYear = at(2024, 1, 1, 10, 0, utc)
        val groups = HistoryGrouper.group(listOf(record(1, at(2023, 12, 31, 22, 0, utc))), newYear, utc)
        assertEquals(DayLabel.Yesterday, groups.single().label)
    }

    @Test
    fun `future records get a dated label and sort first`() {
        val groups = HistoryGrouper.group(
            listOf(record(1, at(2024, 5, 10, 8, 0, utc)), record(2, at(2024, 5, 12, 8, 0, utc))),
            now,
            utc,
        )
        assertEquals(DayLabel.Date(LocalDate.of(2024, 5, 12)), groups.first().label)
        assertEquals(DayLabel.Today, groups.last().label)
    }

    @Test
    fun `unsorted input still gives sorted groups and keeps the order inside a day`() {
        val records = listOf(
            record(1, at(2024, 5, 8, 8, 0, utc)),
            record(2, at(2024, 5, 10, 8, 0, utc)),
            record(3, at(2024, 5, 8, 9, 0, utc)),
        )
        val groups = HistoryGrouper.group(records, now, utc)
        assertEquals(listOf(DayLabel.Today, DayLabel.Date(LocalDate.of(2024, 5, 8))), groups.map { it.label })
        assertEquals(listOf(1L, 3L), groups[1].records.map { it.id })
    }

    @Test
    fun `label keys are unique per day`() {
        val keys = listOf(DayLabel.Today, DayLabel.Yesterday, DayLabel.Date(LocalDate.of(2024, 5, 8)), DayLabel.Date(LocalDate.of(2024, 5, 7))).map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }
}

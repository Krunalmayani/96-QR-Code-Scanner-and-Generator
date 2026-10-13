package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Sticky day header of a history group. The UI turns it into text ("Today", "Yesterday", a formatted date). */
sealed interface DayLabel {
    data object Today : DayLabel

    data object Yesterday : DayLabel

    data class Date(
        val date: LocalDate,
    ) : DayLabel

    /** Stable id for list keys. */
    val key: String
        get() =
            when (this) {
                Today -> "today"
                Yesterday -> "yesterday"
                is Date -> "date-$date"
            }
}

data class DayGroup(
    val label: DayLabel,
    val records: List<ScanRecord>,
)

object HistoryGrouper {
    /**
     * Groups [records] by local calendar day in [zone], newest day first. Order inside a day is the input order
     * (the repository returns newest first). [now] decides what counts as today and yesterday.
     */
    fun group(records: List<ScanRecord>, now: Instant, zone: ZoneId): List<DayGroup> {
        val today = now.atZone(zone).toLocalDate()
        val byDay = LinkedHashMap<LocalDate, MutableList<ScanRecord>>()
        for (record in records) {
            val day = Instant.ofEpochMilli(record.createdAt).atZone(zone).toLocalDate()
            byDay.getOrPut(day) { mutableListOf() }.add(record)
        }
        return byDay.entries
            .sortedByDescending { it.key }
            .map { DayGroup(labelFor(it.key, today), it.value) }
    }

    fun labelFor(day: LocalDate, today: LocalDate): DayLabel =
        when (day) {
            today -> DayLabel.Today
            today.minusDays(1) -> DayLabel.Yesterday
            else -> DayLabel.Date(day)
        }
}

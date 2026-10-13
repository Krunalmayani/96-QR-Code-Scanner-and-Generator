package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

/** One line of the flattened History list: day header, record row or native-ad slot. */
sealed interface HistoryListItem {
    val key: String

    data class Header(
        val label: DayLabel,
    ) : HistoryListItem {
        override val key: String get() = "header-${label.key}"
    }

    /** [first]/[last] tell the UI which rows open and close a rounded card (ads split a card in two). */
    data class Row(
        val record: ScanRecord,
        val first: Boolean,
        val last: Boolean,
    ) : HistoryListItem {
        override val key: String get() = "row-${record.id}"
    }

    data class Ad(
        val index: Int,
    ) : HistoryListItem {
        override val key: String get() = "ad-$index"
    }
}

object HistoryListBuilder {
    const val AD_EVERY = 8

    /** Flattens [groups]; a native ad slot follows every [adEvery]-th record unless it would be the last line. */
    fun build(groups: List<DayGroup>, adEvery: Int = AD_EVERY, adsEnabled: Boolean = true): List<HistoryListItem> {
        val total = groups.sumOf { it.records.size }
        val out = ArrayList<HistoryListItem>(total + groups.size + total / adEvery.coerceAtLeast(1))
        var seen = 0
        var adIndex = 0
        for (group in groups) {
            if (group.records.isEmpty()) continue
            out += HistoryListItem.Header(group.label)
            for (record in group.records) {
                out += HistoryListItem.Row(record, first = false, last = false)
                seen++
                if (adsEnabled && adEvery > 0 && seen % adEvery == 0 && seen < total) {
                    out += HistoryListItem.Ad(adIndex++)
                }
            }
        }
        return markCardEdges(out)
    }

    private fun markCardEdges(items: List<HistoryListItem>): List<HistoryListItem> =
        items.mapIndexed { index, item ->
            if (item is HistoryListItem.Row) {
                item.copy(
                    first = items.getOrNull(index - 1) !is HistoryListItem.Row,
                    last = items.getOrNull(index + 1) !is HistoryListItem.Row,
                )
            } else {
                item
            }
        }
}

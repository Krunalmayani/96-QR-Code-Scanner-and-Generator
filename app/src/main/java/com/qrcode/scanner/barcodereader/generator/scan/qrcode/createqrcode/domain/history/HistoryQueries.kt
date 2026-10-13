package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter

/**
 * What to ask the repository for plus a post-filter. The repository filters one source at a time, so the Scanned
 * tab (CAMERA + IMAGE) observes every source and drops CREATED rows afterwards ([excludeCreated]).
 */
data class HistoryQuerySpec(
    val filter: HistoryFilter,
    val excludeCreated: Boolean,
) {
    fun select(records: List<ScanRecord>): List<ScanRecord> =
        if (excludeCreated) records.filter { it.source != ScanSource.CREATED } else records
}

object HistoryQueries {
    /** Normal browsing: a [tab] narrowed by a filter [chip] (and optionally a [query]). */
    fun browse(tab: HistoryTab, chip: HistoryChip, query: String = ""): HistoryQuerySpec {
        val source = if (tab == HistoryTab.CREATED) ScanSource.CREATED else null
        return HistoryQuerySpec(
            filter = HistoryFilter(
                source = source,
                types = HistoryChipMapper.contentTypes(chip),
                favoritesOnly = HistoryChipMapper.favoritesOnly(chip),
                query = query,
            ),
            excludeCreated = tab == HistoryTab.SCANNED,
        )
    }

    /** Search mode: [scope] chips instead of tabs, text [query] matched by the repository. */
    fun search(scope: SearchScope, query: String): HistoryQuerySpec = when (scope) {
        SearchScope.ALL -> HistoryQuerySpec(HistoryFilter(query = query), excludeCreated = false)
        SearchScope.SCANNED -> HistoryQuerySpec(HistoryFilter(query = query), excludeCreated = true)
        SearchScope.CREATED -> HistoryQuerySpec(HistoryFilter(source = ScanSource.CREATED, query = query), excludeCreated = false)
        SearchScope.FAVORITES -> HistoryQuerySpec(HistoryFilter(favoritesOnly = true, query = query), excludeCreated = false)
    }
}

/** Which empty state to show when nothing matches. */
enum class HistoryEmptyKind { SCANNED, CREATED, FAVORITES, FILTER, SEARCH }

object HistoryEmpty {
    fun resolve(
        tab: HistoryTab,
        chip: HistoryChip,
        searchActive: Boolean,
        query: String,
        scope: SearchScope,
    ): HistoryEmptyKind {
        if (searchActive) {
            if (query.isNotBlank()) return HistoryEmptyKind.SEARCH
            return when (scope) {
                SearchScope.FAVORITES -> HistoryEmptyKind.FAVORITES
                SearchScope.CREATED -> HistoryEmptyKind.CREATED
                SearchScope.SCANNED, SearchScope.ALL -> HistoryEmptyKind.SCANNED
            }
        }
        return when {
            chip == HistoryChip.FAVORITES -> HistoryEmptyKind.FAVORITES
            chip != HistoryChip.ALL -> HistoryEmptyKind.FILTER
            tab == HistoryTab.CREATED -> HistoryEmptyKind.CREATED
            else -> HistoryEmptyKind.SCANNED
        }
    }
}

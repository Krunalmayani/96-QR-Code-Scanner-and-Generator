package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SelectionState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

/** Everything the History screen draws. */
data class HistoryUiState(
    val tab: HistoryTab = HistoryTab.SCANNED,
    val chip: HistoryChip = HistoryChip.ALL,
    val searchActive: Boolean = false,
    val searchScope: SearchScope = SearchScope.ALL,
    /** What the user typed (updates on every key). */
    val query: String = "",
    /** The debounced query the shown results were computed for; used for highlighting and the result count. */
    val appliedQuery: String = "",
    val groups: List<DayGroup> = emptyList(),
    val recordCount: Int = 0,
    val loading: Boolean = true,
    val emptyKind: HistoryEmptyKind = HistoryEmptyKind.SCANNED,
    val selection: SelectionState = SelectionState(),
) {
    val isEmpty: Boolean get() = !loading && recordCount == 0

    val visibleIds: Set<Long> get() = groups.flatMapTo(HashSet()) { group -> group.records.map { it.id } }
}

sealed interface HistoryEvent {
    data class SelectTab(val tab: HistoryTab) : HistoryEvent

    data class SelectChip(val chip: HistoryChip) : HistoryEvent

    data object OpenSearch : HistoryEvent

    data object CloseSearch : HistoryEvent

    data class QueryChanged(val query: String) : HistoryEvent

    data class SelectScope(val scope: SearchScope) : HistoryEvent

    /** Empty-state button: drop the favorites/type filter (or the search scope) and show everything. */
    data object BrowseAll : HistoryEvent

    /** Row tap: opens the record, or toggles it while selecting. */
    data class RowClicked(val id: Long) : HistoryEvent

    data class RowLongClicked(val id: Long) : HistoryEvent

    data class StarClicked(val id: Long) : HistoryEvent

    data class SwipeDeleted(val id: Long) : HistoryEvent

    data object BeginSelection : HistoryEvent

    data object ExitSelection : HistoryEvent

    data object ToggleSelectAll : HistoryEvent

    data object FavoriteSelected : HistoryEvent

    data object DeleteSelected : HistoryEvent

    data object ShareSelected : HistoryEvent

    data object ExportSelected : HistoryEvent

    data class UndoDelete(val records: List<ScanRecord>) : HistoryEvent
}

/** One-shot effects, delivered once through a Channel. */
sealed interface HistoryEffect {
    data class OpenRecord(val id: Long) : HistoryEffect

    /** Rows were deleted: show the 5 s Undo snackbar and send [HistoryEvent.UndoDelete] with [records] on tap. */
    data class RecordsDeleted(val records: List<ScanRecord>) : HistoryEffect

    data class ShareText(val text: String) : HistoryEffect

    data class ExportCsv(val csv: String) : HistoryEffect
}

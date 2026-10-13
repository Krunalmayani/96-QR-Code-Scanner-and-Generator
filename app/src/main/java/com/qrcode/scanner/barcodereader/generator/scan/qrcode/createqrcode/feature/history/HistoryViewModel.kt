package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormatter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmpty
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryGrouper
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryQueries
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryQuerySpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryShareText
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SelectionState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.DeleteRecordsUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * History list: tabs, chips, debounced search, day grouping, multi-select and delete with undo.
 * The primary constructor is for tests (injectable clock); Hilt uses the secondary one.
 */
@HiltViewModel
class HistoryViewModel internal constructor(
    private val history: HistoryRepository,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val deleteRecords: DeleteRecordsUseCase,
    private val analytics: Analytics,
    private val nowProvider: () -> Instant,
    private val zoneProvider: () -> ZoneId,
) : ViewModel() {

    @Inject
    constructor(
        history: HistoryRepository,
        toggleFavorite: ToggleFavoriteUseCase,
        deleteRecords: DeleteRecordsUseCase,
        analytics: Analytics,
    ) : this(history, toggleFavorite, deleteRecords, analytics, { Instant.now() }, { ZoneId.systemDefault() })

    /** User-controlled inputs. [query] is the raw text of the search field. */
    private data class Controls(
        val tab: HistoryTab = HistoryTab.SCANNED,
        val chip: HistoryChip = HistoryChip.ALL,
        val searchActive: Boolean = false,
        val scope: SearchScope = SearchScope.ALL,
        val query: String = "",
    )

    /** [Controls] with the debounced query: what the repository is actually asked for. */
    private data class Request(
        val tab: HistoryTab,
        val chip: HistoryChip,
        val searchActive: Boolean,
        val scope: SearchScope,
        val query: String,
    ) {
        fun spec(): HistoryQuerySpec =
            if (searchActive) HistoryQueries.search(scope, query) else HistoryQueries.browse(tab, chip)
    }

    private class Loaded(val request: Request, val records: List<ScanRecord>)

    private val controls = MutableStateFlow(Controls())
    private val selection = MutableStateFlow(SelectionState())
    private val effects = Channel<HistoryEffect>(Channel.BUFFERED)

    val effect: Flow<HistoryEffect> = effects.receiveAsFlow()

    @OptIn(FlowPreview::class)
    private val appliedQuery: Flow<String> = controls
        .map { if (it.searchActive) it.query else "" }
        .distinctUntilChanged()
        .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()

    private val request: Flow<Request> = combine(
        controls.map { it.copy(query = "") }.distinctUntilChanged(),
        appliedQuery,
    ) { c, query -> Request(c.tab, c.chip, c.searchActive, c.scope, if (c.searchActive) query else "") }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val loaded: Flow<Loaded> = request.flatMapLatest { req ->
        val spec = req.spec()
        history.observe(spec.filter).map { rows -> Loaded(req, spec.select(rows)) }
    }

    val uiState: StateFlow<HistoryUiState> = combine(controls, selection, loaded) { c, sel, result ->
        val groups = HistoryGrouper.group(result.records, nowProvider(), zoneProvider())
        val req = result.request
        val visible = result.records.mapTo(HashSet()) { it.id }
        HistoryUiState(
            tab = c.tab,
            chip = c.chip,
            searchActive = c.searchActive,
            searchScope = c.scope,
            query = c.query,
            appliedQuery = req.query,
            groups = groups,
            recordCount = result.records.size,
            loading = false,
            emptyKind = HistoryEmpty.resolve(req.tab, req.chip, req.searchActive, req.query, req.scope),
            selection = sel.retain(visible),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HistoryUiState())

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.SelectTab -> selectTab(event.tab)
            is HistoryEvent.SelectChip -> controls.update { it.copy(chip = event.chip) }
            HistoryEvent.OpenSearch -> openSearch()
            HistoryEvent.CloseSearch -> closeSearch()
            is HistoryEvent.QueryChanged -> controls.update { it.copy(query = event.query.take(MAX_QUERY_LENGTH)) }
            is HistoryEvent.SelectScope -> controls.update { it.copy(scope = event.scope) }
            HistoryEvent.BrowseAll -> browseAll()
            is HistoryEvent.RowClicked -> rowClicked(event.id)
            is HistoryEvent.RowLongClicked -> selection.update { it.longPress(event.id) }
            is HistoryEvent.StarClicked -> toggleStar(event.id)
            is HistoryEvent.SwipeDeleted -> delete(setOf(event.id))
            HistoryEvent.BeginSelection -> selection.update { it.begin() }
            HistoryEvent.ExitSelection -> selection.value = SelectionState()
            HistoryEvent.ToggleSelectAll -> selection.update { it.toggleAll(uiState.value.visibleIds) }
            HistoryEvent.FavoriteSelected -> favoriteSelected()
            HistoryEvent.DeleteSelected -> delete(selection.value.ids, leaveSelection = true)
            HistoryEvent.ShareSelected -> shareSelected()
            HistoryEvent.ExportSelected -> exportSelected()
            is HistoryEvent.UndoDelete -> viewModelScope.launch { deleteRecords.undo(event.records) }
        }
    }

    private fun selectTab(tab: HistoryTab) {
        selection.value = SelectionState()
        controls.update { it.copy(tab = tab) }
    }

    private fun openSearch() {
        if (controls.value.searchActive) return
        selection.value = SelectionState()
        controls.update { it.copy(searchActive = true, scope = SearchScope.ALL, query = "") }
    }

    private fun closeSearch() {
        selection.value = SelectionState()
        controls.update { it.copy(searchActive = false, scope = SearchScope.ALL, query = "") }
    }

    private fun browseAll() {
        controls.update {
            if (it.searchActive) it.copy(scope = SearchScope.ALL, query = "") else it.copy(chip = HistoryChip.ALL)
        }
    }

    private fun rowClicked(id: Long) {
        if (selection.value.active) {
            selection.update { it.toggle(id) }
        } else {
            effects.trySend(HistoryEffect.OpenRecord(id))
        }
    }

    private fun toggleStar(id: Long) {
        viewModelScope.launch {
            val type = history.getById(id)?.type
            if (toggleFavorite(id) != null && type != null) logFavoriteToggle(listOf(type.name))
        }
    }

    private fun favoriteSelected() {
        viewModelScope.launch {
            val records = selectedRecords()
            if (records.isEmpty()) return@launch
            val makeFavorite = records.any { !it.isFavorite }
            toggleFavorite.set(records.map { it.id }.toSet(), makeFavorite)
            logFavoriteToggle(records.map { it.type.name }.distinct())
            selection.value = SelectionState()
        }
    }

    private fun delete(ids: Set<Long>, leaveSelection: Boolean = false) {
        viewModelScope.launch {
            val removed = deleteRecords(ids)
            if (leaveSelection) selection.value = SelectionState()
            if (removed.isNotEmpty()) effects.send(HistoryEffect.RecordsDeleted(removed))
        }
    }

    private fun shareSelected() {
        viewModelScope.launch {
            val text = HistoryShareText.build(selectedRecords())
            if (text.isNotEmpty()) effects.send(HistoryEffect.ShareText(text))
        }
    }

    private fun exportSelected() {
        viewModelScope.launch {
            val records = selectedRecords()
            if (records.isNotEmpty()) {
                effects.send(HistoryEffect.ExportCsv(HistoryExportFormatter.format(records, HistoryExportFormat.CSV)))
            }
        }
    }

    /** Selected rows as stored right now, newest first. Ids that no longer exist are skipped. */
    private suspend fun selectedRecords(): List<ScanRecord> =
        selection.value.ids.mapNotNull { history.getById(it) }
            .sortedWith(compareByDescending<ScanRecord> { it.createdAt }.thenByDescending { it.id })

    private fun logFavoriteToggle(types: List<String>) {
        types.forEach { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to it)) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
        const val STOP_TIMEOUT_MS = 5_000L
        const val MAX_QUERY_LENGTH = 200
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.IoDispatcher
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormatter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportFileName
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject

/** A formatted export ready to be written to a file the user picks, or shared. */
data class ExportPayload(
    val fileName: String,
    val mimeType: String,
    val text: String,
)

data class ExportUiState(
    val loading: Boolean = true,
    val counts: ExportCounts = ExportCounts(scanned = 0, created = 0),
    val selection: ExportSelection = ExportSelection(),
    val format: HistoryExportFormat = HistoryExportFormat.CSV,
    /** Rows the current selection would export. */
    val itemCount: Int = 0,
    val busy: Boolean = false,
) {
    val canExport: Boolean get() = !loading && !busy && itemCount > 0
}

sealed interface ExportEvent {
    data class SetIncludeScanned(
        val on: Boolean,
    ) : ExportEvent

    data class SetIncludeCreated(
        val on: Boolean,
    ) : ExportEvent

    data class SetFavoritesOnly(
        val on: Boolean,
    ) : ExportEvent

    data class SelectFormat(
        val format: HistoryExportFormat,
    ) : ExportEvent

    data object SaveClicked : ExportEvent

    data object ShareClicked : ExportEvent
}

sealed interface ExportEffect {
    /** Open the system "create document" dialog for [payload], then write its text to the chosen Uri. */
    data class ChooseSaveLocation(
        val payload: ExportPayload,
    ) : ExportEffect

    /** Write [payload] to the cache and open the share sheet. */
    data class Share(
        val payload: ExportPayload,
    ) : ExportEffect

    data object Failed : ExportEffect
}

@HiltViewModel
class ExportHistoryViewModel internal constructor(
    private val historyRepository: HistoryRepository,
    private val analytics: Analytics,
    private val workDispatcher: CoroutineDispatcher,
    private val clock: () -> Long,
    private val zone: ZoneId,
) : ViewModel() {
    @Inject
    constructor(
        historyRepository: HistoryRepository,
        analytics: Analytics,
        @IoDispatcher workDispatcher: CoroutineDispatcher,
    ) : this(historyRepository, analytics, workDispatcher, System::currentTimeMillis, ZoneId.systemDefault())

    private var records: List<ScanRecord> = emptyList()

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    private val effectChannel = Channel<ExportEffect>(Channel.BUFFERED)
    val effects: Flow<ExportEffect> = effectChannel.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: ExportEvent) {
        when (event) {
            is ExportEvent.SetIncludeScanned -> select { it.copy(includeScanned = event.on) }
            is ExportEvent.SetIncludeCreated -> select { it.copy(includeCreated = event.on) }
            is ExportEvent.SetFavoritesOnly -> select { it.copy(favoritesOnly = event.on) }
            is ExportEvent.SelectFormat -> _state.update { it.copy(format = event.format) }
            ExportEvent.SaveClicked -> export(share = false)
            ExportEvent.ShareClicked -> export(share = true)
        }
    }

    private fun load() {
        viewModelScope.launch {
            try {
                records = historyRepository.all()
                _state.update {
                    it.copy(loading = false, counts = ExportCounts.of(records), itemCount = it.selection.select(records).size)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loading = false) }
                effectChannel.send(ExportEffect.Failed)
            }
        }
    }

    private fun select(change: (ExportSelection) -> ExportSelection) {
        _state.update {
            val selection = change(it.selection)
            it.copy(selection = selection, itemCount = selection.select(records).size)
        }
    }

    private fun export(share: Boolean) {
        val current = _state.value
        if (!current.canExport) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val rows = current.selection.select(records)
                val text = withContext(workDispatcher) { HistoryExportFormatter.format(rows, current.format) }
                val payload =
                    ExportPayload(
                        fileName = ExportFileName.build(current.format.extension, clock(), zone),
                        mimeType = current.format.mimeType,
                        text = text,
                    )
                analytics.log(Events.HISTORY_EXPORT, mapOf(Params.FORMAT to current.format.name))
                effectChannel.send(if (share) ExportEffect.Share(payload) else ExportEffect.ChooseSaveLocation(payload))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                effectChannel.send(ExportEffect.Failed)
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}

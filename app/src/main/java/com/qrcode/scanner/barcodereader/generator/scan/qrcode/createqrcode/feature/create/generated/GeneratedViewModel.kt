package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.ApplicationScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeInputs
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CheckDigitInfo
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ContrastChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ExportDimensions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ExportNaming
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.PixelSize
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ScanReliability
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleEdit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.applying
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.CodeExporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.ExportJob
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.LogoStore
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.SaveOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorTab { COLORS, SHAPES, EXTRAS }

enum class GeneratedStatus { LOADING, READY, NOT_FOUND }

/** Short outcomes the screen turns into a toast (successes) or a snackbar (problems). */
enum class GeneratedMessage(val isSuccess: Boolean) {
    SAVED_TO_GALLERY(true),
    SAVED_TO_DOWNLOADS(true),
    COPIED(true),
    SAVE_FAILED(false),
    SHARE_FAILED(false),
    PERMISSION_DENIED(false),
    LOGO_FAILED(false),
}

/**
 * State of the Generated screen and its customize editor. [style] is the live style: it is shown immediately and
 * written to the repository 300 ms after the last change. [image] is the picture for [style] (null when the content
 * cannot be encoded).
 */
data class GeneratedUiState(
    val status: GeneratedStatus = GeneratedStatus.LOADING,
    val record: ScanRecord? = null,
    val style: QrStyle = QrStyle(),
    val image: QrVectorImage? = null,
    val reliability: ScanReliability? = null,
    val pixelSize: PixelSize? = null,
    val checkDigit: CheckDigitInfo? = null,
    val isFavorite: Boolean = false,
    val editing: Boolean = false,
    val tab: EditorTab = EditorTab.COLORS,
    /** True while an export is being produced. */
    val working: Boolean = false,
) {
    val isBarcode: Boolean get() = record?.format?.isQr == false

    /** Editor tabs: barcodes have no shapes (and nothing QR-specific under Extras). */
    val tabs: List<EditorTab> get() = if (isBarcode) listOf(EditorTab.COLORS, EditorTab.EXTRAS) else EditorTab.entries
}

sealed interface GeneratedEvent {
    data object ToggleFavorite : GeneratedEvent

    data class OpenEditor(val tab: EditorTab = EditorTab.COLORS) : GeneratedEvent

    /** Leaves the editor and writes the style right away. */
    data object CloseEditor : GeneratedEvent

    data class SelectTab(val tab: EditorTab) : GeneratedEvent

    data class Edit(val edit: StyleEdit) : GeneratedEvent

    /** The Photo Picker returned [sourceUri]; it is copied into app storage first. */
    data class LogoPicked(val sourceUri: String) : GeneratedEvent

    data object RemoveLogo : GeneratedEvent

    data object SaveToGallery : GeneratedEvent

    data class StoragePermissionResult(val granted: Boolean) : GeneratedEvent

    data object Share : GeneratedEvent

    data object Copy : GeneratedEvent
}

sealed interface GeneratedEffect {
    data class Message(val message: GeneratedMessage) : GeneratedEffect

    data class ShareFile(val uri: String, val mimeType: String) : GeneratedEffect

    data object RequestStoragePermission : GeneratedEffect

    data class CopyText(val text: String) : GeneratedEffect
}

@HiltViewModel
class GeneratedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val history: HistoryRepository,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val exporter: CodeExporter,
    private val logoStore: LogoStore,
    private val analytics: Analytics,
    @ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val recordId: Long = savedStateHandle.get<Long>(KEY_RECORD_ID) ?: 0L

    private val _state = MutableStateFlow(GeneratedUiState())
    val state: StateFlow<GeneratedUiState> = _state.asStateFlow()

    private val _effects = Channel<GeneratedEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var factory: CodeImageFactory? = null
    private var persistJob: Job? = null

    /** The newest style that is not written yet; null when the repository is up to date. */
    private var unsavedStyle: QrStyle? = null

    init {
        viewModelScope.launch { load() }
    }

    override fun onCleared() {
        flushPendingStyle()
    }

    /**
     * Writes a style change that is still waiting for its debounce. Runs on the application scope,
     * because viewModelScope is already cancelled when the screen is left.
     */
    internal fun flushPendingStyle() {
        val pending = unsavedStyle ?: return
        unsavedStyle = null
        appScope.launch { history.updateStyle(recordId, pending) }
    }

    fun onEvent(event: GeneratedEvent) {
        when (event) {
            GeneratedEvent.ToggleFavorite -> toggleFavoriteFlag()
            is GeneratedEvent.OpenEditor -> _state.update { it.copy(editing = true, tab = event.tab) }
            GeneratedEvent.CloseEditor -> closeEditor()
            is GeneratedEvent.SelectTab -> _state.update { it.copy(tab = event.tab) }
            is GeneratedEvent.Edit -> applyEdit(event.edit)
            is GeneratedEvent.LogoPicked -> importLogo(event.sourceUri)
            GeneratedEvent.RemoveLogo -> removeLogo()
            GeneratedEvent.SaveToGallery -> requestSave()
            is GeneratedEvent.StoragePermissionResult -> onPermission(event.granted)
            GeneratedEvent.Share -> share()
            GeneratedEvent.Copy -> copy()
        }
    }

    private fun toggleFavoriteFlag() {
        viewModelScope.launch {
            if (toggleFavorite(recordId) != null) analytics.log(Events.FAVORITE_TOGGLE)
        }
    }

    // ---- loading ----

    private suspend fun load() {
        val record = history.getById(recordId)
        if (record == null) {
            _state.update { it.copy(status = GeneratedStatus.NOT_FOUND) }
            return
        }
        factory = CodeImageFactory(record.format, record.rawValue)
        val style = record.style ?: QrStyle()
        _state.update { rebuilt(it.copy(status = GeneratedStatus.READY, record = record, style = style, isFavorite = record.isFavorite)) }
        history.observeById(recordId).collect { latest ->
            if (latest != null) _state.update { it.copy(isFavorite = latest.isFavorite) }
        }
    }

    /** Recomputes everything derived from the style: the picture, the scan reliability, the export size, the check digit. */
    private fun rebuilt(state: GeneratedUiState): GeneratedUiState {
        val record = state.record ?: return state
        val image = factory?.imageOrNull(state.style)
        return state.copy(
            image = image,
            reliability = ContrastChecker.scanReliability(state.style),
            pixelSize = image?.let { ExportDimensions.of(it, state.style.exportSizePx) },
            checkDigit = BarcodeInputs.checkDigitInfo(record.format, record.rawValue),
        )
    }

    // ---- editing ----

    private fun applyEdit(edit: StyleEdit) {
        var changed: QrStyle? = null
        _state.update { current ->
            if (current.record == null) return@update current
            val next = current.style.applying(edit)
            if (next == current.style) {
                current
            } else {
                changed = next
                rebuilt(current.copy(style = next))
            }
        }
        changed?.let { schedulePersist(it) }
    }

    private fun schedulePersist(style: QrStyle) {
        unsavedStyle = style
        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(PERSIST_DEBOUNCE_MS)
            persistNow()
        }
    }

    private suspend fun persistNow() {
        val pending = unsavedStyle ?: return
        history.updateStyle(recordId, pending)
        if (unsavedStyle == pending) unsavedStyle = null
    }

    private fun closeEditor() {
        _state.update { it.copy(editing = false) }
        persistJob?.cancel()
        viewModelScope.launch { persistNow() }
    }

    private fun importLogo(sourceUri: String) {
        viewModelScope.launch {
            val stored = logoStore.import(sourceUri)
            if (stored == null) {
                _effects.send(GeneratedEffect.Message(GeneratedMessage.LOGO_FAILED))
                return@launch
            }
            val previous = _state.value.style.logoUri
            applyEdit(StyleEdit.Logo(stored))
            if (previous != null) logoStore.discard(previous)
        }
    }

    private fun removeLogo() {
        val previous = _state.value.style.logoUri ?: return
        applyEdit(StyleEdit.Logo(null))
        viewModelScope.launch { logoStore.discard(previous) }
    }

    // ---- exporting ----

    private fun requestSave() {
        if (_state.value.working) return
        if (exporter.needsStoragePermission()) {
            viewModelScope.launch { _effects.send(GeneratedEffect.RequestStoragePermission) }
        } else {
            save()
        }
    }

    private fun onPermission(granted: Boolean) {
        if (granted) {
            save()
        } else {
            viewModelScope.launch { _effects.send(GeneratedEffect.Message(GeneratedMessage.PERMISSION_DENIED)) }
        }
    }

    private fun save() {
        val job = exportJob() ?: return
        val name = fileName() ?: return
        launchExport {
            when (val outcome = exporter.saveToGallery(job, name)) {
                is SaveOutcome.Saved -> {
                    analytics.log(Events.SAVE_TO_GALLERY, mapOf(Params.FORMAT to job.style.exportFormat.name))
                    val message = if (outcome.toDownloads) GeneratedMessage.SAVED_TO_DOWNLOADS else GeneratedMessage.SAVED_TO_GALLERY
                    _effects.send(GeneratedEffect.Message(message))
                }
                SaveOutcome.Failed -> _effects.send(GeneratedEffect.Message(GeneratedMessage.SAVE_FAILED))
            }
        }
    }

    private fun share() {
        if (_state.value.working) return
        val job = exportJob() ?: return
        val name = fileName() ?: return
        launchExport {
            val file = exporter.createShareFile(job, name)
            if (file == null) {
                _effects.send(GeneratedEffect.Message(GeneratedMessage.SHARE_FAILED))
            } else {
                analytics.log(Events.SHARE_TAP, mapOf(Params.FORMAT to job.style.exportFormat.name))
                _effects.send(GeneratedEffect.ShareFile(file.uri, file.mimeType))
            }
        }
    }

    private fun copy() {
        val record = _state.value.record ?: return
        analytics.log(Events.COPY_TAP, mapOf(Params.TYPE to record.type.name))
        viewModelScope.launch { _effects.send(GeneratedEffect.CopyText(record.rawValue)) }
    }

    private fun launchExport(block: suspend () -> Unit) {
        _state.update { it.copy(working = true) }
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } finally {
                _state.update { it.copy(working = false) }
            }
        }
    }

    private fun exportJob(): ExportJob? {
        val current = _state.value
        val record = current.record ?: return null
        return ExportJob(record.format, record.rawValue, current.style)
    }

    private fun fileName(): String? {
        val current = _state.value
        val record = current.record ?: return null
        val now = System.currentTimeMillis()
        val isQr = record.format.isQr
        val label = if (isQr) record.type.name else record.format.displayName
        return ExportNaming.fileName(isQr, label, now, current.style.exportFormat, TimeZone.getDefault().getOffset(now))
    }

    companion object {
        /** Name of the navigation argument of `Generated(recordId)`. */
        const val KEY_RECORD_ID = "recordId"

        /** Style changes are written this long after the last one. */
        const val PERSIST_DEBOUNCE_MS = 300L
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.BatchEntry
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DuplicateFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanBatch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.distinctCodes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State and rules of the live Scan screen: which detections count (mode, duplicates, batch), what happens after
 * one (save, then Result / quick sheet / batch list) and the camera toggles. Camera hardware is driven by the
 * composable from this state.
 */
@HiltViewModel
class ScanViewModel internal constructor(
    private val recorder: ScanRecorder,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val settings: SettingsRepository,
    private val clock: () -> Long,
    private val freezeMillis: Long,
) : ViewModel() {

    @Inject
    constructor(
        recorder: ScanRecorder,
        toggleFavorite: ToggleFavoriteUseCase,
        settings: SettingsRepository,
    ) : this(recorder, toggleFavorite, settings, System::currentTimeMillis, FREEZE_MILLIS)

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private val _effects = Channel<ScanEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val duplicates = DuplicateFilter()
    private var batch = ScanBatch.EMPTY
    private val rowsById = HashMap<Long, BatchRow>()
    private var busy = false

    init {
        observeSettings()
    }

    fun onEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.ModeSelected -> _state.update { it.copy(mode = event.mode) }
            ScanEvent.FlashToggled -> toggleFlash()
            is ScanEvent.ZoomChanged -> _state.update { it.copy(zoom = event.zoom.coerceIn(0f, 1f)) }
            ScanEvent.CameraSwitched -> _state.update { it.copy(frontCamera = !it.frontCamera, flashOn = false) }
            is ScanEvent.CodesDetected -> onCodesDetected(event.codes)
            ScanEvent.ScreenResumed -> _state.update { if (it.quickResult == null) it.copy(detected = false) else it }
            ScanEvent.BatchToggled -> toggleBatch()
            ScanEvent.BatchDoneClicked -> _state.update { it.copy(batchSheetOpen = true) }
            ScanEvent.BatchSheetDismissed -> _state.update { it.copy(batchSheetOpen = false) }
            ScanEvent.BatchFinished -> finishBatch()
            is ScanEvent.BatchRowClicked -> send(ScanEffect.OpenResult(event.recordId))
            ScanEvent.QuickDismissed -> _state.update { it.copy(quickResult = null, detected = false) }
            ScanEvent.QuickOpened -> openQuickResult()
            ScanEvent.QuickFavoriteToggled -> toggleQuickFavorite()
            ScanEvent.GalleryClicked -> send(ScanEffect.OpenGallery)
            ScanEvent.SettingsClicked -> send(ScanEffect.OpenSettings)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            var first = true
            settings.settings.collect { s ->
                val applyFlashOnStart = first
                first = false
                _state.update {
                    it.copy(
                        vibrate = s.vibrate,
                        beep = s.beep,
                        scanAreaOnly = s.scanAreaOnly,
                        continuousScan = s.continuousScan,
                        flashOn = if (applyFlashOnStart && !it.frontCamera) s.flashOnStart else it.flashOn,
                    )
                }
            }
        }
    }

    private fun toggleFlash() {
        _state.update { if (it.frontCamera) it else it.copy(flashOn = !it.flashOn) }
    }

    private fun toggleBatch() {
        if (_state.value.batchMode) finishBatch() else _state.update { it.copy(batchMode = true) }
    }

    private fun finishBatch() {
        batch = ScanBatch.EMPTY
        rowsById.clear()
        _state.update { it.copy(batchMode = false, batch = emptyList(), batchSheetOpen = false, detected = false) }
    }

    private fun openQuickResult() {
        val record = _state.value.quickResult ?: return
        _state.update { it.copy(quickResult = null) }
        send(ScanEffect.OpenResult(record.id))
    }

    private fun toggleQuickFavorite() {
        val record = _state.value.quickResult ?: return
        viewModelScope.launch {
            val favorite = toggleFavorite(record.id) ?: return@launch
            _state.update { s ->
                val current = s.quickResult
                if (current != null && current.id == record.id) s.copy(quickResult = current.copy(isFavorite = favorite)) else s
            }
        }
    }

    private fun onCodesDetected(codes: List<DetectedCode>) {
        val current = _state.value
        if (current.detected || busy) return
        val now = clock()
        val accepted = codes.distinctCodes()
            .filter { it.rawValue.isNotEmpty() && current.mode.accepts(it.format) }
            .filterNot { current.batchMode && batch.contains(it.rawValue, it.format) }
            .filter { duplicates.shouldAccept(it.rawValue, it.format, now) }
        if (accepted.isEmpty()) return
        busy = true
        viewModelScope.launch {
            try {
                handleDetection(if (current.batchMode) accepted else accepted.take(1), current)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(detected = false) }
            } finally {
                busy = false
            }
        }
    }

    private suspend fun handleDetection(codes: List<DetectedCode>, at: ScanUiState) {
        _state.update { it.copy(detected = true) }
        _effects.send(ScanEffect.Feedback(at.vibrate, at.beep))
        val records = codes.map { recorder.record(it, ScanSource.CAMERA) }
        delay(freezeMillis)
        when {
            at.batchMode -> {
                records.forEachIndexed { index, record -> collect(codes[index], record) }
                _state.update { it.copy(detected = false, batch = currentRows()) }
            }
            at.continuousScan -> _state.update { it.copy(quickResult = records.first()) }
            else -> _effects.send(ScanEffect.OpenResult(records.first().id))
        }
    }

    private fun collect(code: DetectedCode, record: ScanRecord) {
        val updated = batch.add(BatchEntry(record.id, code.rawValue, code.format))
        if (updated === batch) return
        batch = updated
        rowsById[record.id] = BatchRow(record.id, record.displayTitle.ifBlank { record.rawValue }, record.type, record.format)
    }

    private fun currentRows(): List<BatchRow> = batch.entries.mapNotNull { rowsById[it.recordId] }

    private fun send(effect: ScanEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    companion object {
        /** How long the frame stays frozen with teal brackets before the result opens (PRD 14). */
        const val FREEZE_MILLIS = 250L
    }
}

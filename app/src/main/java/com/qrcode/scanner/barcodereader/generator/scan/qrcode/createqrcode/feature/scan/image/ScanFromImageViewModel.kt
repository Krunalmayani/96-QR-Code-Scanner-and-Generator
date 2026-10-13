package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.ScanRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reads the codes of a picked image and moves to the result. One code opens its result straight away (and replaces
 * this screen); several are listed so the user picks one. Only the code the user opens is saved to history.
 */
@HiltViewModel
class ScanFromImageViewModel
    @Inject
    constructor(
        private val reader: ImageCodeReader,
        private val recorder: ScanRecorder,
        private val analytics: Analytics,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ImageScanState>(ImageScanState.Idle)
        val state: StateFlow<ImageScanState> = _state.asStateFlow()

        private val _effects = Channel<ImageScanEffect>(Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        private var readJob: Job? = null
        private var codes: List<DetectedCode> = emptyList()
        private val savedIds = HashMap<Int, Long>()
        private var saving = false

        fun onEvent(event: ImageScanEvent) {
            when (event) {
                is ImageScanEvent.ImagePicked -> read(event.imageUri)
                is ImageScanEvent.CodeChosen -> open(event.index)
                ImageScanEvent.Cancelled -> cancel()
                ImageScanEvent.ChooseAnotherClicked -> send(ImageScanEffect.LaunchPicker)
            }
        }

        private fun read(imageUri: String) {
            readJob?.cancel()
            codes = emptyList()
            savedIds.clear()
            _state.value = ImageScanState.Processing(imageUri)
            readJob =
                viewModelScope.launch {
                    try {
                        handle(imageUri, reader.read(imageUri))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        handle(imageUri, ImageReadResult.Unreadable)
                    }
                }
        }

        private fun handle(imageUri: String, result: ImageReadResult) {
            when (result) {
                is ImageReadResult.Found -> onFound(imageUri, result.codes)
                ImageReadResult.NoCode -> onNoCode(imageUri, unreadable = false)
                ImageReadResult.Unreadable -> onNoCode(imageUri, unreadable = true)
            }
        }

        private fun onFound(imageUri: String, found: List<DetectedCode>) {
            codes = found
            val label = if (found.size == 1) SINGLE else MULTIPLE
            analytics.log(Events.SCAN_FROM_IMAGE, mapOf(Params.RESULT to label))
            _state.value = ImageScanState.Found(imageUri, found.map(::label))
            if (found.size == 1) open(0)
        }

        private fun onNoCode(imageUri: String, unreadable: Boolean) {
            analytics.log(Events.SCAN_NO_CODE, mapOf(Params.SOURCE to ScanSource.IMAGE.name))
            _state.value = ImageScanState.NoCode(imageUri, unreadable)
        }

        private fun label(code: DetectedCode): FoundCode {
            val content = recorder.parse(code)
            return FoundCode(content.title.ifBlank { code.rawValue }, content.type, content.format)
        }

        private fun open(index: Int) {
            val code = codes.getOrNull(index) ?: return
            val replace = codes.size == 1
            savedIds[index]?.let {
                send(ImageScanEffect.OpenResult(it, replace))
                return
            }
            if (saving) return
            saving = true
            viewModelScope.launch {
                try {
                    val record = recorder.record(code, ScanSource.IMAGE)
                    savedIds[index] = record.id
                    _effects.send(ImageScanEffect.OpenResult(record.id, replace))
                } finally {
                    saving = false
                }
            }
        }

        private fun cancel() {
            readJob?.cancel()
            _state.value = ImageScanState.Idle
            send(ImageScanEffect.Close)
        }

        private fun send(effect: ImageScanEffect) {
            viewModelScope.launch { _effects.send(effect) }
        }

        private companion object {
            const val SINGLE = "SINGLE"
            const val MULTIPLE = "MULTIPLE"
        }
    }

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode

/** One collected code in the batch list (s10). */
data class BatchRow(
    val recordId: Long,
    val title: String,
    val type: ContentType,
    val format: CodeFormat,
)

/** Everything the live Scan screen shows. Camera hardware facts (flash available, max zoom) live in the route. */
data class ScanUiState(
    val mode: ScanMode = ScanMode.QR,
    val flashOn: Boolean = false,
    /** Linear zoom 0..1. */
    val zoom: Float = 0f,
    val frontCamera: Boolean = false,
    /** A code was just found: brackets are teal and analysis pauses until the result is dealt with. */
    val detected: Boolean = false,
    val batchMode: Boolean = false,
    /** Newest first. */
    val batch: List<BatchRow> = emptyList(),
    val batchSheetOpen: Boolean = false,
    /** The record shown in the quick result sheet (s08) when "continue scanning" is on. */
    val quickResult: ScanRecord? = null,
    val vibrate: Boolean = true,
    val beep: Boolean = false,
    val scanAreaOnly: Boolean = true,
    val continuousScan: Boolean = false,
)

sealed interface ScanEvent {
    data class ModeSelected(val mode: ScanMode) : ScanEvent

    data object FlashToggled : ScanEvent

    data class ZoomChanged(val zoom: Float) : ScanEvent

    data object CameraSwitched : ScanEvent

    data class CodesDetected(val codes: List<DetectedCode>) : ScanEvent

    /** The screen is visible again (first show, or back from Result / system settings). */
    data object ScreenResumed : ScanEvent

    data object BatchToggled : ScanEvent

    data object BatchDoneClicked : ScanEvent

    data object BatchSheetDismissed : ScanEvent

    data object BatchFinished : ScanEvent

    data class BatchRowClicked(val recordId: Long) : ScanEvent

    data object QuickDismissed : ScanEvent

    data object QuickOpened : ScanEvent

    data object QuickFavoriteToggled : ScanEvent

    data object GalleryClicked : ScanEvent

    data object SettingsClicked : ScanEvent
}

sealed interface ScanEffect {
    data class OpenResult(val recordId: Long) : ScanEffect

    data object OpenGallery : ScanEffect

    data object OpenSettings : ScanEffect

    /** Haptic and sound for a detection, already filtered by the user's settings. */
    data class Feedback(val vibrate: Boolean, val beep: Boolean) : ScanEffect
}

/** Why the camera cannot be shown instead of the live preview (s09). */
enum class CameraBlock { PermissionDenied, PermissionPermanentlyDenied, Unavailable }

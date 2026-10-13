package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType

/** One code found in the picked image, labelled for the chooser. */
data class FoundCode(
    val title: String,
    val type: ContentType,
    val format: CodeFormat,
)

/** Screen state of the scan-from-image flow. [imageUri] is the Photo Picker content Uri string. */
sealed interface ImageScanState {
    /** Nothing picked yet (or the picker was cancelled): offers "Choose image". */
    data object Idle : ImageScanState

    /** s12 */
    data class Processing(
        val imageUri: String,
    ) : ImageScanState

    /** s13: one or several codes. A single code opens its result right away; several wait for a choice. */
    data class Found(
        val imageUri: String,
        val codes: List<FoundCode>,
    ) : ImageScanState

    /** s14. [unreadable] is true when the file could not be opened at all. */
    data class NoCode(
        val imageUri: String,
        val unreadable: Boolean = false,
    ) : ImageScanState
}

sealed interface ImageScanEvent {
    data class ImagePicked(
        val imageUri: String,
    ) : ImageScanEvent

    data class CodeChosen(
        val index: Int,
    ) : ImageScanEvent

    data object Cancelled : ImageScanEvent

    data object ChooseAnotherClicked : ImageScanEvent
}

sealed interface ImageScanEffect {
    /** Ask the screen to show the Photo Picker. */
    data object LaunchPicker : ImageScanEffect

    /** Open the result; [replaceScreen] removes the image screen from the back stack (single code). */
    data class OpenResult(
        val recordId: Long,
        val replaceScreen: Boolean,
    ) : ImageScanEffect

    data object Close : ImageScanEffect
}

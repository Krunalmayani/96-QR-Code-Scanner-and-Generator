package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode

/** Outcome of reading the codes out of a picked image. */
sealed interface ImageReadResult {
    /** At least one code, unique by payload and format. */
    data class Found(val codes: List<DetectedCode>) : ImageReadResult

    /** The image was readable but holds no code. */
    data object NoCode : ImageReadResult

    /** The file could not be opened or decoded as an image. */
    data object Unreadable : ImageReadResult
}

/** Finds the QR codes and barcodes in the image behind a content Uri. Implemented with ML Kit. */
interface ImageCodeReader {
    suspend fun read(imageUri: String): ImageReadResult
}

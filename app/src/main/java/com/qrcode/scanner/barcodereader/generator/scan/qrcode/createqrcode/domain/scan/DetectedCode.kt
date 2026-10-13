package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/** Axis-aligned rectangle in pixels (no Android or Compose types so it can be unit tested). */
data class PxRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean = x >= left && x <= right && y >= top && y <= bottom
}

/**
 * One barcode found by the decoder. [bounds] is its bounding box in the upright (rotation applied) image,
 * or null when unknown (still images, tests).
 */
data class DetectedCode(
    val rawValue: String,
    val format: CodeFormat,
    val bounds: PxRect? = null,
) {
    /** Identity used to compare codes: same payload in the same symbology. */
    val key: String get() = "${format.name}|$rawValue"
}

/** Drops repeated codes (same payload and format), keeping the first occurrence and the original order. */
fun List<DetectedCode>.distinctCodes(): List<DetectedCode> = distinctBy { it.key }

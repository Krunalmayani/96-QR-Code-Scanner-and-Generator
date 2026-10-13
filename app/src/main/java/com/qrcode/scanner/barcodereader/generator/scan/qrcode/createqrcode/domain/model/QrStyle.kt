package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DotShape { SQUARE, ROUNDED, DOTS, CLASSY }

@Serializable
enum class EyeShape { SQUARE, ROUNDED, CIRCLE }

@Serializable
enum class ErrorCorrection { L, M, Q, H }

@Serializable
enum class ExportFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    SVG("svg", "image/svg+xml"),
    PDF("pdf", "application/pdf"),
}

/**
 * Visual style of a created code. Colors are ARGB longs (0xAARRGGBB) so the model stays free of Compose types.
 * Persisted as JSON in ScanEntity.styleJson. Barcodes use only the colors, [showNumber] and [barHeightDp].
 */
@Serializable
data class QrStyle(
    val foreground: Long = 0xFF13142B,
    val background: Long = 0xFFFFFFFF,
    /** When set, the modules are painted with a gradient from [foreground] to this color. */
    val gradientEnd: Long? = null,
    val dotShape: DotShape = DotShape.SQUARE,
    val eyeShape: EyeShape = EyeShape.SQUARE,
    val errorCorrection: ErrorCorrection = ErrorCorrection.M,
    /** content:// Uri string of a centre logo picked with the Photo Picker. */
    val logoUri: String? = null,
    /** Text under the code in a rounded frame, e.g. "Scan me". Null or blank means no frame. */
    val frameText: String? = null,
    val exportSizePx: Int = 1024,
    val exportFormat: ExportFormat = ExportFormat.PNG,
    /** Barcodes only: print the digits under the bars. */
    val showNumber: Boolean = true,
    /** Barcodes only: bar height as a percentage of the width (40..200). */
    val barHeightPercent: Int = 100,
)

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle

/**
 * Builds the picture of one fixed code ([format] + [payload]) for any [QrStyle]. The module matrix is encoded once per
 * error-correction level, so changing colors or shapes in the editor only re-runs the cheap vector step.
 * Not thread-safe: use from one thread.
 */
class CodeImageFactory(private val format: CodeFormat, private val payload: String) {
    private var cachedLevel: ErrorCorrection? = null
    private var cachedMatrix: ModuleMatrix? = null

    /** @throws CodeEncodeException when the payload cannot be encoded in [format]. */
    fun image(style: QrStyle): QrVectorImage {
        if (format != CodeFormat.QR_CODE) {
            val matrix = cachedMatrix ?: CodeEncoder.encodeBarcode(format, payload).also { cachedMatrix = it }
            return BarcodeVectorBuilder.build(matrix, style, payload, format)
        }
        val level = QrVectorBuilder.effectiveErrorCorrection(style)
        val cached = cachedMatrix
        val matrix = if (cached != null && cachedLevel == level) {
            cached
        } else {
            CodeEncoder.encodeQr(payload, level).also {
                cachedLevel = level
                cachedMatrix = it
            }
        }
        return QrVectorBuilder.build(matrix, style)
    }

    /** Like [image] but null instead of throwing. */
    fun imageOrNull(style: QrStyle): QrVectorImage? = try {
        image(style)
    } catch (e: CodeEncodeException) {
        null
    }
}

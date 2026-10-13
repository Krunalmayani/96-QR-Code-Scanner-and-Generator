package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.PxRect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.RawValueDecoder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode

/** ML Kit barcode format constant for [this], or null for UNKNOWN (not a format ML Kit can be asked for). */
fun CodeFormat.toMlKitFormat(): Int? =
    when (this) {
        CodeFormat.QR_CODE -> Barcode.FORMAT_QR_CODE
        CodeFormat.EAN_13 -> Barcode.FORMAT_EAN_13
        CodeFormat.EAN_8 -> Barcode.FORMAT_EAN_8
        CodeFormat.UPC_A -> Barcode.FORMAT_UPC_A
        CodeFormat.UPC_E -> Barcode.FORMAT_UPC_E
        CodeFormat.CODE_128 -> Barcode.FORMAT_CODE_128
        CodeFormat.CODE_39 -> Barcode.FORMAT_CODE_39
        CodeFormat.CODE_93 -> Barcode.FORMAT_CODE_93
        CodeFormat.ITF -> Barcode.FORMAT_ITF
        CodeFormat.CODABAR -> Barcode.FORMAT_CODABAR
        CodeFormat.PDF_417 -> Barcode.FORMAT_PDF417
        CodeFormat.DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
        CodeFormat.AZTEC -> Barcode.FORMAT_AZTEC
        CodeFormat.UNKNOWN -> null
    }

/** App format for an ML Kit `Barcode.format` value. */
fun mlKitFormatToCodeFormat(format: Int): CodeFormat =
    when (format) {
        Barcode.FORMAT_QR_CODE -> CodeFormat.QR_CODE
        Barcode.FORMAT_EAN_13 -> CodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> CodeFormat.EAN_8
        Barcode.FORMAT_UPC_A -> CodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> CodeFormat.UPC_E
        Barcode.FORMAT_CODE_128 -> CodeFormat.CODE_128
        Barcode.FORMAT_CODE_39 -> CodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> CodeFormat.CODE_93
        Barcode.FORMAT_ITF -> CodeFormat.ITF
        Barcode.FORMAT_CODABAR -> CodeFormat.CODABAR
        Barcode.FORMAT_PDF417 -> CodeFormat.PDF_417
        Barcode.FORMAT_DATA_MATRIX -> CodeFormat.DATA_MATRIX
        Barcode.FORMAT_AZTEC -> CodeFormat.AZTEC
        else -> CodeFormat.UNKNOWN
    }

/** Decoder options that look for exactly the symbologies of [mode]. */
fun scannerOptionsFor(mode: ScanMode): BarcodeScannerOptions = allFormatsOptions(mode.formats)

/** Decoder options for every supported symbology (used for picked images). */
fun scannerOptionsForAll(): BarcodeScannerOptions = allFormatsOptions(ScanMode.ALL.formats)

private fun allFormatsOptions(formats: Set<CodeFormat>): BarcodeScannerOptions {
    val codes = formats.mapNotNull { it.toMlKitFormat() }.ifEmpty { listOf(Barcode.FORMAT_QR_CODE) }
    return BarcodeScannerOptions
        .Builder()
        .setBarcodeFormats(codes.first(), *codes.drop(1).toIntArray())
        .build()
}

/** The decoded code, or null when it carries no usable text (rawValue and rawBytes both empty). */
fun Barcode.toDetectedCode(): DetectedCode? {
    val text = RawValueDecoder.resolve(rawValue, rawBytes) ?: return null
    val box = boundingBox
    val bounds = if (box == null) null else PxRect(box.left.toFloat(), box.top.toFloat(), box.right.toFloat(), box.bottom.toFloat())
    return DetectedCode(text, mlKitFormatToCodeFormat(format), bounds)
}

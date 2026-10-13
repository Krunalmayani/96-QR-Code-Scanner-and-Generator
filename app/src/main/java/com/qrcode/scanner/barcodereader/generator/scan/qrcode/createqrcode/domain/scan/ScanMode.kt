package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

private val LINEAR_FORMATS: Set<CodeFormat> =
    linkedSetOf(
        CodeFormat.EAN_13,
        CodeFormat.EAN_8,
        CodeFormat.UPC_A,
        CodeFormat.UPC_E,
        CodeFormat.CODE_128,
        CodeFormat.CODE_39,
        CodeFormat.CODE_93,
        CodeFormat.ITF,
        CodeFormat.CODABAR,
    )

private val MATRIX_FORMATS: Set<CodeFormat> =
    linkedSetOf(
        CodeFormat.QR_CODE,
        CodeFormat.PDF_417,
        CodeFormat.DATA_MATRIX,
        CodeFormat.AZTEC,
    )

private val ALL_FORMATS: Set<CodeFormat> = LINEAR_FORMATS + MATRIX_FORMATS

/**
 * What the camera looks for, selected with the mode chips on the Scan screen.
 * [wideFrame] picks the wide (barcode shaped) viewfinder instead of the square one.
 */
enum class ScanMode(
    val formats: Set<CodeFormat>,
    val wideFrame: Boolean,
) {
    QR(setOf(CodeFormat.QR_CODE), wideFrame = false),
    BARCODE(LINEAR_FORMATS, wideFrame = true),
    ALL(ALL_FORMATS, wideFrame = false),
    ;

    /** True when a detection of [format] counts in this mode. ALL accepts every symbology, even UNKNOWN. */
    fun accepts(format: CodeFormat): Boolean = this == ALL || format in formats

    companion object {
        /** Modes in the order the chips are shown. */
        val chipOrder: List<ScanMode> = listOf(QR, BARCODE, ALL)
    }
}

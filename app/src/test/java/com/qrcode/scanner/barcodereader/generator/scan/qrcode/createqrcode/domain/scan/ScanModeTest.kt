package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScanModeTest {
    @Test
    fun `QR mode accepts only QR codes`() {
        assertTrue(ScanMode.QR.accepts(CodeFormat.QR_CODE))
        CodeFormat.entries.filter { it != CodeFormat.QR_CODE }.forEach { assertFalse(ScanMode.QR.accepts(it), it.name) }
    }

    @Test
    fun `barcode mode accepts the 1D symbologies only`() {
        val linear = listOf(
            CodeFormat.EAN_13, CodeFormat.EAN_8, CodeFormat.UPC_A, CodeFormat.UPC_E, CodeFormat.CODE_128,
            CodeFormat.CODE_39, CodeFormat.CODE_93, CodeFormat.ITF, CodeFormat.CODABAR,
        )
        linear.forEach { assertTrue(ScanMode.BARCODE.accepts(it), it.name) }
        listOf(CodeFormat.QR_CODE, CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC, CodeFormat.UNKNOWN)
            .forEach { assertFalse(ScanMode.BARCODE.accepts(it), it.name) }
    }

    @Test
    fun `all mode accepts every symbology`() {
        CodeFormat.entries.forEach { assertTrue(ScanMode.ALL.accepts(it), it.name) }
    }

    @Test
    fun `all mode lists every real symbology for the decoder and not UNKNOWN`() {
        assertEquals(CodeFormat.entries.toSet() - CodeFormat.UNKNOWN, ScanMode.ALL.formats)
    }

    @Test
    fun `only barcode mode uses the wide frame`() {
        assertEquals(listOf(false, true, false), ScanMode.chipOrder.map { it.wideFrame })
    }

    @Test
    fun `chips are shown as QR, Barcode, All`() {
        assertEquals(listOf(ScanMode.QR, ScanMode.BARCODE, ScanMode.ALL), ScanMode.chipOrder)
    }
}

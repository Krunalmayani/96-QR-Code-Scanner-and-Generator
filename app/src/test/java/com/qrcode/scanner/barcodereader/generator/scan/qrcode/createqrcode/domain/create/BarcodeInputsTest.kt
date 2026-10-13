package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BarcodeInputsTest {
    @Test fun rulesPerFormat() {
        assertEquals(BarcodeInputRules(true, 13, 12), BarcodeInputs.rules(CodeFormat.EAN_13))
        assertEquals(BarcodeInputRules(true, 8, 7), BarcodeInputs.rules(CodeFormat.EAN_8))
        assertEquals(BarcodeInputRules(true, 12, 11), BarcodeInputs.rules(CodeFormat.UPC_A))
        assertTrue(BarcodeInputs.rules(CodeFormat.ITF).numericOnly)
        assertFalse(BarcodeInputs.rules(CodeFormat.CODE_39).numericOnly)
        assertFalse(BarcodeInputs.rules(CodeFormat.CODE_128).numericOnly)
        assertEquals(Limits.BARCODE_2D_MAX, BarcodeInputs.rules(CodeFormat.AZTEC).maxLength)
        assertNull(BarcodeInputs.rules(CodeFormat.QR_CODE).bodyLength)
    }

    @Test fun clampCutsToMaxLength() {
        assertEquals("5901234123457", BarcodeInputs.clamp(CodeFormat.EAN_13, "59012341234571234"))
        assertEquals("abc", BarcodeInputs.clamp(CodeFormat.CODE_128, "abc"))
    }

    @Test fun statusWhileTyping() {
        assertEquals(BarcodeStatus.Empty, BarcodeInputs.status(CodeFormat.EAN_13, "  "))
        assertEquals(BarcodeStatus.Incomplete(3, 12), BarcodeInputs.status(CodeFormat.EAN_13, "590"))
        assertEquals(BarcodeStatus.Incomplete(11, 12), BarcodeInputs.status(CodeFormat.EAN_13, "59012341234"))
    }

    @Test fun statusWithAutomaticCheckDigit() {
        assertEquals(BarcodeStatus.CheckDigitWillBeAdded(7), BarcodeInputs.status(CodeFormat.EAN_13, "590123412345"))
    }

    @Test fun statusComplete() {
        assertEquals(BarcodeStatus.Complete(13, 7), BarcodeInputs.status(CodeFormat.EAN_13, "5901234123457"))
        assertEquals(BarcodeStatus.Complete(5, null), BarcodeInputs.status(CodeFormat.CODE_128, "AB-12"))
    }

    @Test fun statusInvalid() {
        assertEquals(BarcodeStatus.Invalid(FieldError.BarcodeBadCheckDigit(7)), BarcodeInputs.status(CodeFormat.EAN_13, "5901234123450"))
        assertEquals(BarcodeStatus.Invalid(FieldError.BarcodeDigitsOnly), BarcodeInputs.status(CodeFormat.EAN_13, "59012A412345"))
        assertTrue(BarcodeInputs.status(CodeFormat.EAN_13, "59012341234578").let { it is BarcodeStatus.Invalid })
        assertEquals(BarcodeStatus.Invalid(FieldError.ItfOddLength), BarcodeInputs.status(CodeFormat.ITF, "123"))
    }

    @Test fun checkDigitInfo() {
        assertEquals(CheckDigitInfo(7, true), BarcodeInputs.checkDigitInfo(CodeFormat.EAN_13, "5901234123457"))
        assertEquals(CheckDigitInfo(0, false), BarcodeInputs.checkDigitInfo(CodeFormat.EAN_13, "5901234123450"))
        assertNull(BarcodeInputs.checkDigitInfo(CodeFormat.EAN_13, "590123412345"))
        assertNull(BarcodeInputs.checkDigitInfo(CodeFormat.CODE_128, "5901234123457"))
    }
}

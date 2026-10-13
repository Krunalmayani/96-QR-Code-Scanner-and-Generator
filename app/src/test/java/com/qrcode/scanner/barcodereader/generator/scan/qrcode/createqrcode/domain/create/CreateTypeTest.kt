package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateTypeTest {
    @Test fun fromKeyRoundTripsEveryType() {
        for (t in CreateType.entries) assertEquals(t, CreateType.fromKey(t.name))
    }

    @Test fun fromKeyUnknownAndNull() {
        assertNull(CreateType.fromKey("NOPE"))
        assertNull(CreateType.fromKey(null))
        assertNull(CreateType.fromKey("website"))
    }

    @Test fun barcodeTypesHaveBarcodeFormats() {
        val barcodes = CreateType.entries.filter { it.isBarcode }
        assertEquals(9, barcodes.size)
        assertEquals(barcodes, CreateType.barcodeTypes)
        for (t in barcodes) {
            assertEquals(CreateGroup.BARCODE, t.group)
            assertFalse(t.defaultFormat.isQr)
            assertTrue(t.defaultFormat.toZxingFormat() != null)
        }
    }

    @Test fun nonBarcodeTypesAreQr() {
        for (t in CreateType.entries.filter { !it.isBarcode }) assertEquals(CodeFormat.QR_CODE, t.defaultFormat)
    }

    @Test fun groups() {
        assertEquals(CreateGroup.SOCIAL, CreateType.SOCIAL.group)
        assertEquals(CreateGroup.SOCIAL, CreateType.APP_STORE.group)
        assertEquals(CreateGroup.CONTENT, CreateType.WIFI.group)
    }

    @Test fun linearBarcodes() {
        assertTrue(CreateType.BARCODE_EAN_13.isLinearBarcode)
        assertTrue(CreateType.BARCODE_ITF.isLinearBarcode)
        assertFalse(CreateType.BARCODE_AZTEC.isLinearBarcode)
        assertFalse(CreateType.BARCODE_PDF_417.isLinearBarcode)
        assertFalse(CreateType.WEBSITE.isLinearBarcode)
    }

    @Test fun hubOrderIsDistinctNonBarcode() {
        assertEquals(CreateType.hubOrder.size, CreateType.hubOrder.toSet().size)
        assertTrue(CreateType.hubOrder.none { it.isBarcode })
        assertEquals(CreateType.entries.filter { !it.isBarcode }.toSet(), CreateType.hubOrder.toSet())
    }

    @Test fun blankSpecMatchesTypeForEveryType() {
        for (t in CreateType.entries) assertEquals(t, CreateSpec.blank(t).type)
    }

    @Test fun blankSpecsAreInvalid() {
        for (t in CreateType.entries) assertFalse(Validators.isValid(CreateSpec.blank(t)), "blank $t")
    }

    @Test fun socialPlatformPhoneBased() {
        assertTrue(SocialPlatform.WHATSAPP.isPhoneBased)
        assertFalse(SocialPlatform.X.isPhoneBased)
    }
}

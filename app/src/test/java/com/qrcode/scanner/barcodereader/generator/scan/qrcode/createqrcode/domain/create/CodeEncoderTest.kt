package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CodeEncoderTest {
    @Test fun qrHasNoQuietZoneAndIsSquare() {
        val m = CodeEncoder.encodeQr("https://example.com", ErrorCorrection.M)
        assertEquals(m.width, m.height)
        assertEquals(25, m.width) // version 2
        // finder pattern corner and separator
        assertTrue(m[0, 0])
        assertTrue(m[0, 0] && m[6, 0] && m[0, 6] && m[6, 6])
        assertFalse(m[1, 1])
        assertTrue(m[3, 3])
    }

    @Test fun higherErrorCorrectionNeverShrinksTheCode() {
        val sizes = ErrorCorrection.entries.map { CodeEncoder.encodeQr("Some text to encode, a bit longer than tiny", it).width }
        assertEquals(sizes.sorted(), sizes)
        assertTrue(sizes.last() > sizes.first())
    }

    @Test fun differentPayloadsDiffer() {
        assertNotEquals(CodeEncoder.encodeQr("a"), CodeEncoder.encodeQr("b"))
        assertEquals(CodeEncoder.encodeQr("a"), CodeEncoder.encodeQr("a"))
        assertEquals(CodeEncoder.encodeQr("a").hashCode(), CodeEncoder.encodeQr("a").hashCode())
    }

    @Test fun emptyPayloadThrows() {
        assertThrows<CodeEncodeException> { CodeEncoder.encodeQr("") }
        assertThrows<CodeEncodeException> { CodeEncoder.encodeBarcode(CodeFormat.CODE_128, "") }
    }

    @Test fun tooLongPayloadThrowsAndCanEncodeReports() {
        val huge = "x".repeat(4000)
        assertThrows<CodeEncodeException> { CodeEncoder.encodeQr(huge, ErrorCorrection.L) }
        assertFalse(CodeEncoder.canEncodeQr(huge, ErrorCorrection.L))
        assertTrue(CodeEncoder.canEncodeQr("x".repeat(1273), ErrorCorrection.H))
        assertFalse(CodeEncoder.canEncodeQr("", ErrorCorrection.M))
    }

    @Test fun maxPayloadFitsAtHighestLevel() {
        val m = CodeEncoder.encodeQr("é".repeat(600), ErrorCorrection.H)
        assertTrue(m.width <= 177)
    }

    @Test fun unsupportedBarcodeFormatThrows() {
        assertThrows<CodeEncodeException> { CodeEncoder.encodeBarcode(CodeFormat.CODE_93, "ABC") }
        assertThrows<CodeEncodeException> { CodeEncoder.encodeBarcode(CodeFormat.UNKNOWN, "ABC") }
    }

    @Test fun badBarcodeDataThrows() {
        assertThrows<CodeEncodeException> { CodeEncoder.encodeBarcode(CodeFormat.EAN_13, "4006381333932") }
        assertThrows<CodeEncodeException> { CodeEncoder.encodeBarcode(CodeFormat.ITF, "123") }
    }

    @Test fun ean13Has95ModulesAndRequestedHeight() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.EAN_13, "4006381333931", 30)
        assertEquals(95, m.width)
        assertEquals(30, m.height)
        for (y in 1 until m.height) for (x in 0 until m.width) assertEquals(m[x, 0], m[x, y])
        assertTrue(m[0, 0] && !m[1, 0] && m[2, 0]) // start guard 101
    }

    @Test fun barcodeHeightDefaultsToOneRow() {
        assertEquals(1, CodeEncoder.encodeBarcode(CodeFormat.CODE_128, "ABC").height)
        assertEquals(1, CodeEncoder.encodeBarcode(CodeFormat.CODE_128, "ABC", 0).height)
    }

    @Test fun datamatrixHasSolidLeftAndBottomEdges() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.DATA_MATRIX, "Hello")
        for (y in 0 until m.height) assertTrue(m[0, y], "left edge at $y")
        for (x in 0 until m.width) assertTrue(m[x, m.height - 1], "bottom edge at $x")
    }

    @Test fun encodeConvenienceOverloads() {
        val viaSpec = CodeEncoder.encode(CreateSpec.Website("example.com"))
        val direct = CodeEncoder.encodeQr("https://example.com", ErrorCorrection.M)
        assertEquals(direct, viaSpec)
        assertEquals(direct, CodeEncoder.encode(CodeFormat.QR_CODE, "https://example.com"))
        assertEquals(95, CodeEncoder.encode(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "400638133393")).width)
        assertEquals(CodeEncoder.encodeBarcode(CodeFormat.CODE_39, "AB"), CodeEncoder.encode(CodeFormat.CODE_39, "AB"))
    }

    @Test fun toZxingFormatMapping() {
        assertEquals(null, CodeFormat.UPC_E.toZxingFormat())
        assertEquals(null, CodeFormat.CODABAR.toZxingFormat())
        assertTrue(CodeFormat.QR_CODE.toZxingFormat() != null)
    }

    @Test fun matrixBoundsAndCrop() {
        val m = ModuleMatrix(3, 2, booleanArrayOf(false, false, false, false, true, false))
        assertEquals(1, m.darkCount())
        assertThrows<IllegalArgumentException> { m[3, 0] }
        assertThrows<IllegalArgumentException> { ModuleMatrix(2, 2, BooleanArray(3)) }
        val c = m.cropped()
        assertEquals(1, c.width)
        assertEquals(1, c.height)
        assertTrue(c[0, 0])
        val blank = ModuleMatrix(2, 2, BooleanArray(4))
        assertEquals(blank, blank.cropped())
    }

    // ---- geometry ----
    @Test fun finderOrigins() {
        assertEquals(listOf(0 to 0, 18 to 0, 0 to 18), QrGeometry.finderOrigins(25))
    }

    @Test fun finderModules() {
        val size = 25
        var count = 0
        for (y in 0 until size) for (x in 0 until size) if (QrGeometry.isFinderModule(x, y, size)) count++
        assertEquals(3 * 49, count)
        assertTrue(QrGeometry.isFinderModule(0, 0, size))
        assertTrue(QrGeometry.isFinderModule(6, 6, size))
        assertFalse(QrGeometry.isFinderModule(7, 0, size))
        assertTrue(QrGeometry.isFinderModule(18, 0, size))
        assertTrue(QrGeometry.isFinderModule(24, 6, size))
        assertFalse(QrGeometry.isFinderModule(17, 0, size))
        assertTrue(QrGeometry.isFinderModule(0, 18, size))
        assertFalse(QrGeometry.isFinderModule(0, 17, size))
        assertFalse(QrGeometry.isFinderModule(24, 24, size))
        assertFalse(QrGeometry.isFinderModule(12, 12, size))
    }

    @Test fun finderModulesAreDarkRingInRealQr() {
        val m = CodeEncoder.encodeQr("hello")
        for ((ox, oy) in QrGeometry.finderOrigins(m.width)) {
            for (i in 0 until 7) {
                assertTrue(m[ox + i, oy] && m[ox + i, oy + 6] && m[ox, oy + i] && m[ox + 6, oy + i])
            }
            assertTrue(m[ox + 3, oy + 3])
            assertFalse(m[ox + 1, oy + 1])
        }
    }
}

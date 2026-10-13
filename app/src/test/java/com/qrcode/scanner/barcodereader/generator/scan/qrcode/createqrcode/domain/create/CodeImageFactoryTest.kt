package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CodeImageFactoryTest {
    private val url = "https://www.brewhouse-coffee.com/menu?table=12"

    @Test fun matchesCodeRendererForQrStyles() {
        val factory = CodeImageFactory(CodeFormat.QR_CODE, url)
        val styles =
            listOf(
                QrStyle(),
                QrStyle(foreground = 0xFF4F46E5, dotShape = DotShape.DOTS, eyeShape = EyeShape.LEAF),
                QrStyle(errorCorrection = ErrorCorrection.H, frameText = "Scan me"),
                QrStyle(logoUri = "file:///logo.png", errorCorrection = ErrorCorrection.L),
                QrStyle(gradientEnd = 0xFF0B7A6E),
                QrStyle(errorCorrection = ErrorCorrection.L),
            )
        for (style in styles) {
            assertEquals(CodeRenderer.render(CodeFormat.QR_CODE, url, style), factory.image(style), "$style")
        }
    }

    @Test fun matchesCodeRendererForBarcodes() {
        val factory = CodeImageFactory(CodeFormat.EAN_13, "5901234123457")
        for (style in listOf(QrStyle(), QrStyle(showNumber = false), QrStyle(barHeightPercent = 60, foreground = 0xFF0B7A6E))) {
            assertEquals(CodeRenderer.render(CodeFormat.EAN_13, "5901234123457", style), factory.image(style))
        }
    }

    @Test fun twoDimensionalBarcodes() {
        for (format in listOf(CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC)) {
            val factory = CodeImageFactory(format, "Hello 2D")
            assertEquals(CodeRenderer.render(format, "Hello 2D", QrStyle()), factory.image(QrStyle()))
        }
    }

    @Test fun logoReservesSpaceAndRaisesCorrectionLikeTheRenderer() {
        val factory = CodeImageFactory(CodeFormat.QR_CODE, url)
        val plain = factory.image(QrStyle())
        val logo = factory.image(QrStyle(logoUri = "file:///logo.png"))
        assertNull(plain.logoRect)
        assertNotNull(logo.logoRect)
        assertEquals(true, logo.width >= plain.width)
    }

    @Test fun invalidPayloadThrowsOrYieldsNull() {
        val factory = CodeImageFactory(CodeFormat.EAN_13, "abc")
        assertThrows(CodeEncodeException::class.java) { factory.image(QrStyle()) }
        assertNull(factory.imageOrNull(QrStyle()))
        assertNull(CodeImageFactory(CodeFormat.UPC_E, "01234565").imageOrNull(QrStyle()))
    }

    @Test fun repeatedCallsAreStable() {
        val factory = CodeImageFactory(CodeFormat.QR_CODE, url)
        val first = factory.image(QrStyle())
        factory.image(QrStyle(errorCorrection = ErrorCorrection.H))
        assertEquals(first, factory.image(QrStyle()))
    }
}

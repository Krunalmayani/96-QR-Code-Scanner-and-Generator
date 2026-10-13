package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContrastCheckerTest {
    private val black = 0xFF000000
    private val white = 0xFFFFFFFF

    @Test fun blackOnWhiteIs21() = assertEquals(21.0, ContrastChecker.contrastRatio(black, white), 0.001)
    @Test fun identicalColorsAre1() = assertEquals(1.0, ContrastChecker.contrastRatio(0xFF336699, 0xFF336699), 0.0001)
    @Test fun ratioIsSymmetric() =
        assertEquals(ContrastChecker.contrastRatio(0xFF123456, white), ContrastChecker.contrastRatio(white, 0xFF123456), 1e-9)

    @Test fun knownWcagValue() {
        // #777777 on white is the classic 4.48:1 example
        assertEquals(4.48, ContrastChecker.contrastRatio(0xFF777777, white), 0.01)
        // design default: dark navy on white
        assertTrue(ContrastChecker.contrastRatio(0xFF13142B, white) > 15)
    }

    @Test fun luminanceBounds() {
        assertEquals(0.0, ContrastChecker.relativeLuminance(black), 1e-9)
        assertEquals(1.0, ContrastChecker.relativeLuminance(white), 1e-6)
        assertEquals(0.2126, ContrastChecker.relativeLuminance(0xFFFF0000), 1e-4)
        assertEquals(0.7152, ContrastChecker.relativeLuminance(0xFF00FF00), 1e-4)
        assertEquals(0.0722, ContrastChecker.relativeLuminance(0xFF0000FF), 1e-4)
        assertEquals(ContrastChecker.relativeLuminance(0xFF808080), ContrastChecker.relativeLuminance(0x00808080), 1e-12)
    }

    @Test fun defaultStyleIsGood() {
        val r = ContrastChecker.scanReliability(QrStyle())
        assertTrue(r.isGood)
        assertEquals(ScanReliability.Level.GOOD, r.level)
        assertTrue(r.reasons.isEmpty())
        assertTrue(r.contrastRatio > 15)
    }

    @Test fun lowContrastIsFlagged() {
        val r = ContrastChecker.scanReliability(QrStyle(foreground = 0xFF9999AA, background = white))
        assertTrue(r.isLow)
        assertEquals(ScanReliability.Level.LOW, r.level)
        assertTrue(ReliabilityReason.LOW_CONTRAST in r.reasons)
        assertFalse(ReliabilityReason.INVERTED in r.reasons)
    }

    @Test fun exactlyThresholdPasses() {
        // #949494 on white is about 3.03:1
        val r = ContrastChecker.scanReliability(QrStyle(foreground = 0xFF949494, background = white))
        assertTrue(r.isGood, r.toString())
    }

    @Test fun invertedCodeIsFlaggedEvenWithHighContrast() {
        val r = ContrastChecker.scanReliability(QrStyle(foreground = white, background = black))
        assertEquals(listOf(ReliabilityReason.INVERTED), r.reasons)
        assertEquals(21.0, r.contrastRatio, 0.001)
    }

    @Test fun lightOnDarkLowContrastHasBothReasons() {
        val r = ContrastChecker.scanReliability(QrStyle(foreground = 0xFF444444, background = 0xFF333333))
        assertTrue(ReliabilityReason.LOW_CONTRAST in r.reasons)
        assertTrue(ReliabilityReason.INVERTED in r.reasons)
    }

    @Test fun gradientEndFailingContrast() {
        val r = ContrastChecker.scanReliability(QrStyle(foreground = black, gradientEnd = 0xFFEEEEEE, background = white))
        assertTrue(ReliabilityReason.GRADIENT_LOW_CONTRAST in r.reasons)
        assertTrue(ReliabilityReason.INVERTED in r.reasons)
        assertTrue(r.contrastRatio < 3)
    }

    @Test fun goodGradientPasses() {
        val r = ContrastChecker.scanReliability(QrStyle(foreground = 0xFF0B2A6B, gradientEnd = 0xFF6A1B9A, background = white))
        assertTrue(r.isGood, r.toString())
    }

    @Test fun dotsNeedHighErrorCorrection() {
        assertTrue(ReliabilityReason.DOTS_LOW_EC in ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.L)).reasons)
        assertTrue(ReliabilityReason.DOTS_LOW_EC in ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.M)).reasons)
        assertTrue(ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.Q)).isGood)
        assertTrue(ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.H)).isGood)
        assertTrue(ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.ROUNDED, errorCorrection = ErrorCorrection.L)).isGood)
    }

    @Test fun logoRaisesEffectiveErrorCorrectionForDots() {
        val r = ContrastChecker.scanReliability(QrStyle(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.M, logoUri = "x"))
        assertTrue(r.isGood)
    }
}

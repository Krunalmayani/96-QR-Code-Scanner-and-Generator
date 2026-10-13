package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StyleEditsTest {
    private val base = QrStyle()

    @Test fun colorsAreForcedOpaque() {
        assertEquals(0xFF112233, base.applying(StyleEdit.Foreground(0x00112233)).foreground)
        assertEquals(0xFF445566, base.applying(StyleEdit.Background(0x80445566)).background)
        assertEquals(0xFF778899, base.applying(StyleEdit.GradientEnd(0x10778899)).gradientEnd)
        assertNull(base.copy(gradientEnd = 0xFF000000).applying(StyleEdit.GradientEnd(null)).gradientEnd)
    }

    @Test fun gradientPresetSetsBothColors() {
        val s = base.applying(StyleEdit.Gradient(GradientPreset.OCEAN))
        assertEquals(GradientPreset.OCEAN.start, s.foreground)
        assertEquals(GradientPreset.OCEAN.end, s.gradientEnd)
    }

    @Test fun shapesAreSet() {
        val s = base.applying(StyleEdit.Dots(DotShape.DOTS)).applying(StyleEdit.Eyes(EyeShape.LEAF))
        assertEquals(DotShape.DOTS, s.dotShape)
        assertEquals(EyeShape.LEAF, s.eyeShape)
    }

    @Test fun logoRaisesCorrectionToQ() {
        val withLogo = base.copy(errorCorrection = ErrorCorrection.L).applying(StyleEdit.Logo("file:///logo.png"))
        assertEquals("file:///logo.png", withLogo.logoUri)
        assertEquals(ErrorCorrection.Q, withLogo.errorCorrection)
        val high = base.copy(errorCorrection = ErrorCorrection.H).applying(StyleEdit.Logo("file:///logo.png"))
        assertEquals(ErrorCorrection.H, high.errorCorrection)
    }

    @Test fun lowCorrectionIsRefusedWhileALogoIsSet() {
        val s = base.applying(StyleEdit.Logo("file:///logo.png")).applying(StyleEdit.Correction(ErrorCorrection.M))
        assertEquals(ErrorCorrection.Q, s.errorCorrection)
        assertEquals(ErrorCorrection.H, s.applying(StyleEdit.Correction(ErrorCorrection.H)).errorCorrection)
    }

    @Test fun removingTheLogoKeepsTheCorrectionAndAllowsLowerAgain() {
        val s = base.applying(StyleEdit.Logo("file:///logo.png")).applying(StyleEdit.Logo(null))
        assertNull(s.logoUri)
        assertEquals(ErrorCorrection.Q, s.errorCorrection)
        assertEquals(ErrorCorrection.L, s.applying(StyleEdit.Correction(ErrorCorrection.L)).errorCorrection)
    }

    @Test fun frameTextIsTrimmedLimitedAndBlankMeansNone() {
        assertEquals("Scan me", base.applying(StyleEdit.Frame("  Scan me ")).frameText)
        assertNull(base.applying(StyleEdit.Frame("   ")).frameText)
        assertNull(base.applying(StyleEdit.Frame(null)).frameText)
        assertEquals(StyleRules.FRAME_TEXT_MAX, base.applying(StyleEdit.Frame("x".repeat(100))).frameText?.length)
    }

    @Test fun sizeSnapsAndFormatIsSet() {
        assertEquals(1536, base.applying(StyleEdit.Size(1400)).exportSizePx)
        assertEquals(ExportFormat.PDF, base.applying(StyleEdit.Format(ExportFormat.PDF)).exportFormat)
    }

    @Test fun barcodeOptions() {
        assertFalse(base.applying(StyleEdit.ShowNumber(false)).showNumber)
        assertEquals(40, base.applying(StyleEdit.BarHeight(1)).barHeightPercent)
        assertEquals(200, base.applying(StyleEdit.BarHeight(999)).barHeightPercent)
        assertEquals(120, base.applying(StyleEdit.BarHeight(120)).barHeightPercent)
    }

    @Test fun editsDoNotTouchOtherFields() {
        val styled = base.copy(foreground = 0xFF4F46E5, dotShape = DotShape.ROUNDED, frameText = "Menu")
        val s = styled.applying(StyleEdit.Background(0xFFFFF0D1))
        assertEquals(styled.copy(background = 0xFFFFF0D1), s)
    }

    @Test fun advicePointsAtFragileCombinations() {
        assertFalse(StyleAdvice.needsStrongerCorrection(base))
        assertTrue(StyleAdvice.needsStrongerCorrection(base.copy(dotShape = DotShape.DOTS, errorCorrection = ErrorCorrection.M)))
        assertTrue(StyleAdvice.needsStrongerCorrection(base.copy(eyeShape = EyeShape.LEAF, errorCorrection = ErrorCorrection.L)))
        assertFalse(StyleAdvice.needsStrongerCorrection(base.copy(eyeShape = EyeShape.LEAF, errorCorrection = ErrorCorrection.Q)))
        assertFalse(StyleAdvice.needsStrongerCorrection(base.copy(dotShape = DotShape.DOTS, logoUri = "file:///l.png")))
        assertFalse(StyleAdvice.needsStrongerCorrection(base.copy(dotShape = DotShape.ROUNDED, eyeShape = EyeShape.ROUNDED)))
    }

    @Test fun recoveryPercentGrowsWithTheLevel() {
        val values = ErrorCorrection.entries.map { StyleAdvice.recoveryPercent(it) }
        assertEquals(values.sorted(), values)
        assertEquals(30, StyleAdvice.recoveryPercent(ErrorCorrection.H))
    }
}

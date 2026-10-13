package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StylePalettesTest {
    private fun opaque(argb: Long) = (argb shr 24) == 0xFFL

    @Test fun swatchesAreOpaqueAndUnique() {
        assertTrue(CodeSwatch.entries.all { opaque(it.argb) })
        assertTrue(BackgroundSwatch.entries.all { opaque(it.argb) })
        assertEquals(CodeSwatch.entries.size, CodeSwatch.entries.map { it.argb }.toSet().size)
        assertEquals(BackgroundSwatch.entries.size, BackgroundSwatch.entries.map { it.argb }.toSet().size)
    }

    @Test fun defaultStyleColorsAreInThePalette() {
        assertEquals(CodeSwatch.INK, CodeSwatch.fromArgb(QrStyle().foreground))
        assertEquals(BackgroundSwatch.WHITE, BackgroundSwatch.fromArgb(QrStyle().background))
        assertNull(CodeSwatch.fromArgb(0xFF123456))
    }

    @Test fun everyCodeColorIsReadableOnEveryBackground() {
        for (code in CodeSwatch.entries) for (bg in BackgroundSwatch.entries) {
            val style = QrStyle(foreground = code.argb, background = bg.argb)
            val reliability = ContrastChecker.scanReliability(style)
            assertTrue(reliability.contrastRatio >= ContrastChecker.MIN_RATIO, "$code on $bg: ${reliability.contrastRatio}")
            assertTrue(reliability.isGood, "$code on $bg: ${reliability.reasons}")
        }
    }

    @Test fun everyGradientPresetIsReadableOnWhite() {
        for (preset in GradientPreset.entries) {
            val style = QrStyle(foreground = preset.start, gradientEnd = preset.end)
            val reliability = ContrastChecker.scanReliability(style)
            assertTrue(reliability.isGood, "$preset: ${reliability.reasons} ${reliability.contrastRatio}")
            assertTrue(opaque(preset.start) && opaque(preset.end))
            assertTrue(preset.start != preset.end)
        }
    }

    @Test fun presetMatching() {
        assertEquals(GradientPreset.BERRY, GradientPreset.match(GradientPreset.BERRY.start, GradientPreset.BERRY.end))
        assertNull(GradientPreset.match(GradientPreset.BERRY.start, null))
        assertNull(GradientPreset.match(GradientPreset.BERRY.start, GradientPreset.OCEAN.end))
    }

    @Test fun hexFormat() {
        assertEquals("#4F46E5", ColorHex.format(0xFF4F46E5))
        assertEquals("#000000", ColorHex.format(0xFF000000))
        assertEquals("#00000A", ColorHex.format(0x1100000A))
    }

    @Test fun hexParse() {
        assertEquals(0xFF4F46E5, ColorHex.parse("#4F46E5"))
        assertEquals(0xFF4F46E5, ColorHex.parse(" 4f46e5 "))
        assertEquals(0xFFAABBCC, ColorHex.parse("#abc"))
        assertEquals(0xFF000000, ColorHex.parse("000"))
        assertNull(ColorHex.parse(""))
        assertNull(ColorHex.parse("#12345"))
        assertNull(ColorHex.parse("#GGGGGG"))
        assertNull(ColorHex.parse("#1234567"))
        assertNull(ColorHex.parse("#FF4F46E5"))
    }

    @Test fun hexRoundTrip() {
        for (c in CodeSwatch.entries) assertEquals(c.argb, ColorHex.parse(ColorHex.format(c.argb)))
    }
}

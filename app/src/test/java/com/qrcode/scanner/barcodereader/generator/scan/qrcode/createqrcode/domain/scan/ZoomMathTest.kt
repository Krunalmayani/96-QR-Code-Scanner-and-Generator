package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ZoomMathTest {
    private val delta = 0.0001f

    @Test
    fun `spreading fingers zooms in`() {
        assertTrue(ZoomMath.applyPinch(0.2f, 1.2f) > 0.2f)
    }

    @Test
    fun `pinching in zooms out`() {
        assertTrue(ZoomMath.applyPinch(0.5f, 0.8f) < 0.5f)
    }

    @Test
    fun `a scale of one changes nothing`() {
        assertEquals(0.4f, ZoomMath.applyPinch(0.4f, 1f), delta)
    }

    @Test
    fun `result stays between zero and one`() {
        assertEquals(1f, ZoomMath.applyPinch(0.95f, 3f), delta)
        assertEquals(0f, ZoomMath.applyPinch(0.05f, 0.1f), delta)
    }

    @Test
    fun `invalid input is ignored`() {
        assertEquals(0.3f, ZoomMath.applyPinch(0.3f, Float.NaN), delta)
        assertEquals(0.3f, ZoomMath.applyPinch(0.3f, -1f), delta)
        assertEquals(0.3f, ZoomMath.applyPinch(0.3f, Float.POSITIVE_INFINITY), delta)
        assertEquals(0f, ZoomMath.applyPinch(Float.NaN, 1f), delta)
        assertEquals(1f, ZoomMath.applyPinch(7f, 1f), delta)
    }

    @Test
    fun `max ratio label is clamped to a readable range`() {
        assertEquals(5, ZoomMath.maxRatioLabel(5.4f))
        assertEquals(2, ZoomMath.maxRatioLabel(1f))
        assertEquals(20, ZoomMath.maxRatioLabel(120f))
        assertEquals(5, ZoomMath.maxRatioLabel(Float.NaN))
    }
}

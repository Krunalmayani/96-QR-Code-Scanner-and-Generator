package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QrIllustrationTest {
    private val last = QR_ILLUSTRATION_GRID - 1

    @Test
    fun `grid is the standard version 1 size`() {
        assertEquals(21, QR_ILLUSTRATION_GRID)
    }

    @Test
    fun `three finder patterns have a dark ring, light ring and dark center`() {
        val origins = listOf(0 to 0, 0 to last - 6, last - 6 to 0)
        for ((row, col) in origins) {
            assertTrue(isIllustrationModuleDark(row, col), "outer corner at $row,$col")
            assertFalse(isIllustrationModuleDark(row + 1, col + 1), "inner ring at $row,$col")
            assertTrue(isIllustrationModuleDark(row + 3, col + 3), "center at $row,$col")
            assertTrue(isIllustrationModuleDark(row + 6, col + 6), "far corner at $row,$col")
        }
    }

    @Test
    fun `finder patterns are surrounded by a light separator`() {
        assertFalse(isIllustrationModuleDark(7, 0))
        assertFalse(isIllustrationModuleDark(0, 7))
        assertFalse(isIllustrationModuleDark(7, 7))
        assertFalse(isIllustrationModuleDark(7, last))
        assertFalse(isIllustrationModuleDark(last - 7, 0))
    }

    @Test
    fun `the data area is neither empty nor full`() {
        var dark = 0
        for (row in 8..12) for (col in 8..12) if (isIllustrationModuleDark(row, col)) dark++
        assertTrue(dark in 1..24, "dark modules in the middle: $dark")
    }

    @Test
    fun `the pattern is deterministic`() {
        for (row in 0 until QR_ILLUSTRATION_GRID) {
            for (col in 0 until QR_ILLUSTRATION_GRID) {
                assertEquals(isIllustrationModuleDark(row, col), isIllustrationModuleDark(row, col))
            }
        }
    }
}

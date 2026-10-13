package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NativeAdPlacementTest {
    private fun layout(contentCount: Int, everyN: Int): List<String> =
        List(NativeAdPlacement.rowCount(contentCount, everyN)) { row ->
            if (NativeAdPlacement.isAdRow(row, everyN)) "ad" else "c${NativeAdPlacement.contentIndex(row, everyN)}"
        }

    @Test
    fun `no ad when the list has at most N items`() {
        assertEquals(0, NativeAdPlacement.adCount(0, 8))
        assertEquals(0, NativeAdPlacement.adCount(1, 8))
        assertEquals(0, NativeAdPlacement.adCount(8, 8))
        assertEquals(8, NativeAdPlacement.rowCount(8, 8))
    }

    @Test
    fun `first ad appears after the eighth item when a ninth exists`() {
        assertEquals(1, NativeAdPlacement.adCount(9, 8))
        assertEquals(10, NativeAdPlacement.rowCount(9, 8))
        assertTrue(NativeAdPlacement.isAdRow(8, 8))
        assertFalse(NativeAdPlacement.isAdRow(7, 8))
        assertFalse(NativeAdPlacement.isAdRow(9, 8))
    }

    @Test
    fun `ads repeat every N items`() {
        assertEquals(1, NativeAdPlacement.adCount(16, 8))
        assertEquals(2, NativeAdPlacement.adCount(17, 8))
        assertTrue(NativeAdPlacement.isAdRow(17, 8))
        assertTrue(NativeAdPlacement.isAdRow(26, 8))
    }

    @Test
    fun `content index skips ad rows`() {
        assertEquals(7, NativeAdPlacement.contentIndex(7, 8))
        assertEquals(8, NativeAdPlacement.contentIndex(9, 8))
        assertEquals(15, NativeAdPlacement.contentIndex(16, 8))
        assertEquals(16, NativeAdPlacement.contentIndex(18, 8))
    }

    @Test
    fun `small interval layout is exact`() {
        assertEquals(listOf("c0", "c1", "ad", "c2", "c3", "ad", "c4"), layout(5, 2))
    }

    @Test
    fun `every content index appears exactly once in order`() {
        for (everyN in listOf(2, 4, 8, 13)) {
            for (count in 0..60) {
                val contents = layout(count, everyN).filter { it != "ad" }
                assertEquals(List(count) { "c$it" }, contents, "everyN=$everyN count=$count")
                assertEquals(NativeAdPlacement.adCount(count, everyN), layout(count, everyN).count { it == "ad" })
            }
        }
    }

    @Test
    fun `last row is never an ad`() {
        for (count in 1..60) {
            val rows = layout(count, 8)
            assertNotEqualsAd(rows.last())
        }
    }

    @Test
    fun `invalid interval disables ads`() {
        assertEquals(0, NativeAdPlacement.adCount(50, 0))
        assertEquals(0, NativeAdPlacement.adCount(50, -3))
        assertFalse(NativeAdPlacement.isAdRow(8, 0))
        assertEquals(5, NativeAdPlacement.contentIndex(5, 0))
        assertEquals(0, NativeAdPlacement.rowCount(-4, 8))
    }

    @Test
    fun `negative row is not an ad`() {
        assertFalse(NativeAdPlacement.isAdRow(-1, 8))
    }

    private fun assertNotEqualsAd(row: String) = assertFalse(row == "ad")
}

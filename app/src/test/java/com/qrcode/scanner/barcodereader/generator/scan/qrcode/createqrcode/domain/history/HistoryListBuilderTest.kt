package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryListBuilderTest {
    private fun rows(count: Int, from: Long = 1): List<ScanRecord> = (0 until count).map { Fixtures.record(id = from + it) }

    private fun kinds(items: List<HistoryListItem>): String =
        items.joinToString("") {
            when (it) {
                is HistoryListItem.Header -> "H"
                is HistoryListItem.Row -> "r"
                is HistoryListItem.Ad -> "A"
            }
        }

    @Test
    fun `empty groups give an empty list`() {
        assertTrue(HistoryListBuilder.build(emptyList()).isEmpty())
        assertTrue(HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, emptyList()))).isEmpty())
    }

    @Test
    fun `header precedes the rows of each group`() {
        val items = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(2)), DayGroup(DayLabel.Yesterday, rows(1, 10))))
        assertEquals("HrrHr", kinds(items))
    }

    @Test
    fun `an ad follows every eighth record but never the last line`() {
        val eight = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(8))))
        assertEquals("H" + "r".repeat(8), kinds(eight))
        val nine = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(9))))
        assertEquals("H" + "r".repeat(8) + "Ar", kinds(nine))
        val seventeen = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(17))))
        assertEquals("H" + "r".repeat(8) + "A" + "r".repeat(8) + "Ar", kinds(seventeen))
    }

    @Test
    fun `ads count records across groups and have distinct keys`() {
        val items =
            HistoryListBuilder.build(
                listOf(DayGroup(DayLabel.Today, rows(5)), DayGroup(DayLabel.Yesterday, rows(12, 100))),
            )
        assertEquals("H" + "r".repeat(5) + "H" + "r".repeat(3) + "A" + "r".repeat(8) + "A" + "r", kinds(items))
        val keys = items.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `ads can be disabled`() {
        val items = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(20))), adsEnabled = false)
        assertTrue(items.none { it is HistoryListItem.Ad })
    }

    @Test
    fun `an ad splits a card so rows around it are last and first`() {
        val items = HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, rows(9))))
        val rowItems = items.filterIsInstance<HistoryListItem.Row>()
        assertTrue(rowItems.first().first)
        assertTrue(rowItems[7].last)
        assertTrue(rowItems[8].first)
        assertTrue(rowItems[8].last)
        assertEquals(false, rowItems[3].first)
        assertEquals(false, rowItems[3].last)
    }
}

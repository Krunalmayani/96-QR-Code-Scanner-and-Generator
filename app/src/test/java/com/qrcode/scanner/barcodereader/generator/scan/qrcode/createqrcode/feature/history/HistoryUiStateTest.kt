package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryUiStateTest {
    private fun loaded(
        chip: HistoryChip = HistoryChip.ALL,
        searchActive: Boolean = false,
        appliedQuery: String = "",
        recordCount: Int = 0,
        emptyKind: HistoryEmptyKind = HistoryEmptyKind.SCANNED,
    ) = HistoryUiState(
        chip = chip,
        searchActive = searchActive,
        appliedQuery = appliedQuery,
        recordCount = recordCount,
        loading = false,
        emptyKind = emptyKind,
    )

    @Test
    fun `favorites filter shows one flat list`() {
        assertTrue(loaded(chip = HistoryChip.FAVORITES, recordCount = 2).flatResults)
    }

    @Test
    fun `other chips keep day groups`() {
        assertFalse(loaded(chip = HistoryChip.LINKS, recordCount = 2).flatResults)
        assertFalse(loaded(recordCount = 2).flatResults)
    }

    @Test
    fun `a typed search is flat but an empty search box still groups by day`() {
        assertTrue(loaded(searchActive = true, appliedQuery = "cafe", recordCount = 3).flatResults)
        assertFalse(loaded(searchActive = true, appliedQuery = "  ", recordCount = 3).flatResults)
    }

    @Test
    fun `the favorites chip only decides the layout outside of search`() {
        assertFalse(loaded(chip = HistoryChip.FAVORITES, searchActive = true, recordCount = 3).flatResults)
    }

    @Test
    fun `an empty tab hides search and chips but an empty filter does not`() {
        assertTrue(loaded(emptyKind = HistoryEmptyKind.SCANNED).tabIsEmpty)
        assertTrue(loaded(emptyKind = HistoryEmptyKind.CREATED).tabIsEmpty)
        assertFalse(loaded(chip = HistoryChip.FAVORITES, emptyKind = HistoryEmptyKind.FAVORITES).tabIsEmpty)
        assertFalse(loaded(chip = HistoryChip.LINKS, emptyKind = HistoryEmptyKind.FILTER).tabIsEmpty)
    }

    @Test
    fun `an open search box keeps its field even when nothing is stored`() {
        assertFalse(loaded(searchActive = true, emptyKind = HistoryEmptyKind.SCANNED).tabIsEmpty)
    }

    @Test
    fun `rows present means the tab is not empty`() {
        assertFalse(loaded(recordCount = 1).tabIsEmpty)
    }
}

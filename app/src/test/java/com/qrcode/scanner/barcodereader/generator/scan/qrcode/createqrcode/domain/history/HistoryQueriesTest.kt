package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryQueriesTest {
    @Test
    fun `chips map to the expected content types`() {
        assertEquals(emptySet<ContentType>(), HistoryChipMapper.contentTypes(HistoryChip.ALL))
        assertEquals(emptySet<ContentType>(), HistoryChipMapper.contentTypes(HistoryChip.FAVORITES))
        assertEquals(setOf(ContentType.WEBSITE), HistoryChipMapper.contentTypes(HistoryChip.LINKS))
        assertEquals(setOf(ContentType.WIFI), HistoryChipMapper.contentTypes(HistoryChip.WIFI))
        assertEquals(setOf(ContentType.CONTACT), HistoryChipMapper.contentTypes(HistoryChip.CONTACTS))
        assertEquals(setOf(ContentType.TEXT), HistoryChipMapper.contentTypes(HistoryChip.TEXT))
        assertEquals(setOf(ContentType.PHONE, ContentType.SMS), HistoryChipMapper.contentTypes(HistoryChip.PHONE))
        assertEquals(setOf(ContentType.EMAIL), HistoryChipMapper.contentTypes(HistoryChip.EMAIL))
        assertEquals(setOf(ContentType.LOCATION), HistoryChipMapper.contentTypes(HistoryChip.LOCATION))
        assertEquals(setOf(ContentType.EVENT), HistoryChipMapper.contentTypes(HistoryChip.EVENTS))
        assertEquals(setOf(ContentType.PRODUCT), HistoryChipMapper.contentTypes(HistoryChip.PRODUCTS))
    }

    @Test
    fun `every type chip selects at least one type and only favorites sets the favorites flag`() {
        for (chip in HistoryChip.entries) {
            val narrowsByType = chip != HistoryChip.ALL && chip != HistoryChip.FAVORITES
            assertEquals(narrowsByType, HistoryChipMapper.contentTypes(chip).isNotEmpty(), chip.name)
            assertEquals(chip == HistoryChip.FAVORITES, HistoryChipMapper.favoritesOnly(chip), chip.name)
        }
    }

    @Test
    fun `scanned tab observes every source and drops created rows`() {
        val spec = HistoryQueries.browse(HistoryTab.SCANNED, HistoryChip.ALL)
        assertEquals(HistoryFilter(), spec.filter)
        assertTrue(spec.excludeCreated)
        val rows = listOf(
            Fixtures.record(source = ScanSource.CAMERA, id = 1),
            Fixtures.record(source = ScanSource.IMAGE, id = 2),
            Fixtures.record(source = ScanSource.CREATED, id = 3),
        )
        assertEquals(listOf(1L, 2L), spec.select(rows).map { it.id })
    }

    @Test
    fun `created tab asks the repository for the created source only`() {
        val spec = HistoryQueries.browse(HistoryTab.CREATED, HistoryChip.LINKS)
        assertEquals(HistoryFilter(source = ScanSource.CREATED, types = setOf(ContentType.WEBSITE)), spec.filter)
        assertFalse(spec.excludeCreated)
    }

    @Test
    fun `favorites chip sets favoritesOnly and the query is passed through`() {
        val spec = HistoryQueries.browse(HistoryTab.SCANNED, HistoryChip.FAVORITES, "cafe")
        assertEquals(HistoryFilter(favoritesOnly = true, query = "cafe"), spec.filter)
    }

    @Test
    fun `search scopes map to repository filters`() {
        assertEquals(HistoryQuerySpec(HistoryFilter(query = "q"), false), HistoryQueries.search(SearchScope.ALL, "q"))
        assertEquals(HistoryQuerySpec(HistoryFilter(query = "q"), true), HistoryQueries.search(SearchScope.SCANNED, "q"))
        assertEquals(
            HistoryQuerySpec(HistoryFilter(source = ScanSource.CREATED, query = "q"), false),
            HistoryQueries.search(SearchScope.CREATED, "q"),
        )
        assertEquals(
            HistoryQuerySpec(HistoryFilter(favoritesOnly = true, query = "q"), false),
            HistoryQueries.search(SearchScope.FAVORITES, "q"),
        )
    }

    @Test
    fun `empty kind follows tab chip and search state`() {
        fun kind(
            tab: HistoryTab = HistoryTab.SCANNED,
            chip: HistoryChip = HistoryChip.ALL,
            searching: Boolean = false,
            query: String = "",
            scope: SearchScope = SearchScope.ALL,
        ) = HistoryEmpty.resolve(tab, chip, searching, query, scope)

        assertEquals(HistoryEmptyKind.SCANNED, kind())
        assertEquals(HistoryEmptyKind.CREATED, kind(tab = HistoryTab.CREATED))
        assertEquals(HistoryEmptyKind.FAVORITES, kind(chip = HistoryChip.FAVORITES))
        assertEquals(HistoryEmptyKind.FAVORITES, kind(tab = HistoryTab.CREATED, chip = HistoryChip.FAVORITES))
        assertEquals(HistoryEmptyKind.FILTER, kind(chip = HistoryChip.WIFI))
        assertEquals(HistoryEmptyKind.SEARCH, kind(searching = true, query = "zzz"))
        assertEquals(HistoryEmptyKind.SEARCH, kind(searching = true, query = "zzz", scope = SearchScope.FAVORITES))
        assertEquals(HistoryEmptyKind.SCANNED, kind(searching = true, query = "  "))
        assertEquals(HistoryEmptyKind.FAVORITES, kind(searching = true, scope = SearchScope.FAVORITES))
        assertEquals(HistoryEmptyKind.CREATED, kind(searching = true, scope = SearchScope.CREATED))
        // Browse chips are ignored while searching.
        assertEquals(HistoryEmptyKind.SEARCH, kind(chip = HistoryChip.WIFI, searching = true, query = "x"))
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FakeHistoryRepositoryTest {
    private val repo = FakeHistoryRepository()

    @Test
    fun `persisted ids are positive and increasing`() = runTest {
        val a = repo.save(Fixtures.record())
        val b = repo.save(Fixtures.record())
        assertEquals(1L, a.id)
        assertEquals(2L, b.id)
    }

    @Test
    fun `transient ids are negative and decreasing and stay out of observe and counts`() = runTest {
        val a = repo.save(Fixtures.record(), persist = false)
        val b = repo.save(Fixtures.record(), persist = false)
        assertEquals(-1L, a.id)
        assertEquals(-2L, b.id)
        assertTrue(repo.observe().first().isEmpty())
        assertTrue(repo.all().isEmpty())
        assertEquals(HistoryCounts(0, 0, 0), repo.counts())
        assertEquals(a, repo.getById(-1))
    }

    @Test
    fun `id based methods work on transient records`() = runTest {
        val saved = repo.save(Fixtures.record(), persist = false)
        repo.setFavorite(setOf(saved.id), true)
        repo.updateNote(saved.id, "memo")
        repo.updateStyle(saved.id, QrStyle(foreground = 0xFF112233))
        val got = repo.getById(saved.id)
        assertNotNull(got)
        assertTrue(got!!.isFavorite)
        assertEquals("memo", got.note)
        assertEquals(0xFF112233, got.style?.foreground)
        repo.delete(setOf(saved.id))
        assertNull(repo.getById(saved.id))
    }

    @Test
    fun `observeById on a transient record emits updates`() = runTest {
        val saved = repo.save(Fixtures.record(), persist = false)
        repo.observeById(saved.id).test {
            assertEquals("", awaitItem()?.note)
            repo.updateNote(saved.id, "x")
            assertEquals("x", awaitItem()?.note)
            repo.delete(setOf(saved.id))
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeById for an unknown or zero id emits null`() = runTest {
        assertNull(repo.observeById(0).first())
        assertNull(repo.observeById(99).first())
        assertNull(repo.observeById(-99).first())
    }

    @Test
    fun `restore keeps original ids for stored and transient records`() = runTest {
        val stored = repo.save(Fixtures.record(createdAt = 5))
        val temp = repo.save(Fixtures.record(createdAt = 6), persist = false)
        repo.delete(setOf(stored.id, temp.id))
        assertNull(repo.getById(stored.id))
        repo.restore(listOf(stored, temp))
        assertEquals(stored, repo.getById(stored.id))
        assertEquals(temp, repo.getById(temp.id))
        val next = repo.save(Fixtures.record())
        assertEquals(stored.id + 1, next.id)
    }

    @Test
    fun `ids are never reused after a delete`() = runTest {
        val a = repo.save(Fixtures.record())
        repo.delete(setOf(a.id))
        assertEquals(a.id + 1, repo.save(Fixtures.record()).id)
    }

    @Test
    fun `observe is newest first and filters by source type favorites and query`() = runTest {
        repo.save(Fixtures.record(Fixtures.text("alpha"), ScanSource.CAMERA, createdAt = 1))
        repo.save(Fixtures.record(Fixtures.website("https://beta.com"), ScanSource.IMAGE, createdAt = 3, isFavorite = true))
        repo.save(Fixtures.record(Fixtures.text("gamma"), ScanSource.CREATED, createdAt = 2, note = "Shopping List"))

        assertEquals(listOf(3L, 2L, 1L), repo.observe().first().map { it.createdAt })
        assertEquals(listOf("https://beta.com"), repo.observe(HistoryFilter(source = ScanSource.IMAGE)).first().map { it.rawValue })
        assertEquals(1, repo.observe(HistoryFilter(types = setOf(ContentType.WEBSITE))).first().size)
        assertEquals(1, repo.observe(HistoryFilter(favoritesOnly = true)).first().size)
        assertEquals(listOf("gamma"), repo.observe(HistoryFilter(query = "shopping")).first().map { it.rawValue })
        assertTrue(repo.observe(HistoryFilter(query = "zzz")).first().isEmpty())
    }

    @Test
    fun `clear respects keepFavorites and source and returns the count`() = runTest {
        repo.save(Fixtures.record(source = ScanSource.CAMERA, isFavorite = true))
        repo.save(Fixtures.record(source = ScanSource.CAMERA))
        repo.save(Fixtures.record(source = ScanSource.CREATED))
        assertEquals(1, repo.clear(keepFavorites = true, source = ScanSource.CAMERA))
        assertEquals(2, repo.all().size)
        assertEquals(2, repo.clear(keepFavorites = false))
        assertTrue(repo.all().isEmpty())
    }

    @Test
    fun `counts split scanned created and favorites`() = runTest {
        repo.save(Fixtures.record(source = ScanSource.CAMERA, isFavorite = true))
        repo.save(Fixtures.record(source = ScanSource.IMAGE))
        repo.save(Fixtures.record(source = ScanSource.CREATED))
        assertEquals(HistoryCounts(scanned = 2, created = 1, favorites = 1), repo.counts())
    }

    @Test
    fun `saving a transient record with persist true promotes it`() = runTest {
        val temp = repo.save(Fixtures.record(), persist = false)
        val stored = repo.save(temp, persist = true)
        assertTrue(stored.id > 0)
        assertNull(repo.getById(temp.id))
        assertEquals(1, repo.all().size)
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.repository

import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryRepositoryImplTest {
    private val dao = FakeHistoryDao()
    private val repo = HistoryRepositoryImpl(dao, Dispatchers.Unconfined)

    @Test
    fun `persisted save returns the row id and stores the entity`() =
        runTest {
            val saved = repo.save(Fixtures.record(Fixtures.text("abc")))
            assertEquals(1L, saved.id)
            assertEquals(1, dao.snapshot.size)
            assertEquals(saved, repo.getById(saved.id))
        }

    @Test
    fun `transient save never touches the dao and ids count down from minus one`() =
        runTest {
            val a = repo.save(Fixtures.record(), persist = false)
            val b = repo.save(Fixtures.record(), persist = false)
            assertEquals(-1L, a.id)
            assertEquals(-2L, b.id)
            assertTrue(dao.snapshot.isEmpty())
            assertTrue(repo.observe().first().isEmpty())
            assertEquals(HistoryCounts(0, 0, 0), repo.counts())
            assertEquals(b, repo.getById(-2))
        }

    @Test
    fun `observeById on a transient record emits every update`() =
        runTest {
            val saved = repo.save(Fixtures.record(), persist = false)
            repo.observeById(saved.id).test {
                assertEquals("", awaitItem()?.note)
                repo.updateNote(saved.id, "n")
                assertEquals("n", awaitItem()?.note)
                repo.setFavorite(setOf(saved.id), true)
                assertEquals(true, awaitItem()?.isFavorite)
                repo.updateStyle(saved.id, QrStyle(foreground = 1))
                assertEquals(1L, awaitItem()?.style?.foreground)
                repo.delete(setOf(saved.id))
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `mixed id sets are routed to the right store`() =
        runTest {
            val stored = repo.save(Fixtures.record())
            val temp = repo.save(Fixtures.record(), persist = false)
            repo.setFavorite(setOf(stored.id, temp.id, 0L), true)
            assertEquals(true, repo.getById(stored.id)?.isFavorite)
            assertEquals(true, repo.getById(temp.id)?.isFavorite)
            repo.delete(setOf(stored.id, temp.id))
            assertNull(repo.getById(stored.id))
            assertNull(repo.getById(temp.id))
        }

    @Test
    fun `large id sets are chunked`() =
        runTest {
            val ids = (1..1200).map { repo.save(Fixtures.record()).id }.toSet()
            repo.setFavorite(ids, true)
            assertEquals(1200, repo.counts().favorites)
            repo.delete(ids)
            assertTrue(dao.snapshot.isEmpty())
        }

    @Test
    fun `style is persisted as json and restored`() =
        runTest {
            val saved = repo.save(Fixtures.record())
            val style = QrStyle(foreground = 0xFF010203, frameText = "Hi")
            repo.updateStyle(saved.id, style)
            assertEquals(style, repo.getById(saved.id)?.style)
            assertEquals(style, repo.observeById(saved.id).first()?.style)
        }

    @Test
    fun `restore puts rows back with their ids and timestamps`() =
        runTest {
            val saved = repo.save(Fixtures.record(createdAt = 77))
            val temp = repo.save(Fixtures.record(createdAt = 88), persist = false)
            repo.delete(setOf(saved.id, temp.id))
            repo.restore(listOf(saved, temp))
            assertEquals(saved, repo.getById(saved.id))
            assertEquals(temp, repo.getById(temp.id))
            assertEquals(listOf(saved), repo.all())
        }

    @Test
    fun `promoting a transient record stores a new row and drops the memory copy`() =
        runTest {
            val temp = repo.save(Fixtures.record(), persist = false)
            val stored = repo.save(temp, persist = true)
            assertTrue(stored.id > 0)
            assertNull(repo.getById(temp.id))
        }

    @Test
    fun `observe applies filter fields`() =
        runTest {
            repo.save(Fixtures.record(Fixtures.text("a_b"), ScanSource.CAMERA, createdAt = 1))
            repo.save(Fixtures.record(Fixtures.website("https://x.com"), ScanSource.CREATED, createdAt = 2, isFavorite = true))
            assertEquals(2, repo.observe().first().size)
            assertEquals(listOf("https://x.com"), repo.observe(HistoryFilter(source = ScanSource.CREATED)).first().map { it.rawValue })
            assertEquals(1, repo.observe(HistoryFilter(types = setOf(ContentType.WEBSITE))).first().size)
            assertEquals(1, repo.observe(HistoryFilter(favoritesOnly = true)).first().size)
            assertEquals(1, repo.observe(HistoryFilter(query = " A_B ")).first().size)
            assertEquals(2, repo.observe(HistoryFilter(query = "   ")).first().size)
        }

    @Test
    fun `clear and counts delegate to the dao`() =
        runTest {
            repo.save(Fixtures.record(source = ScanSource.IMAGE, isFavorite = true))
            repo.save(Fixtures.record(source = ScanSource.CAMERA))
            repo.save(Fixtures.record(source = ScanSource.CREATED))
            assertEquals(HistoryCounts(scanned = 2, created = 1, favorites = 1), repo.counts())
            assertEquals(2, repo.clear(keepFavorites = true))
            assertEquals(1, repo.all().size)
        }
}

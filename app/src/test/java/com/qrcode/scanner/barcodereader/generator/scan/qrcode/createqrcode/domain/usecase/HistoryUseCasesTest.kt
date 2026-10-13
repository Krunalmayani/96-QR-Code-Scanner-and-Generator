package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryUseCasesTest {
    private val history = FakeHistoryRepository()

    @Test
    fun `toggle flips the flag and returns the new state`() =
        runTest {
            val saved = history.save(Fixtures.record())
            val toggle = ToggleFavoriteUseCase(history)
            assertEquals(true, toggle(saved.id))
            assertTrue(history.getById(saved.id)!!.isFavorite)
            assertEquals(false, toggle(saved.id))
            assertFalse(history.getById(saved.id)!!.isFavorite)
        }

    @Test
    fun `toggle works on transient records and returns null for unknown ids`() =
        runTest {
            val temp = history.save(Fixtures.record(), persist = false)
            val toggle = ToggleFavoriteUseCase(history)
            assertEquals(true, toggle(temp.id))
            assertNull(toggle(12345))
        }

    @Test
    fun `set favorite applies to every id`() =
        runTest {
            val a = history.save(Fixtures.record())
            val b = history.save(Fixtures.record())
            ToggleFavoriteUseCase(history).set(setOf(a.id, b.id), true)
            assertEquals(2, history.counts().favorites)
        }

    @Test
    fun `delete returns the removed records and undo restores them`() =
        runTest {
            val a = history.save(Fixtures.record(createdAt = 10))
            val b = history.save(Fixtures.record(createdAt = 20), persist = false)
            val delete = DeleteRecordsUseCase(history)
            val removed = delete(setOf(a.id, b.id, 999))
            assertEquals(setOf(a, b), removed.toSet())
            assertNull(history.getById(a.id))
            assertNull(history.getById(b.id))
            delete.undo(removed)
            assertEquals(a, history.getById(a.id))
            assertEquals(b, history.getById(b.id))
        }

    @Test
    fun `delete of nothing is a no-op`() =
        runTest {
            assertTrue(DeleteRecordsUseCase(history)(emptySet()).isEmpty())
        }

    @Test
    fun `clear returns the count and honours keepFavorites and source`() =
        runTest {
            history.save(Fixtures.record(source = ScanSource.CAMERA, isFavorite = true))
            history.save(Fixtures.record(source = ScanSource.CAMERA))
            history.save(Fixtures.record(source = ScanSource.CREATED))
            val clear = ClearHistoryUseCase(history)
            assertEquals(1, clear(keepFavorites = true, source = ScanSource.CAMERA))
            assertEquals(2, clear(keepFavorites = false))
            assertEquals(0, history.all().size)
        }

    @Test
    fun `first launch is stamped once`() =
        runTest {
            val settings = FakeSettingsRepository()
            var now = 100L
            val ensure = EnsureFirstLaunchUseCase(settings) { now }
            ensure()
            now = 200L
            ensure()
            assertEquals(100L, settings.current().firstLaunchAt)
        }
}

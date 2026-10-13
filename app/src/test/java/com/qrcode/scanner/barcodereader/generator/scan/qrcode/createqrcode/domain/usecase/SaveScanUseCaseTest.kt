package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SaveScanUseCaseTest {
    private val history = FakeHistoryRepository()
    private val settings = FakeSettingsRepository()
    private val useCase = SaveScanUseCase(history, settings) { 42L }

    @Test
    fun `persists the record when save history is on`() = runTest {
        val saved = useCase(Fixtures.text("abc"), ScanSource.CAMERA)
        assertTrue(saved.id > 0)
        assertEquals(42L, saved.createdAt)
        assertEquals(ScanSource.CAMERA, saved.source)
        assertEquals(listOf(saved), history.all())
    }

    @Test
    fun `keeps the record transient when save history is off`() = runTest {
        settings.value = AppSettings(saveHistory = false)
        val saved = useCase(Fixtures.text("abc"), ScanSource.IMAGE)
        assertTrue(saved.id < 0)
        assertTrue(history.all().isEmpty())
        assertEquals(saved, history.getById(saved.id))
    }

    @Test
    fun `camera and image scans increment successfulScans only`() = runTest {
        useCase(Fixtures.text(), ScanSource.CAMERA)
        useCase(Fixtures.text(), ScanSource.IMAGE)
        assertEquals(2, settings.current().successfulScans)
        assertEquals(0, settings.current().completedActions)
    }

    @Test
    fun `created codes increment completedActions only and keep the style`() = runTest {
        val style = QrStyle(foreground = 0xFF000000)
        val saved = useCase(Fixtures.text(), ScanSource.CREATED, style)
        assertEquals(style, saved.style)
        assertEquals(1, settings.current().completedActions)
        assertEquals(0, settings.current().successfulScans)
    }

    @Test
    fun `counters also move when history is off`() = runTest {
        settings.value = AppSettings(saveHistory = false)
        useCase(Fixtures.text(), ScanSource.CAMERA)
        assertEquals(1, settings.current().successfulScans)
    }
}

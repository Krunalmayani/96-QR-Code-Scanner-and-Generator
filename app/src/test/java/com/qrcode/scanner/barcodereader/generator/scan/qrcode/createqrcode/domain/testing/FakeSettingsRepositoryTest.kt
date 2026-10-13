package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FakeSettingsRepositoryTest {
    @Test
    fun `starts with defaults and applies updates`() = runTest {
        val repo = FakeSettingsRepository()
        assertEquals(AppSettings(), repo.current())
        repo.update { it.copy(theme = ThemeMode.DARK, beep = true) }
        assertEquals(ThemeMode.DARK, repo.settings.first().theme)
        assertEquals(true, repo.current().beep)
    }

    @Test
    fun `value can be replaced directly`() = runTest {
        val repo = FakeSettingsRepository(AppSettings(saveHistory = false))
        assertEquals(false, repo.current().saveHistory)
        repo.value = AppSettings(saveHistory = true)
        assertEquals(true, repo.current().saveHistory)
    }
}

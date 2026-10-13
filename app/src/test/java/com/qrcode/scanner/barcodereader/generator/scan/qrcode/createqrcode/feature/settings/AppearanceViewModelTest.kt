package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)

    private fun viewModel() = AppearanceViewModel(settings, analytics)

    @Test
    fun `state mirrors the stored theme and language`() {
        settings.value = settings.value.copy(theme = ThemeMode.LIGHT, languageTag = "fr")
        val vm = viewModel()
        assertEquals(AppearanceUiState(ThemeMode.LIGHT, "fr"), vm.state.value)
    }

    @Test
    fun `picking a theme saves it at once and logs the change`() {
        val vm = viewModel()
        vm.onEvent(AppearanceEvent.SelectTheme(ThemeMode.DARK))
        assertEquals(ThemeMode.DARK, settings.value.theme)
        assertEquals(ThemeMode.DARK, vm.state.value.theme)
        verify { analytics.log(Events.THEME_CHANGED, mapOf(PARAM_CHOICE to "DARK")) }
    }

    @Test
    fun `picking the current theme again changes and logs nothing`() {
        val vm = viewModel()
        vm.onEvent(AppearanceEvent.SelectTheme(ThemeMode.SYSTEM))
        verify(exactly = 0) { analytics.log(Events.THEME_CHANGED, any()) }
    }

    @Test
    fun `picking a language stores the tag, logs it and asks the screen to apply it`() =
        runTest {
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(AppearanceEvent.SelectLanguage("hi"))
                assertEquals(AppearanceEffect.ApplyLanguage("hi"), awaitItem())
            }
            assertEquals("hi", settings.value.languageTag)
            verify { analytics.log(Events.LANGUAGE_CHANGED, mapOf(PARAM_CHOICE to "hi")) }
        }

    @Test
    fun `system default stores an empty tag`() =
        runTest {
            settings.value = settings.value.copy(languageTag = "de")
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(AppearanceEvent.SelectLanguage(""))
                assertEquals(AppearanceEffect.ApplyLanguage(""), awaitItem())
            }
            assertEquals("", settings.value.languageTag)
            verify { analytics.log(Events.LANGUAGE_CHANGED, mapOf(PARAM_CHOICE to "system")) }
        }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ClearHistoryUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val settings = FakeSettingsRepository()
    private val history = FakeHistoryRepository()
    private val analytics = mockk<Analytics>(relaxed = true)

    private fun viewModel() = SettingsViewModel(settings, history, ClearHistoryUseCase(history), analytics)

    @Test
    fun `defaults match the app settings defaults`() {
        val vm = viewModel()
        assertEquals(settings.value, vm.state.value.settings)
        assertFalse(vm.state.value.settings.autoOpenLinks)
        assertNull(vm.state.value.clearDialog)
    }

    @Test
    fun `scanning toggles are written to the repository`() {
        val vm = viewModel()
        vm.onEvent(SettingsEvent.SetVibrate(false))
        vm.onEvent(SettingsEvent.SetBeep(true))
        vm.onEvent(SettingsEvent.SetFlashOnStart(true))
        vm.onEvent(SettingsEvent.SetAutoOpenLinks(true))
        vm.onEvent(SettingsEvent.SetContinuousScan(true))
        vm.onEvent(SettingsEvent.SetScanAreaOnly(false))
        vm.onEvent(SettingsEvent.SetSaveHistory(false))
        val s = settings.value
        assertFalse(s.vibrate)
        assertTrue(s.beep)
        assertTrue(s.flashOnStart)
        assertTrue(s.autoOpenLinks)
        assertTrue(s.continuousScan)
        assertFalse(s.scanAreaOnly)
        assertFalse(s.saveHistory)
        assertEquals(s, vm.state.value.settings)
    }

    @Test
    fun `analytics toggle is stored (the app class applies it to Analytics)`() {
        val vm = viewModel()
        vm.onEvent(SettingsEvent.SetAnalytics(false))
        assertFalse(settings.value.analyticsEnabled)
        vm.onEvent(SettingsEvent.SetAnalytics(true))
        assertTrue(settings.value.analyticsEnabled)
    }

    @Test
    fun `product sites move up and down and stop at the ends`() {
        val vm = viewModel()
        vm.onEvent(SettingsEvent.MoveSite(ProductSite.GOOGLE, up = false))
        assertEquals(
            listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE, ProductSite.AMAZON),
            settings.value.productSiteOrder,
        )
        vm.onEvent(SettingsEvent.MoveSite(ProductSite.AMAZON, up = true))
        assertEquals(
            listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.AMAZON, ProductSite.GOOGLE),
            settings.value.productSiteOrder,
        )
        vm.onEvent(SettingsEvent.MoveSite(ProductSite.OPEN_FOOD_FACTS, up = true))
        assertEquals(ProductSite.OPEN_FOOD_FACTS, settings.value.productSiteOrder.first())
    }

    @Test
    fun `clear dialog shows the counts and keeps favorites by default`() = runTest {
        history.seed(Fixtures.record(source = ScanSource.CAMERA))
        history.seed(Fixtures.record(source = ScanSource.IMAGE, isFavorite = true))
        history.seed(Fixtures.record(source = ScanSource.CREATED))
        val vm = viewModel()
        vm.onEvent(SettingsEvent.ClearHistoryClicked)
        assertEquals(ClearDialogState(scanned = 2, created = 1, keepFavorites = true), vm.state.value.clearDialog)
    }

    @Test
    fun `dismissing the dialog removes it without clearing anything`() = runTest {
        history.seed(Fixtures.record())
        val vm = viewModel()
        vm.onEvent(SettingsEvent.ClearHistoryClicked)
        vm.onEvent(SettingsEvent.ClearDismissed)
        assertNull(vm.state.value.clearDialog)
        assertEquals(1, history.persistedRecords.size)
    }

    @Test
    fun `confirming with keep favorites removes only the other rows and reports the count`() = runTest {
        history.seed(Fixtures.record(source = ScanSource.CAMERA))
        history.seed(Fixtures.record(source = ScanSource.CREATED))
        val favorite = history.seed(Fixtures.record(isFavorite = true))
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(SettingsEvent.ClearHistoryClicked)
            vm.onEvent(SettingsEvent.ClearConfirmed)
            assertEquals(SettingsEffect.HistoryCleared(2), awaitItem())
        }
        assertEquals(listOf(favorite.id), history.persistedRecords.map { it.id })
        assertNull(vm.state.value.clearDialog)
        verify { analytics.log(Events.HISTORY_CLEAR) }
    }

    @Test
    fun `unchecking keep favorites clears favorites too`() = runTest {
        history.seed(Fixtures.record(isFavorite = true))
        history.seed(Fixtures.record())
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(SettingsEvent.ClearHistoryClicked)
            vm.onEvent(SettingsEvent.ClearKeepFavoritesChanged(false))
            assertFalse(vm.state.value.clearDialog!!.keepFavorites)
            vm.onEvent(SettingsEvent.ClearConfirmed)
            assertEquals(SettingsEffect.HistoryCleared(2), awaitItem())
        }
        assertTrue(history.persistedRecords.isEmpty())
    }

    @Test
    fun `clearing an empty history only reports that it is empty`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(SettingsEvent.ClearHistoryClicked)
            assertEquals(SettingsEffect.HistoryAlreadyEmpty, awaitItem())
        }
        assertNull(vm.state.value.clearDialog)
    }

    @Test
    fun `confirm without an open dialog does nothing`() = runTest {
        history.seed(Fixtures.record())
        val vm = viewModel()
        vm.onEvent(SettingsEvent.ClearConfirmed)
        assertEquals(1, history.persistedRecords.size)
    }
}

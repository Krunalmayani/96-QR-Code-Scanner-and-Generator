package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ExportHistoryViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val history = FakeHistoryRepository()
    private val analytics = mockk<Analytics>(relaxed = true)

    private fun viewModel() = ExportHistoryViewModel(
        historyRepository = history,
        analytics = analytics,
        workDispatcher = UnconfinedTestDispatcher(),
        clock = { 1_776_090_330_000L },
        zone = ZoneOffset.UTC,
    )

    private fun seedMixed() {
        history.seed(Fixtures.record(Fixtures.text("scan one"), ScanSource.CAMERA))
        history.seed(Fixtures.record(Fixtures.text("scan two"), ScanSource.IMAGE, isFavorite = true))
        history.seed(Fixtures.record(Fixtures.text("made"), ScanSource.CREATED))
    }

    @Test
    fun `loads counts and the number of items to export`() {
        seedMixed()
        val state = viewModel().state.value
        assertFalse(state.loading)
        assertEquals(ExportCounts(scanned = 2, created = 1), state.counts)
        assertEquals(3, state.itemCount)
        assertEquals(HistoryExportFormat.CSV, state.format)
        assertTrue(state.canExport)
    }

    @Test
    fun `include checkboxes and favorites only change the item count`() {
        seedMixed()
        val vm = viewModel()
        vm.onEvent(ExportEvent.SetIncludeCreated(false))
        assertEquals(2, vm.state.value.itemCount)
        vm.onEvent(ExportEvent.SetFavoritesOnly(true))
        assertEquals(1, vm.state.value.itemCount)
        vm.onEvent(ExportEvent.SetIncludeScanned(false))
        assertEquals(0, vm.state.value.itemCount)
        assertFalse(vm.state.value.canExport)
    }

    @Test
    fun `empty history cannot be exported`() = runTest {
        val vm = viewModel()
        assertFalse(vm.state.value.canExport)
        vm.effects.test {
            vm.onEvent(ExportEvent.SaveClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `save builds a csv payload with a time stamped file name`() = runTest {
        seedMixed()
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ExportEvent.SaveClicked)
            val effect = awaitItem() as ExportEffect.ChooseSaveLocation
            assertEquals("qr-history-20260413-142530.csv", effect.payload.fileName)
            assertEquals("text/csv", effect.payload.mimeType)
            assertTrue(effect.payload.text.startsWith("id,date,source,type,format,title,content,favorite,note"))
            assertTrue(effect.payload.text.contains("scan two"))
        }
        verify { analytics.log(Events.HISTORY_EXPORT, mapOf(Params.FORMAT to "CSV")) }
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `share uses the chosen format and only the selected rows`() = runTest {
        seedMixed()
        val vm = viewModel()
        vm.onEvent(ExportEvent.SelectFormat(HistoryExportFormat.JSON))
        vm.onEvent(ExportEvent.SetIncludeScanned(false))
        vm.effects.test {
            vm.onEvent(ExportEvent.ShareClicked)
            val effect = awaitItem() as ExportEffect.Share
            assertEquals("qr-history-20260413-142530.json", effect.payload.fileName)
            assertEquals("application/json", effect.payload.mimeType)
            assertTrue(effect.payload.text.contains("made"))
            assertFalse(effect.payload.text.contains("scan one"))
        }
        verify { analytics.log(Events.HISTORY_EXPORT, mapOf(Params.FORMAT to "JSON")) }
    }
}

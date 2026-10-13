package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub.CreateHubEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub.CreateHubUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub.CreateHubViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CreateHubViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val history = FakeHistoryRepository()

    private suspend fun CreateHubViewModel.awaitState(predicate: (CreateHubUiState) -> Boolean): CreateHubUiState {
        var found: CreateHubUiState? = null
        state.test {
            while (found == null) {
                val item = awaitItem()
                if (predicate(item)) found = item
            }
            cancelAndIgnoreRemainingEvents()
        }
        return checkNotNull(found)
    }

    @Test
    fun `starts empty`() = runTest {
        val vm = CreateHubViewModel(history)
        assertEquals(CreateHubUiState(), vm.state.value)
    }

    @Test
    fun `query changes flow into the state`() = runTest {
        val vm = CreateHubViewModel(history)
        vm.state.test {
            awaitItem()
            vm.onEvent(CreateHubEvent.QueryChanged("wifi"))
            assertEquals("wifi", awaitItem().query)
        }
    }

    @Test
    fun `recent lists the newest created types, distinct, without scans`() = runTest {
        history.save(Fixtures.record(Fixtures.website(), ScanSource.CREATED, createdAt = 10))
        history.save(Fixtures.record(Fixtures.website("https://b.io"), ScanSource.CREATED, createdAt = 20))
        history.save(Fixtures.record(Fixtures.text("scanned"), ScanSource.CAMERA, createdAt = 30))
        history.save(Fixtures.record(ScanContent.Wifi("WIFI:", CodeFormat.QR_CODE, "x"), ScanSource.CREATED, createdAt = 40))
        val vm = CreateHubViewModel(history)
        val state = vm.awaitState { it.recent.isNotEmpty() }
        assertEquals(listOf(CreateType.WIFI, CreateType.WEBSITE), state.recent)
    }
}

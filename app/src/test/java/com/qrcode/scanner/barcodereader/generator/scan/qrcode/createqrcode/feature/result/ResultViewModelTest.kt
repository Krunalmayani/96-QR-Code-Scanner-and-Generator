package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.DeleteRecordsUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val repo = FakeHistoryRepository()
    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)
    private val wifi = FakeWifiConnector()
    private val network = FakeNetworkStatus()
    private val qr = CodeFormat.QR_CODE

    private fun viewModel(recordId: Long, fromHistory: Boolean = false): ResultViewModel {
        val handle = SavedStateHandle(
            mapOf(ResultViewModel.ARG_RECORD_ID to recordId, ResultViewModel.ARG_FROM_HISTORY to fromHistory),
        )
        return ResultViewModel(
            handle,
            repo,
            settings,
            ToggleFavoriteUseCase(repo),
            DeleteRecordsUseCase(repo),
            wifi,
            network,
            analytics,
            NOTE_DEBOUNCE,
        )
    }

    private fun seed(content: ScanContent, source: ScanSource = ScanSource.CAMERA): ScanRecord =
        repo.seed(Fixtures.record(content, source))

    private fun wifiContent(security: WifiSecurity = WifiSecurity.WPA, password: String = "cafe-nova-2026") =
        ScanContent.Wifi("WIFI:", qr, "CafeNova_Guest", password, security)

    // ---- loading ----------------------------------------------------------------------------------------------

    @Test
    fun `loads the record and computes safety for websites`() = runTest {
        val record = seed(Fixtures.website("https://example.com"))
        val state = viewModel(record.id).uiState.value
        assertEquals(ResultPhase.READY, state.phase)
        assertEquals(record, state.record)
        assertTrue(state.safety!!.isSafe)
    }

    @Test
    fun `unknown id ends in not found`() = runTest {
        assertEquals(ResultPhase.NOT_FOUND, viewModel(99).uiState.value.phase)
    }

    @Test
    fun `transient records are shown too`() = runTest {
        val record = repo.save(Fixtures.record(Fixtures.text("hello")), persist = false)
        val state = viewModel(record.id).uiState.value
        assertTrue(record.id < 0)
        assertEquals(ResultPhase.READY, state.phase)
        assertEquals("hello", state.record?.rawValue)
    }

    // ---- website ----------------------------------------------------------------------------------------------

    @Test
    fun `a safe link opens in a custom tab only after the tap`() = runTest {
        val record = seed(Fixtures.website("https://example.com"))
        val vm = viewModel(record.id)
        vm.effects.test {
            expectNoEvents()
            vm.onEvent(ResultEvent.PrimaryAction)
            assertEquals(ResultEffect.OpenUrl("https://example.com"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.OPEN_LINK, mapOf(Params.TYPE to "WEBSITE", Params.RESULT to "tap")) }
    }

    @Test
    fun `an unsafe link shows the sheet first and logs it without opening`() = runTest {
        val record = seed(Fixtures.website("http://paypa1.com/login"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            assertTrue(vm.uiState.value.unsafeSheetVisible)
            assertTrue(vm.uiState.value.riskReasons.isNotEmpty())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.UNSAFE_LINK_SHOWN, mapOf(Params.TYPE to "WEBSITE", Params.RESULT to "UNSAFE")) }
    }

    @Test
    fun `open anyway opens the link after the warning`() = runTest {
        val record = seed(Fixtures.website("http://paypa1.com/login"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            vm.onEvent(ResultEvent.UnsafeOpenAnyway)
            assertFalse(vm.uiState.value.unsafeSheetVisible)
            assertEquals(ResultEffect.OpenUrl("http://paypa1.com/login"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `copy link instead copies and closes the sheet, go back leaves`() = runTest {
        val record = seed(Fixtures.website("http://paypa1.com/login"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            vm.onEvent(ResultEvent.UnsafeCopyLink)
            assertEquals(ResultEffect.CopyText("http://paypa1.com/login"), awaitItem())
            assertFalse(vm.uiState.value.unsafeSheetVisible)
            vm.onEvent(ResultEvent.PrimaryAction)
            vm.onEvent(ResultEvent.UnsafeGoBack)
            assertEquals(ResultEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `auto open opens a safe link of a new scan exactly once`() = runTest {
        settings.value = settings.value.copy(autoOpenLinks = true)
        val record = seed(Fixtures.website("https://example.com"))
        val vm = viewModel(record.id)
        vm.effects.test {
            assertEquals(ResultEffect.OpenUrl("https://example.com"), awaitItem())
            repo.updateNote(record.id, "changed")
            vm.onEvent(ResultEvent.FavoriteClicked)
            runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.OPEN_LINK, mapOf(Params.TYPE to "WEBSITE", Params.RESULT to "auto")) }
    }

    @Test
    fun `auto open never fires for history items, unsafe links or when the setting is off`() = runTest {
        val safe = seed(Fixtures.website("https://example.com"))
        val unsafe = seed(Fixtures.website("http://paypa1.com/login"))
        settings.value = settings.value.copy(autoOpenLinks = true)
        viewModel(safe.id, fromHistory = true).effects.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        viewModel(unsafe.id).effects.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        settings.value = settings.value.copy(autoOpenLinks = false)
        viewModel(safe.id).effects.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- other primary actions ------------------------------------------------------------------------------

    @Test
    fun `phone call builds a dial action`() = runTest {
        val record = seed(ScanContent.Phone("tel:+1", qr, "+1 (206) 555-0198"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            assertEquals(ResultEffect.Launch(ExternalAction.Dial("tel:+12065550198")), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a number with no digits cannot be dialled and shows a message`() = runTest {
        val record = seed(ScanContent.Phone("tel:#", qr, "#"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            assertEquals(ResultEffect.ShowMessage(ResultMessage.CANNOT_OPEN), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `email sms location and event each produce their own action`() = runTest {
        val email = seed(ScanContent.Email("raw", qr, "a@b.co", "Hi", "Body"))
        val sms = seed(ScanContent.Sms("raw", qr, "+44 7700 900123", "Yes"))
        val location = seed(ScanContent.Location("raw", qr, 37.7749, -122.4194, "SF"))
        val event = seed(ScanContent.Event("raw", qr, "Launch", 1_000_000L, null, false, "Hall", ""))
        val results = listOf(email, sms, location, event).map { record ->
            val vm = viewModel(record.id)
            var effect: ResultEffect? = null
            vm.effects.test {
                vm.onEvent(ResultEvent.PrimaryAction)
                effect = awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
            (effect as ResultEffect.Launch).action
        }
        assertTrue(results[0] is ExternalAction.ComposeEmail)
        assertEquals("mailto:a@b.co", (results[0] as ExternalAction.ComposeEmail).uri)
        assertEquals("smsto:+447700900123", (results[1] as ExternalAction.ComposeSms).uri)
        assertEquals("geo:37.7749,-122.4194?q=37.7749,-122.4194(SF)", (results[2] as ExternalAction.OpenMap).uri)
        val calendar = results[3] as ExternalAction.AddToCalendar
        assertEquals("Launch", calendar.title)
        assertEquals(1_000_000L, calendar.beginMillis)
    }

    @Test
    fun `contact rows dial and compose, add to contacts uses the contact`() = runTest {
        val record = seed(ScanContent.Contact("raw", qr, name = "Sophie", phones = listOf("+1 415"), emails = listOf("s@l.studio")))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.DialClicked("+1 415"))
            assertEquals(ResultEffect.Launch(ExternalAction.Dial("tel:+1415")), awaitItem())
            vm.onEvent(ResultEvent.ComposeEmailClicked("s@l.studio"))
            assertEquals("mailto:s@l.studio", ((awaitItem() as ResultEffect.Launch).action as ExternalAction.ComposeEmail).uri)
            vm.onEvent(ResultEvent.AddToContactsClicked)
            val add = (awaitItem() as ResultEffect.Launch).action as ExternalAction.AddToContacts
            assertEquals("Sophie", add.contact.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- copy, share, favorite ----------------------------------------------------------------------------

    @Test
    fun `copy sends the copy text and logs a content free event`() = runTest {
        val record = seed(Fixtures.text("secret text"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.CopyClicked)
            assertEquals(ResultEffect.CopyText("secret text"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.COPY_TAP, mapOf(Params.TYPE to "TEXT")) }
    }

    @Test
    fun `sharing a scan goes straight to the system sheet`() = runTest {
        val record = seed(Fixtures.text("hello"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.ShareClicked)
            assertEquals(ResultEffect.ShareText("hello"), awaitItem())
            assertFalse(vm.uiState.value.shareSheetVisible)
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.SHARE_TAP, mapOf(Params.TYPE to "TEXT", Params.RESULT to "text")) }
    }

    @Test
    fun `sharing a created code asks text or image first`() = runTest {
        val record = seed(Fixtures.text("hello"), ScanSource.CREATED)
        val vm = viewModel(record.id, fromHistory = true)
        vm.effects.test {
            vm.onEvent(ResultEvent.ShareClicked)
            assertTrue(vm.uiState.value.shareSheetVisible)
            expectNoEvents()
            vm.onEvent(ResultEvent.ShareAsImage)
            assertEquals(ResultEffect.OpenGenerated(record.id), awaitItem())
            assertFalse(vm.uiState.value.shareSheetVisible)
            vm.onEvent(ResultEvent.ShareClicked)
            vm.onEvent(ResultEvent.ShareAsText)
            assertEquals(ResultEffect.ShareText("hello"), awaitItem())
            vm.onEvent(ResultEvent.ShareClicked)
            vm.onEvent(ResultEvent.ShareSheetDismissed)
            assertFalse(vm.uiState.value.shareSheetVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorite toggles the stored flag and the state follows`() = runTest {
        val record = seed(Fixtures.text("hello"))
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.FavoriteClicked)
        assertTrue(repo.getById(record.id)!!.isFavorite)
        assertTrue(vm.uiState.value.record!!.isFavorite)
        vm.onEvent(ResultEvent.FavoriteClicked)
        assertFalse(vm.uiState.value.record!!.isFavorite)
        verify { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to "TEXT", Params.RESULT to "on")) }
    }

    @Test
    fun `favorite works on a transient record`() = runTest {
        val record = repo.save(Fixtures.record(Fixtures.text("hello")), persist = false)
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.FavoriteClicked)
        assertTrue(vm.uiState.value.record!!.isFavorite)
    }

    // ---- product --------------------------------------------------------------------------------------------

    @Test
    fun `product sites follow the preferred order`() = runTest {
        settings.value = settings.value.copy(productSiteOrder = listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS))
        val record = seed(ScanContent.Product("5449000000996", CodeFormat.EAN_13, "5449000000996"))
        val sites = viewModel(record.id).uiState.value.product.sites
        assertEquals(listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS), sites.map { it.site })
        assertEquals("https://www.amazon.com/s?k=5449000000996", sites.first().url)
    }

    @Test
    fun `search opens online, offline shows the retry state and recovers`() = runTest {
        val record = seed(ScanContent.Product("5449000000996", CodeFormat.EAN_13, "5449000000996"))
        val vm = viewModel(record.id)
        network.online = false
        vm.effects.test {
            vm.onEvent(ResultEvent.PrimaryAction)
            assertTrue(vm.uiState.value.product.searchOpen)
            assertFalse(vm.uiState.value.product.online)
            vm.onEvent(ResultEvent.ProductRetry)
            assertEquals(ResultEffect.ShowMessage(ResultMessage.STILL_OFFLINE), awaitItem())
            network.online = true
            vm.onEvent(ResultEvent.ProductRetry)
            assertTrue(vm.uiState.value.product.online)
            expectNoEvents()
            vm.onEvent(ResultEvent.ProductSearchClosed)
            assertFalse(vm.uiState.value.product.searchOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `choosing a site opens its url in a custom tab`() = runTest {
        val record = seed(ScanContent.Product("5449000000996", CodeFormat.EAN_13, "5449000000996"))
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.OpenProductSite(ProductSite.OPEN_FOOD_FACTS))
            assertEquals(ResultEffect.OpenUrl("https://world.openfoodfacts.org/product/5449000000996"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.OPEN_LINK, mapOf(Params.TYPE to "PRODUCT", Params.RESULT to "open_food_facts")) }
    }

    // ---- note and delete ------------------------------------------------------------------------------------

    @Test
    fun `note is saved after a pause and not overwritten by the repository update`() = runTest {
        val record = seed(Fixtures.text("hello"))
        val vm = viewModel(record.id, fromHistory = true)
        vm.onEvent(ResultEvent.NoteChanged("buy milk"))
        advanceTimeBy(NOTE_DEBOUNCE - 1)
        assertEquals("", repo.getById(record.id)!!.note)
        advanceTimeBy(2)
        runCurrent()
        assertEquals("buy milk", repo.getById(record.id)!!.note)
        assertEquals("buy milk", vm.uiState.value.note)
    }

    @Test
    fun `only the last of several quick edits is saved and commit saves at once`() = runTest {
        val record = seed(Fixtures.text("hello"))
        val vm = viewModel(record.id, fromHistory = true)
        vm.onEvent(ResultEvent.NoteChanged("a"))
        vm.onEvent(ResultEvent.NoteChanged("ab"))
        vm.onEvent(ResultEvent.NoteCommit)
        runCurrent()
        assertEquals("ab", repo.getById(record.id)!!.note)
    }

    @Test
    fun `the stored note appears in the field when the item opens`() = runTest {
        val record = repo.seed(Fixtures.record(Fixtures.text("hello"), note = "kept"))
        assertEquals("kept", viewModel(record.id, fromHistory = true).uiState.value.note)
    }

    @Test
    fun `delete asks first, then removes the record and leaves without a not found flash`() = runTest {
        val record = seed(Fixtures.text("hello"), ScanSource.CREATED)
        val vm = viewModel(record.id, fromHistory = true)
        vm.effects.test {
            vm.onEvent(ResultEvent.DeleteClicked)
            assertTrue(vm.uiState.value.deleteDialogVisible)
            vm.onEvent(ResultEvent.DeleteDismissed)
            assertFalse(vm.uiState.value.deleteDialogVisible)
            assertTrue(repo.persistedRecords.isNotEmpty())
            vm.onEvent(ResultEvent.DeleteClicked)
            vm.onEvent(ResultEvent.DeleteConfirmed)
            assertEquals(ResultEffect.NavigateBack, awaitItem())
            assertNull(repo.getById(record.id))
            assertEquals(ResultPhase.READY, vm.uiState.value.phase)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `edit style opens the generated screen`() = runTest {
        val record = seed(Fixtures.text("hello"), ScanSource.CREATED)
        val vm = viewModel(record.id, fromHistory = true)
        vm.effects.test {
            vm.onEvent(ResultEvent.EditStyleClicked)
            assertEquals(ResultEffect.OpenGenerated(record.id), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---- Wi-Fi ----------------------------------------------------------------------------------------------

    @Test
    fun `connect goes connecting then connected and Done returns to scan`() = runTest {
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.WifiConnectClicked)
            assertEquals(WifiPhase.CONNECTING, vm.uiState.value.wifi.phase)
            assertEquals("CafeNova_Guest", wifi.requests.single().ssid)
            wifi.finish(WifiConnectOutcome.Connected)
            assertEquals(WifiPhase.CONNECTED, vm.uiState.value.wifi.phase)
            vm.onEvent(ResultEvent.WifiDone)
            assertEquals(ResultEffect.NavigateToScan, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.WIFI_CONNECT, mapOf(Params.TYPE to "WIFI", Params.RESULT to "success")) }
    }

    @Test
    fun `a failed connection keeps the password copyable and Try again restarts`() = runTest {
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.WifiConnectClicked)
            wifi.finish(WifiConnectOutcome.Failed(WifiFailure.UNAVAILABLE))
            assertEquals(WifiPhase.FAILED, vm.uiState.value.wifi.phase)
            assertEquals(WifiFailure.UNAVAILABLE, vm.uiState.value.wifi.failure)
            vm.onEvent(ResultEvent.WifiCopyPassword)
            assertEquals(ResultEffect.CopyText("cafe-nova-2026", sensitive = true), awaitItem())
            vm.onEvent(ResultEvent.WifiRetry)
            assertEquals(WifiPhase.CONNECTING, vm.uiState.value.wifi.phase)
            assertEquals(2, wifi.requests.size)
            wifi.finish(WifiConnectOutcome.Suggested)
            assertEquals(WifiPhase.CONNECTED, vm.uiState.value.wifi.phase)
            assertTrue(vm.uiState.value.wifi.viaSuggestion)
            cancelAndIgnoreRemainingEvents()
        }
        verify { analytics.log(Events.WIFI_CONNECT, mapOf(Params.TYPE to "WIFI", Params.RESULT to "failed_unavailable")) }
    }

    @Test
    fun `cancel stops the attempt and ignores a late result`() = runTest {
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.WifiConnectClicked)
        vm.onEvent(ResultEvent.WifiCancel)
        assertEquals(WifiPhase.IDLE, vm.uiState.value.wifi.phase)
        assertEquals(1, wifi.cancelCount)
        wifi.finish(WifiConnectOutcome.Connected)
        runCurrent()
        assertEquals(WifiPhase.IDLE, vm.uiState.value.wifi.phase)
    }

    @Test
    fun `back from the failed screen returns to the normal result`() = runTest {
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.WifiConnectClicked)
        wifi.finish(WifiConnectOutcome.Failed(WifiFailure.ERROR))
        vm.onEvent(ResultEvent.WifiBack)
        assertFalse(vm.uiState.value.wifi.flowVisible)
    }

    @Test
    fun `old android without location permission shows the explainer before connecting`() = runTest {
        wifi.sdkInt = 28
        wifi.locationGranted = false
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.WifiConnectClicked)
            assertTrue(vm.uiState.value.locationRationaleVisible)
            assertTrue(wifi.requests.isEmpty())
            vm.onEvent(ResultEvent.LocationRationaleContinue)
            assertFalse(vm.uiState.value.locationRationaleVisible)
            assertEquals(ResultEffect.RequestLocationPermission, awaitItem())
            wifi.locationGranted = true
            vm.onEvent(ResultEvent.LocationPermissionResult(granted = true))
            assertEquals(WifiPhase.CONNECTING, vm.uiState.value.wifi.phase)
            assertEquals(1, wifi.requests.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `denied location permission ends in a failed state without any attempt`() = runTest {
        wifi.sdkInt = 28
        wifi.locationGranted = false
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.WifiConnectClicked)
        vm.onEvent(ResultEvent.LocationRationaleContinue)
        vm.onEvent(ResultEvent.LocationPermissionResult(granted = false))
        assertEquals(WifiPhase.FAILED, vm.uiState.value.wifi.phase)
        assertEquals(WifiFailure.PERMISSION_DENIED, vm.uiState.value.wifi.failure)
        assertTrue(wifi.requests.isEmpty())
    }

    @Test
    fun `dismissing the explainer leaves everything as it was`() = runTest {
        wifi.sdkInt = 28
        wifi.locationGranted = false
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.WifiConnectClicked)
        vm.onEvent(ResultEvent.LocationRationaleDismissed)
        assertFalse(vm.uiState.value.locationRationaleVisible)
        assertEquals(WifiPhase.IDLE, vm.uiState.value.wifi.phase)
    }

    @Test
    fun `why location only explains and never starts a connection`() = runTest {
        wifi.sdkInt = 28
        wifi.locationGranted = true
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.effects.test {
            vm.onEvent(ResultEvent.WhyLocationClicked)
            assertTrue(vm.uiState.value.locationRationaleVisible)
            vm.onEvent(ResultEvent.LocationRationaleContinue)
            assertEquals(ResultEffect.RequestLocationPermission, awaitItem())
            vm.onEvent(ResultEvent.LocationPermissionResult(granted = true))
            assertEquals(WifiPhase.IDLE, vm.uiState.value.wifi.phase)
            assertTrue(wifi.requests.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `modern android does not ask for location`() = runTest {
        wifi.sdkInt = 34
        wifi.locationGranted = false
        val record = seed(wifiContent())
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.WifiConnectClicked)
        assertFalse(vm.uiState.value.locationRationaleVisible)
        assertEquals(WifiPhase.CONNECTING, vm.uiState.value.wifi.phase)
    }

    @Test
    fun `wep on modern android fails at once and an invalid password never reaches the connector`() = runTest {
        val wep = seed(wifiContent(WifiSecurity.WEP, "12345"))
        val vmWep = viewModel(wep.id)
        vmWep.onEvent(ResultEvent.WifiConnectClicked)
        assertEquals(WifiFailure.UNSUPPORTED_SECURITY, vmWep.uiState.value.wifi.failure)

        val short = seed(wifiContent(WifiSecurity.WPA, "short"))
        val vmShort = viewModel(short.id)
        vmShort.onEvent(ResultEvent.WifiConnectClicked)
        assertEquals(WifiFailure.INVALID_PASSWORD, vmShort.uiState.value.wifi.failure)
        assertTrue(wifi.requests.isEmpty())
    }

    @Test
    fun `an open network connects without a password`() = runTest {
        val record = seed(wifiContent(WifiSecurity.NONE, ""))
        val vm = viewModel(record.id)
        vm.onEvent(ResultEvent.PrimaryAction)
        assertEquals(WifiPhase.CONNECTING, vm.uiState.value.wifi.phase)
        assertEquals(WifiSecurity.NONE, wifi.requests.single().security)
    }

    private companion object {
        const val NOTE_DEBOUNCE = 100L
    }
}

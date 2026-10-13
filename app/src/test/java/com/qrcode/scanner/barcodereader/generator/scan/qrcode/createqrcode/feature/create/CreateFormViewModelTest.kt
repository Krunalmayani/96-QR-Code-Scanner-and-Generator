package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.DensityLevel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FieldError
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormEffect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreatedContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CreateFormViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val history = FakeHistoryRepository()
    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)
    private val parser = object : CreatedContentParser() {
        private val real = ScanContentParser(ZoneOffset.UTC)
        override fun parse(payload: String, format: CodeFormat): ScanContent = real.parse(payload, format)
    }

    private fun viewModel(
        type: CreateType?,
        repo: HistoryRepository = history,
    ) = CreateFormViewModel(
        savedStateHandle = SavedStateHandle(if (type == null) emptyMap() else mapOf(CreateFormViewModel.KEY_TYPE to type.name)),
        saveScan = SaveScanUseCase(repo, settings),
        parser = parser,
        analytics = analytics,
    )

    @Test
    fun `an unknown type key leaves the form without a spec`() {
        val vm = CreateFormViewModel(
            savedStateHandle = SavedStateHandle(mapOf(CreateFormViewModel.KEY_TYPE to "NOPE")),
            saveScan = SaveScanUseCase(history, settings),
            parser = parser,
            analytics = analytics,
        )
        assertNull(vm.state.value.spec)
        assertFalse(vm.state.value.isValid)
        vm.onEvent(CreateFormEvent.Generate)
        assertEquals(emptyList<Any>(), history.persistedRecords)
    }

    @Test
    fun `a fresh form is invalid but shows no errors yet`() {
        val vm = viewModel(CreateType.WEBSITE)
        val state = vm.state.value
        assertEquals(CreateSpec.Website(), state.spec)
        assertFalse(state.isValid)
        assertEquals(FieldError.Required, state.errors[FormField.URL])
        assertTrue(state.visibleErrors.isEmpty())
    }

    @Test
    fun `an error appears after the field loses focus`() {
        val vm = viewModel(CreateType.WEBSITE)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Website("brewhouse coffee")))
        assertTrue(vm.state.value.visibleErrors.isEmpty())
        vm.onEvent(CreateFormEvent.Blur(FormField.URL))
        assertEquals(FieldError.InvalidUrl, vm.state.value.visibleErrors[FormField.URL])
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Website("brewhouse-coffee.com")))
        assertTrue(vm.state.value.visibleErrors.isEmpty())
        assertTrue(vm.state.value.isValid)
    }

    @Test
    fun `generating an invalid form shows every error and creates nothing`() = runTest {
        val vm = viewModel(CreateType.WIFI)
        vm.effects.test {
            vm.onEvent(CreateFormEvent.Generate)
            val state = vm.state.value
            assertTrue(state.showAllErrors)
            assertEquals(setOf(FormField.SSID, FormField.PASSWORD), state.visibleErrors.keys)
            expectNoEvents()
        }
        assertTrue(history.persistedRecords.isEmpty())
    }

    @Test
    fun `edits of another type are ignored`() {
        val vm = viewModel(CreateType.WEBSITE)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Text("hello")))
        assertEquals(CreateSpec.Website(), vm.state.value.spec)
    }

    @Test
    fun `generating a website saves a created record, logs the type only and opens it`() = runTest {
        val vm = viewModel(CreateType.WEBSITE)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Website("brewhouse-coffee.com/menu")))
        assertTrue(vm.state.value.isValid)
        vm.effects.test {
            vm.onEvent(CreateFormEvent.Generate)
            val effect = awaitItem() as CreateFormEffect.OpenGenerated
            val record = checkNotNull(history.getById(effect.recordId))
            assertEquals(ScanSource.CREATED, record.source)
            assertEquals(QrStyle(), record.style)
            val content = record.content as ScanContent.Website
            assertEquals("https://brewhouse-coffee.com/menu", content.rawValue)
            assertEquals(CodeFormat.QR_CODE, content.format)
        }
        verify(exactly = 1) { analytics.log(Events.CREATE_SUCCESS, mapOf(Params.TYPE to "WEBSITE")) }
        assertEquals(1, settings.value.completedActions)
        assertFalse(vm.state.value.generating)
    }

    @Test
    fun `with history off the record is transient but still opens`() = runTest {
        settings.value = AppSettings(saveHistory = false)
        val vm = viewModel(CreateType.TEXT)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Text("hello")))
        vm.effects.test {
            vm.onEvent(CreateFormEvent.Generate)
            val effect = awaitItem() as CreateFormEffect.OpenGenerated
            assertTrue(effect.recordId < 0)
        }
        assertTrue(history.persistedRecords.isEmpty())
    }

    @Test
    fun `a failing save reports a failure and allows another try`() = runTest {
        val broken = mockk<HistoryRepository>()
        coEvery { broken.save(any(), any()) } throws IOException("disk full")
        val vm = viewModel(CreateType.TEXT, repo = broken)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Text("hello")))
        vm.effects.test {
            vm.onEvent(CreateFormEvent.Generate)
            assertEquals(CreateFormEffect.GenerateFailed, awaitItem())
        }
        assertFalse(vm.state.value.generating)
        verify(exactly = 0) { analytics.log(Events.CREATE_SUCCESS, any()) }
    }

    @Test
    fun `the text form reports how dense the code will be`() {
        val vm = viewModel(CreateType.TEXT)
        assertNull(vm.state.value.density)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Text("hello")))
        assertEquals(DensityLevel.LOW, vm.state.value.density)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Text("x".repeat(600))))
        assertEquals(DensityLevel.HIGH, vm.state.value.density)
    }

    @Test
    fun `wifi without a password is invalid unless the network is open`() {
        val vm = viewModel(CreateType.WIFI)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Wifi(ssid = "CafeNova", password = "", security = WifiSecurity.WPA)))
        assertFalse(vm.state.value.isValid)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Wifi(ssid = "CafeNova", password = "", security = WifiSecurity.NONE)))
        assertTrue(vm.state.value.isValid)
    }

    // ---- barcodes ----

    @Test
    fun `barcode forms track the status and preview of the value`() {
        val vm = viewModel(CreateType.BARCODE_EAN_13)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "5901234")))
        assertEquals(BarcodeStatus.Incomplete(7, 12), vm.state.value.barcodeStatus)
        assertNull(vm.state.value.barcodePreview)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "590123412345")))
        assertEquals(BarcodeStatus.CheckDigitWillBeAdded(7), vm.state.value.barcodeStatus)
        assertNotNull(vm.state.value.barcodePreview)
        assertTrue(vm.state.value.isValid)
    }

    @Test
    fun `switching the barcode type keeps the typed value`() {
        val vm = viewModel(CreateType.BARCODE_EAN_13)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "12345670")))
        vm.onEvent(CreateFormEvent.SelectBarcode(CreateType.BARCODE_EAN_8))
        assertEquals(CreateSpec.Barcode(CreateType.BARCODE_EAN_8, "12345670"), vm.state.value.spec)
        assertTrue(vm.state.value.isValid)
        vm.onEvent(CreateFormEvent.SelectBarcode(CreateType.WEBSITE))
        assertEquals(CreateType.BARCODE_EAN_8, vm.state.value.type)
    }

    @Test
    fun `barcode data is cut at the symbology length`() {
        val vm = viewModel(CreateType.BARCODE_EAN_13)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "59012341234571234")))
        assertEquals("5901234123457", (vm.state.value.spec as CreateSpec.Barcode).data)
    }

    @Test
    fun `an EAN-13 without check digit is completed and read back as a product`() = runTest {
        val vm = viewModel(CreateType.BARCODE_EAN_13)
        vm.onEvent(CreateFormEvent.Edit(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "590123412345")))
        vm.effects.test {
            vm.onEvent(CreateFormEvent.Generate)
            val effect = awaitItem() as CreateFormEffect.OpenGenerated
            val content = checkNotNull(history.getById(effect.recordId)).content as ScanContent.Product
            assertEquals("5901234123457", content.rawValue)
            assertEquals(CodeFormat.EAN_13, content.format)
        }
        verify { analytics.log(Events.CREATE_SUCCESS, mapOf(Params.TYPE to "BARCODE_EAN_13")) }
    }

    // ---- events ----

    @Test
    fun `the event form starts with a one hour slot and needs a title`() {
        val vm = viewModel(CreateType.EVENT)
        val event = vm.state.value.spec as CreateSpec.Event
        assertNotNull(event.startMillis)
        assertEquals(3_600_000L, event.endMillis!! - event.startMillis!!)
        assertEquals(setOf(FormField.TITLE), vm.state.value.errors.keys)
        vm.onEvent(CreateFormEvent.Edit(event.copy(title = "Launch")))
        assertTrue(vm.state.value.isValid)
    }

    @Test
    fun `all-day converts both ends to dates and back`() {
        val vm = viewModel(CreateType.EVENT)
        vm.onEvent(CreateFormEvent.SetAllDay(true))
        val allDay = vm.state.value.spec as CreateSpec.Event
        assertTrue(allDay.allDay)
        assertEquals(0L, allDay.startMillis!! % 86_400_000L)
        vm.onEvent(CreateFormEvent.SetAllDay(false))
        assertFalse((vm.state.value.spec as CreateSpec.Event).allDay)
    }

    @Test
    fun `picking an end before the start shows the error on the end field`() {
        val vm = viewModel(CreateType.EVENT)
        val event = vm.state.value.spec as CreateSpec.Event
        val dayBefore = event.startMillis!! - 2 * 86_400_000L
        vm.onEvent(CreateFormEvent.EventDatePicked(start = false, utcDateMillis = dayBefore - dayBefore % 86_400_000L))
        assertEquals(FieldError.EndBeforeStart, vm.state.value.visibleErrors[FormField.END])
    }

    @Test
    fun `moving the start moves the end with it`() {
        val vm = viewModel(CreateType.EVENT)
        val before = vm.state.value.spec as CreateSpec.Event
        val duration = before.endMillis!! - before.startMillis!!
        val nextWeek = before.startMillis!! + 7 * 86_400_000L
        vm.onEvent(CreateFormEvent.EventDatePicked(start = true, utcDateMillis = nextWeek - nextWeek % 86_400_000L))
        val after = vm.state.value.spec as CreateSpec.Event
        assertEquals(duration, after.endMillis!! - after.startMillis!!)
        assertTrue(after.startMillis!! > before.startMillis!!)
    }
}

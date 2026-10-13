package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.ScanRecorder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ScanFromImageViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private class FakeReader(var result: ImageReadResult) : ImageCodeReader {
        override suspend fun read(imageUri: String): ImageReadResult = result
    }

    private val history = FakeHistoryRepository()
    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)
    private val reader = FakeReader(ImageReadResult.NoCode)

    private fun viewModel(): ScanFromImageViewModel {
        val recorder = ScanRecorder(SaveScanUseCase(history, settings) { 1L }, analytics, ScanContentParser())
        return ScanFromImageViewModel(reader, recorder, analytics)
    }

    private val site = DetectedCode("https://example.com", CodeFormat.QR_CODE)
    private val product = DetectedCode("5901234123457", CodeFormat.EAN_13)

    @Test
    fun `starts idle`() = runTest {
        assertEquals(ImageScanState.Idle, viewModel().state.value)
    }

    @Test
    fun `a single code is saved and opens its result replacing the screen`() = runTest {
        reader.result = ImageReadResult.Found(listOf(site))
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ImageScanEvent.ImagePicked("content://image/1"))

            val saved = history.persistedRecords.single()
            assertEquals(ScanSource.IMAGE, saved.source)
            assertEquals(ImageScanEffect.OpenResult(saved.id, replaceScreen = true), awaitItem())
        }
        val state = vm.state.value as ImageScanState.Found
        assertEquals(1, state.codes.size)
        assertEquals(ContentType.WEBSITE, state.codes.single().type)
        verify { analytics.log(Events.SCAN_FROM_IMAGE, mapOf(Params.RESULT to "SINGLE")) }
        verify {
            analytics.log(
                Events.SCAN_SUCCESS,
                mapOf(Params.TYPE to "WEBSITE", Params.SOURCE to "IMAGE", Params.FORMAT to "QR_CODE"),
            )
        }
    }

    @Test
    fun `several codes wait for a choice and only the chosen one is saved`() = runTest {
        reader.result = ImageReadResult.Found(listOf(site, product))
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ImageScanEvent.ImagePicked("content://image/2"))
            expectNoEvents()
            assertTrue(history.persistedRecords.isEmpty())
            assertEquals(2, (vm.state.value as ImageScanState.Found).codes.size)

            vm.onEvent(ImageScanEvent.CodeChosen(1))
            val saved = history.persistedRecords.single()
            assertEquals(ContentType.PRODUCT, saved.type)
            assertEquals(ImageScanEffect.OpenResult(saved.id, replaceScreen = false), awaitItem())
        }
        verify { analytics.log(Events.SCAN_FROM_IMAGE, mapOf(Params.RESULT to "MULTIPLE")) }
    }

    @Test
    fun `choosing the same code twice does not save it twice`() = runTest {
        reader.result = ImageReadResult.Found(listOf(site, product))
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ImageScanEvent.ImagePicked("content://image/3"))
            vm.onEvent(ImageScanEvent.CodeChosen(0))
            val first = awaitItem() as ImageScanEffect.OpenResult
            vm.onEvent(ImageScanEvent.CodeChosen(0))
            val second = awaitItem() as ImageScanEffect.OpenResult
            assertEquals(first.recordId, second.recordId)
        }
        assertEquals(1, history.persistedRecords.size)
    }

    @Test
    fun `an out of range choice is ignored`() = runTest {
        reader.result = ImageReadResult.Found(listOf(site, product))
        val vm = viewModel()
        vm.onEvent(ImageScanEvent.ImagePicked("content://image/4"))
        vm.onEvent(ImageScanEvent.CodeChosen(7))
        assertTrue(history.persistedRecords.isEmpty())
    }

    @Test
    fun `no code shows the empty result and logs it`() = runTest {
        reader.result = ImageReadResult.NoCode
        val vm = viewModel()
        vm.onEvent(ImageScanEvent.ImagePicked("content://image/5"))

        assertEquals(ImageScanState.NoCode("content://image/5", unreadable = false), vm.state.value)
        verify { analytics.log(Events.SCAN_NO_CODE, mapOf(Params.SOURCE to "IMAGE")) }
        assertTrue(history.persistedRecords.isEmpty())
    }

    @Test
    fun `an unreadable file shows the no code state flagged as unreadable`() = runTest {
        reader.result = ImageReadResult.Unreadable
        val vm = viewModel()
        vm.onEvent(ImageScanEvent.ImagePicked("content://image/6"))

        assertEquals(ImageScanState.NoCode("content://image/6", unreadable = true), vm.state.value)
    }

    @Test
    fun `cancel returns to idle and closes the screen`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ImageScanEvent.Cancelled)
            assertEquals(ImageScanEffect.Close, awaitItem())
        }
        assertEquals(ImageScanState.Idle, vm.state.value)
    }

    @Test
    fun `try another image asks for the picker`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onEvent(ImageScanEvent.ChooseAnotherClicked)
            assertEquals(ImageScanEffect.LaunchPicker, awaitItem())
        }
    }

    @Test
    fun `picking a new image after no code starts over`() = runTest {
        val vm = viewModel()
        vm.onEvent(ImageScanEvent.ImagePicked("content://image/7"))
        reader.result = ImageReadResult.Found(listOf(site, product))
        vm.onEvent(ImageScanEvent.ImagePicked("content://image/8"))

        assertTrue(vm.state.value is ImageScanState.Found)
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ScanViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val history = FakeHistoryRepository()
    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)
    private var now = 10_000L

    private fun viewModel(): ScanViewModel {
        val recorder = ScanRecorder(SaveScanUseCase(history, settings) { now }, analytics, ScanContentParser())
        return ScanViewModel(recorder, ToggleFavoriteUseCase(history), settings, { now }, 0L)
    }

    private fun qr(raw: String = "https://example.com") = DetectedCode(raw, CodeFormat.QR_CODE)

    private fun ean(raw: String = "5901234123457") = DetectedCode(raw, CodeFormat.EAN_13)

    @Test
    fun `a detected code is saved and opens its result after the feedback`() =
        runTest {
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))

                assertEquals(ScanEffect.Feedback(vibrate = true, beep = false), awaitItem())
                val open = awaitItem() as ScanEffect.OpenResult
                val saved = history.persistedRecords.single()
                assertEquals(saved.id, open.recordId)
                assertEquals(ScanSource.CAMERA, saved.source)
                assertEquals(ContentType.WEBSITE, saved.type)
            }
            assertTrue(vm.state.value.detected)
            verify {
                analytics.log(
                    Events.SCAN_SUCCESS,
                    mapOf(Params.TYPE to "WEBSITE", Params.SOURCE to "CAMERA", Params.FORMAT to "QR_CODE"),
                )
            }
        }

    @Test
    fun `codes outside the selected mode are ignored`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(ean())))

            assertTrue(history.persistedRecords.isEmpty())
            assertFalse(vm.state.value.detected)
        }

    @Test
    fun `barcode mode accepts a barcode`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.ModeSelected(ScanMode.BARCODE))
            vm.onEvent(ScanEvent.CodesDetected(listOf(ean())))

            assertEquals(ContentType.PRODUCT, history.persistedRecords.single().type)
        }

    @Test
    fun `the same code within two seconds is a duplicate, after two seconds it counts again`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            vm.onEvent(ScanEvent.ScreenResumed)

            now += 1_000L
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            assertEquals(1, history.persistedRecords.size)

            now += 2_500L
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            assertEquals(2, history.persistedRecords.size)
        }

    @Test
    fun `analysis stays blocked until the screen resumes`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://a.example"))))
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://b.example"))))
            assertEquals(1, history.persistedRecords.size)

            vm.onEvent(ScanEvent.ScreenResumed)
            assertFalse(vm.state.value.detected)
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://b.example"))))
            assertEquals(2, history.persistedRecords.size)
        }

    @Test
    fun `vibrate and beep follow the settings`() =
        runTest {
            settings.value = AppSettings(vibrate = false, beep = true)
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
                assertEquals(ScanEffect.Feedback(vibrate = false, beep = true), awaitItem())
                awaitItem()
            }
        }

    @Test
    fun `with continue scanning on, a quick result is shown instead of opening the result`() =
        runTest {
            settings.value = AppSettings(continuousScan = true)
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
                assertTrue(awaitItem() is ScanEffect.Feedback)
                expectNoEvents()
            }
            val quick = vm.state.value.quickResult
            assertEquals(history.persistedRecords.single().id, quick?.id)
            assertTrue(vm.state.value.detected)
        }

    @Test
    fun `dismissing the quick result keeps scanning`() =
        runTest {
            settings.value = AppSettings(continuousScan = true)
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            vm.onEvent(ScanEvent.QuickDismissed)

            assertNull(vm.state.value.quickResult)
            assertFalse(vm.state.value.detected)
        }

    @Test
    fun `tapping the quick result opens the full result`() =
        runTest {
            settings.value = AppSettings(continuousScan = true)
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            val id =
                vm.state.value.quickResult!!
                    .id
            vm.effects.test {
                assertTrue(awaitItem() is ScanEffect.Feedback)
                vm.onEvent(ScanEvent.QuickOpened)
                assertEquals(ScanEffect.OpenResult(id), awaitItem())
            }
            assertNull(vm.state.value.quickResult)
        }

    @Test
    fun `favorite toggle in the quick result updates the record`() =
        runTest {
            settings.value = AppSettings(continuousScan = true)
            val vm = viewModel()
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))

            vm.onEvent(ScanEvent.QuickFavoriteToggled)

            assertTrue(
                vm.state.value.quickResult!!
                    .isFavorite,
            )
            assertTrue(history.persistedRecords.single().isFavorite)
        }

    @Test
    fun `batch mode collects unique codes without leaving the screen`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.ModeSelected(ScanMode.ALL))
            vm.onEvent(ScanEvent.BatchToggled)

            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://a.example"))))
            vm.onEvent(ScanEvent.CodesDetected(listOf(ean())))
            now += 5_000L
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://a.example"))))

            val state = vm.state.value
            assertTrue(state.batchMode)
            assertFalse(state.detected)
            assertEquals(listOf(ContentType.PRODUCT, ContentType.WEBSITE), state.batch.map { it.type })
            assertEquals(2, history.persistedRecords.size)
        }

    @Test
    fun `batch mode saves several codes found in one frame`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.BatchToggled)
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr("https://a.example"), qr("https://b.example"))))

            assertEquals(2, vm.state.value.batch.size)
            assertEquals(2, history.persistedRecords.size)
        }

    @Test
    fun `batch rows open their result and finishing clears the batch`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.BatchToggled)
            vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
            val row =
                vm.state.value.batch
                    .single()

            vm.onEvent(ScanEvent.BatchDoneClicked)
            assertTrue(vm.state.value.batchSheetOpen)
            vm.effects.test {
                assertTrue(awaitItem() is ScanEffect.Feedback)
                vm.onEvent(ScanEvent.BatchRowClicked(row.recordId))
                assertEquals(ScanEffect.OpenResult(row.recordId), awaitItem())
            }

            vm.onEvent(ScanEvent.BatchFinished)
            val state = vm.state.value
            assertFalse(state.batchMode)
            assertFalse(state.batchSheetOpen)
            assertTrue(state.batch.isEmpty())
        }

    @Test
    fun `flash on start comes from the settings and switching camera turns it off`() =
        runTest {
            settings.value = AppSettings(flashOnStart = true)
            val vm = viewModel()
            assertTrue(vm.state.value.flashOn)

            vm.onEvent(ScanEvent.CameraSwitched)
            assertTrue(vm.state.value.frontCamera)
            assertFalse(vm.state.value.flashOn)

            vm.onEvent(ScanEvent.FlashToggled)
            assertFalse(vm.state.value.flashOn)
        }

    @Test
    fun `flash toggles on the back camera`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.FlashToggled)
            assertTrue(vm.state.value.flashOn)
            vm.onEvent(ScanEvent.FlashToggled)
            assertFalse(vm.state.value.flashOn)
        }

    @Test
    fun `zoom is clamped to the linear range`() =
        runTest {
            val vm = viewModel()
            vm.onEvent(ScanEvent.ZoomChanged(3f))
            assertEquals(1f, vm.state.value.zoom)
            vm.onEvent(ScanEvent.ZoomChanged(-1f))
            assertEquals(0f, vm.state.value.zoom)
        }

    @Test
    fun `scan area and continuous settings reach the state`() =
        runTest {
            settings.value = AppSettings(scanAreaOnly = false, continuousScan = true)
            val vm = viewModel()
            assertFalse(vm.state.value.scanAreaOnly)
            assertTrue(vm.state.value.continuousScan)
        }

    @Test
    fun `history off still opens a result with a transient record`() =
        runTest {
            settings.value = AppSettings(saveHistory = false)
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(ScanEvent.CodesDetected(listOf(qr())))
                assertTrue(awaitItem() is ScanEffect.Feedback)
                val open = awaitItem() as ScanEffect.OpenResult
                assertTrue(open.recordId < 0)
            }
            assertTrue(history.persistedRecords.isEmpty())
        }

    @Test
    fun `gallery and settings buttons emit navigation effects`() =
        runTest {
            val vm = viewModel()
            vm.effects.test {
                vm.onEvent(ScanEvent.GalleryClicked)
                assertEquals(ScanEffect.OpenGallery, awaitItem())
                vm.onEvent(ScanEvent.SettingsClicked)
                assertEquals(ScanEffect.OpenSettings, awaitItem())
            }
        }
}

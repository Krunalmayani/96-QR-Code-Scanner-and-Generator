package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CheckDigitInfo
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.GradientPreset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ReliabilityReason
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleEdit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.VectorShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.SaveOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.ShareFile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.EditorTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedEffect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedMessage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class GeneratedViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension(StandardTestDispatcher())

    private val history = FakeHistoryRepository()
    private val exporter = FakeCodeExporter()
    private val logos = FakeLogoStore()
    private val analytics = mockk<Analytics>(relaxed = true)

    private fun record(
        content: ScanContent = ScanContent.Website("https://example.com", CodeFormat.QR_CODE, "https://example.com"),
        style: QrStyle? = QrStyle(),
    ): ScanRecord = history.seed(ScanRecord(content = content, createdAt = 1_000L, source = ScanSource.CREATED, style = style))

    private fun viewModel(id: Long, appScope: CoroutineScope = CoroutineScope(main.dispatcher)) = GeneratedViewModel(
        savedStateHandle = SavedStateHandle(mapOf(GeneratedViewModel.KEY_RECORD_ID to id)),
        history = history,
        toggleFavorite = ToggleFavoriteUseCase(history),
        exporter = exporter,
        logoStore = logos,
        analytics = analytics,
        appScope = appScope,
    )

    private fun GeneratedViewModel.ready(): GeneratedUiState {
        advanceUntilIdle()
        return state.value
    }

    private fun advanceUntilIdle() = main.dispatcher.scheduler.advanceUntilIdle()

    private fun barcodeRecord() = record(ScanContent.Product("5901234123457", CodeFormat.EAN_13, "5901234123457"))

    // ---- loading ----

    @Test
    fun `loads the record with its picture, reliability and size`() = runTest {
        val vm = viewModel(record().id)
        assertEquals(GeneratedStatus.LOADING, vm.state.value.status)
        val state = vm.ready()
        assertEquals(GeneratedStatus.READY, state.status)
        assertNotNull(state.image)
        assertTrue(state.reliability!!.isGood)
        assertEquals(1024, state.pixelSize!!.width)
        assertFalse(state.isBarcode)
        assertEquals(EditorTab.entries, state.tabs)
        assertNull(state.checkDigit)
    }

    @Test
    fun `an unknown id ends in not found`() = runTest {
        val vm = viewModel(999)
        assertEquals(GeneratedStatus.NOT_FOUND, vm.ready().status)
    }

    @Test
    fun `a stored style is restored`() = runTest {
        val style = QrStyle(foreground = 0xFF4F46E5, dotShape = DotShape.ROUNDED, frameText = "Scan me")
        val vm = viewModel(record(style = style).id)
        assertEquals(style, vm.ready().style)
        assertEquals(style.copy(), vm.state.value.style)
    }

    @Test
    fun `a record without a style starts with the default one`() = runTest {
        val vm = viewModel(record(style = null).id)
        assertEquals(QrStyle(), vm.ready().style)
    }

    @Test
    fun `barcodes have no shapes tab and show the check digit`() = runTest {
        val vm = viewModel(barcodeRecord().id)
        val state = vm.ready()
        assertTrue(state.isBarcode)
        assertEquals(listOf(EditorTab.COLORS, EditorTab.EXTRAS), state.tabs)
        assertEquals(CheckDigitInfo(7, true), state.checkDigit)
    }

    @Test
    fun `transient records work like stored ones`() = runTest {
        val transient = history.save(ScanRecord(content = ScanContent.Text("hi", CodeFormat.QR_CODE), createdAt = 1, source = ScanSource.CREATED), persist = false)
        assertTrue(transient.id < 0)
        val vm = viewModel(transient.id)
        assertEquals(GeneratedStatus.READY, vm.ready().status)
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFF0B7A6E)))
        advanceUntilIdle()
        assertEquals(0xFF0B7A6E, history.getById(transient.id)!!.style!!.foreground)
    }

    // ---- editing ----

    @Test
    fun `a style change is visible at once and persisted 300 ms later`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        val before = vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFF4F46E5)))
        val after = vm.state.value
        assertEquals(0xFF4F46E5, after.style.foreground)
        assertTrue(after.image != before.image)
        assertEquals(QrStyle(), history.getById(saved.id)!!.style)
        main.dispatcher.scheduler.advanceTimeBy(299)
        main.dispatcher.scheduler.runCurrent()
        assertEquals(QrStyle(), history.getById(saved.id)!!.style)
        main.dispatcher.scheduler.advanceTimeBy(1)
        main.dispatcher.scheduler.runCurrent()
        assertEquals(0xFF4F46E5, history.getById(saved.id)!!.style!!.foreground)
    }

    @Test
    fun `quick changes are written once with the last style`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFF4F46E5)))
        main.dispatcher.scheduler.advanceTimeBy(200)
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Dots(DotShape.DOTS)))
        main.dispatcher.scheduler.advanceTimeBy(200)
        main.dispatcher.scheduler.runCurrent()
        assertEquals(QrStyle(), history.getById(saved.id)!!.style, "the first change was superseded, nothing written yet")
        main.dispatcher.scheduler.advanceTimeBy(100)
        main.dispatcher.scheduler.runCurrent()
        val style = history.getById(saved.id)!!.style!!
        assertEquals(0xFF4F46E5, style.foreground)
        assertEquals(DotShape.DOTS, style.dotShape)
    }

    @Test
    fun `an edit that changes nothing writes nothing`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        val before = vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(QrStyle().foreground)))
        assertEquals(before, vm.state.value)
    }

    @Test
    fun `closing the editor writes the pending style right away`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        vm.ready()
        vm.onEvent(GeneratedEvent.OpenEditor(EditorTab.SHAPES))
        assertTrue(vm.state.value.editing)
        assertEquals(EditorTab.SHAPES, vm.state.value.tab)
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Eyes(EyeShape.LEAF)))
        vm.onEvent(GeneratedEvent.CloseEditor)
        advanceUntilIdle()
        assertFalse(vm.state.value.editing)
        assertEquals(EyeShape.LEAF, history.getById(saved.id)!!.style!!.eyeShape)
    }

    @Test
    fun `a pending style survives the screen being left`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Gradient(GradientPreset.OCEAN)))
        vm.flushPendingStyle()
        advanceUntilIdle()
        val style = history.getById(saved.id)!!.style!!
        assertEquals(GradientPreset.OCEAN.start, style.foreground)
        assertEquals(GradientPreset.OCEAN.end, style.gradientEnd)
    }

    @Test
    fun `tabs can be selected`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.onEvent(GeneratedEvent.SelectTab(EditorTab.EXTRAS))
        assertEquals(EditorTab.EXTRAS, vm.state.value.tab)
    }

    @Test
    fun `low contrast produces a warning with the ratio`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFFCCCCCC)))
        val reliability = vm.state.value.reliability!!
        assertTrue(reliability.isLow)
        assertTrue(ReliabilityReason.LOW_CONTRAST in reliability.reasons)
        assertTrue(reliability.contrastRatio < 3.0)
    }

    @Test
    fun `the export size follows the size and frame changes`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Size(2048)))
        assertEquals(2048, vm.state.value.pixelSize!!.width)
        assertEquals(2048, vm.state.value.pixelSize!!.height)
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Frame("Scan me")))
        assertTrue(vm.state.value.pixelSize!!.height > 2048)
    }

    @Test
    fun `barcode options re-render the picture`() = runTest {
        val vm = viewModel(barcodeRecord().id)
        val before = vm.ready().image!!
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.BarHeight(200)))
        assertTrue(vm.state.value.image!!.height > before.height)
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.ShowNumber(false)))
        assertTrue(vm.state.value.image!!.items.none { it.shape is VectorShape.Text })
    }

    // ---- logo ----

    @Test
    fun `a picked logo is copied into app storage and raises the correction`() = runTest {
        val saved = record(style = QrStyle(errorCorrection = ErrorCorrection.L))
        val vm = viewModel(saved.id)
        vm.ready()
        vm.onEvent(GeneratedEvent.LogoPicked("content://media/picker/1"))
        advanceUntilIdle()
        assertEquals(listOf("content://media/picker/1"), logos.imported)
        val style = vm.state.value.style
        assertEquals("file:///data/logos/logo_1.png", style.logoUri)
        assertEquals(ErrorCorrection.Q, style.errorCorrection)
        assertNotNull(vm.state.value.image!!.logoRect)
        assertTrue(logos.discarded.isEmpty())
    }

    @Test
    fun `replacing the logo discards the old file`() = runTest {
        val vm = viewModel(record(style = QrStyle(logoUri = "file:///data/logos/old.png", errorCorrection = ErrorCorrection.Q)).id)
        vm.ready()
        vm.onEvent(GeneratedEvent.LogoPicked("content://media/picker/2"))
        advanceUntilIdle()
        assertEquals(listOf<String?>("file:///data/logos/old.png"), logos.discarded)
    }

    @Test
    fun `a logo that cannot be read keeps the style and tells the user`() = runTest {
        logos.result = null
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.LogoPicked("content://media/picker/3"))
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.LOGO_FAILED), awaitItem())
        }
        assertNull(vm.state.value.style.logoUri)
    }

    @Test
    fun `removing the logo deletes the file and clears the style`() = runTest {
        val vm = viewModel(record(style = QrStyle(logoUri = "file:///data/logos/old.png", errorCorrection = ErrorCorrection.Q)).id)
        vm.ready()
        vm.onEvent(GeneratedEvent.RemoveLogo)
        advanceUntilIdle()
        assertNull(vm.state.value.style.logoUri)
        assertNull(vm.state.value.image!!.logoRect)
        assertEquals(listOf<String?>("file:///data/logos/old.png"), logos.discarded)
    }

    // ---- exporting ----

    @Test
    fun `save to gallery exports the live style and reports success`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFF4F46E5)))
        vm.effects.test {
            vm.onEvent(GeneratedEvent.SaveToGallery)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.SAVED_TO_GALLERY), awaitItem())
        }
        val (job, name) = exporter.saved.single()
        assertEquals("https://example.com", job.payload)
        assertEquals(CodeFormat.QR_CODE, job.format)
        assertEquals(0xFF4F46E5, job.style.foreground)
        assertTrue(Regex("qr-website-\\d{8}-\\d{6}\\.png").matches(name), name)
        verify { analytics.log(Events.SAVE_TO_GALLERY, mapOf(Params.FORMAT to "PNG")) }
        assertFalse(vm.state.value.working)
    }

    @Test
    fun `a pdf goes to downloads`() = runTest {
        exporter.saveOutcome = SaveOutcome.Saved(toDownloads = true)
        val vm = viewModel(record(style = QrStyle(exportFormat = ExportFormat.PDF)).id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.SaveToGallery)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.SAVED_TO_DOWNLOADS), awaitItem())
        }
        assertTrue(exporter.saved.single().second.endsWith(".pdf"))
    }

    @Test
    fun `a failed save is reported and not counted`() = runTest {
        exporter.saveOutcome = SaveOutcome.Failed
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.SaveToGallery)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.SAVE_FAILED), awaitItem())
        }
        verify(exactly = 0) { analytics.log(Events.SAVE_TO_GALLERY, any()) }
        assertFalse(vm.state.value.working)
    }

    @Test
    fun `old android asks for the storage permission first`() = runTest {
        exporter.permissionNeeded = true
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.SaveToGallery)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.RequestStoragePermission, awaitItem())
            assertTrue(exporter.saved.isEmpty())
            vm.onEvent(GeneratedEvent.StoragePermissionResult(granted = true))
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.SAVED_TO_GALLERY), awaitItem())
        }
        assertEquals(1, exporter.saved.size)
    }

    @Test
    fun `a refused permission saves nothing`() = runTest {
        exporter.permissionNeeded = true
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.StoragePermissionResult(granted = false))
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.PERMISSION_DENIED), awaitItem())
        }
        assertTrue(exporter.saved.isEmpty())
    }

    @Test
    fun `share hands the file to the screen and logs the tap`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.Share)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.ShareFile("content://test.fileprovider/shared/code.png", "image/png"), awaitItem())
        }
        verify { analytics.log(Events.SHARE_TAP, mapOf(Params.FORMAT to "PNG")) }
    }

    @Test
    fun `a share file that could not be made is reported`() = runTest {
        exporter.shareFile = null
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.Share)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.Message(GeneratedMessage.SHARE_FAILED), awaitItem())
        }
        verify(exactly = 0) { analytics.log(Events.SHARE_TAP, any()) }
    }

    @Test
    fun `sharing uses the chosen format`() = runTest {
        exporter.shareFile = ShareFile("content://x/code.svg", "image/svg+xml")
        val vm = viewModel(record(style = QrStyle(exportFormat = ExportFormat.SVG)).id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Share)
        advanceUntilIdle()
        assertTrue(exporter.shared.single().second.endsWith(".svg"))
    }

    @Test
    fun `barcode exports are named after the symbology`() = runTest {
        val vm = viewModel(barcodeRecord().id)
        vm.ready()
        vm.onEvent(GeneratedEvent.SaveToGallery)
        advanceUntilIdle()
        assertTrue(Regex("barcode-ean-13-\\d{8}-\\d{6}\\.png").matches(exporter.saved.single().second))
        assertEquals(CodeFormat.EAN_13, exporter.saved.single().first.format)
    }

    // ---- copy and favorite ----

    @Test
    fun `copy sends the raw content and logs the type only`() = runTest {
        val vm = viewModel(record().id)
        vm.ready()
        vm.effects.test {
            vm.onEvent(GeneratedEvent.Copy)
            advanceUntilIdle()
            assertEquals(GeneratedEffect.CopyText("https://example.com"), awaitItem())
        }
        verify { analytics.log(Events.COPY_TAP, mapOf(Params.TYPE to "WEBSITE")) }
    }

    @Test
    fun `favorite toggles in both directions`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        vm.ready()
        assertFalse(vm.state.value.isFavorite)
        vm.onEvent(GeneratedEvent.ToggleFavorite)
        advanceUntilIdle()
        assertTrue(vm.state.value.isFavorite)
        assertTrue(history.getById(saved.id)!!.isFavorite)
        vm.onEvent(GeneratedEvent.ToggleFavorite)
        advanceUntilIdle()
        assertFalse(vm.state.value.isFavorite)
    }

    @Test
    fun `an edit does not overwrite the live style when the stored row changes`() = runTest {
        val saved = record()
        val vm = viewModel(saved.id)
        vm.ready()
        vm.onEvent(GeneratedEvent.Edit(StyleEdit.Foreground(0xFF4F46E5)))
        vm.onEvent(GeneratedEvent.ToggleFavorite)
        advanceUntilIdle()
        assertEquals(0xFF4F46E5, vm.state.value.style.foreground)
    }
}

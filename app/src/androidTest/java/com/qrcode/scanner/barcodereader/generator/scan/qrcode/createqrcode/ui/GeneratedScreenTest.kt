package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.rememberToastState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ContrastChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.EditorTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.rememberTestSnackbarHost
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Generated screen (design s40, s44) and its Colors / Shapes / Extras editor tabs (s41 to s43). */
class GeneratedScreenTest : ThemedComposeTest() {
    private val events = mutableListOf<GeneratedEvent>()
    private var backs = 0

    private fun qrState(
        editing: Boolean = false,
        tab: EditorTab = EditorTab.COLORS,
    ): GeneratedUiState = readyState(CodeFormat.QR_CODE, "https://www.brewhouse-coffee.com/menu", editing, tab)

    private fun barcodeState(editing: Boolean = false): GeneratedUiState = readyState(CodeFormat.EAN_13, "5901234123457", editing)

    private fun readyState(
        format: CodeFormat,
        payload: String,
        editing: Boolean,
        tab: EditorTab = EditorTab.COLORS,
    ): GeneratedUiState {
        val content =
            if (format == CodeFormat.QR_CODE) {
                ScanContent.Website(payload, format, payload)
            } else {
                ScanContent.Product(payload, format, payload)
            }
        val style = QrStyle()
        return GeneratedUiState(
            status = GeneratedStatus.READY,
            record = ScanRecord(id = 1, content = content, createdAt = 0L, source = ScanSource.CREATED, style = style),
            style = style,
            image = CodeImageFactory(format, payload).imageOrNull(style),
            reliability = ContrastChecker.scanReliability(style),
            editing = editing,
            tab = tab,
        )
    }

    @Composable
    private fun Host(initial: GeneratedUiState) {
        var state by remember { mutableStateOf(initial) }
        GeneratedScreen(
            state = state,
            onEvent = { event ->
                events += event
                state =
                    when (event) {
                        is GeneratedEvent.OpenEditor -> state.copy(editing = true, tab = event.tab)
                        GeneratedEvent.CloseEditor -> state.copy(editing = false)
                        is GeneratedEvent.SelectTab -> state.copy(tab = event.tab)
                        GeneratedEvent.ToggleFavorite -> state.copy(isFavorite = !state.isFavorite)
                        else -> state
                    }
            },
            onBack = { backs++ },
            onPickLogo = {},
            toastState = rememberToastState(),
            snackbarHostState = rememberTestSnackbarHost(),
        )
    }

    private fun show(state: GeneratedUiState) {
        setThemedContent { Host(state) }
    }

    private fun tab(tab: EditorTab): String =
        Res.string(
            when (tab) {
                EditorTab.COLORS -> R.string.create_tab_colors
                EditorTab.SHAPES -> R.string.create_tab_shapes
                EditorTab.EXTRAS -> R.string.create_tab_extras
            },
        )

    @Test
    fun generatedQrShowsItsActionsAndReportsEachTap() {
        show(qrState())

        composeRule.onNodeWithText(Res.string(R.string.create_generated_title_qr)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.common_copy)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.common_share)).performClick()
        composeRule.onNodeWithText(Res.string(R.string.create_save_to_gallery)).performClick()

        assertEquals(listOf(GeneratedEvent.Copy, GeneratedEvent.Share, GeneratedEvent.SaveToGallery), events)
    }

    @Test
    fun favoriteStarTogglesBetweenFavoriteAndUnfavorite() {
        show(qrState())

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_favorite)).performClick()

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_unfavorite)).assertIsDisplayed()
        assertEquals(listOf<GeneratedEvent>(GeneratedEvent.ToggleFavorite), events)
    }

    @Test
    fun customizeOpensTheEditorOnTheColorsTab() {
        show(qrState())

        composeRule.onNodeWithText(Res.string(R.string.create_action_customize)).performClick()

        composeRule.onNodeWithText(Res.string(R.string.create_editor_title)).assertIsDisplayed()
        composeRule.onNodeWithText(tab(EditorTab.COLORS)).assertIsSelected()
        composeRule.onNodeWithText(Res.string(R.string.create_colors_code), ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun editorTabsSwitchBetweenColorsShapesAndExtras() {
        show(qrState(editing = true))

        composeRule.onNodeWithText(tab(EditorTab.SHAPES)).performClick()
        composeRule.onNodeWithText(tab(EditorTab.SHAPES)).assertIsSelected()
        composeRule.onNodeWithText(Res.string(R.string.create_shapes_pattern), ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.create_colors_code), ignoreCase = true).assertDoesNotExist()

        composeRule.onNodeWithText(tab(EditorTab.EXTRAS)).performClick()
        composeRule.onNodeWithText(tab(EditorTab.EXTRAS)).assertIsSelected()
        composeRule.onNodeWithText(Res.string(R.string.create_extras_logo_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.create_shapes_pattern), ignoreCase = true).assertDoesNotExist()

        composeRule.onNodeWithText(tab(EditorTab.COLORS)).performClick()
        composeRule.onNodeWithText(tab(EditorTab.COLORS)).assertIsSelected()
        composeRule.onNodeWithText(Res.string(R.string.create_colors_code), ignoreCase = true).assertIsDisplayed()

        assertEquals(
            listOf<GeneratedEvent>(
                GeneratedEvent.SelectTab(EditorTab.SHAPES),
                GeneratedEvent.SelectTab(EditorTab.EXTRAS),
                GeneratedEvent.SelectTab(EditorTab.COLORS),
            ),
            events,
        )
    }

    @Test
    fun barcodeEditorHasNoShapesTab() {
        show(barcodeState(editing = true))

        composeRule.onNodeWithText(tab(EditorTab.COLORS)).assertIsDisplayed()
        composeRule.onNodeWithText(tab(EditorTab.EXTRAS)).assertIsDisplayed()
        composeRule.onNodeWithText(tab(EditorTab.SHAPES)).assertDoesNotExist()
    }

    @Test
    fun doneClosesTheEditorAndShowsTheGeneratedCodeAgain() {
        show(qrState(editing = true))

        composeRule.onNodeWithText(Res.string(R.string.common_done)).performClick()

        composeRule.onNodeWithText(Res.string(R.string.create_save_to_gallery)).assertIsDisplayed()
        assertEquals(listOf<GeneratedEvent>(GeneratedEvent.CloseEditor), events)
    }

    @Test
    fun notFoundStateOffersBack() {
        show(GeneratedUiState(status = GeneratedStatus.NOT_FOUND))

        composeRule.onNodeWithText(Res.string(R.string.create_not_found_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.common_back)).performClick()

        assertEquals(1, backs)
    }
}

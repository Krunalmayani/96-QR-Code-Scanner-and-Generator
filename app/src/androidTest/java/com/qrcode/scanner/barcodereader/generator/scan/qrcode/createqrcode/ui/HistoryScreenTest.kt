package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryGrouper
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SelectionState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history.HistoryEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history.HistoryScreen
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history.HistoryUiState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.Res
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.ThemedComposeTest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil.rememberTestSnackbarHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/** History list rows, multi-select bar and every empty state, driven with sample [HistoryUiState]s. */
class HistoryScreenTest : ThemedComposeTest() {
    private val events = mutableListOf<HistoryEvent>()
    private var startScanning = 0
    private var createCode = 0
    private var exportOpened = 0

    private val notes = record(1, "Meeting notes: ship v2.4 on Friday")
    private val shopping = record(2, "Shopping list: milk, eggs")

    private fun record(
        id: Long,
        text: String,
        favorite: Boolean = false,
        source: ScanSource = ScanSource.CAMERA,
    ) = ScanRecord(
        id = id,
        content = ScanContent.Text(text, CodeFormat.QR_CODE),
        createdAt = System.currentTimeMillis() - id * 60_000L,
        source = source,
        isFavorite = favorite,
    )

    private fun listState(
        vararg records: ScanRecord,
        tab: HistoryTab = HistoryTab.SCANNED,
        selection: SelectionState = SelectionState(),
    ): HistoryUiState =
        HistoryUiState(
            tab = tab,
            groups = HistoryGrouper.group(records.toList(), Instant.now(), ZoneId.systemDefault()),
            recordCount = records.size,
            loading = false,
            selection = selection,
        )

    private fun emptyState(
        kind: HistoryEmptyKind,
        tab: HistoryTab = HistoryTab.SCANNED,
        chip: HistoryChip = HistoryChip.ALL,
        searchActive: Boolean = false,
        query: String = "",
    ): HistoryUiState =
        HistoryUiState(
            tab = tab,
            chip = chip,
            searchActive = searchActive,
            query = query,
            appliedQuery = query,
            recordCount = 0,
            loading = false,
            emptyKind = kind,
        )

    private fun show(state: HistoryUiState) {
        setThemedContent {
            CompositionLocalProvider(LocalBottomBarInset provides 80.dp) {
                HistoryScreen(
                    state = state,
                    snackbarHostState = rememberTestSnackbarHost(),
                    onEvent = { events += it },
                    onStartScanning = { startScanning++ },
                    onCreateCode = { createCode++ },
                    onOpenExportHistory = { exportOpened++ },
                )
            }
        }
    }

    @Test
    fun rowsShowTheirTitles() {
        show(listState(notes, shopping))

        composeRule.onNodeWithText(notes.displayTitle).assertIsDisplayed()
        composeRule.onNodeWithText(shopping.displayTitle).assertIsDisplayed()
    }

    @Test
    fun tappingARowReportsRowClicked() {
        show(listState(notes, shopping))

        composeRule.onNodeWithText(shopping.displayTitle).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.RowClicked(shopping.id)), events)
    }

    @Test
    fun longPressingARowReportsRowLongClicked() {
        show(listState(notes, shopping))

        composeRule.onNodeWithText(notes.displayTitle).performTouchInput { longClick() }

        assertTrue(events.contains(HistoryEvent.RowLongClicked(notes.id)))
    }

    @Test
    fun swipingARowToTheLeftReportsSwipeDeleted() {
        show(listState(notes, shopping))

        composeRule.onNodeWithText(notes.displayTitle).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertTrue(events.contains(HistoryEvent.SwipeDeleted(notes.id)))
    }

    @Test
    fun tappingTheStarReportsStarClicked() {
        show(listState(notes))

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_favorite)).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.StarClicked(notes.id)), events)
    }

    @Test
    fun favoriteRowOffersToUnfavorite() {
        show(listState(record(3, "Pinned note", favorite = true)))

        composeRule.onNodeWithContentDescription(Res.string(R.string.common_unfavorite)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(Res.string(R.string.common_favorite)).assertDoesNotExist()
    }

    @Test
    fun tabSwitcherReportsTheTappedTab() {
        show(listState(notes))

        composeRule.onNodeWithText(Res.string(R.string.hist_tab_scanned)).assertIsSelected()
        composeRule.onNodeWithText(Res.string(R.string.hist_tab_created)).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.SelectTab(HistoryTab.CREATED)), events)
    }

    @Test
    fun filterChipReportsTheTappedChip() {
        show(listState(notes))

        composeRule.onNodeWithText(Res.string(R.string.hist_chip_links)).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.SelectChip(HistoryChip.LINKS)), events)
    }

    @Test
    fun selectionBarShowsTheCountAndItsActionsReportEvents() {
        show(listState(notes, shopping, selection = SelectionState(active = true, ids = setOf(notes.id))))

        composeRule.onNodeWithText(Res.plural(R.plurals.hist_selected_count, 1, 1)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(Res.string(R.string.hist_cd_select_all)).performClick()
        composeRule.onNodeWithContentDescription(Res.string(R.string.hist_cd_favorite_selected)).performClick()
        composeRule.onAllNodesWithContentDescription(Res.string(R.string.common_delete)).onFirst().performClick()
        composeRule.onNodeWithContentDescription(Res.string(R.string.hist_cd_exit_selection)).performClick()

        assertEquals(
            listOf(
                HistoryEvent.ToggleSelectAll,
                HistoryEvent.FavoriteSelected,
                HistoryEvent.DeleteSelected,
                HistoryEvent.ExitSelection,
            ),
            events,
        )
    }

    @Test
    fun tappingARowWhileSelectingStillReportsRowClicked() {
        show(listState(notes, shopping, selection = SelectionState(active = true, ids = setOf(notes.id))))

        composeRule.onNodeWithText(shopping.displayTitle).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.RowClicked(shopping.id)), events)
    }

    @Test
    fun emptyScannedTabOffersToStartScanning() {
        show(emptyState(HistoryEmptyKind.SCANNED))

        composeRule.onNodeWithText(Res.string(R.string.hist_empty_scanned_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_scanned_message)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_scanned_action)).performClick()

        assertEquals(1, startScanning)
        assertEquals(0, createCode)
    }

    @Test
    fun emptyCreatedTabOffersToCreateACode() {
        show(emptyState(HistoryEmptyKind.CREATED, tab = HistoryTab.CREATED))

        composeRule.onNodeWithText(Res.string(R.string.hist_empty_created_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_created_action)).performClick()

        assertEquals(1, createCode)
        assertEquals(0, startScanning)
    }

    @Test
    fun emptyFavoritesOffersToBrowseEverything() {
        show(emptyState(HistoryEmptyKind.FAVORITES, chip = HistoryChip.FAVORITES))

        composeRule.onNodeWithText(Res.string(R.string.hist_empty_favorites_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_favorites_action)).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.BrowseAll), events)
    }

    @Test
    fun emptyTypeFilterOffersToShowEverything() {
        show(emptyState(HistoryEmptyKind.FILTER, chip = HistoryChip.WIFI))

        composeRule.onNodeWithText(Res.string(R.string.hist_empty_filter_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_filter_action)).performClick()

        assertEquals(listOf<HistoryEvent>(HistoryEvent.BrowseAll), events)
    }

    @Test
    fun emptySearchEchoesTheQueryAndHasNoAction() {
        show(emptyState(HistoryEmptyKind.SEARCH, searchActive = true, query = "zebra"))

        composeRule.onNodeWithText(Res.string(R.string.hist_empty_search_title)).assertIsDisplayed()
        composeRule.onNodeWithText(Res.string(R.string.hist_empty_search_message, "zebra")).assertIsDisplayed()
    }
}

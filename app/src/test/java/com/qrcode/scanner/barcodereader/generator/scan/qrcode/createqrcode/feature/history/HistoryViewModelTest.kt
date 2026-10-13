package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.DeleteRecordsUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val now: Instant = Instant.parse("2024-05-10T12:00:00Z")
    private val repo = FakeHistoryRepository()
    private val analytics = mockk<Analytics>(relaxed = true)

    private fun viewModel() =
        HistoryViewModel(
            history = repo,
            toggleFavorite = ToggleFavoriteUseCase(repo),
            deleteRecords = DeleteRecordsUseCase(repo),
            analytics = analytics,
            nowProvider = { now },
            zoneProvider = { ZoneId.of("UTC") },
        )

    private fun seed(
        content: ScanContent = Fixtures.text(),
        source: ScanSource = ScanSource.CAMERA,
        at: String = "2024-05-10T10:00:00Z",
        favorite: Boolean = false,
    ): ScanRecord = repo.seed(Fixtures.record(content, source, createdAt = Instant.parse(at).toEpochMilli(), isFavorite = favorite))

    private fun HistoryUiState.raws(): List<String> = groups.flatMap { it.records }.map { it.rawValue }

    private suspend fun ReceiveTurbine<HistoryUiState>.awaitMatch(predicate: (HistoryUiState) -> Boolean): HistoryUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Test
    fun `scanned tab hides created rows and created tab shows only them`() =
        runTest {
            seed(Fixtures.text("camera"), ScanSource.CAMERA)
            seed(Fixtures.text("image"), ScanSource.IMAGE, "2024-05-10T09:00:00Z")
            seed(Fixtures.text("made"), ScanSource.CREATED, "2024-05-10T08:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                val scanned = awaitMatch { !it.loading }
                assertEquals(listOf("camera", "image"), scanned.raws())
                assertEquals(HistoryTab.SCANNED, scanned.tab)

                vm.onEvent(HistoryEvent.SelectTab(HistoryTab.CREATED))
                val created = awaitMatch { it.tab == HistoryTab.CREATED && it.raws() == listOf("made") }
                assertEquals(1, created.recordCount)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `rows are grouped by day with today and yesterday labels`() =
        runTest {
            seed(Fixtures.text("t"), at = "2024-05-10T11:00:00Z")
            seed(Fixtures.text("y"), at = "2024-05-09T11:00:00Z")
            seed(Fixtures.text("old"), at = "2024-05-01T11:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                val state = awaitMatch { it.recordCount == 3 }
                assertEquals(listOf(DayLabel.Today, DayLabel.Yesterday), state.groups.map { it.label }.take(2))
                assertTrue(state.groups.last().label is DayLabel.Date)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `type chips and favorites chip filter the list`() =
        runTest {
            seed(Fixtures.website("https://a.com"), favorite = true)
            seed(Fixtures.text("note"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.SelectChip(HistoryChip.LINKS))
                assertEquals(listOf("https://a.com"), awaitMatch { it.chip == HistoryChip.LINKS && it.recordCount == 1 }.raws())
                vm.onEvent(HistoryEvent.SelectChip(HistoryChip.WIFI))
                val wifi = awaitMatch { it.chip == HistoryChip.WIFI && it.recordCount == 0 }
                assertEquals(HistoryEmptyKind.FILTER, wifi.emptyKind)
                vm.onEvent(HistoryEvent.SelectChip(HistoryChip.FAVORITES))
                assertEquals(listOf("https://a.com"), awaitMatch { it.chip == HistoryChip.FAVORITES && it.recordCount == 1 }.raws())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `empty tabs report the matching empty state`() =
        runTest {
            val vm = viewModel()
            vm.uiState.test {
                val scanned = awaitMatch { !it.loading }
                assertTrue(scanned.isEmpty)
                assertEquals(HistoryEmptyKind.SCANNED, scanned.emptyKind)
                vm.onEvent(HistoryEvent.SelectTab(HistoryTab.CREATED))
                assertEquals(
                    HistoryEmptyKind.CREATED,
                    awaitMatch { it.tab == HistoryTab.CREATED && it.emptyKind == HistoryEmptyKind.CREATED }.emptyKind,
                )
                vm.onEvent(HistoryEvent.SelectChip(HistoryChip.FAVORITES))
                assertEquals(HistoryEmptyKind.FAVORITES, awaitMatch { it.emptyKind == HistoryEmptyKind.FAVORITES }.emptyKind)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `search filters only after the 250 ms debounce and clearing is immediate`() =
        runTest {
            seed(Fixtures.text("cafe nova"))
            seed(Fixtures.text("other"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.OpenSearch)
                awaitMatch { it.searchActive && it.recordCount == 2 }

                vm.onEvent(HistoryEvent.QueryChanged("c"))
                vm.onEvent(HistoryEvent.QueryChanged("ca"))
                vm.onEvent(HistoryEvent.QueryChanged("cafe"))
                advanceTimeBy(249)
                runCurrent()
                val typing = expectMostRecentItem()
                assertEquals("cafe", typing.query)
                assertEquals("", typing.appliedQuery)
                assertEquals(2, typing.recordCount)

                advanceTimeBy(2)
                runCurrent()
                val done = awaitMatch { it.appliedQuery == "cafe" && it.recordCount == 1 }
                assertEquals(listOf("cafe nova"), done.raws())

                vm.onEvent(HistoryEvent.QueryChanged(""))
                runCurrent()
                val cleared = awaitMatch { it.query.isEmpty() && it.appliedQuery.isEmpty() && it.recordCount == 2 }
                assertEquals(2, cleared.recordCount)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `search with no hits shows the search empty state and closing search restores browsing`() =
        runTest {
            seed(Fixtures.text("alpha"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.onEvent(HistoryEvent.OpenSearch)
                vm.onEvent(HistoryEvent.QueryChanged("zzz"))
                advanceTimeBy(300)
                runCurrent()
                val none = awaitMatch { it.appliedQuery == "zzz" && it.recordCount == 0 }
                assertEquals(HistoryEmptyKind.SEARCH, none.emptyKind)

                vm.onEvent(HistoryEvent.CloseSearch)
                runCurrent()
                val browse = awaitMatch { !it.searchActive && it.query.isEmpty() && it.recordCount == 1 }
                assertEquals(HistoryEmptyKind.SCANNED, browse.emptyKind)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `search scopes cover scanned created and favorites`() =
        runTest {
            seed(Fixtures.text("s-plain"), ScanSource.CAMERA)
            seed(Fixtures.text("c-plain"), ScanSource.CREATED, "2024-05-10T09:00:00Z")
            seed(Fixtures.text("s-fav"), ScanSource.IMAGE, "2024-05-10T08:00:00Z", favorite = true)
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.OpenSearch)
                awaitMatch { it.searchActive && it.recordCount == 3 }
                vm.onEvent(HistoryEvent.SelectScope(SearchScope.CREATED))
                assertEquals(listOf("c-plain"), awaitMatch { it.searchScope == SearchScope.CREATED && it.recordCount == 1 }.raws())
                vm.onEvent(HistoryEvent.SelectScope(SearchScope.SCANNED))
                assertEquals(2, awaitMatch { it.searchScope == SearchScope.SCANNED && it.recordCount == 2 }.recordCount)
                vm.onEvent(HistoryEvent.SelectScope(SearchScope.FAVORITES))
                assertEquals(listOf("s-fav"), awaitMatch { it.searchScope == SearchScope.FAVORITES && it.recordCount == 1 }.raws())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `tapping a row opens it, or toggles it while selecting`() =
        runTest {
            val a = seed(Fixtures.text("a"))
            val b = seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.RowClicked(a.id))
                    assertEquals(HistoryEffect.OpenRecord(a.id), awaitItem())
                }
                vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                assertEquals(setOf(a.id), awaitMatch { it.selection.active }.selection.ids)
                vm.onEvent(HistoryEvent.RowClicked(b.id))
                assertEquals(setOf(a.id, b.id), awaitMatch { it.selection.count == 2 }.selection.ids)
                vm.onEvent(HistoryEvent.RowClicked(a.id))
                assertEquals(setOf(b.id), awaitMatch { it.selection.count == 1 }.selection.ids)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `select all toggles every visible row and exit clears the selection`() =
        runTest {
            seed(Fixtures.text("a"))
            seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.BeginSelection)
                awaitMatch { it.selection.active }
                vm.onEvent(HistoryEvent.ToggleSelectAll)
                assertEquals(2, awaitMatch { it.selection.count == 2 }.selection.count)
                vm.onEvent(HistoryEvent.ToggleSelectAll)
                assertEquals(0, awaitMatch { it.selection.active && it.selection.count == 0 }.selection.count)
                vm.onEvent(HistoryEvent.ExitSelection)
                assertFalse(awaitMatch { !it.selection.active }.selection.active)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `changing tab leaves selection mode`() =
        runTest {
            val a = seed(Fixtures.text("a"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                awaitMatch { it.selection.active }
                vm.onEvent(HistoryEvent.SelectTab(HistoryTab.CREATED))
                assertFalse(awaitMatch { it.tab == HistoryTab.CREATED }.selection.active)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `star toggles favorite and logs only the type`() =
        runTest {
            val site = seed(Fixtures.website("https://a.com"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.onEvent(HistoryEvent.StarClicked(site.id))
                awaitMatch { state -> state.groups.flatMap { it.records }.all { it.isFavorite } }
                verify(exactly = 1) { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to "WEBSITE")) }
                vm.onEvent(HistoryEvent.StarClicked(site.id))
                awaitMatch { state -> state.groups.flatMap { it.records }.none { it.isFavorite } }
                verify(exactly = 2) { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to "WEBSITE")) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `swipe delete removes the row and undo brings it back`() =
        runTest {
            val a = seed(Fixtures.text("a"))
            seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.SwipeDeleted(a.id))
                    val deleted = awaitItem() as HistoryEffect.RecordsDeleted
                    assertEquals(listOf(a), deleted.records)
                    awaitMatch { it.raws() == listOf("b") }
                    vm.onEvent(HistoryEvent.UndoDelete(deleted.records))
                    awaitMatch { it.recordCount == 2 }
                    assertEquals(a, repo.getById(a.id))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleting a missing row emits no undo effect`() =
        runTest {
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { !it.loading }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.SwipeDeleted(999))
                    runCurrent()
                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `delete selected removes all selected rows, leaves selection mode and offers undo`() =
        runTest {
            val a = seed(Fixtures.text("a"))
            val b = seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            seed(Fixtures.text("c"), at = "2024-05-10T08:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 3 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                    vm.onEvent(HistoryEvent.RowClicked(b.id))
                    vm.onEvent(HistoryEvent.DeleteSelected)
                    val deleted = awaitItem() as HistoryEffect.RecordsDeleted
                    assertEquals(setOf(a.id, b.id), deleted.records.map { it.id }.toSet())
                }
                val state = awaitMatch { it.recordCount == 1 && !it.selection.active }
                assertEquals(listOf("c"), state.raws())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `favorite selected favorites a mixed selection then unfavorites a full one`() =
        runTest {
            val a = seed(Fixtures.website("https://a.com"), favorite = true)
            val b = seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                vm.onEvent(HistoryEvent.RowClicked(b.id))
                vm.onEvent(HistoryEvent.FavoriteSelected)
                awaitMatch { state -> !state.selection.active && state.groups.flatMap { it.records }.all { it.isFavorite } }
                verify { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to "WEBSITE")) }
                verify { analytics.log(Events.FAVORITE_TOGGLE, mapOf(Params.TYPE to "TEXT")) }

                vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                vm.onEvent(HistoryEvent.RowClicked(b.id))
                vm.onEvent(HistoryEvent.FavoriteSelected)
                awaitMatch { state -> !state.selection.active && state.groups.flatMap { it.records }.none { it.isFavorite } }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `share selected sends the raw values newest first as plain text`() =
        runTest {
            val older = seed(Fixtures.text("older"), at = "2024-05-10T08:00:00Z")
            val newer = seed(Fixtures.website("https://new.com"), at = "2024-05-10T10:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.RowLongClicked(older.id))
                    vm.onEvent(HistoryEvent.RowClicked(newer.id))
                    vm.onEvent(HistoryEvent.ShareSelected)
                    assertEquals(HistoryEffect.ShareText("https://new.com\nolder"), awaitItem())
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `export selected builds a csv with a header and one line per row`() =
        runTest {
            val a = seed(Fixtures.text("hello, world"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                    vm.onEvent(HistoryEvent.ExportSelected)
                    val csv = (awaitItem() as HistoryEffect.ExportCsv).csv
                    val lines = csv.trimEnd().split("\r\n")
                    assertEquals("id,date,source,type,format,title,content,favorite,note", lines.first())
                    assertEquals(2, lines.size)
                    assertTrue(lines[1].contains("\"hello, world\""))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `share and export with nothing selected do nothing`() =
        runTest {
            seed(Fixtures.text("a"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.effect.test {
                    vm.onEvent(HistoryEvent.BeginSelection)
                    vm.onEvent(HistoryEvent.ShareSelected)
                    vm.onEvent(HistoryEvent.ExportSelected)
                    runCurrent()
                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `browse all clears the favorites chip or the search scope`() =
        runTest {
            seed(Fixtures.text("a"))
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 1 }
                vm.onEvent(HistoryEvent.SelectChip(HistoryChip.FAVORITES))
                awaitMatch { it.chip == HistoryChip.FAVORITES && it.recordCount == 0 }
                vm.onEvent(HistoryEvent.BrowseAll)
                assertEquals(1, awaitMatch { it.chip == HistoryChip.ALL && it.recordCount == 1 }.recordCount)
                vm.onEvent(HistoryEvent.OpenSearch)
                vm.onEvent(HistoryEvent.SelectScope(SearchScope.FAVORITES))
                awaitMatch { it.searchScope == SearchScope.FAVORITES && it.recordCount == 0 }
                vm.onEvent(HistoryEvent.BrowseAll)
                assertEquals(1, awaitMatch { it.searchScope == SearchScope.ALL && it.recordCount == 1 }.recordCount)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `rows deleted elsewhere drop out of the selection`() =
        runTest {
            val a = seed(Fixtures.text("a"))
            val b = seed(Fixtures.text("b"), at = "2024-05-10T09:00:00Z")
            val vm = viewModel()
            vm.uiState.test {
                awaitMatch { it.recordCount == 2 }
                vm.onEvent(HistoryEvent.RowLongClicked(a.id))
                vm.onEvent(HistoryEvent.RowClicked(b.id))
                awaitMatch { it.selection.count == 2 }
                repo.delete(setOf(a.id))
                val state = awaitMatch { it.recordCount == 1 }
                assertEquals(setOf(b.id), state.selection.ids)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

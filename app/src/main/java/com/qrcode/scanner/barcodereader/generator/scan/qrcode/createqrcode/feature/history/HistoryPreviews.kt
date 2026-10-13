package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryGrouper
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SelectionState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import java.time.Instant
import java.time.ZoneId

// Previews use sample data only (never a ViewModel). Artboard numbers refer to the Design artifact.

private val zone: ZoneId = ZoneId.systemDefault()

private fun at(daysAgo: Long, hour: Int, minute: Int): Long =
    Instant
        .now()
        .atZone(zone)
        .toLocalDate()
        .minusDays(daysAgo)
        .atTime(hour, minute)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

private fun sample(
    id: Long,
    content: ScanContent,
    createdAt: Long,
    source: ScanSource = ScanSource.CAMERA,
    favorite: Boolean = false,
) = ScanRecord(id = id, content = content, createdAt = createdAt, source = source, isFavorite = favorite)

private val QR = CodeFormat.QR_CODE

private val scannedSamples: List<ScanRecord> =
    listOf(
        sample(
            1,
            ScanContent.Website("https://brewhouse-coffee.com/menu", QR, "https://brewhouse-coffee.com/menu"),
            at(0, 16, 36),
            favorite = true,
        ),
        sample(2, ScanContent.Wifi("WIFI:S:CafeNova_Guest;;", QR, "CafeNova_Guest"), at(0, 13, 5)),
        sample(3, ScanContent.Contact("BEGIN:VCARD", QR, name = "Sophie Lambert"), at(0, 11, 42)),
        sample(4, ScanContent.Product("5901234123457", CodeFormat.EAN_13, "5901234123457"), at(1, 17, 18)),
        sample(5, ScanContent.Event("BEGIN:VEVENT", QR, summary = "Product Launch Meetup"), at(1, 14, 20), favorite = true),
        sample(6, ScanContent.Text("Meeting notes: ship v2.4 on Friday", QR), at(1, 9, 3)),
    )

private val createdSamples: List<ScanRecord> =
    listOf(
        sample(
            11,
            ScanContent.Website("https://brewhouse-coffee.com/menu", QR, "https://brewhouse-coffee.com/menu"),
            at(0, 10, 0),
            ScanSource.CREATED,
            favorite = true,
        ),
        sample(12, ScanContent.Wifi("WIFI:S:CafeNova;;", QR, "Guest Wi-Fi · CafeNova"), at(1, 10, 0), ScanSource.CREATED),
        sample(13, ScanContent.Contact("BEGIN:VCARD", QR, name = "Lumen Studio contact"), at(3, 10, 0), ScanSource.CREATED),
        sample(14, ScanContent.Phone("tel:+1555", QR, "+1 555 0100"), at(7, 10, 0), ScanSource.CREATED),
    )

private fun stateOf(
    records: List<ScanRecord>,
    tab: HistoryTab = HistoryTab.SCANNED,
    chip: HistoryChip = HistoryChip.ALL,
    emptyKind: HistoryEmptyKind = if (tab == HistoryTab.CREATED) HistoryEmptyKind.CREATED else HistoryEmptyKind.SCANNED,
    searchActive: Boolean = false,
    query: String = "",
    scope: SearchScope = SearchScope.ALL,
    selection: SelectionState = SelectionState(),
): HistoryUiState =
    HistoryUiState(
        tab = tab,
        chip = chip,
        searchActive = searchActive,
        searchScope = scope,
        query = query,
        appliedQuery = query,
        groups = HistoryGrouper.group(records, Instant.now(), zone),
        recordCount = records.size,
        loading = false,
        emptyKind = emptyKind,
        selection = selection,
    )

@Composable
private fun PreviewFrame(dark: Boolean, state: HistoryUiState, undoMessage: String? = null) {
    AppTheme(dark = dark) {
        val host = remember { SnackbarHostState() }
        if (undoMessage != null) {
            LaunchedEffect(undoMessage) { host.showSnackbar(message = undoMessage, actionLabel = "Undo") }
        }
        CompositionLocalProvider(LocalBottomBarInset provides 80.dp) {
            Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
                HistoryScreen(
                    state = state,
                    snackbarHostState = host,
                    onEvent = {},
                    onStartScanning = {},
                    onCreateCode = {},
                    onOpenExportHistory = {},
                )
            }
        }
    }
}

private const val PHONE_W = 360
private const val PHONE_H = 760

// s45 Scanned list
@Preview(name = "S45_HistoryScanned_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S45_HistoryScanned_Light() = PreviewFrame(false, stateOf(scannedSamples))

@Preview(name = "S45_HistoryScanned_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S45_HistoryScanned_Dark() = PreviewFrame(true, stateOf(scannedSamples))

// s46 Created tab
@Preview(name = "S46_HistoryCreated_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S46_HistoryCreated_Light() = PreviewFrame(false, stateOf(createdSamples, tab = HistoryTab.CREATED))

@Preview(name = "S46_HistoryCreated_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S46_HistoryCreated_Dark() = PreviewFrame(true, stateOf(createdSamples, tab = HistoryTab.CREATED))

// s47 Multi-select
private val multiSelect = stateOf(scannedSamples, selection = SelectionState(active = true, ids = setOf(1L, 2L, 4L)))

@Preview(name = "S47_HistoryMultiSelect_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S47_HistoryMultiSelect_Light() = PreviewFrame(false, multiSelect)

@Preview(name = "S47_HistoryMultiSelect_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S47_HistoryMultiSelect_Dark() = PreviewFrame(true, multiSelect)

// s48 Favorites filter
private val favoritesFilter = stateOf(scannedSamples.filter { it.isFavorite }, chip = HistoryChip.FAVORITES)

@Preview(name = "S48_HistoryFavorites_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S48_HistoryFavorites_Light() = PreviewFrame(false, favoritesFilter)

@Preview(name = "S48_HistoryFavorites_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S48_HistoryFavorites_Dark() = PreviewFrame(true, favoritesFilter)

// s49 Empty state
@Preview(name = "S49_HistoryEmpty_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S49_HistoryEmpty_Light() = PreviewFrame(false, stateOf(emptyList()))

@Preview(name = "S49_HistoryEmpty_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S49_HistoryEmpty_Dark() = PreviewFrame(true, stateOf(emptyList()))

// s51 Undo snackbar after a swipe delete (the snackbar shows when the preview runs interactively)
@Preview(name = "S51_HistorySwipeUndo_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S51_HistorySwipeUndo_Light() = PreviewFrame(false, stateOf(scannedSamples.drop(2)), undoMessage = "Scan deleted")

@Preview(name = "S51_HistorySwipeUndo_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S51_HistorySwipeUndo_Dark() = PreviewFrame(true, stateOf(scannedSamples.drop(2)), undoMessage = "Scan deleted")

// s52 Favorites, empty
private val favoritesEmpty = stateOf(emptyList(), chip = HistoryChip.FAVORITES, emptyKind = HistoryEmptyKind.FAVORITES)

@Preview(name = "S52_HistoryFavoritesEmpty_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S52_HistoryFavoritesEmpty_Light() = PreviewFrame(false, favoritesEmpty)

@Preview(name = "S52_HistoryFavoritesEmpty_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S52_HistoryFavoritesEmpty_Dark() = PreviewFrame(true, favoritesEmpty)

// s53 Search results
private val searchResults =
    stateOf(
        records = listOf(scannedSamples[1], createdSamples[1]),
        searchActive = true,
        query = "cafe",
    )

@Preview(name = "S53_HistorySearch_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S53_HistorySearch_Light() = PreviewFrame(false, searchResults)

@Preview(name = "S53_HistorySearch_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S53_HistorySearch_Dark() = PreviewFrame(true, searchResults)

// s76 Dark history (the Scanned list in dark mode)
@Preview(name = "S76_History_Light", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S76_History_Light() = PreviewFrame(false, stateOf(scannedSamples.take(4)))

@Preview(name = "S76_History_Dark", widthDp = PHONE_W, heightDp = PHONE_H)
@Composable
private fun S76_History_Dark() = PreviewFrame(true, stateOf(scannedSamples.take(4)))

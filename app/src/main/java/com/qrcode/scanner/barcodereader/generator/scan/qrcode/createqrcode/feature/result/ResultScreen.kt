package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.LocationRationaleSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LoadingSpinner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.Level

/**
 * One screen for a fresh scan result and for a history item (state.fromHistory). Stateless: everything comes in as
 * [state] and goes out as [onEvent]; [onBack] leaves the screen.
 */
@Composable
fun ResultScreen(
    state: ResultUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ResultEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val record = state.record
    val back: () -> Unit = {
        when {
            state.wifi.flowVisible -> onEvent(ResultEvent.WifiBack)
            state.product.searchOpen -> onEvent(ResultEvent.ProductSearchClosed)
            else -> {
                if (state.fromHistory) onEvent(ResultEvent.NoteCommit)
                onBack()
            }
        }
    }
    BackHandler(onBack = back)
    val actionBar: @Composable () -> Unit = { if (record != null) ResultActionBar(state, record, onEvent) }
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = screenTitle(state),
                onBack = back,
                actions = {
                    if (record != null && showsFavoriteStar(state)) {
                        FavoriteTopAction(record.isFavorite, { onEvent(ResultEvent.FavoriteClicked) })
                    }
                },
            )
        },
        bottomBar = if (record != null && hasActionBar(state)) actionBar else null,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ResultBody(state, record, onEvent, onBack = back)
        }
    }
    ResultOverlays(state, record, onEvent)
}

/** s15 and s50 have the star in the top bar; the Wi-Fi flow (s26 to s28) and the product search (s30, s70) do not. */
private fun showsFavoriteStar(state: ResultUiState): Boolean =
    state.phase == ResultPhase.READY && !state.wifi.flowVisible && !state.product.searchOpen

/** s30 is titled "Search product", its offline variant s70 just "Product". */
@Composable
private fun screenTitle(state: ResultUiState): String {
    val record = state.record ?: return ""
    return when {
        state.product.searchOpen && state.product.online -> stringResource(R.string.result_title_product_search)
        else -> stringResource(record.type.titleRes())
    }
}

@Composable
private fun ResultBody(state: ResultUiState, record: ScanRecord?, onEvent: (ResultEvent) -> Unit, onBack: () -> Unit) {
    val content = record?.content
    when {
        state.phase == ResultPhase.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingSpinner() }
        record == null -> NotFound(onBack)
        state.wifi.flowVisible && content is ScanContent.Wifi -> WifiFlowContent(content, state, onEvent)
        state.product.searchOpen && content is ScanContent.Product -> ProductSearchContent(content, state.product, onEvent)
        else -> ResultScroll(state, record, onEvent)
    }
}

@Composable
private fun NotFound(onBack: () -> Unit) {
    EmptyState(
        icon = AppIcons.Info,
        title = stringResource(R.string.result_not_found_title),
        message = stringResource(R.string.result_not_found_message),
        action = { PrimaryButton(stringResource(R.string.common_back), onBack) },
    )
}

@Composable
private fun ResultScroll(state: ResultUiState, record: ScanRecord, onEvent: (ResultEvent) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // s23 puts the map above the header row.
        if (record.content is ScanContent.Location) MapPlaceholder()
        ResultHeader(record, state.safety)
        if (state.fromHistory && record.source == ScanSource.CREATED) CreatedPreviewCard(record)
        TypeContent(state, record, onEvent)
        ResultQuickActions(record, state.fromHistory, onEvent)
        if (state.fromHistory) NoteField(state.note, onEvent)
    }
}

/** Sheets and dialogs that sit on top of the screen (s29, s66, s67, s69). */
@Composable
private fun ResultOverlays(state: ResultUiState, record: ScanRecord?, onEvent: (ResultEvent) -> Unit) {
    if (state.unsafeSheetVisible) {
        UnsafeLinkSheet(
            level = state.safety?.level ?: Level.CAUTION,
            reasons = state.riskReasons,
            onGoBack = { onEvent(ResultEvent.UnsafeGoBack) },
            onCopyLink = { onEvent(ResultEvent.UnsafeCopyLink) },
            onOpenAnyway = { onEvent(ResultEvent.UnsafeOpenAnyway) },
            onDismiss = { onEvent(ResultEvent.UnsafeDismissed) },
        )
    }
    if (state.shareSheetVisible) {
        ShareSheet(onEvent = onEvent, onDismiss = { onEvent(ResultEvent.ShareSheetDismissed) })
    }
    if (state.deleteDialogVisible && record != null) {
        DeleteDialog(
            title = record.displayTitle,
            createdCode = record.source == ScanSource.CREATED,
            onConfirm = { onEvent(ResultEvent.DeleteConfirmed) },
            onDismiss = { onEvent(ResultEvent.DeleteDismissed) },
        )
    }
    if (state.locationRationaleVisible) {
        LocationRationaleSheet(
            onContinue = { onEvent(ResultEvent.LocationRationaleContinue) },
            onDismiss = { onEvent(ResultEvent.LocationRationaleDismissed) },
        )
    }
}

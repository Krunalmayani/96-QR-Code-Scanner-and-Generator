package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import android.content.Context
import android.content.res.Resources
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Length of the Undo snackbar after a delete (PRD section 14). */
private const val UNDO_WINDOW_MILLIS = 5_000L

/**
 * Stateful History entry: owns the ViewModel, the snackbar host and the Android side of one-shot effects
 * (navigation, share sheet, CSV export).
 */
@Composable
fun HistoryRoute(
    onOpenRecord: (Long) -> Unit,
    onStartScanning: () -> Unit,
    onCreateCode: () -> Unit,
    onOpenExportHistory: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val openRecord by rememberUpdatedState(onOpenRecord)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HistoryEffect.OpenRecord -> openRecord(effect.id)
                is HistoryEffect.RecordsDeleted ->
                    scope.launch {
                        showUndoSnackbar(
                            host = snackbarHostState,
                            message = deletedMessage(context.resources, effect.records),
                            actionLabel = context.getString(R.string.common_undo),
                            onUndo = { viewModel.onEvent(HistoryEvent.UndoDelete(effect.records)) },
                        )
                    }
                is HistoryEffect.ShareText ->
                    if (!HistoryShare.shareText(context, effect.text)) {
                        scope.launch { showFailure(snackbarHostState, context, R.string.hist_share_failed) }
                    }
                is HistoryEffect.ExportCsv ->
                    scope.launch {
                        if (!HistoryShare.exportCsv(context, effect.csv)) {
                            showFailure(snackbarHostState, context, R.string.hist_export_failed)
                        }
                    }
            }
        }
    }

    HistoryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onStartScanning = onStartScanning,
        onCreateCode = onCreateCode,
        onOpenExportHistory = onOpenExportHistory,
    )
}

private suspend fun showFailure(host: SnackbarHostState, context: Context, messageRes: Int) {
    host.currentSnackbarData?.dismiss()
    host.showSnackbar(context.getString(messageRes))
}

/** Shows [message] with an Undo action for exactly [UNDO_WINDOW_MILLIS]; replaces any snackbar still on screen. */
private suspend fun showUndoSnackbar(
    host: SnackbarHostState,
    message: String,
    actionLabel: String,
    onUndo: () -> Unit,
) = coroutineScope {
    host.currentSnackbarData?.dismiss()
    val timer =
        launch {
            delay(UNDO_WINDOW_MILLIS)
            host.currentSnackbarData?.dismiss()
        }
    val result =
        host.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = false,
            duration = SnackbarDuration.Indefinite,
        )
    timer.cancel()
    if (result == SnackbarResult.ActionPerformed) onUndo()
}

/** "Scan deleted" / "3 codes deleted" / "2 items deleted", depending on what was removed. */
private fun deletedMessage(resources: Resources, records: List<ScanRecord>): String {
    val count = records.size
    val plural =
        when {
            records.all { it.source == ScanSource.CREATED } -> R.plurals.hist_deleted_codes
            records.none { it.source == ScanSource.CREATED } -> R.plurals.hist_deleted_scans
            else -> R.plurals.hist_deleted_items
        }
    return resources.getQuantityString(plural, count, count)
}

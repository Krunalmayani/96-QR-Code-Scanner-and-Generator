package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CheckboxRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ExportSelection
import kotlinx.coroutines.launch

@Composable
fun ExportHistoryRoute(onBack: () -> Unit, viewModel: ExportHistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var pending by remember { mutableStateOf<ExportPayload?>(null) }

    fun tell(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(state.format.mimeType),
    ) { uri: Uri? ->
        val payload = pending
        pending = null
        if (uri != null && payload != null) {
            scope.launch {
                val ok = ExportFiles.writeToUri(context, uri, payload.text)
                tell(context.getString(if (ok) R.string.settings_export_saved else R.string.settings_export_failed))
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExportEffect.ChooseSaveLocation -> {
                    pending = effect.payload
                    saveLauncher.launch(effect.payload.fileName)
                }
                is ExportEffect.Share -> {
                    val uri = ExportFiles.writeSharedFile(context, effect.payload)
                    val shared = uri != null &&
                        SettingsIntents.shareFile(context, uri, effect.payload, context.getString(R.string.settings_export_share_title))
                    if (!shared) tell(context.getString(R.string.settings_export_failed))
                }
                ExportEffect.Failed -> tell(context.getString(R.string.settings_export_failed))
            }
        }
    }

    ExportHistoryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        snackbarHostState = snackbar,
    )
}

/** Export history (s63): choose what to include and the format, then save a file or share it. */
@Composable
fun ExportHistoryScreen(
    state: ExportUiState,
    onEvent: (ExportEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val formats = HistoryExportFormat.entries
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_export_title), onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            ActionBar {
                AppOutlinedButton(
                    text = stringResource(R.string.settings_export_share),
                    onClick = { onEvent(ExportEvent.ShareClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = state.canExport,
                    leadingIcon = AppIcons.Share,
                )
                PrimaryButton(
                    text = stringResource(R.string.settings_export_save),
                    onClick = { onEvent(ExportEvent.SaveClicked) },
                    modifier = Modifier.weight(1f),
                    enabled = state.canExport,
                    leadingIcon = AppIcons.Download,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppCard {
                SectionLabel(stringResource(R.string.settings_export_include))
                CheckboxRow(
                    label = stringResource(R.string.settings_export_scanned, state.counts.scanned),
                    checked = state.selection.includeScanned,
                    onCheckedChange = { onEvent(ExportEvent.SetIncludeScanned(it)) },
                )
                CheckboxRow(
                    label = stringResource(R.string.settings_export_created, state.counts.created),
                    checked = state.selection.includeCreated,
                    onCheckedChange = { onEvent(ExportEvent.SetIncludeCreated(it)) },
                )
                CheckboxRow(
                    label = stringResource(R.string.settings_export_favorites),
                    checked = state.selection.favoritesOnly,
                    onCheckedChange = { onEvent(ExportEvent.SetFavoritesOnly(it)) },
                )
            }
            AppCard {
                SectionLabel(stringResource(R.string.settings_export_format))
                SegmentedControl(
                    options = formats.map { stringResource(formatLabelRes(it)) },
                    selectedIndex = formats.indexOf(state.format),
                    onSelect = { onEvent(ExportEvent.SelectFormat(formats[it])) },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = if (state.itemCount > 0) {
                    quantityString(R.plurals.settings_export_summary, state.itemCount)
                } else {
                    stringResource(R.string.settings_export_nothing)
                },
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.ink,
            )
            Text(
                text = stringResource(R.string.settings_export_note),
                style = AppTheme.type.body,
                color = AppTheme.colors.inkSecondary,
            )
        }
    }
}

private fun formatLabelRes(format: HistoryExportFormat): Int = when (format) {
    HistoryExportFormat.CSV -> R.string.settings_export_format_csv
    HistoryExportFormat.JSON -> R.string.settings_export_format_json
    HistoryExportFormat.TEXT -> R.string.settings_export_format_text
}

private val PreviewState = ExportUiState(
    loading = false,
    counts = ExportCounts(scanned = 124, created = 18),
    selection = ExportSelection(includeScanned = true, includeCreated = true, favoritesOnly = false),
    format = HistoryExportFormat.CSV,
    itemCount = 142,
)

@Preview(name = "S63_ExportHistory_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S63_ExportHistory_Light() = AppTheme(dark = false) {
    ExportHistoryScreen(state = PreviewState, onEvent = {}, onBack = {})
}

@Preview(name = "S63_ExportHistory_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S63_ExportHistory_Dark() = AppTheme(dark = true) {
    ExportHistoryScreen(state = PreviewState.copy(format = HistoryExportFormat.JSON), onEvent = {}, onBack = {})
}

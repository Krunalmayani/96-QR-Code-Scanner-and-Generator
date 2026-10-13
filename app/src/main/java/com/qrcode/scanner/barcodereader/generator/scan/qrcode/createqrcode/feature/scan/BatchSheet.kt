package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType

/**
 * List sheet opened by "Done" in batch mode (s10). Every row was already saved to history when it was scanned;
 * tapping a row opens its result.
 */
@Composable
fun BatchSheet(
    rows: List<BatchRow>,
    onRowClick: (Long) -> Unit,
    onKeepScanning: () -> Unit,
    onFinish: () -> Unit,
) {
    AppBottomSheet(onDismissRequest = onKeepScanning) {
        BatchSheetContent(rows, onRowClick, onKeepScanning, onFinish)
    }
}

/** Sheet body, separate from the window so previews can render it. */
@Composable
fun BatchSheetContent(
    rows: List<BatchRow>,
    onRowClick: (Long) -> Unit,
    onKeepScanning: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.scan_batch_sheet_title),
                style = AppTheme.type.titleLarge,
                color = AppTheme.colors.ink,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(R.string.scan_batch_newest_first), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
        }
        if (rows.isEmpty()) {
            Text(stringResource(R.string.scan_batch_empty), style = AppTheme.type.body, color = AppTheme.colors.inkSecondary)
        } else {
            BatchRows(rows, onRowClick)
        }
        PrimaryButton(stringResource(R.string.scan_batch_finish), onFinish, Modifier.fillMaxWidth().padding(top = 4.dp), leadingIcon = AppIcons.Check)
        AppTextButton(stringResource(R.string.scan_quick_keep_scanning), onKeepScanning, Modifier.fillMaxWidth())
    }
}

@Composable
private fun BatchRows(rows: List<BatchRow>, onRowClick: (Long) -> Unit) {
    CardGroup(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
        rows.forEachIndexed { index, row ->
            if (index > 0) AppDivider()
            ListRow(
                title = row.title,
                subtitle = scanTypeAndFormat(row.type, row.format),
                leading = { IconTile(scanTypeIcon(row.type), tone = if (row.type == ContentType.PRODUCT) Tone.Warning else Tone.Primary) },
                trailing = {
                    Icon(
                        AppIcons.Chevron,
                        contentDescription = null,
                        tint = AppTheme.colors.inkSecondary,
                    )
                },
                onClick = { onRowClick(row.recordId) },
            )
        }
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CheckboxRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ConfirmDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme

/**
 * Clear-all-history confirmation (s68). Destructive: [ConfirmDialog] ignores back and outside taps, so only the
 * Cancel and Clear all buttons close it.
 */
@Composable
fun ClearHistoryDialog(
    state: ClearDialogState,
    onKeepFavoritesChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfirmDialog(
        title = stringResource(R.string.settings_clear_title),
        confirmText = stringResource(R.string.settings_clear_confirm),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        message = stringResource(R.string.settings_clear_message),
        destructive = true,
        icon = AppIcons.Delete,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = quantityString(R.plurals.settings_clear_scanned, state.scanned),
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.ink,
            )
            Text(
                text = quantityString(R.plurals.settings_clear_created, state.created),
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.ink,
            )
        }
        CheckboxRow(
            label = stringResource(R.string.settings_clear_keep_favorites),
            checked = state.keepFavorites,
            onCheckedChange = onKeepFavoritesChange,
        )
    }
}

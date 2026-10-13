package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ConfirmDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone

/** Artboard s72: "Update available". Dismissing it (Later, back, outside tap) hides it for 3 days. */
@Composable
internal fun UpdateDialog(onUpdate: () -> Unit, onLater: () -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.system_update_title),
        message = stringResource(R.string.system_update_body),
        confirmText = stringResource(R.string.system_update_action),
        dismissText = stringResource(R.string.system_update_later),
        onConfirm = onUpdate,
        onDismiss = onLater,
        icon = AppIcons.Update,
        iconTone = Tone.Success,
    )
}

/** Shown after a flexible update finished downloading: restart to install it. */
@Composable
internal fun UpdateReadyDialog(onRestart: () -> Unit, onLater: () -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.system_update_ready_title),
        message = stringResource(R.string.system_update_ready_body),
        confirmText = stringResource(R.string.system_update_ready_action),
        dismissText = stringResource(R.string.system_update_later),
        onConfirm = onRestart,
        onDismiss = onLater,
        icon = AppIcons.Download,
        iconTone = Tone.Success,
    )
}

@Preview(name = "S72_UpdateDialog_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateDialog_Light() {
    AppTheme(dark = false) {
        Box(Modifier.fillMaxSize().background(AppTheme.colors.background))
        UpdateDialog(onUpdate = {}, onLater = {})
    }
}

@Preview(name = "S72_UpdateDialog_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateDialog_Dark() {
    AppTheme(dark = true) {
        Box(Modifier.fillMaxSize().background(AppTheme.colors.background))
        UpdateDialog(onUpdate = {}, onLater = {})
    }
}

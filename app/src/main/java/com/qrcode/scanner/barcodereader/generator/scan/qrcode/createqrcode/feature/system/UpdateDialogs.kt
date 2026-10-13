package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SmallButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf

/** Artboard s72: "Update available". Dismissing it (Later, back, outside tap) hides it for 3 days. */
@Composable
internal fun UpdateDialog(onUpdate: () -> Unit, onLater: () -> Unit) {
    Dialog(onDismissRequest = onLater) { UpdateDialogBody(AppIcons.Download, onUpdate, onLater, ready = false) }
}

/** Shown after a flexible update finished downloading: restart to install it. */
@Composable
internal fun UpdateReadyDialog(onRestart: () -> Unit, onLater: () -> Unit) {
    Dialog(onDismissRequest = onLater) { UpdateDialogBody(AppIcons.Update, onRestart, onLater, ready = true) }
}

/** Left-aligned dialog card of s72: 56dp round icon, title, message, then "Later" and a compact primary button. */
@Composable
private fun UpdateDialogBody(icon: ImageVector, onConfirm: () -> Unit, onLater: () -> Unit, ready: Boolean) {
    val c = AppTheme.colors
    val title = if (ready) R.string.system_update_ready_title else R.string.system_update_title
    val body = if (ready) R.string.system_update_ready_body else R.string.system_update_body
    val action = if (ready) R.string.system_update_ready_action else R.string.system_update_action
    Column(
        modifier = Modifier.fillMaxWidth().background(c.surface, AppTheme.shapes.dialog).padding(24.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(c.containerOf(Tone.Success)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.contentOf(Tone.Success), modifier = Modifier.size(32.dp))
        }
        Text(
            text = stringResource(title),
            style = AppTheme.type.headline,
            color = c.ink,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        Text(
            text = stringResource(body),
            style = AppTheme.type.body,
            color = c.inkSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTextButton(stringResource(R.string.system_update_later), onLater)
            SmallButton(stringResource(action), onConfirm, tonal = false)
        }
    }
}

@Composable
private fun UpdatePreviewFrame(dark: Boolean, ready: Boolean) {
    AppTheme(dark = dark) {
        Box(Modifier.fillMaxSize().background(AppTheme.colors.scrim).padding(24.dp), contentAlignment = Alignment.Center) {
            UpdateDialogBody(if (ready) AppIcons.Update else AppIcons.Download, {}, {}, ready)
        }
    }
}

@Preview(name = "S72_UpdateDialog_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateDialog_Light() = UpdatePreviewFrame(dark = false, ready = false)

@Preview(name = "S72_UpdateDialog_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateDialog_Dark() = UpdatePreviewFrame(dark = true, ready = false)

@Preview(name = "S72_UpdateReadyDialog_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateReadyDialog_Light() = UpdatePreviewFrame(dark = false, ready = true)

@Preview(name = "S72_UpdateReadyDialog_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S72_UpdateReadyDialog_Dark() = UpdatePreviewFrame(dark = true, ready = true)

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions

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
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Artboard s64: explains why the camera is needed before the system permission prompt.
 * [onAllow] is the "Continue" button (start the permission request), [onDismiss] is "Not now" / back / outside tap.
 */
@Composable
fun CameraRationaleDialog(
    onAllow: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        CameraRationaleDialogBody(onAllow = onAllow, onDismiss = onDismiss)
    }
}

/**
 * Artboard s65: explains that only the picked photo is read. [onContinue] is "Choose photo"
 * (open the photo picker), [onDismiss] is "Cancel" / back / outside tap.
 */
@Composable
fun PhotosRationaleDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        PhotosRationaleDialogBody(onContinue = onContinue, onDismiss = onDismiss)
    }
}

@Composable
private fun CameraRationaleDialogBody(onAllow: () -> Unit, onDismiss: () -> Unit) {
    RationaleDialogBody(
        icon = AppIcons.Camera,
        tone = Tone.Primary,
        title = stringResource(R.string.perm_camera_dialog_title),
        message = stringResource(R.string.perm_camera_dialog_body),
        confirmText = stringResource(R.string.perm_continue),
        dismissText = stringResource(R.string.perm_not_now),
        onConfirm = onAllow,
        onDismiss = onDismiss,
    )
}

@Composable
private fun PhotosRationaleDialogBody(onContinue: () -> Unit, onDismiss: () -> Unit) {
    RationaleDialogBody(
        icon = AppIcons.Image,
        tone = Tone.Success,
        title = stringResource(R.string.perm_photos_title),
        message = stringResource(R.string.perm_photos_body),
        confirmText = stringResource(R.string.perm_photos_choose),
        dismissText = stringResource(R.string.perm_photos_cancel),
        onConfirm = onContinue,
        onDismiss = onDismiss,
    )
}

/** Centered dialog card of s64/s65: round icon, title, message, then a text button and a compact primary button. */
@Composable
private fun RationaleDialogBody(
    icon: ImageVector,
    tone: Tone,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surface, AppTheme.shapes.dialog)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(c.containerOf(tone)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(32.dp))
        }
        Text(
            text = title,
            style = AppTheme.type.headline,
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        Text(
            text = message,
            style = AppTheme.type.body,
            color = c.inkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTextButton(dismissText, onDismiss)
            SmallButton(confirmText, onConfirm, tonal = false)
        }
    }
}

@Composable
private fun PreviewScrim(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.scrim).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(name = "S64_CameraRationaleDialog_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S64_CameraRationaleDialog_Light() {
    AppTheme(dark = false) { PreviewScrim { CameraRationaleDialogBody(onAllow = {}, onDismiss = {}) } }
}

@Preview(name = "S64_CameraRationaleDialog_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S64_CameraRationaleDialog_Dark() {
    AppTheme(dark = true) { PreviewScrim { CameraRationaleDialogBody(onAllow = {}, onDismiss = {}) } }
}

@Preview(name = "S65_PhotosRationaleDialog_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S65_PhotosRationaleDialog_Light() {
    AppTheme(dark = false) { PreviewScrim { PhotosRationaleDialogBody(onContinue = {}, onDismiss = {}) } }
}

@Preview(name = "S65_PhotosRationaleDialog_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S65_PhotosRationaleDialog_Dark() {
    AppTheme(dark = true) { PreviewScrim { PhotosRationaleDialogBody(onContinue = {}, onDismiss = {}) } }
}

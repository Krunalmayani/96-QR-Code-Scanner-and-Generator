package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.ScanHitColor
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import kotlinx.coroutines.delay

/**
 * Modal bottom sheet (radius 28, drag handle, 24dp content padding). Swipe down or scrim tap calls
 * [onDismissRequest]. The content column already adds the navigation-bar inset.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AppTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = state,
        shape = AppTheme.shapes.sheet,
        containerColor = c.surface,
        contentColor = c.ink,
        scrimColor = c.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.inkSecondary.copy(alpha = 0.4f)) },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp).navigationBarsPadding(),
            content = content,
        )
    }
}

/**
 * Confirmation dialog (radius 28). Destructive dialogs can only be dismissed with a button
 * (back and outside taps are ignored), as required by the PRD. Extra [content] sits between the message and buttons.
 */
@Composable
fun ConfirmDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissText: String = stringResource(R.string.common_cancel),
    destructive: Boolean = false,
    icon: ImageVector? = null,
    iconTone: Tone = if (destructive) Tone.Error else Tone.Primary,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val c = AppTheme.colors
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !destructive, dismissOnClickOutside = !destructive),
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(c.surface, AppTheme.shapes.dialog)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (icon != null) IconTile(icon, tone = iconTone, size = 48.dp)
            Text(title, style = AppTheme.type.titleLarge, color = c.ink)
            if (message != null) Text(message, style = AppTheme.type.body, color = c.inkSecondary)
            content?.invoke(this)
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                AppTextButton(dismissText, onDismiss)
                if (destructive) DestructiveButton(confirmText, onConfirm) else PrimaryButton(confirmText, onConfirm)
            }
        }
    }
}

/** Tiny state holder for [AppToastHost]. Call [show] from a coroutine (e.g. `scope.launch { toast.show("Saved") }`). */
@Stable
class ToastState {
    var message: String? by mutableStateOf(null)
        private set

    suspend fun show(message: String, durationMillis: Long = 1800L) {
        this.message = message
        delay(durationMillis)
        if (this.message == message) this.message = null
    }
}

@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

/** Dark pill toast with a teal check ("Saved to gallery"). Place it near the bottom of a Box. */
@Composable
fun AppToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val message = state.message
    var last by remember { mutableStateOf("") }
    if (message != null) last = message
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Row(
                Modifier
                    .height(48.dp)
                    .background(Color(0xFF13142B), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(AppIcons.CheckCircle, contentDescription = null, tint = ScanHitColor, modifier = Modifier.size(20.dp))
                Text(last, style = AppTheme.type.bodyStrong, color = Color.White, maxLines = 1)
            }
        }
    }
}

@Preview(name = "Dialog")
@Composable
private fun ConfirmDialogPreview() {
    AppTheme(dark = false) {
        Box(Modifier.background(AppTheme.colors.background).padding(24.dp)) {
            Column(
                Modifier.fillMaxWidth().background(AppTheme.colors.surface, AppTheme.shapes.dialog).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Delete this item?", style = AppTheme.type.titleLarge)
                Text("This can't be undone.", style = AppTheme.type.body, color = AppTheme.colors.inkSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    AppTextButton("Cancel", {})
                    DestructiveButton("Delete", {})
                }
            }
        }
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ConfirmDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.QuickAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.RiskReason
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.Level

/** s69: shown instead of opening a link whose safety level is not SAFE. */
@Composable
internal fun UnsafeLinkSheet(
    level: Level,
    reasons: List<RiskReason>,
    onGoBack: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenAnyway: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismissRequest = onDismiss) {
        UnsafeLinkSheetBody(level, reasons, onGoBack, onCopyLink, onOpenAnyway)
    }
}

@Composable
internal fun ColumnScope.UnsafeLinkSheetBody(
    level: Level,
    reasons: List<RiskReason>,
    onGoBack: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenAnyway: () -> Unit,
) {
    val unsafe = level == Level.UNSAFE
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconTile(AppIcons.Warning, tone = if (unsafe) Tone.Error else Tone.Warning, size = 56.dp)
        Text(
            stringResource(if (unsafe) R.string.result_unsafe_title_unsafe else R.string.result_unsafe_title_caution),
            style = AppTheme.type.headline,
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
    }
    Text(
        stringResource(if (unsafe) R.string.result_unsafe_body_unsafe else R.string.result_unsafe_body_caution),
        style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
        color = AppTheme.colors.inkSecondary,
        modifier = Modifier.padding(top = 12.dp),
    )
    if (reasons.isNotEmpty()) {
        CardGroup(Modifier.padding(top = 16.dp)) {
            reasons.forEachIndexed { index, reason ->
                if (index > 0) AppDivider()
                ReasonRow(reason)
            }
        }
    }
    PrimaryButton(
        stringResource(R.string.result_unsafe_go_back),
        onGoBack,
        Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 52.dp),
    )
    AppOutlinedButton(
        stringResource(R.string.result_unsafe_copy),
        onCopyLink,
        Modifier.fillMaxWidth().padding(top = 8.dp),
    )
    DangerTextButton(stringResource(R.string.result_unsafe_open_anyway), onOpenAnyway, Modifier.fillMaxWidth().padding(top = 4.dp))
}

@Composable
private fun ReasonRow(reason: RiskReason) {
    val detail = reason.detail
    val detailText = if (detail == null) stringResource(reason.kind.detailRes()) else stringResource(reason.kind.detailRes(), detail)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppBadge(stringResource(reason.kind.chipRes()), tone = if (reason.severe) Tone.Error else Tone.Warning)
        Text(detailText, style = AppTheme.type.body, color = AppTheme.colors.ink, modifier = Modifier.weight(1f))
    }
}

/** Text-only button in the error color (the design system has none): the deliberate "Open anyway" choice. */
@Composable
private fun DangerTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppTheme.type.button, color = AppTheme.colors.error, textAlign = TextAlign.Center)
    }
}

/** s29: choose what to share for a created code. */
@Composable
internal fun ShareSheet(onShareText: () -> Unit, onShareImage: () -> Unit, onDismiss: () -> Unit) {
    AppBottomSheet(onDismissRequest = onDismiss) {
        ShareSheetBody(onShareText, onShareImage)
    }
}

@Composable
internal fun ColumnScope.ShareSheetBody(onShareText: () -> Unit, onShareImage: () -> Unit) {
    Text(
        stringResource(R.string.result_share_title),
        style = AppTheme.type.titleLarge,
        color = AppTheme.colors.ink,
        modifier = Modifier.semantics { heading() },
    )
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        QuickAction(AppIcons.Text, stringResource(R.string.result_share_as_text), onShareText)
        QuickAction(AppIcons.Image, stringResource(R.string.result_share_as_image), onShareImage)
    }
}

/** s67: destructive confirmation for the history detail's Delete. Only the buttons dismiss it. */
@Composable
internal fun DeleteDialog(title: String, createdCode: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = stringResource(if (createdCode) R.string.result_delete_title_created else R.string.result_delete_title_scan),
        confirmText = stringResource(R.string.common_delete),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        message = stringResource(R.string.result_delete_message, title),
        destructive = true,
        icon = AppIcons.Delete,
    )
}

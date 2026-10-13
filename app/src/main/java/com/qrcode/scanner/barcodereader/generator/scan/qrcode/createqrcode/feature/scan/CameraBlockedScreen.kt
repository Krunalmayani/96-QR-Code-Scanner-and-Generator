package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone

/**
 * Artboard s09: shown instead of the camera when it cannot be used. [block] picks the copy and primary action:
 * "Request again" while the system prompt can still be shown, "Open settings" once it is permanently denied,
 * "Try again" when the camera failed to start. Scanning from a saved image stays available.
 */
@Composable
fun CameraBlockedScreen(
    block: CameraBlock,
    onPrimary: () -> Unit,
    onPickImage: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(AppTheme.colors.background).statusBarsPadding()) {
        BlockedTopBar(onOpenSettings)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = LocalBottomBarInset.current + 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            BlockedHeader(block)
            BlockedActions(block, onPrimary, onPickImage)
            if (block == CameraBlock.PermissionPermanentlyDenied) StepsCard()
        }
    }
}

@Composable
private fun BlockedTopBar(onOpenSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).padding(start = 24.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.scan_title),
            style = AppTheme.type.topBar,
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        IconButtonRounded(AppIcons.Settings, stringResource(R.string.scan_settings), onOpenSettings)
    }
}

@Composable
private fun BlockedHeader(block: CameraBlock) {
    val colors = AppTheme.colors
    Box(Modifier.size(144.dp).clip(CircleShape).background(colors.errorContainer), contentAlignment = Alignment.Center) {
        Icon(AppIcons.Camera, contentDescription = null, tint = colors.error, modifier = Modifier.size(64.dp))
    }
    val title = if (block == CameraBlock.Unavailable) R.string.scan_unavailable_title else R.string.scan_denied_title
    val message = when (block) {
        CameraBlock.PermissionDenied -> R.string.scan_denied_message_request
        CameraBlock.PermissionPermanentlyDenied -> R.string.scan_denied_message_settings
        CameraBlock.Unavailable -> R.string.scan_unavailable_message
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(title), style = AppTheme.type.headline, color = colors.ink, textAlign = TextAlign.Center)
        Text(stringResource(message), style = AppTheme.type.body, color = colors.inkSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BlockedActions(block: CameraBlock, onPrimary: () -> Unit, onPickImage: () -> Unit) {
    val primary = when (block) {
        CameraBlock.PermissionDenied -> R.string.scan_request_again
        CameraBlock.PermissionPermanentlyDenied -> R.string.scan_open_settings
        CameraBlock.Unavailable -> R.string.common_retry
    }
    val icon = if (block == CameraBlock.PermissionPermanentlyDenied) AppIcons.SettingsGear else AppIcons.Camera
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrimaryButton(stringResource(primary), onPrimary, Modifier.fillMaxWidth(), leadingIcon = icon)
        TonalButton(stringResource(R.string.scan_from_gallery), onPickImage, Modifier.fillMaxWidth(), leadingIcon = AppIcons.Gallery)
    }
}

@Composable
private fun StepsCard() {
    AppCard(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            IconTile(AppIcons.Info, tone = Tone.Primary)
            Text(
                stringResource(R.string.scan_denied_steps),
                style = AppTheme.type.bodySmall,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

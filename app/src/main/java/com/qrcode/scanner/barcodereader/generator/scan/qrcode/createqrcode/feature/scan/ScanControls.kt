package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSlider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.CameraColors
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ZoomMath

private val HintBackground = Color(0x66000000)
private val PillShape = RoundedCornerShape(18.dp)

/** Top row over the camera: flash + switch camera, title, settings (s06/s07). */
@Composable
internal fun ScanTopBar(
    state: ScanUiState,
    hasFlash: Boolean,
    onEvent: (ScanEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
    ) {
        Row(Modifier.align(Alignment.CenterStart), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FlashButton(on = state.flashOn, enabled = hasFlash && !state.frontCamera, onClick = { onEvent(ScanEvent.FlashToggled) })
            IconButtonRounded(
                icon = AppIcons.FlipCamera,
                contentDescription = stringResource(R.string.scan_switch_camera),
                onClick = { onEvent(ScanEvent.CameraSwitched) },
                style = IconButtonStyle.OnCamera,
            )
        }
        Text(
            text = stringResource(R.string.scan_title),
            style = AppTheme.type.topBar,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center).semantics { heading() },
        )
        IconButtonRounded(
            icon = AppIcons.Settings,
            contentDescription = stringResource(R.string.scan_settings),
            onClick = { onEvent(ScanEvent.SettingsClicked) },
            style = IconButtonStyle.OnCamera,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/** Top row in batch mode (s10): close, title, Done. */
@Composable
internal fun BatchTopBar(onEvent: (ScanEvent) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
    ) {
        IconButtonRounded(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.scan_batch_close),
            onClick = { onEvent(ScanEvent.BatchToggled) },
            style = IconButtonStyle.OnCamera,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = stringResource(R.string.scan_batch_title),
            style = AppTheme.type.topBar,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center).semantics { heading() },
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .heightIn(min = 48.dp)
                    .clip(PillShape)
                    .clickable(role = Role.Button) { onEvent(ScanEvent.BatchDoneClicked) }
                    .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.common_done), style = AppTheme.type.button, color = Color.White)
        }
    }
}

@Composable
private fun FlashButton(on: Boolean, enabled: Boolean, onClick: () -> Unit) {
    IconButtonRounded(
        icon = if (on) AppIcons.FlashOn else AppIcons.FlashOff,
        contentDescription = stringResource(if (on) R.string.scan_flash_on else R.string.scan_flash_off),
        onClick = onClick,
        style = if (on) IconButtonStyle.OnCameraActive else IconButtonStyle.OnCamera,
        enabled = enabled,
    )
}

/** Translucent pill with the "align the code" hint. */
@Composable
internal fun ScanHint(mode: ScanMode, modifier: Modifier = Modifier) {
    val text = stringResource(if (mode.wideFrame) R.string.scan_hint_barcode else R.string.scan_hint_qr)
    Box(
        modifier
            .heightIn(min = 36.dp)
            .clip(PillShape)
            .background(HintBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AppTheme.type.chip, color = Color.White, textAlign = TextAlign.Center, maxLines = 2)
    }
}

/** Teal "N codes scanned" pill shown under the viewfinder in batch mode. */
@Composable
internal fun BatchCountPill(count: Int, modifier: Modifier = Modifier) {
    val description = pluralStringResource(R.plurals.scan_batch_count, count, count)
    Row(
        modifier
            .heightIn(min = 36.dp)
            .clip(PillShape)
            .background(CameraColors.hit)
            .animateContentSize()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Check, contentDescription = null, tint = CameraColors.onHit, modifier = Modifier.size(18.dp))
        Text(text = description, style = AppTheme.type.chipStrong, color = CameraColors.onHit, maxLines = 1)
    }
}

/** Mode chips QR Code / Barcode / All. */
@Composable
internal fun ModeSelector(mode: ScanMode, onSelect: (ScanMode) -> Unit, modifier: Modifier = Modifier) {
    val labels = ScanMode.chipOrder.map { stringResource(modeLabelRes(it)) }
    SegmentedControl(
        options = labels,
        selectedIndex = ScanMode.chipOrder.indexOf(mode).coerceAtLeast(0),
        onSelect = { onSelect(ScanMode.chipOrder[it]) },
        modifier = modifier.width(300.dp),
        onCamera = true,
    )
}

private fun modeLabelRes(mode: ScanMode): Int =
    when (mode) {
        ScanMode.QR -> R.string.scan_mode_qr
        ScanMode.BARCODE -> R.string.scan_mode_barcode
        ScanMode.ALL -> R.string.scan_mode_all
    }

/** "1x [slider] 5x"; the slider mirrors pinch zoom. */
@Composable
internal fun ZoomRow(
    zoom: Float,
    maxZoomRatio: Float,
    enabled: Boolean,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.scan_zoom)
    Row(
        modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.scan_zoom_min), style = AppTheme.type.captionStrong, color = Color.White)
        AppSlider(
            value = zoom,
            onValueChange = onZoomChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = description },
            enabled = enabled,
            onCamera = true,
        )
        Text(
            stringResource(R.string.scan_zoom_max, ZoomMath.maxRatioLabel(maxZoomRatio)),
            style = AppTheme.type.captionStrong,
            color = Color.White,
        )
    }
}

/** Translucent button used over the camera ("Scan from gallery"). */
@Composable
internal fun GlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CameraColors.onGlass.copy(alpha = 0.16f))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Image, contentDescription = null, tint = CameraColors.onGlass, modifier = Modifier.size(20.dp))
        Text(text, style = AppTheme.type.button, color = CameraColors.onGlass, maxLines = 1)
    }
}

/** Gallery button and the batch toggle chip, side by side. */
@Composable
internal fun GalleryAndBatchRow(
    batchMode: Boolean,
    onGallery: () -> Unit,
    onBatchToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        GlassButton(stringResource(R.string.scan_from_gallery), onGallery, Modifier.weight(1f))
        AppChip(
            label = stringResource(R.string.scan_batch_chip),
            selected = batchMode,
            onClick = onBatchToggle,
            leadingIcon = AppIcons.Apps,
            onCamera = true,
        )
    }
}

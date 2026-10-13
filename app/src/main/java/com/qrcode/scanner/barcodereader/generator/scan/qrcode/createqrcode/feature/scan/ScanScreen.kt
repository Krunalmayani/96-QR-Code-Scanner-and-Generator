package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppToastHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ViewfinderOverlay
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ViewfinderShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.rememberToastState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.viewfinderRect
import kotlinx.coroutines.launch

/** Height reserved above the system bars for the bottom controls: mode, zoom, gallery/batch rows plus gaps. */
private val ControlsHeight = 160.dp
private val ControlsExtraBottom = 28.dp
private val HintGap = 20.dp
private val HintHeight = 36.dp

/**
 * Live scan screen (s06, s07, s10, s73). Stateless: the camera is supplied by [camera] so previews can draw a
 * stand-in. The camera fills the whole screen under the system bars; the controls clear the bottom navigation bar
 * with [LocalBottomBarInset].
 */
@Composable
fun ScanScreen(
    state: ScanUiState,
    capabilities: CameraCapabilities,
    onEvent: (ScanEvent) -> Unit,
    modifier: Modifier = Modifier,
    camera: @Composable () -> Unit,
) {
    val toast = rememberToastState()
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.common_copied)

    BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
        camera()
        ViewfinderOverlay(
            shape = if (state.mode.wideFrame) ViewfinderShape.Wide else ViewfinderShape.Square,
            detected = state.detected,
            scanning = !state.detected,
        )
        ScanOverlayContent(state, capabilities, maxWidth, maxHeight, onEvent)
        AppToastHost(toast, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp))
    }

    state.quickResult?.let { record ->
        QuickResultSheet(
            record = record,
            onOpen = { onEvent(ScanEvent.QuickOpened) },
            onDismiss = { onEvent(ScanEvent.QuickDismissed) },
            onToggleFavorite = { onEvent(ScanEvent.QuickFavoriteToggled) },
            onCopy = { text ->
                clipboard.setText(AnnotatedString(text))
                scope.launch { toast.show(copiedMessage) }
            },
            onShare = { text -> shareText(context, text) },
        )
    }
    if (state.batchSheetOpen) {
        BatchSheet(
            rows = state.batch,
            onRowClick = { onEvent(ScanEvent.BatchRowClicked(it)) },
            onKeepScanning = { onEvent(ScanEvent.BatchSheetDismissed) },
            onFinish = { onEvent(ScanEvent.BatchFinished) },
        )
    }
}

@Composable
private fun BoxScope.ScanOverlayContent(
    state: ScanUiState,
    capabilities: CameraCapabilities,
    width: Dp,
    height: Dp,
    onEvent: (ScanEvent) -> Unit,
) {
    val density = LocalDensity.current
    val shape = if (state.mode.wideFrame) ViewfinderShape.Wide else ViewfinderShape.Square
    val frame = with(density) { viewfinderRect(width.toPx(), height.toPx(), density.density, shape) }
    val frameBottom = with(density) { frame.bottom.toDp() }
    val bottomInset = LocalBottomBarInset.current + ControlsExtraBottom
    val hintTop = frameBottom + HintGap
    val room = hintTop + HintHeight < height - bottomInset - ControlsHeight

    if (state.batchMode) BatchTopBar(onEvent) else ScanTopBar(state, capabilities.hasFlash, onEvent)
    if (room) {
        val modifier = Modifier.align(Alignment.TopCenter).offset(y = hintTop)
        if (state.batchMode && state.batch.isNotEmpty()) BatchCountPill(state.batch.size, modifier) else ScanHint(state.mode, modifier)
    }
    Column(
        Modifier.align(Alignment.BottomCenter).padding(start = 24.dp, end = 24.dp, bottom = bottomInset),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ModeSelector(state.mode, { onEvent(ScanEvent.ModeSelected(it)) })
        ZoomRow(state.zoom, capabilities.maxZoomRatio, enabled = true, onZoomChange = { onEvent(ScanEvent.ZoomChanged(it)) })
        GalleryAndBatchRow(state.batchMode, { onEvent(ScanEvent.GalleryClicked) }, { onEvent(ScanEvent.BatchToggled) })
    }
}

/** Shares [text] through the system chooser (only ever from a user tap). */
internal fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    try {
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        // No app can share text: nothing else to offer.
    }
}

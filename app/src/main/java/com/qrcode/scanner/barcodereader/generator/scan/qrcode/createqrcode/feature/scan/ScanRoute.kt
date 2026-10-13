package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.CameraRationaleDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.CameraPermissionStatus

/**
 * Stateful entry of the Scan tab: permission flow, camera, haptics and navigation hand-offs.
 * The navigation lambdas are supplied by [scanGraph]; this composable never touches a NavController.
 */
@Composable
fun ScanRoute(
    onOpenResult: (recordId: Long) -> Unit,
    onOpenGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberCameraPermissionState()
    val feedback = rememberScanFeedback()
    var capabilities by remember { mutableStateOf(CameraCapabilities()) }
    var cameraFailed by remember { mutableStateOf(false) }

    ResumeEffect {
        viewModel.onEvent(ScanEvent.ScreenResumed)
        permission.refresh()
    }
    HandleScanEffects(viewModel, feedback, onOpenResult, onOpenGallery, onOpenSettings)

    when {
        permission.status != CameraPermissionStatus.GRANTED -> {
            CameraBlockedScreen(
                block = blockFor(permission.status),
                onPrimary = if (permission.status == CameraPermissionStatus.PERMANENTLY_DENIED) permission.openSettings else permission.request,
                onPickImage = onOpenGallery,
                onOpenSettings = onOpenSettings,
                modifier = modifier,
            )
            if (permission.rationaleVisible) {
                CameraRationaleDialog(onAllow = permission.onRationaleAllow, onDismiss = permission.onRationaleDismiss)
            }
        }
        cameraFailed -> CameraBlockedScreen(
            block = CameraBlock.Unavailable,
            onPrimary = { cameraFailed = false },
            onPickImage = onOpenGallery,
            onOpenSettings = onOpenSettings,
            modifier = modifier,
        )
        else -> ScanScreen(
            state = state,
            capabilities = capabilities,
            onEvent = viewModel::onEvent,
            modifier = modifier,
            camera = {
                CameraPreview(
                    mode = state.mode,
                    frontCamera = state.frontCamera,
                    torchOn = state.flashOn,
                    zoom = state.zoom,
                    analyzing = !state.detected,
                    scanAreaOnly = state.scanAreaOnly,
                    frozen = state.detected && !state.batchMode,
                    onZoomChange = { viewModel.onEvent(ScanEvent.ZoomChanged(it)) },
                    onCodes = { viewModel.onEvent(ScanEvent.CodesDetected(it)) },
                    onCameraReady = { capabilities = it },
                    onCameraError = { cameraFailed = true },
                )
            },
        )
    }
}

private fun blockFor(status: CameraPermissionStatus): CameraBlock =
    if (status == CameraPermissionStatus.PERMANENTLY_DENIED) CameraBlock.PermissionPermanentlyDenied else CameraBlock.PermissionDenied

@Composable
private fun rememberScanFeedback(): ScanFeedback {
    val context = LocalContext.current
    val feedback = remember { ScanFeedback(context.applicationContext) }
    DisposableEffect(feedback) { onDispose { feedback.release() } }
    return feedback
}

/** Runs [action] every time the host lifecycle reaches RESUMED (including the first time). */
@Composable
private fun ResumeEffect(action: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latest by rememberUpdatedState(action)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) latest() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun HandleScanEffects(
    viewModel: ScanViewModel,
    feedback: ScanFeedback,
    onOpenResult: (Long) -> Unit,
    onOpenGallery: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val openResult by rememberUpdatedState(onOpenResult)
    val openGallery by rememberUpdatedState(onOpenGallery)
    val openSettings by rememberUpdatedState(onOpenSettings)
    LaunchedEffect(viewModel, feedback) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ScanEffect.OpenResult -> openResult(effect.recordId)
                ScanEffect.OpenGallery -> openGallery()
                ScanEffect.OpenSettings -> openSettings()
                is ScanEffect.Feedback -> feedback.play(effect.vibrate, effect.beep)
            }
        }
    }
}

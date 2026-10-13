package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.PhotosRationaleDialog

/** True once the user continued past the photos rationale in this process: it is not shown again until the app restarts. */
private object PhotosRationaleMemory {
    @Volatile var acknowledged = false
}

/**
 * Stateful entry of scan-from-image: opens the Android Photo Picker (no storage permission needed) as soon as the
 * screen opens, then follows the view model's state. [onOpenResult] receives the record id and whether this
 * screen should be removed from the back stack.
 */
@Composable
fun ScanFromImageRoute(
    onBack: () -> Unit,
    onOpenResult: (recordId: Long, replaceScreen: Boolean) -> Unit,
    onUseCamera: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanFromImageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var rationaleVisible by remember { mutableStateOf(false) }
    var autoLaunched by rememberSaveable { mutableStateOf(false) }

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.onEvent(ImageScanEvent.ImagePicked(uri.toString()))
        }
    val launchPicker: () -> Unit = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val requestPicker: () -> Unit = {
        if (PhotosRationaleMemory.acknowledged) {
            launchPicker()
        } else {
            rationaleVisible = true
        }
    }

    LaunchedEffect(Unit) {
        if (!autoLaunched && state is ImageScanState.Idle) {
            autoLaunched = true
            requestPicker()
        }
    }
    val latestRequestPicker by rememberUpdatedState(requestPicker)
    val latestOpenResult by rememberUpdatedState(onOpenResult)
    val latestBack by rememberUpdatedState(onBack)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ImageScanEffect.LaunchPicker -> latestRequestPicker()
                is ImageScanEffect.OpenResult -> latestOpenResult(effect.recordId, effect.replaceScreen)
                ImageScanEffect.Close -> latestBack()
            }
        }
    }

    ScanFromImageScreen(
        state = state,
        onBack = onBack,
        onChooseImage = requestPicker,
        onCancel = { viewModel.onEvent(ImageScanEvent.Cancelled) },
        onCodeClick = { viewModel.onEvent(ImageScanEvent.CodeChosen(it)) },
        onUseCamera = onUseCamera,
        modifier = modifier,
    )
    if (rationaleVisible) {
        PhotosRationaleDialog(
            onContinue = {
                PhotosRationaleMemory.acknowledged = true
                rationaleVisible = false
                launchPicker()
            },
            onDismiss = { rationaleVisible = false },
        )
    }
}

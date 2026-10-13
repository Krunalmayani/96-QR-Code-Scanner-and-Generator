package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LocalAdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.rememberToastState
import kotlinx.coroutines.launch

@StringRes
private fun GeneratedMessage.textRes(): Int = when (this) {
    GeneratedMessage.SAVED_TO_GALLERY -> R.string.create_toast_saved_gallery
    GeneratedMessage.SAVED_TO_DOWNLOADS -> R.string.create_toast_saved_downloads
    GeneratedMessage.COPIED -> R.string.common_copied
    GeneratedMessage.SAVE_FAILED -> R.string.create_error_save_failed
    GeneratedMessage.SHARE_FAILED -> R.string.create_error_share_failed
    GeneratedMessage.PERMISSION_DENIED -> R.string.create_error_storage_permission
    GeneratedMessage.LOGO_FAILED -> R.string.create_error_logo_failed
}

/**
 * Entry of the Generated destination: wires the ViewModel to the Android pieces the screen needs (permission request,
 * Photo Picker, clipboard, share sheet) and counts the ad impression once when the result is first shown.
 */
@Composable
fun GeneratedRoute(
    onBack: () -> Unit,
    viewModel: GeneratedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    val snackbar = remember { SnackbarHostState() }

    val adController = LocalAdController.current
    var adCounted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.status) {
        if (state.status == GeneratedStatus.READY && !adCounted) {
            adCounted = true
            adController.onResultShown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onEvent(GeneratedEvent.StoragePermissionResult(granted))
    }
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onEvent(GeneratedEvent.LogoPicked(uri.toString()))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GeneratedEffect.Message -> {
                    val text = context.getString(effect.message.textRes())
                    scope.launch { if (effect.message.isSuccess) toast.show(text) else snackbar.showSnackbar(text) }
                }
                is GeneratedEffect.ShareFile -> {
                    if (!startShare(context, effect.uri, effect.mimeType)) {
                        scope.launch { snackbar.showSnackbar(context.getString(R.string.create_error_share_failed)) }
                    }
                }
                is GeneratedEffect.CopyText -> {
                    clipboard.setText(AnnotatedString(effect.text))
                    scope.launch { toast.show(context.getString(R.string.common_copied)) }
                }
                GeneratedEffect.RequestStoragePermission -> permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    BackHandler(enabled = state.editing) { viewModel.onEvent(GeneratedEvent.CloseEditor) }
    GeneratedScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onPickLogo = { logoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        toastState = toast,
        snackbarHostState = snackbar,
    )
}

/** Opens the system share sheet for [uri]; false when no app can handle it. */
private fun startShare(context: Context, uri: String, mimeType: String): Boolean {
    val stream = Uri.parse(uri)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, stream)
        clipData = ClipData.newRawUri(null, stream)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, context.getString(R.string.create_share_chooser_title))
    return try {
        context.startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}


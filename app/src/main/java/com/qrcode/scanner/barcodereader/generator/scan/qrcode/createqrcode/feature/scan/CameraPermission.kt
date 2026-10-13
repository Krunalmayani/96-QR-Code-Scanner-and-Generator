package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.CameraPermissionStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.resolveCameraPermission

/**
 * Camera permission flow for the Scan screen: first time shows the rationale dialog ([rationaleVisible]) and then
 * the system prompt; [status] says what to show when the camera cannot start.
 */
@Stable
class CameraPermissionState internal constructor(
    val status: CameraPermissionStatus,
    val rationaleVisible: Boolean,
    /** "Continue" in the rationale dialog: starts the system prompt. */
    val onRationaleAllow: () -> Unit,
    /** "Not now" / back in the rationale dialog. */
    val onRationaleDismiss: () -> Unit,
    /** "Request again": starts the system prompt directly. */
    val request: () -> Unit,
    val openSettings: () -> Unit,
    /** Re-reads the permission (call when the screen resumes, e.g. back from system settings). */
    val refresh: () -> Unit,
)

@Composable
fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    var requested by rememberSaveable { mutableStateOf(false) }
    var rationaleDismissed by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf(readCameraStatus(context, requested)) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            requested = true
            status = readCameraStatus(context, true)
        }
    return CameraPermissionState(
        status = status,
        rationaleVisible = status == CameraPermissionStatus.NOT_ASKED && !rationaleDismissed,
        onRationaleAllow = { launcher.launch(Manifest.permission.CAMERA) },
        onRationaleDismiss = { rationaleDismissed = true },
        request = { launcher.launch(Manifest.permission.CAMERA) },
        openSettings = { openAppSettings(context) },
        refresh = { status = readCameraStatus(context, requested) },
    )
}

private fun readCameraStatus(context: Context, requestedThisSession: Boolean): CameraPermissionStatus {
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val activity = context.findActivity()
    val showRationale =
        activity != null &&
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    return resolveCameraPermission(granted, showRationale, requestedThisSession)
}

/** Opens this app's page in the system settings (user tap only). */
internal fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No settings app to handle it: nothing else to offer.
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

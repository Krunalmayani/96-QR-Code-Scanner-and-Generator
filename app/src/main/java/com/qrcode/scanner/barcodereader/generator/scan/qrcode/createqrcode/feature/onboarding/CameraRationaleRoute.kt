package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions.CameraRationaleContent

/**
 * Last onboarding step (s05). "Allow camera" shows the system permission prompt, "Not now" skips it; either way
 * onboarding is then marked done and [onFinished] navigates to Scan.
 */
@Composable
internal fun CameraRationaleRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.Finished -> currentOnFinished()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.finish(if (granted) CameraStepResult.GRANTED else CameraStepResult.DENIED)
    }

    CameraRationaleContent(
        onAllow = {
            val alreadyGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            if (alreadyGranted) viewModel.finish(CameraStepResult.GRANTED) else launcher.launch(Manifest.permission.CAMERA)
        },
        onNotNow = { viewModel.finish(CameraStepResult.SKIPPED) },
    )
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.CameraRationaleOnboarding
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Feedback
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Onboarding
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Result as ResultRoute

/**
 * Overlay host for the app-wide prompts: the rate sheet (s71), the update dialog (s72) and the "update downloaded"
 * dialog. Call it once from the app shell, on top of the NavHost. Prompts never appear while [scanningInProgress],
 * on a result screen, or during onboarding; the policies live in `domain/system`.
 */
@Composable
fun SystemPromptsHost(
    navController: NavHostController,
    scanningInProgress: Boolean,
) {
    val viewModel: SystemPromptsViewModel = hiltViewModel()
    val activity = LocalContext.current.findActivity()
    val currentActivity by rememberUpdatedState(activity)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val suppressed = scanningInProgress || destination == null || destination.isQuietDestination()
    LaunchedEffect(suppressed) { viewModel.setSuppressed(suppressed) }

    UpdateCheckOnStart(activity, viewModel)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SystemPromptsEffect.OpenFeedback -> navController.navigate(Feedback)
                SystemPromptsEffect.LaunchReview -> currentActivity?.let { launchInAppReview(it) }
            }
        }
    }

    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    when (prompt) {
        ActivePrompt.RATE -> RateSheet(onRate = viewModel::onRateConfirmed, onLater = viewModel::onRateDismissed)
        ActivePrompt.UPDATE ->
            UpdateDialog(
                onUpdate = { viewModel.onUpdateConfirmed(currentActivity) },
                onLater = viewModel::onUpdateLater,
            )
        ActivePrompt.UPDATE_READY ->
            UpdateReadyDialog(
                onRestart = viewModel::onUpdateReadyConfirmed,
                onLater = viewModel::onUpdateReadyDismissed,
            )
        ActivePrompt.NONE -> Unit
    }
}

/** Asks Play for an update every time the app starts or returns to the foreground. */
@Composable
private fun UpdateCheckOnStart(activity: Activity?, viewModel: SystemPromptsViewModel) {
    val owner = activity as? LifecycleOwner ?: return
    val lifecycle = remember(owner) { owner.lifecycle }
    DisposableEffect(lifecycle, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) viewModel.refreshUpdate()
            }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

/** Screens where an overlay would interrupt: reading a result, onboarding, the feedback form itself. */
private fun NavDestination.isQuietDestination(): Boolean =
    hasRoute<ResultRoute>() ||
        hasRoute<Onboarding>() ||
        hasRoute<CameraRationaleOnboarding>() ||
        hasRoute<Feedback>()

/** Unwraps [ContextWrapper]s (Compose themes wrap the Activity) down to the hosting [Activity], or null. */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

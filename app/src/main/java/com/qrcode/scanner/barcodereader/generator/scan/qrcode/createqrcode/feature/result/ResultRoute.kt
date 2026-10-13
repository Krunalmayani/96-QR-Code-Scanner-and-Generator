package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LocalAdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform.ExternalLauncher
import kotlinx.coroutines.launch

/**
 * Stateful entry of the result screen: connects the ViewModel to Android (clipboard, intents, Custom Tabs, the
 * location permission launcher) and to navigation through the lambdas.
 */
@Composable
fun ResultRoute(
    onBack: () -> Unit,
    onDone: () -> Unit,
    onOpenGenerated: (recordId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val dark = AppTheme.colors.isDark
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onEvent(ResultEvent.LocationPermissionResult(granted))
    }

    ResultAdEffect(state)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            val message = perform(effect, context, dark, onBack, onDone, onOpenGenerated) {
                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (message != null) {
                val text = context.getString(message.messageRes())
                launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(text)
                }
            }
        }
    }

    ResultScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

/** Tells the ad controller once that a NEW result is on screen (never for a history item). */
@Composable
private fun ResultAdEffect(state: ResultUiState) {
    val controller = LocalAdController.current
    val record = state.record
    var counted by rememberSaveable(record?.id) { mutableStateOf(false) }
    LaunchedEffect(record?.id, state.fromHistory) {
        if (record != null && !state.fromHistory && !counted) {
            counted = true
            controller.onResultShown()
        }
    }
}

/** Runs one effect. Returns the message to show in the snackbar, if any. */
private fun perform(
    effect: ResultEffect,
    context: Context,
    dark: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onOpenGenerated: (Long) -> Unit,
    requestLocation: () -> Unit,
): ResultMessage? = when (effect) {
    is ResultEffect.CopyText -> if (ExternalLauncher.copy(context, effect.text, effect.sensitive)) ResultMessage.COPIED else ResultMessage.CANNOT_OPEN
    is ResultEffect.ShareText -> if (ExternalLauncher.shareText(context, effect.text)) null else ResultMessage.NO_APP_FOUND
    is ResultEffect.OpenUrl -> if (ExternalLauncher.openCustomTab(context, effect.url, dark)) null else ResultMessage.NO_APP_FOUND
    is ResultEffect.Launch -> if (ExternalLauncher.launch(context, effect.action)) null else ResultMessage.NO_APP_FOUND
    is ResultEffect.ShowMessage -> effect.message
    ResultEffect.RequestLocationPermission -> {
        requestLocation()
        null
    }
    ResultEffect.NavigateBack -> {
        onBack()
        null
    }
    ResultEffect.NavigateToScan -> {
        onDone()
        null
    }
    is ResultEffect.OpenGenerated -> {
        onOpenGenerated(effect.recordId)
        null
    }
}

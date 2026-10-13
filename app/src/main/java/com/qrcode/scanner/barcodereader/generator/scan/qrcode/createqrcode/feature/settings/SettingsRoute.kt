package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import kotlinx.coroutines.launch

/** Where the Settings list navigates to; wired by [settingsGraph]. */
data class SettingsNavigation(
    val toTheme: () -> Unit,
    val toLanguage: () -> Unit,
    val toHelp: () -> Unit,
    val toFeedback: () -> Unit,
    val toAbout: () -> Unit,
    val toExport: () -> Unit,
    val toPrivacySummary: () -> Unit,
    val toComponentGallery: () -> Unit,
)

@Composable
fun SettingsRoute(navigation: SettingsNavigation, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    fun tell(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.HistoryCleared ->
                    snackbar.showSnackbar(
                        context.resources.getQuantityString(R.plurals.settings_history_cleared, effect.count, effect.count),
                    )
                SettingsEffect.HistoryAlreadyEmpty -> snackbar.showSnackbar(context.getString(R.string.settings_history_empty))
                SettingsEffect.Failed -> snackbar.showSnackbar(context.getString(R.string.common_something_went_wrong))
            }
        }
    }

    val actions =
        SettingsActions(
            onTheme = navigation.toTheme,
            onLanguage = navigation.toLanguage,
            onHelp = navigation.toHelp,
            onFeedback = navigation.toFeedback,
            onAbout = navigation.toAbout,
            onExport = navigation.toExport,
            onPrivacySummary = navigation.toPrivacySummary,
            onPrivacyPolicyWeb = {
                if (!SettingsIntents.openUrl(context, context.getString(R.string.privacy_policy_url))) {
                    tell(context.getString(R.string.settings_no_app_found))
                }
            },
            onManagePermissions = {
                if (!SettingsIntents.openAppSettings(context)) tell(context.getString(R.string.settings_no_app_found))
            },
            onRate = { SettingsIntents.rateApp(context) { tell(context.getString(R.string.settings_play_unavailable)) } },
            onShareApp = { shareApp(context) { tell(context.getString(R.string.settings_no_app_found)) } },
            onGallery = if (BuildConfig.DEBUG) navigation.toComponentGallery else null,
        )

    SettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        actions = actions,
        snackbarHostState = snackbar,
        versionName = BuildConfig.VERSION_NAME,
    )

    state.clearDialog?.let { dialog ->
        ClearHistoryDialog(
            state = dialog,
            onKeepFavoritesChange = { viewModel.onEvent(SettingsEvent.ClearKeepFavoritesChanged(it)) },
            onConfirm = { viewModel.onEvent(SettingsEvent.ClearConfirmed) },
            onDismiss = { viewModel.onEvent(SettingsEvent.ClearDismissed) },
        )
    }
}

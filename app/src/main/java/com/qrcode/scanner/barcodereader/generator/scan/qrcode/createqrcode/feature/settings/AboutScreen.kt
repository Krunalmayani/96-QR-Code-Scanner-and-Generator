package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AboutRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun tell(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }
    AboutScreen(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        actions =
            AboutActions(
                onRate = { SettingsIntents.rateApp(context) { tell(context.getString(R.string.settings_play_unavailable)) } },
                onShare = { shareApp(context) { tell(context.getString(R.string.settings_no_app_found)) } },
                onLicenses = {
                    if (!SettingsIntents.openLicenses(context, context.getString(R.string.settings_about_licenses))) {
                        tell(context.getString(R.string.settings_no_app_found))
                    }
                },
                onTerms = {
                    if (!SettingsIntents.openUrl(context, context.getString(R.string.settings_terms_url))) {
                        tell(context.getString(R.string.settings_no_app_found))
                    }
                },
            ),
        onBack = onBack,
        snackbarHostState = snackbar,
    )
}

/** Share sheet with the app name and its Play link; [onFailed] runs when no app can share text. */
internal fun shareApp(context: Context, onFailed: () -> Unit) {
    val text = context.getString(R.string.settings_share_text, context.getString(R.string.app_name), SettingsIntents.playWebLink())
    if (!SettingsIntents.shareText(context, text, context.getString(R.string.settings_share_app))) onFailed()
}

data class AboutActions(
    val onRate: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onLicenses: () -> Unit = {},
    val onTerms: () -> Unit = {},
)

/** About screen (s62): app identity, version, Rate, Share, Open source licenses and Terms. */
@Composable
fun AboutScreen(
    versionName: String,
    versionCode: Int,
    actions: AboutActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val c = AppTheme.colors
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_about_title), onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppMark()
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = AppTheme.type.headline,
                    color = c.ink,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.settings_about_version, versionName, versionCode),
                    style = AppTheme.type.caption,
                    color = c.inkSecondary,
                )
            }
            CardGroup(Modifier.fillMaxWidth()) {
                SettingsActionItem(title = stringResource(R.string.settings_rate), onClick = actions.onRate, icon = AppIcons.Star)
                AppDivider()
                SettingsActionItem(title = stringResource(R.string.settings_share_app), onClick = actions.onShare, icon = AppIcons.Share)
                AppDivider()
                SettingsActionItem(
                    title = stringResource(R.string.settings_about_licenses),
                    onClick = actions.onLicenses,
                    icon = AppIcons.Document,
                )
                AppDivider()
                SettingsActionItem(title = stringResource(R.string.settings_about_terms), onClick = actions.onTerms, icon = AppIcons.Info)
            }
            Text(stringResource(R.string.settings_made_by), style = AppTheme.type.caption, color = c.inkSecondary)
        }
    }
}

/** The launcher mark on its brand gradient (the same in light and dark, as in the design). */
@Composable
private fun AppMark() {
    Box(
        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)).background(SettingsBrand.logoBrush),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "S62_About_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S62_About_Light() =
    AppTheme(dark = false) {
        AboutScreen(versionName = "2.3.0", versionCode = 230, actions = AboutActions(), onBack = {})
    }

@Preview(name = "S62_About_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S62_About_Dark() =
    AppTheme(dark = true) {
        AboutScreen(versionName = "2.3.0", versionCode = 230, actions = AboutActions(), onBack = {})
    }

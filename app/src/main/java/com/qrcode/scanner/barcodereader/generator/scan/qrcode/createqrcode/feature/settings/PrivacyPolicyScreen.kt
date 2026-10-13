package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun PrivacyPolicyRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    PrivacyPolicyScreen(
        onReadFullPolicy = {
            if (!SettingsIntents.openUrl(context, context.getString(R.string.privacy_policy_url))) {
                scope.launch { snackbar.showSnackbar(context.getString(R.string.settings_no_app_found)) }
            }
        },
        onBack = onBack,
        snackbarHostState = snackbar,
    )
}

/** Privacy policy summary (s60) with a button that opens the full hosted policy. */
@Composable
fun PrivacyPolicyScreen(
    onReadFullPolicy: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_privacy_title), onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            ActionBar {
                AppOutlinedButton(
                    text = stringResource(R.string.settings_privacy_read_full),
                    onClick = onReadFullPolicy,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = AppIcons.OpenInNew,
                )
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrivacyHero()
            CardGroup {
                ListRow(
                    title = stringResource(R.string.settings_perm_camera),
                    subtitle = stringResource(R.string.settings_privacy_camera),
                    leading = { IconTile(AppIcons.Camera) },
                )
                AppDivider()
                ListRow(
                    title = stringResource(R.string.settings_perm_photos),
                    subtitle = stringResource(R.string.settings_privacy_photos),
                    leading = { IconTile(AppIcons.Image) },
                )
                AppDivider()
                ListRow(
                    title = stringResource(R.string.settings_privacy_history_title),
                    subtitle = stringResource(R.string.settings_privacy_history),
                    leading = { IconTile(AppIcons.Lock) },
                )
            }
            AppCard {
                Text(
                    text = stringResource(R.string.settings_privacy_note),
                    style = AppTheme.type.body,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        }
    }
}

@Composable
private fun PrivacyHero() {
    val c = AppTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(c.primary, c.onPrimaryContainer)))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(AppIcons.Shield, contentDescription = null, tint = c.onPrimary)
        Text(stringResource(R.string.settings_privacy_hero_title), style = AppTheme.type.titleLarge, color = c.onPrimary)
        Text(stringResource(R.string.settings_privacy_hero_text), style = AppTheme.type.body, color = c.onPrimary)
    }
}

@Preview(name = "S60_PrivacyPolicy_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S60_PrivacyPolicy_Light() =
    AppTheme(dark = false) {
        PrivacyPolicyScreen(onReadFullPolicy = {}, onBack = {})
    }

@Preview(name = "S60_PrivacyPolicy_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S60_PrivacyPolicy_Dark() =
    AppTheme(dark = true) {
        PrivacyPolicyScreen(onReadFullPolicy = {}, onBack = {})
    }

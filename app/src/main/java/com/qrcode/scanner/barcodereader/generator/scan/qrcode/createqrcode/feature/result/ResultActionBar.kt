package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase

/** A single pinned button is 52dp (s15 to s25); two side by side are 48dp (s16, s18, s25). */
private val ButtonHeight = 52.dp
private val PairHeight = 48.dp

/** True when [state] shows an ActionBar at the bottom (s30 / s70 have none). */
internal fun hasActionBar(state: ResultUiState): Boolean = state.record != null && !state.product.searchOpen

/** The pinned bottom bar: the primary action of the type, or the Wi-Fi connect-flow buttons. */
@Composable
internal fun ResultActionBar(state: ResultUiState, record: ScanRecord, onEvent: (ResultEvent) -> Unit) {
    ActionBar {
        when {
            state.wifi.flowVisible -> WifiFlowButtons(state, onEvent)
            state.fromHistory && record.source == ScanSource.CREATED -> CreatedButtons(onEvent)
            else -> TypeButtons(record.content, onEvent)
        }
    }
}

@Composable
private fun RowScope.Primary(text: String, icon: ImageVector?, onClick: () -> Unit) {
    PrimaryButton(text, onClick, Modifier.weight(1f).heightIn(min = ButtonHeight), leadingIcon = icon)
}

@Composable
private fun RowScope.PairPrimary(text: String, icon: ImageVector?, onClick: () -> Unit) {
    PrimaryButton(text, onClick, Modifier.weight(1f).heightIn(min = PairHeight), leadingIcon = icon)
}

@Composable
private fun RowScope.PairTonal(text: String, icon: ImageVector?, onClick: () -> Unit) {
    TonalButton(text, onClick, Modifier.weight(1f).heightIn(min = PairHeight), leadingIcon = icon)
}

/** s50: one full-width "Share QR image" (opens the share sheet). */
@Composable
private fun RowScope.CreatedButtons(onEvent: (ResultEvent) -> Unit) {
    Primary(stringResource(R.string.result_action_share_qr_image), AppIcons.Share) { onEvent(ResultEvent.ShareClicked) }
}

@Composable
private fun RowScope.WifiFlowButtons(state: ResultUiState, onEvent: (ResultEvent) -> Unit) {
    when (state.wifi.phase) {
        WifiPhase.CONNECTING ->
            AppOutlinedButton(
                stringResource(R.string.common_cancel),
                { onEvent(ResultEvent.WifiCancel) },
                Modifier.weight(1f).heightIn(min = PairHeight),
            )
        WifiPhase.CONNECTED -> Primary(stringResource(R.string.common_done), null) { onEvent(ResultEvent.WifiDone) }
        WifiPhase.FAILED -> Primary(stringResource(R.string.common_retry), AppIcons.Refresh) { onEvent(ResultEvent.WifiRetry) }
        WifiPhase.IDLE -> Unit
    }
}

@Composable
private fun RowScope.TypeButtons(content: ScanContent, onEvent: (ResultEvent) -> Unit) {
    val primary = { onEvent(ResultEvent.PrimaryAction) }
    when (content) {
        is ScanContent.Website -> Primary(stringResource(R.string.result_action_open_link), AppIcons.OpenInNew, primary)
        is ScanContent.Text -> {
            PairTonal(stringResource(R.string.result_action_copy_text), AppIcons.Copy) { onEvent(ResultEvent.CopyClicked) }
            PairPrimary(stringResource(R.string.result_action_search_web), AppIcons.Search) { onEvent(ResultEvent.SearchWebClicked) }
        }
        is ScanContent.Contact -> Primary(stringResource(R.string.result_action_add_contact), AppIcons.Create, primary)
        is ScanContent.Wifi -> WifiButtons(content, onEvent)
        is ScanContent.Event -> Primary(stringResource(R.string.result_action_add_calendar), AppIcons.Calendar, primary)
        is ScanContent.Phone -> Primary(stringResource(R.string.result_action_call), AppIcons.Phone, primary)
        is ScanContent.Email -> Primary(stringResource(R.string.result_action_compose_email), AppIcons.Email, primary)
        is ScanContent.Sms -> Primary(stringResource(R.string.result_action_open_messages), AppIcons.Sms, primary)
        is ScanContent.Location -> Primary(stringResource(R.string.result_action_open_maps), AppIcons.OpenInNew, primary)
        is ScanContent.Product -> Primary(stringResource(R.string.result_action_search_product), AppIcons.Search, primary)
        is ScanContent.Unknown -> {
            PairTonal(stringResource(R.string.common_share), AppIcons.Share) { onEvent(ResultEvent.ShareClicked) }
            PairPrimary(stringResource(R.string.result_action_copy_data), AppIcons.Copy) { onEvent(ResultEvent.CopyClicked) }
        }
    }
}

@Composable
private fun RowScope.WifiButtons(content: ScanContent.Wifi, onEvent: (ResultEvent) -> Unit) {
    if (content.security != WifiSecurity.NONE && content.password.isNotEmpty()) {
        PairTonal(stringResource(R.string.result_action_copy_password), AppIcons.Copy) { onEvent(ResultEvent.WifiCopyPassword) }
        PairPrimary(stringResource(R.string.result_action_connect), AppIcons.Wifi) { onEvent(ResultEvent.WifiConnectClicked) }
    } else {
        Primary(stringResource(R.string.result_action_connect), AppIcons.Wifi) { onEvent(ResultEvent.WifiConnectClicked) }
    }
}

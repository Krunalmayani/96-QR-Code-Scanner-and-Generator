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

private val ButtonHeight = 52.dp

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
private fun RowScope.Tonal(text: String, icon: ImageVector?, onClick: () -> Unit) {
    TonalButton(text, onClick, Modifier.weight(1f).heightIn(min = ButtonHeight), leadingIcon = icon)
}

@Composable
private fun RowScope.CreatedButtons(onEvent: (ResultEvent) -> Unit) {
    Tonal(stringResource(R.string.result_action_edit_style), AppIcons.Edit) { onEvent(ResultEvent.EditStyleClicked) }
    Primary(stringResource(R.string.common_share), AppIcons.Share) { onEvent(ResultEvent.ShareClicked) }
}

@Composable
private fun RowScope.WifiFlowButtons(state: ResultUiState, onEvent: (ResultEvent) -> Unit) {
    when (state.wifi.phase) {
        WifiPhase.CONNECTING -> AppOutlinedButton(
            stringResource(R.string.common_cancel),
            { onEvent(ResultEvent.WifiCancel) },
            Modifier.weight(1f).heightIn(min = ButtonHeight),
        )
        WifiPhase.CONNECTED -> Primary(stringResource(R.string.common_done), null) { onEvent(ResultEvent.WifiDone) }
        WifiPhase.FAILED -> Primary(stringResource(R.string.common_retry), null) { onEvent(ResultEvent.WifiRetry) }
        WifiPhase.IDLE -> Unit
    }
}

@Composable
private fun RowScope.TypeButtons(content: ScanContent, onEvent: (ResultEvent) -> Unit) {
    val primary = { onEvent(ResultEvent.PrimaryAction) }
    when (content) {
        is ScanContent.Website -> Primary(stringResource(R.string.result_action_open_link), AppIcons.OpenInNew, primary)
        is ScanContent.Text -> {
            Tonal(stringResource(R.string.result_action_copy_text), AppIcons.Copy) { onEvent(ResultEvent.CopyClicked) }
            Primary(stringResource(R.string.result_action_share_text), AppIcons.Share) { onEvent(ResultEvent.ShareClicked) }
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
            Tonal(stringResource(R.string.common_share), AppIcons.Share) { onEvent(ResultEvent.ShareClicked) }
            Primary(stringResource(R.string.result_action_copy_data), AppIcons.Copy) { onEvent(ResultEvent.CopyClicked) }
        }
    }
}

@Composable
private fun RowScope.WifiButtons(content: ScanContent.Wifi, onEvent: (ResultEvent) -> Unit) {
    if (content.security != WifiSecurity.NONE && content.password.isNotEmpty()) {
        Tonal(stringResource(R.string.result_action_copy_password), AppIcons.Copy) { onEvent(ResultEvent.WifiCopyPassword) }
    }
    Primary(stringResource(R.string.result_action_connect), AppIcons.Wifi) { onEvent(ResultEvent.WifiConnectClicked) }
}

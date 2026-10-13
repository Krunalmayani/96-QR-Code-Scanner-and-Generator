package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IndeterminateBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase

private const val MASK_LENGTH = 12
private const val MASK_CHAR = "•"

/** s18: network name, security, masked password with show / copy, and the note about the Android prompt. */
@Composable
internal fun WifiContent(content: ScanContent.Wifi, onEvent: (ResultEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NetworkCard(content)
        PasswordCard(content.security, content.password, onEvent)
        TitledBanner(
            title = stringResource(R.string.result_wifi_info_title),
            body = stringResource(R.string.result_wifi_info_body),
            tone = Tone.Primary,
            icon = AppIcons.Info,
            linkText = stringResource(R.string.result_wifi_why_location),
            onLinkClick = { onEvent(ResultEvent.WhyLocationClicked) },
        )
    }
}

@Composable
private fun NetworkCard(content: ScanContent.Wifi) {
    AppCard(Modifier.fillMaxWidth()) {
        OverlineLabel(stringResource(R.string.result_wifi_network_name))
        SelectionContainer {
            Text(
                content.ssid,
                style = AppTheme.type.headline.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.ink,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(
                text = stringResource(content.security.labelRes()),
                icon = if (content.security == WifiSecurity.NONE) null else AppIcons.Lock,
            )
            AppBadge(stringResource(if (content.hidden) R.string.result_wifi_hidden else R.string.result_wifi_visible))
        }
    }
}

@Composable
private fun PasswordCard(security: WifiSecurity, password: String, onEvent: (ResultEvent) -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AppCard(Modifier.fillMaxWidth()) {
        OverlineLabel(stringResource(R.string.result_wifi_password))
        if (security == WifiSecurity.NONE || password.isEmpty()) {
            Text(
                stringResource(R.string.result_wifi_no_password),
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.ink,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            PasswordRow(password, visible, onToggle = { visible = !visible }, onCopy = { onEvent(ResultEvent.WifiCopyPassword) })
        }
    }
}

@Composable
private fun PasswordRow(password: String, visible: Boolean, onToggle: () -> Unit, onCopy: () -> Unit) {
    Row(
        Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (visible) password else MASK_CHAR.repeat(MASK_LENGTH),
            style = AppTheme.type.mono.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f),
        )
        IconButtonRounded(
            icon = if (visible) AppIcons.Hidden else AppIcons.Visible,
            contentDescription = stringResource(if (visible) R.string.common_hide_password else R.string.common_show_password),
            onClick = onToggle,
            style = IconButtonStyle.Filled,
        )
        IconButtonRounded(
            icon = AppIcons.Copy,
            contentDescription = stringResource(R.string.result_action_copy_password),
            onClick = onCopy,
            style = IconButtonStyle.Filled,
        )
    }
}

/** s26 to s28: the three states of the connect flow. They replace the normal result until dismissed. */
@Composable
internal fun WifiFlowContent(
    content: ScanContent.Wifi,
    state: ResultUiState,
    onEvent: (ResultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.wifi.phase) {
        WifiPhase.CONNECTING -> WifiConnecting(content, modifier)
        WifiPhase.CONNECTED -> WifiConnected(content, state, modifier)
        WifiPhase.FAILED -> WifiFailed(content, state, onEvent, modifier)
        WifiPhase.IDLE -> Unit
    }
}

/**
 * Body shared by s26 to s28: a 96dp disc, the heading and message (announced as a live region when the state changes),
 * then [content]. Top-aligned like the artboards (64dp below the top bar).
 */
@Composable
private fun WifiStateLayout(
    icon: ImageVector,
    tone: Tone,
    title: String,
    message: String,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val c = AppTheme.colors
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(c.containerOf(tone)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(44.dp))
        }
        Column(
            Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                title,
                style = AppTheme.type.headline,
                color = c.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(message, style = AppTheme.type.body, color = c.inkSecondary, textAlign = TextAlign.Center)
        }
        content()
    }
}

@Composable
private fun WifiConnecting(content: ScanContent.Wifi, modifier: Modifier) {
    WifiStateLayout(
        icon = AppIcons.Wifi,
        tone = Tone.Success,
        title = stringResource(R.string.result_wifi_connecting_title, content.ssid),
        message = stringResource(R.string.result_wifi_connecting_body),
        modifier = modifier,
    ) {
        Box(Modifier.width(200.dp)) { IndeterminateBar() }
    }
}

@Composable
private fun WifiConnected(content: ScanContent.Wifi, state: ResultUiState, modifier: Modifier) {
    val saved = state.wifi.viaSuggestion
    val savedToHistory = !state.fromHistory && (state.record?.id ?: 0L) > 0L
    val lines = ArrayList<DetailLine>()
    lines += DetailLine(stringResource(R.string.result_wifi_security), stringResource(content.security.labelRes()))
    if (savedToHistory) {
        lines += DetailLine(stringResource(R.string.result_wifi_saved_to), stringResource(R.string.result_wifi_saved_to_history))
    }
    WifiStateLayout(
        icon = AppIcons.Check,
        tone = Tone.Success,
        title = stringResource(if (saved) R.string.result_wifi_saved_title else R.string.result_wifi_connected_title),
        message = stringResource(if (saved) R.string.result_wifi_saved_body else R.string.result_wifi_connected_body, content.ssid),
        modifier = modifier,
    ) {
        DetailsCard(lines)
    }
}

@Composable
private fun WifiFailed(content: ScanContent.Wifi, state: ResultUiState, onEvent: (ResultEvent) -> Unit, modifier: Modifier) {
    val failure = state.wifi.failure
    val showPassword = content.security != WifiSecurity.NONE && content.password.isNotEmpty()
    WifiStateLayout(
        icon = AppIcons.Close,
        tone = Tone.Error,
        title = stringResource(R.string.result_wifi_failed_title),
        message = stringResource((failure ?: WifiFailure.UNAVAILABLE).messageRes()),
        modifier = modifier,
    ) {
        FailedDetails(content, showPassword, onEvent)
    }
}

@Composable
private fun FailedDetails(content: ScanContent.Wifi, showPassword: Boolean, onEvent: (ResultEvent) -> Unit) {
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        LabeledRow(stringResource(R.string.result_wifi_network)) {
            Text(content.ssid, style = AppTheme.type.bodyStrong, color = AppTheme.colors.ink, modifier = Modifier.weight(1f))
        }
        if (showPassword) {
            AppDivider()
            LabeledRow(stringResource(R.string.result_wifi_password), vertical = 0.dp) {
                SelectionContainer(Modifier.weight(1f)) {
                    Text(content.password, style = AppTheme.type.mono, color = AppTheme.colors.ink)
                }
                IconButtonRounded(
                    icon = AppIcons.Copy,
                    contentDescription = stringResource(R.string.result_action_copy_password),
                    onClick = { onEvent(ResultEvent.WifiCopyPassword) },
                )
            }
        }
    }
}

/** s50: a created Wi-Fi code in history lists network, security and the masked password in one card. */
@Composable
internal fun CreatedWifiContent(content: ScanContent.Wifi) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val hasPassword = content.security != WifiSecurity.NONE && content.password.isNotEmpty()
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        LabeledRow(stringResource(R.string.result_wifi_network)) {
            SelectionContainer(Modifier.weight(1f)) {
                Text(content.ssid, style = AppTheme.type.bodyStrong, color = AppTheme.colors.ink)
            }
        }
        AppDivider()
        LabeledRow(stringResource(R.string.result_wifi_security)) {
            Text(stringResource(content.security.labelRes()), style = AppTheme.type.bodyStrong, color = AppTheme.colors.ink)
        }
        AppDivider()
        LabeledRow(stringResource(R.string.result_wifi_password), vertical = if (hasPassword) 0.dp else 12.dp) {
            if (hasPassword) {
                Text(
                    text = if (visible) content.password else MASK_CHAR.repeat(MASK_LENGTH),
                    style = AppTheme.type.mono.copy(letterSpacing = 0.1.em),
                    color = AppTheme.colors.ink,
                    modifier = Modifier.weight(1f),
                )
                IconButtonRounded(
                    icon = if (visible) AppIcons.Hidden else AppIcons.Visible,
                    contentDescription = stringResource(if (visible) R.string.common_hide_password else R.string.common_show_password),
                    onClick = { visible = !visible },
                )
            } else {
                Text(stringResource(R.string.result_wifi_no_password), style = AppTheme.type.bodyStrong, color = AppTheme.colors.ink)
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, vertical: Dp = 12.dp, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = vertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = AppTheme.type.bodySmall, color = AppTheme.colors.inkSecondary, modifier = Modifier.width(84.dp))
        content()
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.InfoBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalActions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.HexDump
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.LinkRiskReasons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.UrlDisplay
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult

/** Content card(s) of a result, one branch per [ScanContent] subtype (artboards s15 to s25). */
@Composable
internal fun TypeContent(state: ResultUiState, record: ScanRecord, onEvent: (ResultEvent) -> Unit) {
    when (val content = record.content) {
        is ScanContent.Website -> WebsiteContent(content, state.safety)
        is ScanContent.Text -> TextContent(content, record)
        is ScanContent.Contact -> ContactContent(content, onEvent)
        is ScanContent.Phone -> PhoneContent(content)
        is ScanContent.Email -> EmailContent(content)
        is ScanContent.Sms -> SmsContent(content)
        is ScanContent.Location -> LocationContent(content)
        is ScanContent.Event -> EventContent(content)
        is ScanContent.Wifi -> WifiContent(content, onEvent)
        is ScanContent.Product -> ProductContent(content, record)
        is ScanContent.Unknown -> UnknownContent(content)
    }
}

// ---- s15 / s74 Website ----------------------------------------------------------------------------------------

@Composable
internal fun WebsiteContent(content: ScanContent.Website, safety: LinkSafetyResult?) {
    val parts = remember(content.url) { UrlDisplay.split(content.url) }
    val secure = LinkRiskReasons.isSecure(content.url)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_website_opens))
            SelectionContainer {
                Text(
                    parts.host,
                    style = AppTheme.type.display.copy(fontSize = 26.sp, lineHeight = 32.sp),
                    color = AppTheme.colors.ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (parts.rest.isNotEmpty()) {
                Text(parts.rest, style = AppTheme.type.mono, color = AppTheme.colors.inkSecondary, modifier = Modifier.padding(top = 4.dp))
            }
            Row(
                Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppBadge(
                    text = stringResource(if (secure) R.string.result_website_https else R.string.result_website_http),
                    tone = if (secure) Tone.Success else Tone.Warning,
                    icon = if (secure) AppIcons.Lock else AppIcons.Warning,
                )
                Text(
                    stringResource(if (secure) R.string.result_website_secure else R.string.result_website_not_secure),
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        }
        if (safety != null && !safety.isSafe) {
            TitledBanner(
                title = stringResource(R.string.result_website_check_title),
                body = stringResource(R.string.result_website_check_body),
            )
        }
        DetailsCard(
            listOf(
                DetailLine(stringResource(R.string.result_label_type), stringResource(R.string.result_website_type_value)),
                DetailLine(stringResource(R.string.result_website_full_link), content.url, mono = true),
            ),
        )
    }
}

// ---- s16 Text -------------------------------------------------------------------------------------------------

private fun Context.charactersText(count: Int): String = resources.getQuantityString(R.plurals.result_characters, count, count)

@Composable
internal fun TextContent(content: ScanContent.Text, record: ScanRecord) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_text_content))
            SelectionContainer {
                Text(
                    content.rawValue,
                    style = AppTheme.type.title.copy(fontSize = 18.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
                    color = AppTheme.colors.ink,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Text(
                stringResource(R.string.result_text_stats, context.charactersText(content.rawValue.length)),
                style = AppTheme.type.caption,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        DetailsCard(
            listOf(
                DetailLine(stringResource(R.string.result_label_type), stringResource(R.string.result_text_type_value)),
                DetailLine(stringResource(R.string.result_label_format), content.format.displayName),
                DetailLine(stringResource(R.string.result_label_source), sourceLabel(record.source)),
            ),
        )
    }
}

// ---- s17 Contact ----------------------------------------------------------------------------------------------

private class ContactRow(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val onClick: (() -> Unit)? = null,
    val maxLines: Int = 1,
)

@Composable
internal fun ContactContent(content: ScanContent.Contact, onEvent: (ResultEvent) -> Unit) {
    val rows = contactRows(content, onEvent)
    if (rows.isEmpty()) return
    CardGroup {
        rows.forEachIndexed { index, row ->
            if (index > 0) AppDivider()
            ListRow(
                title = row.value,
                subtitle = row.label,
                leading = { IconTile(row.icon) },
                onClick = row.onClick,
                titleMaxLines = row.maxLines,
            )
        }
    }
}

@Composable
private fun contactRows(content: ScanContent.Contact, onEvent: (ResultEvent) -> Unit): List<ContactRow> {
    val rows = ArrayList<ContactRow>()
    val phoneLabel = stringResource(R.string.result_contact_phone)
    val emailLabel = stringResource(R.string.result_contact_email)
    content.phones.forEach { number ->
        rows += ContactRow(AppIcons.Phone, phoneLabel, number, { onEvent(ResultEvent.DialClicked(number)) })
    }
    content.emails.forEach { address ->
        rows += ContactRow(AppIcons.Email, emailLabel, address, { onEvent(ResultEvent.ComposeEmailClicked(address)) })
    }
    val company = listOf(content.organization, content.jobTitle).filter { it.isNotBlank() }.joinToString(", ")
    if (company.isNotEmpty()) rows += ContactRow(AppIcons.Person, stringResource(R.string.result_contact_company), company)
    if (content.address.isNotBlank()) {
        rows += ContactRow(AppIcons.Location, stringResource(R.string.result_contact_address), content.address, maxLines = ADDRESS_LINES)
    }
    if (content.website.isNotBlank()) rows += ContactRow(AppIcons.Link, stringResource(R.string.result_contact_website), content.website)
    if (content.note.isNotBlank()) {
        rows += ContactRow(AppIcons.Notes, stringResource(R.string.result_contact_note), content.note, maxLines = ADDRESS_LINES)
    }
    return rows
}

private const val ADDRESS_LINES = 4

// ---- s20 Phone ------------------------------------------------------------------------------------------------

@Composable
internal fun PhoneContent(content: ScanContent.Phone) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 28.dp)) {
            OverlineLabel(stringResource(R.string.result_phone_label), Modifier.fillMaxWidth())
            SelectionContainer {
                Text(
                    content.number,
                    style = AppTheme.type.display.copy(fontSize = 30.sp, lineHeight = 38.sp),
                    color = AppTheme.colors.ink,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        InfoBanner(stringResource(R.string.result_phone_dialer_note), Modifier.fillMaxWidth(), Tone.Primary, AppIcons.Info)
    }
}

// ---- s21 Email ------------------------------------------------------------------------------------------------

@Composable
internal fun EmailContent(content: ScanContent.Email) {
    val lines =
        buildList {
            add(DetailLine(stringResource(R.string.result_email_to_label), content.address))
            if (content.subject.isNotBlank()) add(DetailLine(stringResource(R.string.result_email_subject), content.subject))
            if (content.body.isNotBlank()) add(DetailLine(stringResource(R.string.result_email_message), content.body))
        }
    DetailsCard(lines)
}

// ---- s22 SMS --------------------------------------------------------------------------------------------------

@Composable
internal fun SmsContent(content: ScanContent.Sms) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_sms_to))
            SelectionContainer {
                Text(content.number, style = AppTheme.type.title, color = AppTheme.colors.ink, modifier = Modifier.padding(top = 2.dp))
            }
            if (content.message.isNotBlank()) {
                MessageBubble(content.message, Modifier.padding(top = 16.dp))
            }
        }
        InfoBanner(stringResource(R.string.result_sms_note), Modifier.fillMaxWidth(), Tone.Warning, AppIcons.Info)
    }
}

// ---- s23 Location ---------------------------------------------------------------------------------------------

@Composable
internal fun LocationContent(content: ScanContent.Location) {
    val latitude = remember(content.latitude) { ExternalActions.formatCoordinate(content.latitude) }
    val longitude = remember(content.longitude) { ExternalActions.formatCoordinate(content.longitude) }
    val lines =
        buildList {
            if (content.label.isNotBlank()) add(DetailLine(stringResource(R.string.result_location_name), content.label))
            add(DetailLine(stringResource(R.string.result_location_latitude), latitude, mono = true))
            add(DetailLine(stringResource(R.string.result_location_longitude), longitude, mono = true))
        }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MapPlaceholder()
        DetailsCard(lines)
    }
}

// ---- s25 Unknown ----------------------------------------------------------------------------------------------

@Composable
internal fun UnknownContent(content: ScanContent.Unknown) {
    val context = LocalContext.current
    var view by rememberSaveable { mutableStateOf(VIEW_TEXT) }
    val hex = remember(content.rawValue) { HexDump.of(content.rawValue) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_unknown_raw))
            SegmentedControl(
                options = listOf(stringResource(R.string.result_unknown_view_text), stringResource(R.string.result_unknown_view_hex)),
                selectedIndex = view,
                onSelect = { view = it },
                modifier = Modifier.padding(top = 8.dp),
            )
            if (view == VIEW_TEXT) {
                RawText(content.rawValue)
            } else {
                HexView(hex.lines, Modifier.padding(top = 12.dp))
                if (hex.truncated) {
                    Text(
                        stringResource(R.string.result_unknown_hex_truncated, HexDump.MAX_BYTES, hex.totalBytes),
                        style = AppTheme.type.caption,
                        color = AppTheme.colors.inkSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.result_unknown_not_recognized, context.charactersText(content.rawValue.length)),
                style = AppTheme.type.caption,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        DetailsCard(
            listOf(
                DetailLine(stringResource(R.string.result_label_format), content.format.displayName),
                DetailLine(stringResource(R.string.result_unknown_encoding), stringResource(R.string.result_unknown_encoding_value)),
            ),
        )
    }
}

@Composable
private fun RawText(text: String) {
    val c = AppTheme.colors
    SelectionContainer {
        Text(
            text,
            style = AppTheme.type.mono.copy(fontSize = 14.sp, lineHeight = 22.sp),
            color = c.ink,
            modifier =
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceVariant)
                    .padding(14.dp),
        )
    }
}

private const val VIEW_TEXT = 0

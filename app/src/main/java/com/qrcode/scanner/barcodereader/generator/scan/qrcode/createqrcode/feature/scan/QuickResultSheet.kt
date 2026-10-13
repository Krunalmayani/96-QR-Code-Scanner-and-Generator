package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

/**
 * Artboard s08: sheet shown after a scan when "continue scanning" is on. Swipe down (or tap the scrim) keeps
 * scanning, tapping the body opens the full result.
 */
@Composable
fun QuickResultSheet(
    record: ScanRecord,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
    AppBottomSheet(onDismissRequest = onDismiss) {
        QuickResultContent(
            record = record,
            onOpen = onOpen,
            onKeepScanning = onDismiss,
            onToggleFavorite = onToggleFavorite,
            onCopy = onCopy,
            onShare = onShare,
        )
    }
}

/** Sheet body, separate from the window so previews can render it. */
@Composable
fun QuickResultContent(
    record: ScanRecord,
    onOpen: () -> Unit,
    onKeepScanning: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openLabel = stringResource(R.string.scan_quick_open_result)
    Column(
        modifier = modifier.fillMaxWidth().clickable(onClickLabel = openLabel, onClick = onOpen),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickHeader(record, onToggleFavorite)
        Text(
            text = quickText(record.content).ifBlank { record.rawValue },
            style = AppTheme.type.title,
            color = AppTheme.colors.ink,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        if (record.type == ContentType.WEBSITE) {
            AddressWarning(stringResource(R.string.scan_quick_check_address))
        }
        PrimaryButton(
            openLabel,
            onOpen,
            Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(min = 52.dp),
            leadingIcon = AppIcons.OpenInNew,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TonalButton(stringResource(R.string.common_copy), { onCopy(record.rawValue) }, Modifier.weight(1f), leadingIcon = AppIcons.Copy)
            TonalButton(
                stringResource(R.string.common_share),
                { onShare(record.rawValue) },
                Modifier.weight(1f),
                leadingIcon = AppIcons.Share,
            )
        }
        AppTextButton(stringResource(R.string.scan_quick_keep_scanning), onKeepScanning, Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuickHeader(record: ScanRecord, onToggleFavorite: () -> Unit) {
    val tone = scanTypeTone(record.type)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(scanTypeIcon(record.type), tone = tone)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppBadge(scanTypeLabel(record.type), tone = tone)
            Text(stringResource(R.string.scan_quick_just_now), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
        }
        IconButtonRounded(
            icon = if (record.isFavorite) AppIcons.Star else AppIcons.StarOutline,
            contentDescription = stringResource(if (record.isFavorite) R.string.common_unfavorite else R.string.common_favorite),
            onClick = onToggleFavorite,
        )
    }
}

/** Amber notice under a scanned link (s08): 12/16 text, 20dp icon, radius 14. */
@Composable
private fun AddressWarning(text: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    AppTheme.shapes.tile,
                ).background(c.containerOf(Tone.Warning))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Info, contentDescription = null, tint = c.contentOf(Tone.Warning), modifier = Modifier.size(20.dp))
        Text(text, style = AppTheme.type.caption, color = c.contentOf(Tone.Warning), modifier = Modifier.weight(1f))
    }
}

/** The line shown for a code: the URL for websites, otherwise the content title. */
private fun quickText(content: ScanContent): String =
    if (content is ScanContent.Website) content.url else content.title

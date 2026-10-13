package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalActions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.Initials
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult

/** Top of every result: type badge + timestamp (PRD 4.4). Contacts and events get their own avatar / date tile. */
@Composable
internal fun ResultHeader(record: ScanRecord, safety: LinkSafetyResult?, modifier: Modifier = Modifier) {
    when (val content = record.content) {
        is ScanContent.Contact -> ContactHeader(content, record, modifier)
        is ScanContent.Event -> EventHeader(content, record, modifier)
        is ScanContent.Location -> LocationHeader(content, record, modifier)
        else -> StandardHeader(record, safety, modifier)
    }
}

@Composable
private fun StandardHeader(record: ScanRecord, safety: LinkSafetyResult?, modifier: Modifier) {
    val type = record.type
    val badgeTone = if (safety != null && !safety.isSafe) Tone.Warning else type.tone()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconTile(type.icon(), tone = type.tone(), size = 56.dp, iconSize = 32.dp)
        Column(Modifier.weight(1f)) {
            BadgeRow(record, stringResource(type.badgeRes()), badgeTone)
            Caption(headerCaption(record))
        }
    }
}

@Composable
private fun BadgeRow(record: ScanRecord, label: String, tone: Tone) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppBadge(label, tone = tone)
        if (record.source == ScanSource.CREATED) AppBadge(stringResource(R.string.result_badge_created), tone = Tone.Primary)
    }
}

@Composable
private fun Caption(text: String) {
    Text(text, style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun ContactHeader(content: ScanContent.Contact, record: ScanRecord, modifier: Modifier) {
    val c = AppTheme.colors
    val initials = Initials.of(content.name.ifBlank { content.organization })
    val subtitle = headerCaption(record)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(c.primary),
            contentAlignment = Alignment.Center,
        ) {
            if (initials.isEmpty()) {
                Icon(AppIcons.Person, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(32.dp))
            } else {
                Text(initials, style = AppTheme.type.headline.copy(fontWeight = FontWeight.ExtraBold), color = c.onPrimary)
            }
        }
        Column(Modifier.weight(1f)) {
            BadgeRow(record, stringResource(R.string.result_badge_contact), Tone.Primary)
            val name = content.name.ifBlank { content.organization }
            if (name.isNotBlank()) {
                Text(name, style = AppTheme.type.headline, color = c.ink, modifier = Modifier.padding(top = 4.dp))
            }
            val company = if (content.name.isNotBlank()) content.organization else ""
            Caption(if (company.isBlank()) subtitle else stringResource(R.string.result_caption, company, subtitle))
        }
    }
}

@Composable
private fun EventHeader(content: ScanContent.Event, record: ScanRecord, modifier: Modifier) {
    val c = AppTheme.colors
    val start = content.startMillis
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (start != null) {
            val (month, day) = rememberEventTile(start, content.allDay)
            DateTile(month, day)
        } else {
            IconTile(AppIcons.Event, tone = Tone.Primary, size = 56.dp, iconSize = 32.dp)
        }
        Column(Modifier.weight(1f)) {
            BadgeRow(record, stringResource(R.string.result_badge_event), Tone.Primary)
            Text(
                content.summary.ifBlank { stringResource(R.string.result_title_event) },
                style = AppTheme.type.headline,
                color = c.ink,
                modifier = Modifier.padding(top = 4.dp),
            )
            Caption(headerCaption(record))
        }
    }
}

/** s23: a small tile, the badge and the place name (or the coordinates) as the title; the map sits above this row. */
@Composable
private fun LocationHeader(content: ScanContent.Location, record: ScanRecord, modifier: Modifier) {
    val title =
        content.label.ifBlank {
            "${ExternalActions.formatCoordinate(content.latitude)}, ${ExternalActions.formatCoordinate(content.longitude)}"
        }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconTile(AppIcons.Location, tone = Tone.Primary)
        Column(Modifier.weight(1f)) {
            BadgeRow(record, stringResource(R.string.result_badge_location), Tone.Primary)
            Text(title, style = AppTheme.type.title, color = AppTheme.colors.ink, modifier = Modifier.padding(top = 4.dp))
            Caption(headerCaption(record))
        }
    }
}

@Composable
private fun DateTile(month: String, day: String) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .width(64.dp)
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(c.surface)
            .border(1.dp, c.outline, shape),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            month,
            style = AppTheme.type.label,
            color = c.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().background(c.primary).padding(vertical = 4.dp),
        )
        Text(
            day,
            style = AppTheme.type.display.copy(fontSize = 28.sp, lineHeight = 40.sp),
            color = c.ink,
            textAlign = TextAlign.Center,
        )
    }
}

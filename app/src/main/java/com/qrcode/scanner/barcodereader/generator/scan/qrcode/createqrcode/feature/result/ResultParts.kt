package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf

/** Small upper-case caption above a value ("OPENS", "PASSWORD"). */
@Composable
internal fun OverlineLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = AppTheme.type.label, color = AppTheme.colors.inkSecondary, modifier = modifier)
}

/** One label / value line inside [DetailsCard]. [secondary] is an optional dimmer second line (time under a date). */
internal data class DetailLine(
    val label: String,
    val value: String,
    val secondary: String? = null,
    val mono: Boolean = false,
)

@Composable
internal fun DetailsCard(lines: List<DetailLine>, modifier: Modifier = Modifier) {
    if (lines.isEmpty()) return
    AppCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        lines.forEachIndexed { index, line ->
            if (index > 0) AppDivider()
            DetailRow(line)
        }
    }
}

@Composable
private fun DetailRow(line: DetailLine) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(line.label, style = AppTheme.type.bodySmall, color = c.inkSecondary, modifier = Modifier.width(84.dp))
        Column(Modifier.weight(1f)) {
            SelectionContainer {
                Text(line.value, style = if (line.mono) AppTheme.type.mono else AppTheme.type.bodyStrong, color = c.ink)
            }
            if (line.secondary != null) {
                Text(line.secondary, style = AppTheme.type.body, color = c.inkSecondary)
            }
        }
    }
}

/** Tinted banner with a bold [title], a [body] and an optional underlined [linkText] (tap target [onLinkClick]). */
@Composable
internal fun TitledBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Warning,
    icon: ImageVector = AppIcons.Shield,
    linkText: String? = null,
    onLinkClick: () -> Unit = {},
) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.containerOf(tone))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.type.bodyStrong, color = c.contentOf(tone))
            Text(body, style = AppTheme.type.bodySmall, color = c.contentOf(tone), modifier = Modifier.padding(top = 2.dp))
            if (linkText != null) {
                Text(
                    text = linkText,
                    style = AppTheme.type.bodySmall,
                    color = c.contentOf(tone),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button, onClick = onLinkClick)
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}

/** Neutral grey note with an info icon (disclaimers). */
@Composable
internal fun NoteBox(text: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(AppIcons.Info, contentDescription = null, tint = c.inkSecondary, modifier = Modifier.size(24.dp))
        Text(text, style = AppTheme.type.caption, color = c.inkSecondary, modifier = Modifier.weight(1f))
    }
}

/** Chat bubble for the SMS preview (s22). The text can be selected. */
@Composable
internal fun MessageBubble(text: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp))
            .background(c.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        SelectionContainer {
            Text(text, style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp), color = c.onPrimaryContainer)
        }
    }
}

/** Decorative stand-in for a map (the app ships no map SDK): parks, roads and a pin, drawn from theme colors. */
@Composable
internal fun MapPlaceholder(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val description = stringResource(R.string.result_location_map_description)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(c.surfaceVariant)
            .semantics { contentDescription = description },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val corner = CornerRadius(12.dp.toPx())
            drawRoundRect(
                c.successContainer,
                Offset(size.width * 0.08f, size.height * 0.10f),
                Size(size.width * 0.25f, size.height * 0.27f),
                corner,
            )
            drawRoundRect(
                c.successContainer,
                Offset(size.width * 0.66f, size.height * 0.66f),
                Size(size.width * 0.30f, size.height * 0.25f),
                corner,
            )
            drawLine(c.surface, Offset(0f, size.height * 0.40f), Offset(size.width, size.height * 0.22f), strokeWidth = 26.dp.toPx())
            drawLine(c.surface, Offset(0f, size.height * 0.78f), Offset(size.width, size.height * 0.88f), strokeWidth = 18.dp.toPx())
            drawLine(c.surface, Offset(size.width * 0.42f, 0f), Offset(size.width * 0.52f, size.height), strokeWidth = 20.dp.toPx())
        }
        Icon(
            AppIcons.Location,
            contentDescription = null,
            tint = c.error,
            modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp).size(44.dp),
        )
    }
}

/** Monospace hex dump lines (s25 hex view). */
@Composable
internal fun HexView(lines: List<String>, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    SelectionContainer {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(c.surfaceVariant)
                .padding(14.dp),
        ) {
            lines.forEach { Text(it, style = AppTheme.type.mono, color = c.ink) }
        }
    }
}

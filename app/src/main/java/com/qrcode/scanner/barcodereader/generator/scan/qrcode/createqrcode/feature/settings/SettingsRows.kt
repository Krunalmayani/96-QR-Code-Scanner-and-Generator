package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppRadio
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSwitch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf

/*
 * Settings list rows with the metrics of the design (`.li` rows on the Settings artboards): 56dp rows (64dp with a
 * caption), a 36dp icon tile with 12dp corners holding a 20dp icon, and a 15sp semi-bold title. The shared ListRow
 * is 64dp / 40dp tile / 16sp, which is right for other screens, so Settings has its own frame.
 */

/** Row title: the design's `tt` role at 15sp. */
@Composable
internal fun settingsTitleStyle(): TextStyle = AppTheme.type.title.copy(fontSize = 15.sp)

/** Small uppercase label above a card (`.lb`): [topPadding] is 0 for the first label of a screen, 14 otherwise. */
@Composable
internal fun SettingsSectionLabel(text: String, topPadding: Dp = 14.dp, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AppTheme.type.label,
        color = AppTheme.colors.inkSecondary,
        modifier = modifier.padding(start = 4.dp, end = 4.dp, top = topPadding, bottom = 6.dp).semantics { heading() },
    )
}

/** Rounded icon tile of a settings row: 36dp by default, 12dp corners, 20dp icon. */
@Composable
internal fun SettingsTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    size: Dp = 36.dp,
    cornerRadius: Dp = 12.dp,
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(cornerRadius)).background(c.containerOf(tone)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = c.contentOf(tone), modifier = Modifier.size(20.dp))
    }
}

/**
 * Frame of one settings row: [leading] slot, title with optional caption, [trailing] slot. Put it in a
 * [com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup] with dividers.
 * Click, toggle or select behaviour goes in [modifier] so the whole row is the touch target.
 */
@Composable
internal fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color = AppTheme.colors.ink,
    minHeight: Dp = if (subtitle == null) 56.dp else 64.dp,
    verticalPadding: Dp = 12.dp,
) {
    val c = AppTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(horizontal = 16.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(title, style = settingsTitleStyle(), color = titleColor)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = AppTheme.type.caption, color = c.inkSecondary)
            }
        }
        trailing?.invoke()
    }
}

/** Row with a trailing switch; the whole row toggles. */
@Composable
internal fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        leading = { SettingsTile(icon) },
        trailing = { AppSwitch(checked = checked, onCheckedChange = null) },
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

/** Row that navigates: optional [value] text, then a chevron. */
@Composable
internal fun SettingsNavItem(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    value: String? = null,
    subtitle: String? = null,
    tone: Tone = Tone.Primary,
    titleColor: Color = AppTheme.colors.ink,
) {
    val c = AppTheme.colors
    SettingsRow(
        title = title,
        subtitle = subtitle,
        titleColor = titleColor,
        leading = { SettingsTile(icon, tone = tone) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (value != null) {
                    Text(value, style = AppTheme.type.bodySmall, color = c.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(AppIcons.Chevron, contentDescription = null, tint = c.inkSecondary)
            }
        },
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

/** Tappable row without a trailing glyph (About screen, s62). */
@Composable
internal fun SettingsActionItem(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        title = title,
        leading = { SettingsTile(icon) },
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

/** Row that leaves the app (browser): same as [SettingsNavItem] but with the "open" glyph. */
@Composable
internal fun SettingsLinkItem(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        title = title,
        leading = { SettingsTile(icon) },
        trailing = { Icon(AppIcons.OpenInNew, contentDescription = null, tint = AppTheme.colors.inkSecondary) },
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

/** Radio row of the theme picker (s57): radio on the left, 52dp high. */
@Composable
internal fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        title = title,
        minHeight = 52.dp,
        verticalPadding = 0.dp,
        leading = { AppRadio(selected) },
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

/** Language row (s58): name with optional caption; the selected one is tinted and shows a check. */
@Composable
internal fun SettingsLanguageItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val c = AppTheme.colors
    SettingsRow(
        title = title,
        subtitle = subtitle,
        minHeight = 52.dp,
        verticalPadding = 0.dp,
        trailing = { if (selected) Icon(AppIcons.Check, contentDescription = null, tint = c.onPrimaryContainer) },
        modifier =
            modifier
                .background(if (selected) c.primaryContainer else Color.Transparent)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

/** Plain label inside a card (`.lb` followed by 12dp of content): no horizontal inset, unlike [SettingsSectionLabel]. */
@Composable
internal fun CardLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = AppTheme.type.label,
        color = AppTheme.colors.inkSecondary,
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp).semantics { heading() },
    )
}

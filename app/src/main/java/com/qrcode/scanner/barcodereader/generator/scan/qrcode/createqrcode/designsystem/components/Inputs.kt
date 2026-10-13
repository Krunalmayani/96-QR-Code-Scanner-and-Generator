package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone

/**
 * Outlined text field with the label inside the border (design "tf"). Shows [error] in red under the field
 * (replacing [helper]). To "validate on blur" call your validator from [onFocusChanged] when it reports false.
 * Keyboard type and IME action are set through [keyboardOptions].
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    error: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val c = AppTheme.colors
    var focused by remember { mutableStateOf(false) }
    val borderColor =
        when {
            error != null -> c.error
            focused -> c.primary
            else -> c.outline
        }
    val labelColor =
        when {
            error != null -> c.error
            focused -> c.primary
            else -> c.inkSecondary
        }
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(AppTheme.shapes.field)
                    .background(c.surface)
                    .border(if (focused || error != null) 2.dp else 1.5.dp, borderColor, AppTheme.shapes.field)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = c.inkSecondary, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = AppTheme.type.fieldLabel, color = labelColor)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                focused = it.isFocused
                                onFocusChanged(it.isFocused)
                            }.semantics {
                                contentDescription = label
                                if (error != null) error(error)
                            },
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = AppTheme.type.input.copy(color = c.ink),
                    cursorBrush = SolidColor(c.primary),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    decorationBox = { inner ->
                        Box {
                            if (value.isEmpty() && placeholder != null) {
                                Text(placeholder, style = AppTheme.type.input, color = c.inkSecondary.copy(alpha = 0.7f))
                            }
                            inner()
                        }
                    },
                )
            }
            trailing?.invoke()
        }
        val supporting = error ?: helper
        if (supporting != null) {
            Text(
                supporting,
                style = AppTheme.type.caption,
                color = if (error != null) c.error else c.inkSecondary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
            )
        }
    }
}

/** Show/hide toggle button for password fields (use as [AppTextField]'s `trailing`). */
@Composable
fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    IconButtonRounded(
        icon = if (visible) AppIcons.Hidden else AppIcons.Visible,
        contentDescription = stringResource(if (visible) R.string.common_hide_password else R.string.common_show_password),
        onClick = onToggle,
        size = 40.dp,
        tint = AppTheme.colors.inkSecondary,
    )
}

/** Pill search field (48dp). Shows a clear button when [query] is not empty. Search IME action. */
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val c = AppTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(AppTheme.shapes.pill)
                .background(c.surfaceVariant)
                .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(AppIcons.Search, contentDescription = null, tint = c.inkSecondary, modifier = Modifier.size(24.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).onFocusChanged { onFocusChanged(it.isFocused) },
            textStyle = AppTheme.type.input.copy(color = c.ink),
            singleLine = true,
            cursorBrush = SolidColor(c.primary),
            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
            keyboardActions = keyboardActions,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, style = AppTheme.type.body, color = c.inkSecondary)
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButtonRounded(
                AppIcons.Close,
                stringResource(R.string.common_clear),
                { onQueryChange("") },
                size = 40.dp,
                tint = c.inkSecondary,
            )
        }
    }
}

/** Material switch recoloured to the design. Stateless. */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = AppTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = c.onPrimary,
                checkedTrackColor = c.primary,
                checkedBorderColor = c.primary,
                uncheckedThumbColor = c.inkSecondary,
                uncheckedTrackColor = c.surfaceVariant,
                uncheckedBorderColor = c.inkSecondary,
            ),
    )
}

@Composable
fun AppCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (checked) c.primary else Color.Transparent)
                .border(2.dp, if (checked) c.primary else c.inkSecondary, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(AppIcons.Check, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(16.dp))
    }
}

/** Checkbox with a text label; the whole row toggles. */
@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppCheckbox(checked)
        Text(label, style = AppTheme.type.body, color = AppTheme.colors.ink, modifier = Modifier.weight(1f))
    }
}

@Composable
fun AppRadio(selected: Boolean, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Box(
        modifier = modifier.size(20.dp).border(2.dp, if (selected) c.primary else c.inkSecondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(10.dp).background(c.primary, CircleShape))
    }
}

/** Settings row with a trailing switch. The whole row toggles. */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    ListRow(
        title = title,
        subtitle = subtitle,
        titleMaxLines = 2,
        leading = icon?.let { { IconTile(it) } },
        trailing = { AppSwitch(checked = checked, onCheckedChange = null, enabled = enabled) },
        modifier = modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

/** Settings row with a radio button (theme / language pickers). */
@Composable
fun SettingsRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    ListRow(
        title = title,
        subtitle = subtitle,
        leading = icon?.let { { IconTile(it) } },
        trailing = { AppRadio(selected) },
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

/** Settings row that navigates: optional [value] text then a chevron. */
@Composable
fun SettingsNavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    icon: ImageVector? = null,
    tone: Tone = Tone.Primary,
) {
    ListRow(
        title = title,
        subtitle = subtitle,
        leading = icon?.let { { IconTile(it, tone = tone) } },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (value != null) Text(value, style = AppTheme.type.body, color = AppTheme.colors.inkSecondary)
                Icon(AppIcons.Chevron, contentDescription = null, tint = AppTheme.colors.inkSecondary)
            }
        },
        onClick = onClick,
        modifier = modifier,
    )
}

/** Colour swatch for the style editor. [color] is opaque ARGB-compatible. */
@Composable
fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(AppTheme.shapes.tile)
                .background(color)
                .border(if (selected) 3.dp else 1.dp, if (selected) c.primary else c.outline, AppTheme.shapes.tile)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            val onColor = if (color.luminance01() > 0.5f) Color.Black else Color.White
            Icon(AppIcons.Check, contentDescription = null, tint = onColor, modifier = Modifier.size(18.dp))
        }
    }
}

private fun Color.luminance01(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

/** Themed slider. For snap points pass [steps] = (number of stops - 2). */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onCamera: Boolean = false,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val c = AppTheme.colors
    val active = if (onCamera) Color.White else c.primary
    val inactive = if (onCamera) Color.White.copy(alpha = 0.32f) else c.outline
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors =
            SliderDefaults.colors(
                thumbColor = active,
                activeTrackColor = active,
                inactiveTrackColor = inactive,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
    )
}

@Preview(name = "Inputs")
@Composable
private fun InputsPreview() {
    AppTheme(dark = false) { InputsPreviewContent() }
}

@Preview(name = "Inputs dark")
@Composable
private fun InputsPreviewDark() {
    AppTheme(dark = true) { InputsPreviewContent() }
}

@Composable
private fun InputsPreviewContent() {
    Column(Modifier.background(AppTheme.colors.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppSearchBar("", {}, "Search history")
        AppTextField("example.com", {}, "Website URL", helper = "We add https:// for you")
        AppTextField("exa mple", {}, "Website URL", error = "Enter a valid web address")
        CardGroup {
            SettingsSwitchRow("Vibrate", true, {}, subtitle = "Short buzz on every scan", icon = AppIcons.Vibrate)
            AppDivider()
            SettingsRadioRow("Dark", true, {}, icon = AppIcons.DarkMode)
            AppDivider()
            SettingsNavRow("Language", {}, value = "English", icon = AppIcons.Language)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorSwatch(Color(0xFF13142B), true, {}, "Ink")
            ColorSwatch(Color(0xFF4F46E5), false, {}, "Indigo")
        }
    }
}

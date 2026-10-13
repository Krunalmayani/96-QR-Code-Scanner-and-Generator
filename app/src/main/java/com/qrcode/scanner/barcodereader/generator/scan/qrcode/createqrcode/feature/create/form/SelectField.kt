package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme

/**
 * Field that looks like a text field but opens a dropdown of [options] (design s36 "Security"): the label sits inside
 * the border, the current value below it and a chevron at the end. The whole field is one 56dp (or taller) target.
 */
@Composable
internal fun SelectField(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.field
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .border(1.5.dp, colors.outline, shape)
                    .clickable(role = Role.DropdownList) { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = AppTheme.type.fieldLabel, color = colors.inkSecondary)
                Text(options.getOrElse(selectedIndex) { "" }, style = AppTheme.type.input, color = colors.ink)
            }
            Icon(AppIcons.ExpandMore, contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(24.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, style = AppTheme.type.input, color = colors.ink) },
                    onClick = {
                        expanded = false
                        onSelect(index)
                    },
                    trailingIcon =
                        if (index == selectedIndex) {
                            { Icon(AppIcons.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp)) }
                        } else {
                            null
                        },
                )
            }
        }
    }
}

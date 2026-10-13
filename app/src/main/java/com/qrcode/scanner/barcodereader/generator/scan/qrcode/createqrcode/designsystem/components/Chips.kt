package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.CameraColors
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.badgeContentOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf

/** Single filter/choice chip, 36dp high. */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onCamera: Boolean = false,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(18.dp)
    val bg = when {
        onCamera && selected -> Color.White
        onCamera -> CameraColors.glass
        selected -> c.primaryContainer
        else -> Color.Transparent
    }
    val fg = when {
        onCamera && selected -> Color(0xFF13142B)
        onCamera -> CameraColors.onGlass
        selected -> c.onPrimaryContainer
        else -> c.ink
    }
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(bg)
            .then(if (!selected && !onCamera) Modifier.border(1.dp, c.outline, shape) else Modifier)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        }
        Text(label, style = AppTheme.type.chip, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

data class ChipItem(val label: String, val selected: Boolean, val icon: ImageVector? = null)

/** Horizontally scrolling row of chips; [onChipClick] receives the index in [chips]. */
@Composable
fun ChipRow(
    chips: List<ChipItem>,
    onChipClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onCamera: Boolean = false,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(chips) { index, chip ->
            AppChip(chip.label, chip.selected, { onChipClick(index) }, leadingIcon = chip.icon, onCamera = onCamera)
        }
    }
}

/** Small pill label such as the content-type badge on results ("Website", "Wi-Fi"). */
@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Primary,
    icon: ImageVector? = null,
) {
    val c = AppTheme.colors
    val fg = c.badgeContentOf(tone)
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.containerOf(tone))
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Text(text, style = AppTheme.type.caption, color = fg, maxLines = 1)
    }
}

/** Segmented control (2 to 4 options). [onCamera] renders the translucent variant used over the viewfinder. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCamera: Boolean = false,
) {
    val c = AppTheme.colors
    val track = if (onCamera) CameraColors.glass else c.surfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(track)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val thumb = when {
                !selected -> Color.Transparent
                onCamera -> Color.White
                else -> c.surface
            }
            val fg = when {
                selected && onCamera -> Color(0xFF13142B)
                selected -> c.ink
                onCamera -> Color(0xFFE8E9FF)
                else -> c.inkSecondary
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .then(if (selected && !onCamera) Modifier.shadow(1.dp, RoundedCornerShape(16.dp)) else Modifier)
                    .clip(RoundedCornerShape(16.dp))
                    .background(thumb)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = AppTheme.type.chipStrong, color = fg, maxLines = 1)
            }
        }
    }
}

@Preview(name = "Chips")
@Composable
private fun ChipsPreview() {
    AppTheme(dark = false) {
        androidx.compose.foundation.layout.Column(Modifier.background(AppTheme.colors.background).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipRow(listOf(ChipItem("All", true), ChipItem("Favorites", false, AppIcons.Star), ChipItem("Links", false)), {})
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppBadge("Website", tone = Tone.Primary, icon = AppIcons.Link)
                AppBadge("Secure", tone = Tone.Success)
                AppBadge("Not secure", tone = Tone.Warning)
                AppBadge("Unsafe", tone = Tone.Error)
            }
            SegmentedControl(listOf("QR", "Barcode", "All"), 0, {}, Modifier.padding(horizontal = 16.dp))
        }
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSlider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ColorSwatch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.InfoBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.KeyValueRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsSwitchRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BackgroundSwatch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeSwatch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ColorHex
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.GradientPreset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ReliabilityReason
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ScanReliability
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleEdit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleRules
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import java.util.Locale

/** Which color a custom-color dialog edits. */
internal enum class ColorTarget { FOREGROUND, BACKGROUND }

/** The check mark on a swatch is white or black by the swatch's own brightness, never by the app theme. */
private val OnSwatchLight = Color.White

internal fun Long.toColor(): Color = Color(this.toInt())

// ---- names ----

@StringRes
internal fun CodeSwatch.nameRes(): Int = when (this) {
    CodeSwatch.INDIGO -> R.string.create_color_indigo
    CodeSwatch.INK -> R.string.create_color_ink
    CodeSwatch.TEAL -> R.string.create_color_teal
    CodeSwatch.RED -> R.string.create_color_red
    CodeSwatch.BROWN -> R.string.create_color_brown
    CodeSwatch.BLUE -> R.string.create_color_blue
    CodeSwatch.PURPLE -> R.string.create_color_purple
}

@StringRes
internal fun BackgroundSwatch.nameRes(): Int = when (this) {
    BackgroundSwatch.WHITE -> R.string.create_color_white
    BackgroundSwatch.MIST -> R.string.create_color_mist
    BackgroundSwatch.CREAM -> R.string.create_color_cream
    BackgroundSwatch.MINT -> R.string.create_color_mint
    BackgroundSwatch.LAVENDER -> R.string.create_color_lavender
}

@StringRes
internal fun GradientPreset.nameRes(): Int = when (this) {
    GradientPreset.AURORA -> R.string.create_gradient_aurora
    GradientPreset.BERRY -> R.string.create_gradient_berry
    GradientPreset.OCEAN -> R.string.create_gradient_ocean
    GradientPreset.EMBER -> R.string.create_gradient_ember
    GradientPreset.FOREST -> R.string.create_gradient_forest
}

// ---- color swatches ----

/** Code color, gradient and background controls (design s41) with the scan-reliability message. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorControls(
    style: QrStyle,
    reliability: ScanReliability?,
    onEdit: (StyleEdit) -> Unit,
    onCustomColor: (ColorTarget) -> Unit,
    showGradient: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            SectionLabel(stringResource(R.string.create_colors_code))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (CodeSwatch.fromArgb(style.foreground) == null) {
                    ColorSwatch(
                        color = style.foreground.toColor(),
                        selected = true,
                        onClick = {},
                        contentDescription = ColorHex.format(style.foreground),
                    )
                }
                for (swatch in CodeSwatch.entries) {
                    ColorSwatch(
                        color = swatch.argb.toColor(),
                        selected = style.foreground == swatch.argb,
                        onClick = { onEdit(StyleEdit.Foreground(swatch.argb)) },
                        contentDescription = stringResource(swatch.nameRes()),
                    )
                }
                CustomColorTile(onClick = { onCustomColor(ColorTarget.FOREGROUND) })
            }
        }
        Column {
            SectionLabel(stringResource(R.string.create_colors_background))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (BackgroundSwatch.fromArgb(style.background) == null) {
                    ColorSwatch(
                        color = style.background.toColor(),
                        selected = true,
                        onClick = {},
                        contentDescription = ColorHex.format(style.background),
                    )
                }
                for (swatch in BackgroundSwatch.entries) {
                    ColorSwatch(
                        color = swatch.argb.toColor(),
                        selected = style.background == swatch.argb,
                        onClick = { onEdit(StyleEdit.Background(swatch.argb)) },
                        contentDescription = stringResource(swatch.nameRes()),
                    )
                }
                CustomColorTile(onClick = { onCustomColor(ColorTarget.BACKGROUND) })
            }
        }
        if (showGradient) GradientControls(style.foreground, style.gradientEnd, onEdit)
        if (reliability != null) ReliabilityMessage(reliability)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GradientControls(foreground: Long, gradientEnd: Long?, onEdit: (StyleEdit) -> Unit) {
    Column {
        CardGroup {
            SettingsSwitchRow(
                title = stringResource(R.string.create_colors_gradient),
                subtitle = stringResource(R.string.create_colors_gradient_hint),
                checked = gradientEnd != null,
                onCheckedChange = { on ->
                    val preset = GradientPreset.match(foreground, gradientEnd) ?: GradientPreset.entries.first()
                    onEdit(if (on) StyleEdit.Gradient(preset) else StyleEdit.GradientEnd(null))
                },
            )
        }
        if (gradientEnd != null) {
            SectionLabel(stringResource(R.string.create_colors_gradient_presets), Modifier.padding(top = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val current = GradientPreset.match(foreground, gradientEnd)
                for (preset in GradientPreset.entries) {
                    GradientSwatch(
                        preset = preset,
                        selected = preset == current,
                        onClick = { onEdit(StyleEdit.Gradient(preset)) },
                        contentDescription = stringResource(preset.nameRes()),
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientSwatch(preset: GradientPreset, selected: Boolean, onClick: () -> Unit, contentDescription: String) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.tile
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(preset.start.toColor(), preset.end.toColor())))
            .border(if (selected) 3.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(AppIcons.Check, contentDescription = null, tint = OnSwatchLight, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun CustomColorTile(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.tile
    val description = stringResource(R.string.create_colors_custom)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(colors.surfaceVariant)
            .border(1.dp, colors.outline, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.Create, contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
    }
}

/** "Contrast 7.6:1 · scans reliably" or the "May be hard to scan" warning with its reasons and the ratio row. */
@Composable
internal fun ReliabilityMessage(reliability: ScanReliability) {
    val ratio = String.format(Locale.getDefault(), "%.1f", reliability.contrastRatio)
    if (reliability.isGood) {
        InfoBanner(
            text = stringResource(R.string.create_colors_reliable, ratio),
            tone = Tone.Success,
            icon = AppIcons.Check,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoBanner(text = stringResource(R.string.create_colors_hard_to_scan), tone = Tone.Warning, icon = AppIcons.Warning)
        for (reason in reliability.reasons) {
            Text(
                stringResource(reason.messageRes()),
                style = AppTheme.type.caption,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        KeyValueRow(
            label = stringResource(R.string.create_colors_contrast_ratio),
            value = stringResource(R.string.create_colors_ratio_value, ratio),
        )
    }
}

@StringRes
private fun ReliabilityReason.messageRes(): Int = when (this) {
    ReliabilityReason.LOW_CONTRAST -> R.string.create_reason_low_contrast
    ReliabilityReason.INVERTED -> R.string.create_reason_inverted
    ReliabilityReason.GRADIENT_LOW_CONTRAST -> R.string.create_reason_gradient
    ReliabilityReason.DOTS_LOW_EC -> R.string.create_reason_dots
}

// ---- barcode options ----

/** Show-number switch and bar-height slider of barcodes (design s44). */
@Composable
internal fun BarcodeOptions(
    style: QrStyle,
    onEdit: (StyleEdit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CardGroup {
            SettingsSwitchRow(
                title = stringResource(R.string.create_barcode_show_number),
                subtitle = stringResource(R.string.create_barcode_show_number_hint),
                checked = style.showNumber,
                onCheckedChange = { onEdit(StyleEdit.ShowNumber(it)) },
            )
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.create_barcode_bar_height),
                style = AppTheme.type.title,
                color = AppTheme.colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.create_percent, style.barHeightPercent),
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.inkSecondary,
            )
        }
        AppSlider(
            value = style.barHeightPercent.toFloat(),
            onValueChange = { onEdit(StyleEdit.BarHeight(it.toInt())) },
            valueRange = StyleRules.BAR_HEIGHT_MIN.toFloat()..StyleRules.BAR_HEIGHT_MAX.toFloat(),
        )
    }
}

// ---- shape tiles ----

/** A tile with a tiny drawing of a dot shape (design s42 "Pattern"). */
@Composable
internal fun DotShapeTile(
    shape: DotShape,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val tileShape = AppTheme.shapes.card
    val ink = if (selected) colors.onPrimaryContainer else colors.ink
    Column(
        modifier = modifier
            .clip(tileShape)
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, tileShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(top = 12.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DotShapeSample(shape, ink)
        Text(label, style = AppTheme.type.captionStrong, color = ink, maxLines = 1)
    }
}

@Composable
private fun DotShapeSample(
    shape: DotShape,
    color: Color,
) {
    val sampleSize = 44.dp
    when (shape) {
        DotShape.SQUARE ->
            Box(Modifier.size(sampleSize).background(color, RoundedCornerShape(4.dp)))
        DotShape.ROUNDED ->
            Box(Modifier.size(sampleSize).background(color, RoundedCornerShape(14.dp)))
        DotShape.CLASSY ->
            Box(
                Modifier.size(sampleSize).background(
                    color,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 3.dp, bottomEnd = 16.dp, bottomStart = 3.dp),
                ),
            )
        DotShape.DOTS ->
            Column(Modifier.size(sampleSize), verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
                repeat(3) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
                        repeat(3) { Box(Modifier.size(9.dp).background(color, CircleShape)) }
                    }
                }
            }
    }
}

/** Chip row entry for an option; the selected one carries a check mark so it is not color-only. */
@Composable
internal fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AppChip(label = label, selected = selected, onClick = onClick, leadingIcon = if (selected) AppIcons.Check else null)
}

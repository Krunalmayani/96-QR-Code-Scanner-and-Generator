package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSlider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ConfirmDialog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SmallButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ColorHex
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ExportSizes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorBuilder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleAdvice
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleEdit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleRules
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.NoticeBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.CodeCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.rememberLogoBitmap
import kotlin.math.roundToInt

/** 428dp of the 800dp artboard: the sheet starts 372dp from the top. */
private const val SHEET_HEIGHT_FRACTION = 0.535f

/** 216dp code + 2 x 12dp padding (design s41). */
private val PreviewMaxWidth = 240.dp
private val HandleWidth = 32.dp
private val HandleHeight = 4.dp

@StringRes
private fun EditorTab.labelRes(): Int =
    when (this) {
        EditorTab.COLORS -> R.string.create_tab_colors
        EditorTab.SHAPES -> R.string.create_tab_shapes
        EditorTab.EXTRAS -> R.string.create_tab_extras
    }

@StringRes
private fun DotShape.labelRes(): Int =
    when (this) {
        DotShape.SQUARE -> R.string.create_dot_square
        DotShape.ROUNDED -> R.string.create_dot_rounded
        DotShape.DOTS -> R.string.create_dot_dots
        DotShape.CLASSY -> R.string.create_dot_classy
    }

@StringRes
private fun EyeShape.labelRes(): Int =
    when (this) {
        EyeShape.SQUARE -> R.string.create_eye_square
        EyeShape.ROUNDED -> R.string.create_eye_rounded
        EyeShape.CIRCLE -> R.string.create_eye_circle
        EyeShape.LEAF -> R.string.create_eye_leaf
    }

@StringRes
private fun ErrorCorrection.labelRes(): Int =
    when (this) {
        ErrorCorrection.L -> R.string.create_ec_low
        ErrorCorrection.M -> R.string.create_ec_medium
        ErrorCorrection.Q -> R.string.create_ec_quartile
        ErrorCorrection.H -> R.string.create_ec_high
    }

/** The customize editor (design s41 to s43, barcode s44): live preview on top, a sheet with the Colors / Shapes / Extras tabs below. */
@Composable
internal fun EditorScreen(
    state: GeneratedUiState,
    onEvent: (GeneratedEvent) -> Unit,
    onPickLogo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logo = rememberLogoBitmap(state.style.logoUri)
    var customTarget by remember { mutableStateOf<ColorTarget?>(null) }
    val onEdit: (StyleEdit) -> Unit = { onEvent(GeneratedEvent.Edit(it)) }
    Column(modifier.fillMaxSize().background(AppTheme.colors.background).imePadding()) {
        EditorTopBar(onClose = { onEvent(GeneratedEvent.CloseEditor) }, onDone = { onEvent(GeneratedEvent.CloseEditor) })
        Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CodeCard(
                image = state.image,
                logo = logo,
                contentDescription = stringResource(R.string.create_editor_preview_description),
                modifier = Modifier.widthIn(max = PreviewMaxWidth),
            )
        }
        EditorSheet(state = state, onEvent = onEvent, onEdit = onEdit, onPickLogo = onPickLogo, onCustomColor = { customTarget = it })
    }
    customTarget?.let { target ->
        val initial = if (target == ColorTarget.FOREGROUND) state.style.foreground else state.style.background
        CustomColorDialog(
            initial = initial,
            onApply = { argb ->
                onEdit(if (target == ColorTarget.FOREGROUND) StyleEdit.Foreground(argb) else StyleEdit.Background(argb))
            },
            onDismiss = { customTarget = null },
        )
    }
}

@Composable
private fun EditorTopBar(onClose: () -> Unit, onDone: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButtonRounded(AppIcons.Close, stringResource(R.string.common_close), onClose)
        Text(
            text = stringResource(R.string.create_editor_title),
            style = AppTheme.type.topBar,
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        SmallButton(text = stringResource(R.string.common_done), onClick = onDone, tonal = false)
    }
}

@Composable
private fun EditorSheet(
    state: GeneratedUiState,
    onEvent: (GeneratedEvent) -> Unit,
    onEdit: (StyleEdit) -> Unit,
    onPickLogo: () -> Unit,
    onCustomColor: (ColorTarget) -> Unit,
) {
    val tabs = state.tabs
    val tab = if (state.tab in tabs) state.tab else EditorTab.COLORS
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(SHEET_HEIGHT_FRACTION)
                .clip(AppTheme.shapes.sheet)
                .background(AppTheme.colors.surface)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = HandleWidth, height = HandleHeight)
                .clip(RoundedCornerShape(HandleHeight / 2))
                .background(AppTheme.colors.inkSecondary.copy(alpha = 0.4f)),
        )
        Spacer(Modifier.height(12.dp))
        SegmentedControl(
            options = tabs.map { stringResource(it.labelRes()) },
            selectedIndex = tabs.indexOf(tab),
            onSelect = { onEvent(GeneratedEvent.SelectTab(tabs[it])) },
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (tab) {
                EditorTab.COLORS ->
                    ColorControls(
                        style = state.style,
                        reliability = state.reliability,
                        onEdit = onEdit,
                        onCustomColor = onCustomColor,
                        showGradient = true,
                    )
                EditorTab.SHAPES -> ShapesTab(state.style, onEdit)
                EditorTab.EXTRAS ->
                    if (state.isBarcode) {
                        BarcodeExtras(state.style, onEdit)
                    } else {
                        QrExtras(state.style, onEdit, onPickLogo, onRemoveLogo = { onEvent(GeneratedEvent.RemoveLogo) })
                    }
            }
        }
    }
}

// ---- shapes ----

/** The pattern tiles in the artboard's order (square, dots, rounded) followed by the one it does not show. */
private val PatternOrder = listOf(DotShape.SQUARE, DotShape.DOTS, DotShape.ROUNDED, DotShape.CLASSY)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapesTab(style: QrStyle, onEdit: (StyleEdit) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column {
            SectionLabel(stringResource(R.string.create_shapes_pattern))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (shape in PatternOrder) {
                    DotShapeTile(
                        shape = shape,
                        label = stringResource(shape.labelRes()),
                        selected = style.dotShape == shape,
                        onClick = { onEdit(StyleEdit.Dots(shape)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Column {
            SectionLabel(stringResource(R.string.create_shapes_eyes))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (shape in EyeShape.entries) {
                    OptionChip(stringResource(shape.labelRes()), style.eyeShape == shape) { onEdit(StyleEdit.Eyes(shape)) }
                }
            }
        }
        if (StyleAdvice.needsStrongerCorrection(style)) {
            NoticeBanner(
                stringResource(R.string.create_shapes_correction_hint),
                tone = Tone.Warning,
                icon = AppIcons.Shield,
                modifier = Modifier.padding(top = BannerGap),
                compact = true,
                live = true,
            )
        }
    }
}

/** Space above a status banner on top of the 8dp column gap: 14dp in the artboards. */
private val BannerGap = 6.dp

// ---- extras ----

@Composable
private fun QrExtras(style: QrStyle, onEdit: (StyleEdit) -> Unit, onPickLogo: () -> Unit, onRemoveLogo: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LogoRow(style.logoUri, onPickLogo, onRemoveLogo, Modifier.padding(top = 8.dp))
        FrameControls(style.frameText, onEdit)
        CorrectionControls(style, onEdit)
        ExportControls(style, onEdit)
    }
}

@Composable
private fun BarcodeExtras(style: QrStyle, onEdit: (StyleEdit) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BarcodeOptions(style, onEdit, Modifier.padding(top = 8.dp))
        ExportControls(style, onEdit)
    }
}

@Composable
private fun LogoRow(logoUri: String?, onPickLogo: () -> Unit, onRemoveLogo: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val logo = rememberLogoBitmap(logoUri)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(56.dp)
                .clip(AppTheme.shapes.card)
                .background(colors.surfaceVariant)
                .then(if (logo == null) Modifier.dashedBorder(colors.inkSecondary) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (logo != null) {
                Image(bitmap = logo, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.size(48.dp))
            } else {
                Icon(AppIcons.Image, contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(24.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.create_extras_logo_title), style = AppTheme.type.title, color = colors.ink)
            Text(stringResource(R.string.create_extras_logo_hint), style = AppTheme.type.caption, color = colors.inkSecondary)
        }
        if (logoUri == null) {
            SmallButton(text = stringResource(R.string.create_extras_logo_add), onClick = onPickLogo)
        } else {
            SmallButton(text = stringResource(R.string.create_extras_logo_remove), onClick = onRemoveLogo)
        }
    }
}

/** 1dp dashed outline inside a 20dp-rounded box: the empty logo slot of the artboard. */
private fun Modifier.dashedBorder(color: Color): Modifier =
    drawBehind {
        val stroke = 1.dp.toPx()
        val dash = 4.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(20.dp.toPx()),
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))),
        )
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrameControls(frameText: String?, onEdit: (StyleEdit) -> Unit) {
    val scanMe = stringResource(R.string.create_frame_scan_me)
    val menu = stringResource(R.string.create_frame_menu)
    var customMode by rememberSaveable { mutableStateOf(frameText != null && frameText != scanMe && frameText != menu) }
    Column {
        SectionLabel(stringResource(R.string.create_extras_frame))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionChip(stringResource(R.string.create_frame_none), !customMode && frameText == null) {
                customMode = false
                onEdit(StyleEdit.Frame(null))
            }
            OptionChip(scanMe, !customMode && frameText == scanMe) {
                customMode = false
                onEdit(StyleEdit.Frame(scanMe))
            }
            OptionChip(menu, !customMode && frameText == menu) {
                customMode = false
                onEdit(StyleEdit.Frame(menu))
            }
            OptionChip(stringResource(R.string.create_frame_custom), customMode) { customMode = true }
        }
        if (customMode) CustomFrameField(initial = frameText.orEmpty(), onEdit = onEdit)
    }
}

/** Keeps the typed text (spaces included) locally; the style receives the trimmed value. */
@Composable
private fun CustomFrameField(initial: String, onEdit: (StyleEdit) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    AppTextField(
        value = text,
        onValueChange = {
            text = it.take(StyleRules.FRAME_TEXT_MAX)
            onEdit(StyleEdit.Frame(text))
        },
        label = stringResource(R.string.create_frame_text_label),
        modifier = Modifier.padding(top = 12.dp),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
    )
}

@Composable
private fun CorrectionControls(style: QrStyle, onEdit: (StyleEdit) -> Unit) {
    val effective = QrVectorBuilder.effectiveErrorCorrection(style)
    Column {
        SectionLabel(stringResource(R.string.create_extras_correction))
        SegmentedControl(
            options = ErrorCorrection.entries.map { it.name },
            selectedIndex = effective.ordinal,
            onSelect = { onEdit(StyleEdit.Correction(ErrorCorrection.entries[it])) },
        )
        Text(
            text = stringResource(R.string.create_ec_caption, stringResource(effective.labelRes()), StyleAdvice.recoveryPercent(effective)),
            style = AppTheme.type.caption,
            color = AppTheme.colors.inkSecondary,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp),
        )
        if (style.logoUri != null) {
            Text(
                text = stringResource(R.string.create_ec_logo_hint),
                style = AppTheme.type.caption,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportControls(style: QrStyle, onEdit: (StyleEdit) -> Unit, modifier: Modifier = Modifier) {
    val points = ExportSizes.points
    val sizeLabel = stringResource(R.string.create_extras_export_size)
    val sizeText = stringResource(R.string.create_px, style.exportSizePx)
    Column(modifier.padding(top = BannerGap), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                sizeLabel,
                style = AppTheme.type.title,
                color = AppTheme.colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                sizeText,
                style = AppTheme.type.bodyStrong,
                color = AppTheme.colors.inkSecondary,
            )
        }
        AppSlider(
            value = ExportSizes.indexOf(style.exportSizePx).toFloat(),
            onValueChange = { onEdit(StyleEdit.Size(ExportSizes.at(it.roundToInt()))) },
            modifier =
                Modifier.semantics {
                    contentDescription = sizeLabel
                    stateDescription = sizeText
                },
            valueRange = 0f..points.lastIndex.toFloat(),
            steps = points.size - 2,
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(points.first().toString(), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
            Text(points.last().toString(), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
        }
        SectionLabel(stringResource(R.string.create_extras_format))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (format in ExportFormat.entries) {
                OptionChip(format.name, style.exportFormat == format) { onEdit(StyleEdit.Format(format)) }
            }
        }
    }
}

// ---- custom color ----

@Composable
private fun CustomColorDialog(initial: Long, onApply: (Long) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(ColorHex.format(initial)) }
    val parsed = ColorHex.parse(text)
    ConfirmDialog(
        title = stringResource(R.string.create_custom_color_title),
        confirmText = stringResource(R.string.common_ok),
        onConfirm = {
            if (parsed != null) {
                onApply(parsed)
                onDismiss()
            }
        },
        onDismiss = onDismiss,
        message = stringResource(R.string.create_custom_color_message),
    ) {
        AppTextField(
            value = text,
            onValueChange = { text = it.take(HEX_FIELD_MAX) },
            label = stringResource(R.string.create_custom_color_label),
            error = if (parsed == null) stringResource(R.string.create_custom_color_invalid) else null,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
        )
    }
}

private const val HEX_FIELD_MAX = 7

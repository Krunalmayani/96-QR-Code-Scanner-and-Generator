package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSwitch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppToastHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.KeyValueRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LoadingSpinner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ToastState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.rememberToastState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeInputs
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ContrastChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateHubLogic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ExportDimensions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyleEdit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.labelRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.CodeCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.rememberLogoBitmap

/** 232dp code + 2 x 12dp padding (design s40). */
private val CodeCardMaxWidth = 256.dp

/** Distance of the toast above the bottom edge: 116dp in the artboard (the 77dp action bar plus 39dp). */
private val ToastBottomOffset = 116.dp

/** Generated code (design s40, barcode s44) and, one tap away, the customize editor (s41 to s43). */
@Composable
fun GeneratedScreen(
    state: GeneratedUiState,
    onEvent: (GeneratedEvent) -> Unit,
    onBack: () -> Unit,
    onPickLogo: () -> Unit,
    toastState: ToastState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        when (state.status) {
            GeneratedStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingSpinner() }
            GeneratedStatus.NOT_FOUND -> NotFound(onBack)
            GeneratedStatus.READY ->
                if (state.editing) {
                    EditorScreen(state = state, onEvent = onEvent, onPickLogo = onPickLogo)
                } else {
                    ResultContent(state = state, onEvent = onEvent, onBack = onBack)
                }
        }
        AppToastHost(
            state = toastState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = ToastBottomOffset),
        )
        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = ToastBottomOffset),
        )
    }
}

@Composable
private fun NotFound(onBack: () -> Unit) {
    AppScaffold(topBar = { AppTopBar(title = stringResource(R.string.create_generated_title_qr), onBack = onBack) }) { padding ->
        EmptyState(
            icon = AppIcons.Warning,
            title = stringResource(R.string.create_not_found_title),
            message = stringResource(R.string.create_not_found_message),
            modifier = Modifier.fillMaxSize().padding(padding),
            action = { AppTextButton(text = stringResource(R.string.common_back), onClick = onBack) },
        )
    }
}

@Composable
private fun ResultContent(state: GeneratedUiState, onEvent: (GeneratedEvent) -> Unit, onBack: () -> Unit) {
    val record = state.record ?: return
    val style = state.style
    val logo = rememberLogoBitmap(style.logoUri)
    AppScaffold(
        topBar = {
            AppTopBar(
                title =
                    stringResource(
                        if (state.isBarcode) R.string.create_generated_title_barcode else R.string.create_generated_title_qr,
                    ),
                onBack = onBack,
                actions = {
                    IconButtonRounded(
                        icon = if (state.isFavorite) AppIcons.Star else AppIcons.StarOutline,
                        contentDescription = stringResource(if (state.isFavorite) R.string.common_unfavorite else R.string.common_favorite),
                        onClick = { onEvent(GeneratedEvent.ToggleFavorite) },
                        tint = if (state.isFavorite) AppTheme.colors.warning else null,
                    )
                },
            )
        },
        bottomBar = {
            ActionBar {
                AppOutlinedButton(
                    text = stringResource(R.string.common_share),
                    onClick = { onEvent(GeneratedEvent.Share) },
                    enabled = !state.working,
                    leadingIcon = AppIcons.Share,
                )
                PrimaryButton(
                    text = stringResource(R.string.create_save_to_gallery),
                    onClick = { onEvent(GeneratedEvent.SaveToGallery) },
                    enabled = !state.working,
                    leadingIcon = AppIcons.Download,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewCard(state = state, record = record, logo = logo)
            ActionRow(state = state, onEvent = onEvent)
            DetailsCard(state = state, record = record, onShowNumber = { onEvent(GeneratedEvent.Edit(StyleEdit.ShowNumber(it))) })
        }
    }
}

@Composable
private fun PreviewCard(state: GeneratedUiState, record: ScanRecord, logo: ImageBitmap?) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.dialog
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(1.dp, colors.outline, shape)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (state.isBarcode) {
            CodeCard(
                image = state.image,
                logo = null,
                contentDescription = stringResource(R.string.create_preview_description, record.format.displayName),
                modifier = Modifier.fillMaxWidth(),
                shape = AppTheme.shapes.field,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
            )
        } else {
            CodeCard(
                image = state.image,
                logo = logo,
                contentDescription = stringResource(R.string.create_preview_description, record.format.displayName),
                modifier = Modifier.widthIn(max = CodeCardMaxWidth).fillMaxWidth(),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val type = CreateHubLogic.typeOf(record)
            AppBadge(
                text = type?.let { stringResource(it.labelRes()) } ?: record.format.displayName,
                tone = if (state.isBarcode) Tone.Warning else Tone.Primary,
                icon = if (state.isBarcode) AppIcons.Barcode else AppIcons.QrCode,
            )
            Text(
                text = record.displayTitle,
                style = if (state.isBarcode) barcodeNumberStyle() else AppTheme.type.caption,
                color = if (state.isBarcode) AppTheme.colors.ink else AppTheme.colors.inkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** The number next to the barcode badge: monospaced, bold, 14sp (design s44). */
@Composable
private fun barcodeNumberStyle(): TextStyle = AppTheme.type.mono.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)

@Composable
private fun ActionRow(state: GeneratedUiState, onEvent: (GeneratedEvent) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TonalButton(
            text = stringResource(if (state.isBarcode) R.string.create_action_colors else R.string.create_action_customize),
            onClick = { onEvent(GeneratedEvent.OpenEditor()) },
            leadingIcon = AppIcons.Palette,
            modifier = Modifier.weight(1f),
        )
        TonalButton(
            text = stringResource(if (state.isBarcode) R.string.create_action_copy_number else R.string.common_copy),
            onClick = { onEvent(GeneratedEvent.Copy) },
            leadingIcon = AppIcons.Copy,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DetailsCard(state: GeneratedUiState, record: ScanRecord, onShowNumber: (Boolean) -> Unit) {
    AppCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        if (state.isBarcode) {
            KeyValueRow(stringResource(R.string.create_details_type), record.format.displayName)
            val check = state.checkDigit
            if (check != null) {
                AppDivider()
                val verdict = stringResource(if (check.valid) R.string.create_details_valid else R.string.create_details_invalid)
                KeyValueRow(
                    stringResource(R.string.create_details_check_digit),
                    stringResource(R.string.create_details_check_digit_value, check.digit, verdict),
                )
            }
            AppDivider()
            ShowNumberRow(checked = state.style.showNumber, onCheckedChange = onShowNumber)
        } else {
            KeyValueRow(stringResource(R.string.create_details_content), record.rawValue, valueMaxLines = 3)
        }
        val size = state.pixelSize
        if (size != null) {
            AppDivider()
            KeyValueRow(
                stringResource(R.string.create_details_size),
                stringResource(R.string.create_details_size_value, size.width, size.height),
            )
        }
        AppDivider()
        KeyValueRow(
            stringResource(R.string.create_details_saved_to),
            stringResource(if (record.id > 0) R.string.create_details_saved_history else R.string.create_details_saved_nowhere),
        )
    }
}

/** "Show number | Under bars [switch]" row of the barcode details (design s44); the whole row toggles. */
@Composable
private fun ShowNumberRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.create_barcode_show_number),
            style = AppTheme.type.bodySmall,
            color = AppTheme.colors.inkSecondary,
            modifier = Modifier.width(84.dp),
        )
        Text(
            stringResource(R.string.create_barcode_show_number_hint),
            style = AppTheme.type.bodyStrong,
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f),
        )
        AppSwitch(checked = checked, onCheckedChange = null)
    }
}

// ---- previews ----

private fun sampleRecord(payload: String, format: CodeFormat, style: QrStyle): ScanRecord {
    val content =
        if (format == CodeFormat.QR_CODE) {
            ScanContent.Website(payload, format, payload)
        } else {
            ScanContent.Product(payload, format, payload)
        }
    return ScanRecord(id = 1, content = content, createdAt = 0L, source = ScanSource.CREATED, style = style)
}

private fun sampleState(record: ScanRecord, editing: Boolean = false, tab: EditorTab = EditorTab.COLORS): GeneratedUiState {
    val style = record.style ?: QrStyle()
    val image = CodeImageFactory(record.format, record.rawValue).imageOrNull(style)
    return GeneratedUiState(
        status = GeneratedStatus.READY,
        record = record,
        style = style,
        image = image,
        reliability = ContrastChecker.scanReliability(style),
        pixelSize = image?.let { ExportDimensions.of(it, style.exportSizePx) },
        checkDigit = BarcodeInputs.checkDigitInfo(record.format, record.rawValue),
        isFavorite = false,
        editing = editing,
        tab = tab,
    )
}

private const val SAMPLE_URL = "https://www.brewhouse-coffee.com/menu?table=12"
private const val SAMPLE_EAN = "5901234123457"
private val qrRecord = sampleRecord(SAMPLE_URL, CodeFormat.QR_CODE, QrStyle())
private val indigoRecord = sampleRecord(SAMPLE_URL, CodeFormat.QR_CODE, QrStyle(foreground = 0xFF4F46E5))
private val shapedRecord =
    sampleRecord(
        SAMPLE_URL,
        CodeFormat.QR_CODE,
        QrStyle(foreground = 0xFF4F46E5, dotShape = DotShape.DOTS, eyeShape = EyeShape.LEAF, errorCorrection = ErrorCorrection.M),
    )
private val framedRecord =
    sampleRecord(
        SAMPLE_URL,
        CodeFormat.QR_CODE,
        QrStyle(foreground = 0xFF4F46E5, frameText = "Scan me", errorCorrection = ErrorCorrection.H),
    )
private val lowContrastRecord = sampleRecord(SAMPLE_URL, CodeFormat.QR_CODE, QrStyle(foreground = 0xFFBBBBBB))
private val barcodeRecord = sampleRecord(SAMPLE_EAN, CodeFormat.EAN_13, QrStyle())

@Composable
private fun PreviewGenerated(dark: Boolean, state: GeneratedUiState) {
    AppTheme(dark = dark) {
        GeneratedScreen(
            state = state,
            onEvent = {},
            onBack = {},
            onPickLogo = {},
            toastState = rememberToastState(),
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(name = "S40_GeneratedQr_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S40_GeneratedQr_Light() = PreviewGenerated(false, sampleState(qrRecord))

@Preview(name = "S40_GeneratedQr_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S40_GeneratedQr_Dark() = PreviewGenerated(true, sampleState(qrRecord))

@Preview(name = "S41_CustomizeColors_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S41_CustomizeColors_Light() = PreviewGenerated(false, sampleState(indigoRecord, editing = true))

@Preview(name = "S41_CustomizeColors_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S41_CustomizeColors_Dark() = PreviewGenerated(true, sampleState(indigoRecord, editing = true))

@Preview(name = "S41_CustomizeColorsWarning_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S41_CustomizeColorsWarning_Light() = PreviewGenerated(false, sampleState(lowContrastRecord, editing = true))

@Preview(name = "S41_CustomizeColorsWarning_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S41_CustomizeColorsWarning_Dark() = PreviewGenerated(true, sampleState(lowContrastRecord, editing = true))

@Preview(name = "S42_CustomizeShapes_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S42_CustomizeShapes_Light() = PreviewGenerated(false, sampleState(shapedRecord, editing = true, tab = EditorTab.SHAPES))

@Preview(name = "S42_CustomizeShapes_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S42_CustomizeShapes_Dark() = PreviewGenerated(true, sampleState(shapedRecord, editing = true, tab = EditorTab.SHAPES))

@Preview(name = "S43_CustomizeExtras_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S43_CustomizeExtras_Light() = PreviewGenerated(false, sampleState(framedRecord, editing = true, tab = EditorTab.EXTRAS))

@Preview(name = "S43_CustomizeExtras_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S43_CustomizeExtras_Dark() = PreviewGenerated(true, sampleState(framedRecord, editing = true, tab = EditorTab.EXTRAS))

@Preview(name = "S44_GeneratedBarcode_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S44_GeneratedBarcode_Light() = PreviewGenerated(false, sampleState(barcodeRecord))

@Preview(name = "S44_GeneratedBarcode_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S44_GeneratedBarcode_Dark() = PreviewGenerated(true, sampleState(barcodeRecord))

@Preview(name = "S44_BarcodeCustomize_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S44_BarcodeCustomize_Light() = PreviewGenerated(false, sampleState(barcodeRecord, editing = true, tab = EditorTab.EXTRAS))

@Preview(name = "S44_BarcodeCustomize_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S44_BarcodeCustomize_Dark() = PreviewGenerated(true, sampleState(barcodeRecord, editing = true, tab = EditorTab.EXTRAS))

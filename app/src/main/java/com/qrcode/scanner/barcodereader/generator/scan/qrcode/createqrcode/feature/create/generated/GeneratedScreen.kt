package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LocalAdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
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
import kotlinx.coroutines.launch

private val CodeCardMaxWidth = 280.dp
private val ToastBottomOffset = 96.dp

@StringRes
private fun GeneratedMessage.textRes(): Int = when (this) {
    GeneratedMessage.SAVED_TO_GALLERY -> R.string.create_toast_saved_gallery
    GeneratedMessage.SAVED_TO_DOWNLOADS -> R.string.create_toast_saved_downloads
    GeneratedMessage.COPIED -> R.string.common_copied
    GeneratedMessage.SAVE_FAILED -> R.string.create_error_save_failed
    GeneratedMessage.SHARE_FAILED -> R.string.create_error_share_failed
    GeneratedMessage.PERMISSION_DENIED -> R.string.create_error_storage_permission
    GeneratedMessage.LOGO_FAILED -> R.string.create_error_logo_failed
}

/**
 * Entry of the Generated destination: wires the ViewModel to the Android pieces the screen needs (permission request,
 * Photo Picker, clipboard, share sheet) and counts the ad impression once when the result is first shown.
 */
@Composable
fun GeneratedRoute(
    onBack: () -> Unit,
    viewModel: GeneratedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()
    val snackbar = remember { SnackbarHostState() }

    val adController = LocalAdController.current
    var adCounted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.status) {
        if (state.status == GeneratedStatus.READY && !adCounted) {
            adCounted = true
            adController.onResultShown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onEvent(GeneratedEvent.StoragePermissionResult(granted))
    }
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onEvent(GeneratedEvent.LogoPicked(uri.toString()))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GeneratedEffect.Message -> {
                    val text = context.getString(effect.message.textRes())
                    scope.launch { if (effect.message.isSuccess) toast.show(text) else snackbar.showSnackbar(text) }
                }
                is GeneratedEffect.ShareFile -> {
                    if (!startShare(context, effect.uri, effect.mimeType)) {
                        scope.launch { snackbar.showSnackbar(context.getString(R.string.create_error_share_failed)) }
                    }
                }
                is GeneratedEffect.CopyText -> {
                    clipboard.setText(AnnotatedString(effect.text))
                    scope.launch { toast.show(context.getString(R.string.common_copied)) }
                }
                GeneratedEffect.RequestStoragePermission -> permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    GeneratedScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onPickLogo = { logoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        toastState = toast,
        snackbarHostState = snackbar,
    )
}

/** Opens the system share sheet for [uri]; false when no app can handle it. */
private fun startShare(context: Context, uri: String, mimeType: String): Boolean {
    val stream = Uri.parse(uri)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, stream)
        clipData = ClipData.newRawUri(null, stream)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(send, context.getString(R.string.create_share_chooser_title))
    return try {
        context.startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

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
    BackHandler(enabled = state.editing) { onEvent(GeneratedEvent.CloseEditor) }
    Box(modifier.fillMaxSize()) {
        when (state.status) {
            GeneratedStatus.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingSpinner() }
            GeneratedStatus.NOT_FOUND -> NotFound(onBack)
            GeneratedStatus.READY -> if (state.editing) {
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
                title = stringResource(if (state.isBarcode) R.string.create_generated_title_barcode else R.string.create_generated_title_qr),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewCard(state = state, record = record, logo = logo)
            ActionRow(state = state, onEvent = onEvent)
            if (state.isBarcode) BarcodeOptions(style = style, onEdit = { onEvent(GeneratedEvent.Edit(it)) })
            DetailsCard(state = state, record = record)
        }
    }
}

@Composable
private fun PreviewCard(state: GeneratedUiState, record: ScanRecord, logo: ImageBitmap?) {
    AppCard(contentPadding = PaddingValues(20.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CodeCard(
                image = state.image,
                logo = logo,
                contentDescription = stringResource(R.string.create_preview_description, record.format.displayName),
                modifier = Modifier.widthIn(max = CodeCardMaxWidth).fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val type = CreateHubLogic.typeOf(record)
                AppBadge(
                    text = type?.let { stringResource(it.labelRes()) } ?: record.format.displayName,
                    tone = if (state.isBarcode) Tone.Warning else Tone.Primary,
                    icon = if (state.isBarcode) AppIcons.Barcode else AppIcons.QrCode,
                )
                Text(
                    text = record.displayTitle,
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

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
private fun DetailsCard(state: GeneratedUiState, record: ScanRecord) {
    AppCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
        if (state.isBarcode) {
            KeyValueRow(stringResource(R.string.create_details_type), record.format.displayName)
        }
        KeyValueRow(stringResource(R.string.create_details_content), record.rawValue, valueMaxLines = 3)
        val check = state.checkDigit
        if (check != null) {
            val verdict = stringResource(if (check.valid) R.string.create_details_valid else R.string.create_details_invalid)
            KeyValueRow(stringResource(R.string.create_details_check_digit), stringResource(R.string.create_details_check_digit_value, check.digit, verdict))
        }
        val size = state.pixelSize
        if (size != null) {
            KeyValueRow(stringResource(R.string.create_details_size), stringResource(R.string.create_details_size_value, size.width, size.height))
        }
        KeyValueRow(
            stringResource(R.string.create_details_saved_to),
            stringResource(if (record.id > 0) R.string.create_details_saved_history else R.string.create_details_saved_nowhere),
        )
    }
}

// ---- previews ----

private fun sampleRecord(payload: String, format: CodeFormat, style: QrStyle): ScanRecord {
    val content = if (format == CodeFormat.QR_CODE) {
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
private val shapedRecord = sampleRecord(
    SAMPLE_URL,
    CodeFormat.QR_CODE,
    QrStyle(foreground = 0xFF4F46E5, dotShape = DotShape.DOTS, eyeShape = EyeShape.LEAF, errorCorrection = ErrorCorrection.M),
)
private val framedRecord = sampleRecord(SAMPLE_URL, CodeFormat.QR_CODE, QrStyle(foreground = 0xFF4F46E5, frameText = "Scan me", errorCorrection = ErrorCorrection.H))
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
private fun S41_CustomizeColors_Light() = PreviewGenerated(false, sampleState(lowContrastRecord, editing = true))

@Preview(name = "S41_CustomizeColors_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S41_CustomizeColors_Dark() = PreviewGenerated(true, sampleState(qrRecord, editing = true))

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

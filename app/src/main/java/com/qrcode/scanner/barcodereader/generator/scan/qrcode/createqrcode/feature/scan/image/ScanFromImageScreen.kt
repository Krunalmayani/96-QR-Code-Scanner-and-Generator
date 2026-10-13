package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LoadingSpinner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.CameraColors
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scanTypeAndFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scanTypeIcon
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scanTypeLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scanTypeTone

private val ImageCardShape = RoundedCornerShape(28.dp)

/** 15/22 paragraph under the headlines of s12 and s14. */
private val BodyLarge: TextStyle
    @Composable get() = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp)

/**
 * Scan from image (s11 to s14). Stateless. [image] draws the picked image into the given modifier so previews can
 * pass a stand-in. s11 itself is the system Photo Picker; [ImageScanState.Idle] is the prompt shown before and after it.
 */
@Composable
fun ScanFromImageScreen(
    state: ImageScanState,
    onBack: () -> Unit,
    onChooseImage: () -> Unit,
    onCancel: () -> Unit,
    onCodeClick: (index: Int) -> Unit,
    onUseCamera: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable (imageUri: String, modifier: Modifier) -> Unit = { uri, m -> PickedImage(uri, m) },
) {
    when (state) {
        ImageScanState.Idle -> IdleContent(onBack, onChooseImage, modifier)
        is ImageScanState.Processing -> ProcessingContent(state, onCancel, image, modifier)
        is ImageScanState.Found -> FoundContent(state, onBack, onCodeClick, image, modifier)
        is ImageScanState.NoCode -> NoCodeContent(state, onBack, onChooseImage, onUseCamera, image, modifier)
    }
}

// ---- Idle ------------------------------------------------------------------------------------------------

@Composable
private fun IdleContent(onBack: () -> Unit, onChooseImage: () -> Unit, modifier: Modifier) {
    AppScaffold(modifier = modifier, topBar = { AppTopBar(stringResource(R.string.scan_image_choose_title), onBack = onBack) }) { padding ->
        Box(Modifier.padding(padding)) {
            EmptyState(
                icon = AppIcons.Image,
                title = stringResource(R.string.scan_image_choose_title),
                message = stringResource(R.string.scan_image_choose_message),
                action = {
                    PrimaryButton(
                        stringResource(R.string.scan_image_choose_button),
                        onChooseImage,
                        leadingIcon = AppIcons.Image,
                    )
                },
            )
        }
    }
}

// ---- Processing (s12) ------------------------------------------------------------------------------------

@Composable
private fun ProcessingContent(
    state: ImageScanState.Processing,
    onCancel: () -> Unit,
    image: @Composable (String, Modifier) -> Unit,
    modifier: Modifier,
) {
    AppScaffold(
        modifier = modifier,
        topBar = { CancelTopBar(stringResource(R.string.scan_image_reading_title), onCancel) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ImageCard(state.imageUri, image, height = 380.dp, modifier = Modifier.padding(top = 32.dp)) {
                    Box(Modifier.matchParentSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(AppTheme.colors.surface),
                            contentAlignment = Alignment.Center,
                        ) { LoadingSpinner(size = 56.dp) }
                    }
                }
                Column(
                    Modifier.fillMaxWidth().padding(top = 32.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.scan_image_processing_title),
                        style = AppTheme.type.headline,
                        color = AppTheme.colors.ink,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(R.string.scan_image_processing_message),
                        style = BodyLarge,
                        color = AppTheme.colors.inkSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            AppOutlinedButton(
                stringResource(R.string.scan_image_cancel),
                onCancel,
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 32.dp).heightIn(min = 56.dp),
            )
        }
    }
}

/** Top bar of s12: a close (cancel) button instead of the usual back arrow, then the title. */
@Composable
private fun CancelTopBar(title: String, onCancel: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButtonRounded(AppIcons.Close, stringResource(R.string.scan_image_cancel), onCancel)
        Text(
            title,
            style = AppTheme.type.topBar,
            color = AppTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
    }
}

// ---- Found (s13) -----------------------------------------------------------------------------------------

@Composable
private fun FoundContent(
    state: ImageScanState.Found,
    onBack: () -> Unit,
    onCodeClick: (Int) -> Unit,
    image: @Composable (String, Modifier) -> Unit,
    modifier: Modifier,
) {
    val single = state.codes.size == 1
    AppScaffold(modifier = modifier, topBar = { AppTopBar(stringResource(R.string.scan_image_result_title), onBack = onBack) }) { padding ->
        Column(Modifier.padding(top = padding.calculateTopPadding()).fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                ImageCard(state.imageUri, image, height = 300.dp) {
                    FoundBadge(state.codes.size, Modifier.align(Alignment.TopStart).padding(12.dp))
                }
                if (!single) {
                    Column(Modifier.padding(top = 20.dp).navigationBarsPadding()) { CodeChooser(state.codes, onCodeClick) }
                }
            }
            if (single) SingleFoundPanel(state.codes.first(), onOpen = { onCodeClick(0) })
        }
    }
}

@Composable
private fun ImageCard(
    imageUri: String,
    image: @Composable (String, Modifier) -> Unit,
    height: Dp,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth().height(height).clip(ImageCardShape)) {
        image(imageUri, Modifier.matchParentSize())
        overlay()
    }
}

@Composable
private fun FoundBadge(count: Int, modifier: Modifier) {
    Row(
        modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CameraColors.hit)
            .padding(start = 8.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Check, contentDescription = null, tint = CameraColors.onHit, modifier = Modifier.size(16.dp))
        Text(
            pluralStringResource(R.plurals.scan_image_codes_found, count, count),
            style = AppTheme.type.captionStrong,
            color = CameraColors.onHit,
            maxLines = 1,
        )
    }
}

/** Bottom panel of s13 for one code: type badge, what was found and the button that opens its result. */
@Composable
private fun SingleFoundPanel(code: FoundCode, onOpen: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.sheet)
            .background(AppTheme.colors.surface)
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 20.dp),
    ) {
        FoundHeader(code)
        Text(
            code.title,
            style = AppTheme.type.titleLarge,
            color = AppTheme.colors.ink,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
        PrimaryButton(
            stringResource(R.string.scan_image_open_result),
            onOpen,
            Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 52.dp),
            leadingIcon = AppIcons.OpenInNew,
        )
    }
}

@Composable
private fun FoundHeader(code: FoundCode) {
    val tone = scanTypeTone(code.type)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(scanTypeIcon(code.type), tone = tone)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppBadge(scanTypeLabel(code.type), tone = tone)
            Text(stringResource(R.string.scan_image_from_image), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
        }
    }
}

@Composable
private fun CodeChooser(codes: List<FoundCode>, onCodeClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.scan_image_pick_one),
            style = AppTheme.type.titleLarge,
            color = AppTheme.colors.ink,
            modifier = Modifier.semantics { heading() },
        )
        CardGroup {
            codes.forEachIndexed { index, code ->
                if (index > 0) AppDivider()
                ListRow(
                    title = code.title,
                    subtitle = scanTypeAndFormat(code.type, code.format),
                    leading = { IconTile(scanTypeIcon(code.type), tone = scanTypeTone(code.type)) },
                    trailing = { Icon(AppIcons.Chevron, contentDescription = null, tint = AppTheme.colors.inkSecondary) },
                    onClick = { onCodeClick(index) },
                )
            }
        }
    }
}

// ---- No code (s14) ---------------------------------------------------------------------------------------

@Composable
private fun NoCodeContent(
    state: ImageScanState.NoCode,
    onBack: () -> Unit,
    onTryAnother: () -> Unit,
    onUseCamera: () -> Unit,
    image: @Composable (String, Modifier) -> Unit,
    modifier: Modifier,
) {
    AppScaffold(modifier = modifier, topBar = { AppTopBar(stringResource(R.string.scan_image_result_title), onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ImageCard(state.imageUri, image, height = 200.dp) {
                    Box(Modifier.matchParentSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape).background(AppTheme.colors.surface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                AppIcons.Search,
                                contentDescription = null,
                                tint = AppTheme.colors.warning,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
                NoCodeText(state.unreadable)
                if (!state.unreadable) NoCodeTips()
            }
            Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(
                    stringResource(R.string.scan_nocode_try_another),
                    onTryAnother,
                    Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    leadingIcon = AppIcons.Image,
                )
                AppTextButton(
                    stringResource(R.string.scan_nocode_use_camera),
                    onUseCamera,
                    Modifier.fillMaxWidth(),
                    leadingIcon = AppIcons.Camera,
                )
            }
        }
    }
}

@Composable
private fun NoCodeText(unreadable: Boolean) {
    val title = if (unreadable) R.string.scan_unreadable_title else R.string.scan_nocode_title
    val message = if (unreadable) R.string.scan_unreadable_message else R.string.scan_nocode_message
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(title),
            style = AppTheme.type.display.copy(fontSize = 24.sp, lineHeight = 30.sp),
            color = AppTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(message), style = BodyLarge, color = AppTheme.colors.inkSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NoCodeTips() {
    CardGroup {
        TipRow(AppIcons.Scan, R.string.scan_nocode_tip_visible)
        AppDivider()
        TipRow(AppIcons.Image, R.string.scan_nocode_tip_quality)
        AppDivider()
        TipRow(AppIcons.Info, R.string.scan_nocode_tip_resolution)
    }
}

@Composable
private fun TipRow(
    icon: ImageVector,
    @StringRes text: Int,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, tone = Tone.Warning)
        Text(stringResource(text), style = AppTheme.type.body, color = AppTheme.colors.ink, modifier = Modifier.weight(1f))
    }
}

// ---- Previews --------------------------------------------------------------------------------------------

private val sampleCodes =
    listOf(
        FoundCode("Product Launch Meetup", ContentType.EVENT, CodeFormat.QR_CODE),
        FoundCode("5901234123457", ContentType.PRODUCT, CodeFormat.EAN_13),
        FoundCode("lumen.studio/portfolio", ContentType.WEBSITE, CodeFormat.QR_CODE),
    )

private val previewImage: @Composable (String, Modifier) -> Unit = { _, modifier ->
    Box(modifier.background(Brush.linearGradient(listOf(Color(0xFFCDB9A0), Color(0xFF8A7662)))))
}

@Composable
private fun PreviewHost(dark: Boolean, state: ImageScanState) {
    AppTheme(dark = dark) {
        ScanFromImageScreen(state, {}, {}, {}, {}, {}, image = previewImage)
    }
}

@Preview(name = "S11_PickImage_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S11_PickImage_Light() = PreviewHost(false, ImageScanState.Idle)

@Preview(name = "S11_PickImage_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S11_PickImage_Dark() = PreviewHost(true, ImageScanState.Idle)

@Preview(name = "S12_Processing_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S12_Processing_Light() = PreviewHost(false, ImageScanState.Processing("content://sample"))

@Preview(name = "S12_Processing_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S12_Processing_Dark() = PreviewHost(true, ImageScanState.Processing("content://sample"))

@Preview(name = "S13_CodeFound_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S13_CodeFound_Light() = PreviewHost(false, ImageScanState.Found("content://sample", sampleCodes.take(1)))

@Preview(name = "S13_CodeFound_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S13_CodeFound_Dark() = PreviewHost(true, ImageScanState.Found("content://sample", sampleCodes.take(1)))

@Preview(name = "S13_CodesFoundChooser_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S13_CodesFoundChooser_Light() = PreviewHost(false, ImageScanState.Found("content://sample", sampleCodes))

@Preview(name = "S13_CodesFoundChooser_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S13_CodesFoundChooser_Dark() = PreviewHost(true, ImageScanState.Found("content://sample", sampleCodes))

@Preview(name = "S14_NoCode_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S14_NoCode_Light() = PreviewHost(false, ImageScanState.NoCode("content://sample"))

@Preview(name = "S14_NoCode_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S14_NoCode_Dark() = PreviewHost(true, ImageScanState.NoCode("content://sample"))

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
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

/** Text colour on the teal "found" pill (design token `#06312B`). */
private val OnHitColor = Color(0xFF06312B)
private val ImageCardShape = RoundedCornerShape(28.dp)

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
                        leadingIcon = AppIcons.Gallery,
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
        topBar = { AppTopBar(stringResource(R.string.scan_image_reading_title), onBack = onCancel) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp)) {
            ImageCard(state.imageUri, image, height = 380.dp) {
                Box(Modifier.matchParentSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(AppTheme.colors.surface),
                        contentAlignment = Alignment.Center,
                    ) { LoadingSpinner(size = 40.dp) }
                }
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 32.dp),
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
                    style = AppTheme.type.body,
                    color = AppTheme.colors.inkSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Box(Modifier.weight(1f))
            AppOutlinedButton(stringResource(R.string.scan_image_cancel), onCancel, Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }
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
    AppScaffold(modifier = modifier, topBar = { AppTopBar(stringResource(R.string.scan_image_result_title), onBack = onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            ImageCard(state.imageUri, image, height = 300.dp) {
                FoundBadge(state.codes.size, Modifier.align(Alignment.TopStart).padding(12.dp))
            }
            Column(Modifier.padding(top = 20.dp)) {
                if (state.codes.size == 1) {
                    SingleFound(state.codes.first(), onOpen = { onCodeClick(0) })
                } else {
                    CodeChooser(state.codes, onCodeClick)
                }
            }
        }
    }
}

@Composable
private fun ImageCard(
    imageUri: String,
    image: @Composable (String, Modifier) -> Unit,
    height: Dp,
    overlay: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(height).clip(ImageCardShape)) {
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
        Icon(AppIcons.Check, contentDescription = null, tint = OnHitColor, modifier = Modifier.size(16.dp))
        Text(
            pluralStringResource(R.plurals.scan_image_codes_found, count, count),
            style = AppTheme.type.captionStrong,
            color = OnHitColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun SingleFound(code: FoundCode, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FoundHeader(code)
        Text(code.title, style = AppTheme.type.headline, color = AppTheme.colors.ink, maxLines = 3)
        PrimaryButton(
            stringResource(R.string.scan_image_open_result),
            onOpen,
            Modifier.fillMaxWidth().padding(top = 4.dp),
            leadingIcon = AppIcons.OpenInNew,
        )
    }
}

@Composable
private fun FoundHeader(code: FoundCode) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(scanTypeIcon(code.type), tone = Tone.Primary)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppBadge(scanTypeLabel(code.type))
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
                    leading = { IconTile(scanTypeIcon(code.type), tone = Tone.Primary) },
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
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
            Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(
                    stringResource(R.string.scan_nocode_try_another),
                    onTryAnother,
                    Modifier.fillMaxWidth(),
                    leadingIcon = AppIcons.Gallery,
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
            style = AppTheme.type.headline,
            color = AppTheme.colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(message), style = AppTheme.type.body, color = AppTheme.colors.inkSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NoCodeTips() {
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
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

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.InfoBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeInputs
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.PayloadBuilder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.SocialPlatform
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.labelRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.CodeCard

private const val HTTPS_PREFIX = "https://"
private val PreviewMaxWidth = 280.dp

@StringRes
private fun SocialPlatform.labelRes(): Int = when (this) {
    SocialPlatform.INSTAGRAM -> R.string.create_social_instagram
    SocialPlatform.FACEBOOK -> R.string.create_social_facebook
    SocialPlatform.X -> R.string.create_social_x
    SocialPlatform.LINKEDIN -> R.string.create_social_linkedin
    SocialPlatform.YOUTUBE -> R.string.create_social_youtube
    SocialPlatform.WHATSAPP -> R.string.create_social_whatsapp
    SocialPlatform.TELEGRAM -> R.string.create_social_telegram
}

/** Social profile (design s38): platform chips, handle, and the live "The code will open ..." line. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SocialFields(spec: CreateSpec.Social, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            SectionLabel(stringResource(R.string.create_social_platform))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (platform in SocialPlatform.entries) {
                    val selected = platform == spec.platform
                    AppChip(
                        label = stringResource(platform.labelRes()),
                        selected = selected,
                        onClick = { onSpec(spec.copy(platform = platform)) },
                        leadingIcon = if (selected) AppIcons.Check else null,
                    )
                }
            }
        }
        FormTextField(
            host = host,
            field = FormField.HANDLE,
            value = spec.handle,
            onValueChange = { onSpec(spec.copy(handle = it)) },
            label = stringResource(if (spec.platform.isPhoneBased) R.string.create_field_social_phone else R.string.create_field_username),
            helper = stringResource(if (spec.platform.isPhoneBased) R.string.create_helper_social_phone else R.string.create_helper_username),
            keyboardType = if (spec.platform.isPhoneBased) KeyboardType.Phone else KeyboardType.Text,
            last = true,
        )
        if (spec.handle.isNotBlank() && FormField.HANDLE !in host.invalid) {
            val target = PayloadBuilder.socialUrl(spec.platform, spec.handle).removePrefix(HTTPS_PREFIX)
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(AppIcons.Social)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.create_social_will_open), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
                        Text(target, style = AppTheme.type.title, color = AppTheme.colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@StringRes
private fun CreateType.valueLabelRes(): Int = when (this) {
    CreateType.BARCODE_EAN_13 -> R.string.create_field_value_ean13
    CreateType.BARCODE_EAN_8 -> R.string.create_field_value_ean8
    CreateType.BARCODE_UPC_A -> R.string.create_field_value_upca
    else -> R.string.create_field_value
}

@StringRes
private fun CreateType.helperRes(): Int = when (this) {
    CreateType.BARCODE_EAN_13 -> R.string.create_helper_ean13
    CreateType.BARCODE_EAN_8 -> R.string.create_helper_ean8
    CreateType.BARCODE_UPC_A -> R.string.create_helper_upca
    CreateType.BARCODE_CODE_128 -> R.string.create_helper_code128
    CreateType.BARCODE_CODE_39 -> R.string.create_helper_code39
    CreateType.BARCODE_ITF -> R.string.create_helper_itf
    else -> R.string.create_helper_2d
}

/** Custom data (design s39): QR / Barcode switch, symbology chips, the value with live validation and a preview. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BarcodeFields(
    spec: CreateSpec.Barcode,
    state: CreateFormUiState,
    host: FormFieldHost,
    onSpec: (CreateSpec) -> Unit,
    onSelectType: (CreateType) -> Unit,
    onSwitchToQr: () -> Unit,
) {
    val rules = BarcodeInputs.rules(spec.format)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            SectionLabel(stringResource(R.string.create_barcode_code_type))
            SegmentedControl(
                options = listOf(stringResource(R.string.create_barcode_switch_qr), stringResource(R.string.create_barcode_switch_barcode)),
                selectedIndex = 1,
                onSelect = { if (it == 0) onSwitchToQr() },
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (type in CreateType.barcodeTypes) {
                val selected = type == spec.type
                AppChip(
                    label = stringResource(type.labelRes()),
                    selected = selected,
                    onClick = { onSelectType(type) },
                    leadingIcon = if (selected) AppIcons.Check else null,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FormTextField(
                host = host,
                field = FormField.BARCODE_DATA,
                value = spec.data,
                onValueChange = { onSpec(spec.copy(data = it)) },
                label = stringResource(spec.type.valueLabelRes()),
                helper = stringResource(spec.type.helperRes()),
                keyboardType = if (rules.numericOnly) KeyboardType.Number else KeyboardType.Text,
                capitalization = if (spec.format == CodeFormat.CODE_39) KeyboardCapitalization.Characters else KeyboardCapitalization.None,
                singleLine = spec.format in SingleLineFormats,
                minLines = if (spec.format in SingleLineFormats) 1 else 2,
                last = true,
            )
            BarcodeStatusLine(state.barcodeStatus)
        }
        val preview = state.barcodePreview
        if (preview != null) {
            AppCard(Modifier.fillMaxWidth()) {
                SectionLabel(stringResource(R.string.create_barcode_preview))
                CodeCard(
                    image = preview,
                    logo = null,
                    contentDescription = stringResource(R.string.create_barcode_preview_description, spec.format.displayName),
                    modifier = Modifier.widthIn(max = PreviewMaxWidth).align(Alignment.CenterHorizontally),
                )
            }
        }
        InfoBanner(stringResource(R.string.create_barcode_tip), tone = Tone.Primary, icon = AppIcons.Info)
    }
}

private val SingleLineFormats = setOf(
    CodeFormat.EAN_13,
    CodeFormat.EAN_8,
    CodeFormat.UPC_A,
    CodeFormat.ITF,
    CodeFormat.CODE_39,
    CodeFormat.CODE_128,
)

@Composable
private fun BarcodeStatusLine(status: BarcodeStatus) {
    when (status) {
        BarcodeStatus.Empty, is BarcodeStatus.Invalid -> Unit
        is BarcodeStatus.Incomplete -> Text(
            stringResource(R.string.create_barcode_status_incomplete, status.count, status.needed),
            style = AppTheme.type.caption,
            color = AppTheme.colors.inkSecondary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        is BarcodeStatus.CheckDigitWillBeAdded -> ValidLine(stringResource(R.string.create_barcode_status_will_add, status.digit))
        is BarcodeStatus.Complete -> ValidLine(
            if (status.checkDigit != null) {
                stringResource(R.string.create_barcode_status_valid_check, status.length)
            } else {
                stringResource(R.string.create_barcode_status_valid, status.length)
            },
        )
    }
}

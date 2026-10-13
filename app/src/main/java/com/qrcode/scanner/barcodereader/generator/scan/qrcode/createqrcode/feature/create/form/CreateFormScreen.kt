package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.DensityLevel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.EventTimes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.Validators
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.fieldErrorText
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generateLabelRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.icon
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.introRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.titleRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.tone
import java.util.TimeZone

@Composable
fun CreateFormRoute(
    onBack: () -> Unit,
    onGenerated: (recordId: Long) -> Unit,
    onSwitchToQr: () -> Unit,
    viewModel: CreateFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val failedText = stringResource(R.string.create_error_generate_failed)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CreateFormEffect.OpenGenerated -> onGenerated(effect.recordId)
                CreateFormEffect.GenerateFailed -> snackbar.showSnackbar(failedText)
            }
        }
    }
    CreateFormScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onSwitchToQr = onSwitchToQr,
        snackbarHostState = snackbar,
    )
}

/**
 * One screen for every Create form (design s32 to s39): the fields come from [CreateFormUiState.spec] and
 * Generate stays disabled until the form validates.
 */
@Composable
fun CreateFormScreen(
    state: CreateFormUiState,
    onEvent: (CreateFormEvent) -> Unit,
    onBack: () -> Unit,
    onSwitchToQr: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spec = state.spec
    if (spec == null) {
        UnknownTypeScreen(onBack = onBack, modifier = modifier)
        return
    }
    val errorTexts = LinkedHashMap<FormField, String>()
    for ((field, error) in state.visibleErrors) errorTexts[field] = fieldErrorText(error, field)
    val host = FormFieldHost(
        errors = errorTexts,
        invalid = state.errors.keys,
        onBlur = { onEvent(CreateFormEvent.Blur(it)) },
        onSubmit = { onEvent(CreateFormEvent.Generate) },
    )
    AppScaffold(
        modifier = modifier.imePadding(),
        topBar = { AppTopBar(title = stringResource(spec.type.titleRes()), onBack = onBack) },
        bottomBar = {
            ActionBar {
                PrimaryButton(
                    text = stringResource(spec.type.generateLabelRes()),
                    onClick = { onEvent(CreateFormEvent.Generate) },
                    enabled = state.isValid && !state.generating,
                    leadingIcon = if (spec.type.isBarcode) AppIcons.Barcode else AppIcons.QrCode,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FormIntro(spec.type)
            FormFields(spec = spec, state = state, host = host, onEvent = onEvent, onSwitchToQr = onSwitchToQr)
        }
    }
}

@Composable
private fun FormIntro(type: CreateType) {
    val intro = type.introRes() ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconTile(icon = type.icon(), tone = type.tone(), size = 56.dp)
        Column(Modifier.weight(1f)) {
            Text(stringResource(intro.first), style = AppTheme.type.titleLarge, color = AppTheme.colors.ink)
            Text(stringResource(intro.second), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
        }
    }
}

/** Picks the fields of [spec]'s type. */
@Composable
private fun FormFields(
    spec: CreateSpec,
    state: CreateFormUiState,
    host: FormFieldHost,
    onEvent: (CreateFormEvent) -> Unit,
    onSwitchToQr: () -> Unit,
) {
    val onSpec: (CreateSpec) -> Unit = { onEvent(CreateFormEvent.Edit(it)) }
    when (spec) {
        is CreateSpec.Website -> WebsiteFields(spec, host, onSpec)
        is CreateSpec.Text -> TextFields(spec, state.density, host, onSpec)
        is CreateSpec.Contact -> ContactFields(spec, host, onSpec)
        is CreateSpec.Wifi -> WifiFields(spec, host, onSpec)
        is CreateSpec.Email -> EmailFields(spec, host, onSpec)
        is CreateSpec.Phone -> PhoneFields(spec, host, onSpec)
        is CreateSpec.Sms -> SmsFields(spec, host, onSpec)
        is CreateSpec.Location -> LocationFields(spec, host, onSpec)
        is CreateSpec.Event -> EventFields(spec, host, onSpec, onEvent)
        is CreateSpec.Social -> SocialFields(spec, host, onSpec)
        is CreateSpec.AppStore -> AppStoreFields(spec, host, onSpec)
        is CreateSpec.Barcode -> BarcodeFields(
            spec = spec,
            state = state,
            host = host,
            onSpec = onSpec,
            onSelectType = { onEvent(CreateFormEvent.SelectBarcode(it)) },
            onSwitchToQr = onSwitchToQr,
        )
    }
}

@Composable
private fun UnknownTypeScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.create_hub_title), onBack = onBack) },
    ) { padding ->
        EmptyState(
            icon = AppIcons.Warning,
            title = stringResource(R.string.create_unknown_type_title),
            message = stringResource(R.string.create_unknown_type_message),
            modifier = Modifier.fillMaxSize().padding(padding),
            action = { AppTextButton(text = stringResource(R.string.common_back), onClick = onBack) },
        )
    }
}

// ---- previews ----

private fun previewState(spec: CreateSpec, touched: Set<FormField> = emptySet()): CreateFormUiState =
    CreateFormUiState(spec = spec, errors = Validators.validate(spec), touched = touched)

@Composable
private fun PreviewForm(dark: Boolean, state: CreateFormUiState) {
    AppTheme(dark = dark) {
        CreateFormScreen(state = state, onEvent = {}, onBack = {}, onSwitchToQr = {})
    }
}

private val websiteError = previewState(CreateSpec.Website("brewhouse coffee"), setOf(FormField.URL))
private val textState = previewState(CreateSpec.Text("Meeting notes: ship v2.4 on Friday.")).copy(density = DensityLevel.LOW)
private val phoneState = previewState(CreateSpec.Phone("(206) 555-0198"))
private val contactState = previewState(
    CreateSpec.Contact(name = "Sophie Lambert", phone = "+1 (415) 555-0142", email = "sophie@lumen.studio"),
)
private val wifiState = previewState(CreateSpec.Wifi(ssid = "CafeNova_Guest", password = "guestpass123"))
private val eventState = previewState(
    EventTimes.blankEvent(1_800_000_000_000L, TimeZone.getTimeZone("UTC")).copy(title = "Product Launch Meetup"),
)
private val socialState = previewState(CreateSpec.Social(handle = "lumen.studio"))
private val barcodeState = previewState(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "590123412345"))
    .copy(barcodeStatus = BarcodeStatus.CheckDigitWillBeAdded(7))

@Preview(name = "S32_WebsiteFormError_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S32_WebsiteFormError_Light() = PreviewForm(false, websiteError)

@Preview(name = "S32_WebsiteFormError_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S32_WebsiteFormError_Dark() = PreviewForm(true, websiteError)

@Preview(name = "S33_TextForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S33_TextForm_Light() = PreviewForm(false, textState)

@Preview(name = "S33_TextForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S33_TextForm_Dark() = PreviewForm(true, textState)

@Preview(name = "S34_PhoneForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S34_PhoneForm_Light() = PreviewForm(false, phoneState)

@Preview(name = "S34_PhoneForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S34_PhoneForm_Dark() = PreviewForm(true, phoneState)

@Preview(name = "S35_ContactForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S35_ContactForm_Light() = PreviewForm(false, contactState)

@Preview(name = "S35_ContactForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S35_ContactForm_Dark() = PreviewForm(true, contactState)

@Preview(name = "S36_WifiForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S36_WifiForm_Light() = PreviewForm(false, wifiState)

@Preview(name = "S36_WifiForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S36_WifiForm_Dark() = PreviewForm(true, wifiState)

@Preview(name = "S37_EventForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S37_EventForm_Light() = PreviewForm(false, eventState)

@Preview(name = "S37_EventForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S37_EventForm_Dark() = PreviewForm(true, eventState)

@Preview(name = "S38_SocialForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S38_SocialForm_Light() = PreviewForm(false, socialState)

@Preview(name = "S38_SocialForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S38_SocialForm_Dark() = PreviewForm(true, socialState)

@Preview(name = "S39_CustomDataForm_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S39_CustomDataForm_Light() = PreviewForm(false, barcodeState)

@Preview(name = "S39_CustomDataForm_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S39_CustomDataForm_Dark() = PreviewForm(true, barcodeState)

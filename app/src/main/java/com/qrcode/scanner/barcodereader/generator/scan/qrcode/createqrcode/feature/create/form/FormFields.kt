package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBadge
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.InfoBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PasswordVisibilityToggle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsSwitchRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.DensityLevel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.Limits
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

private val FieldSpacing = 12.dp

/** Website (design s32): one URL field and a tip. */
@Composable
internal fun WebsiteFields(spec: CreateSpec.Website, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FormTextField(
            host = host,
            field = FormField.URL,
            value = spec.url,
            onValueChange = { onSpec(spec.copy(url = it)) },
            label = stringResource(R.string.create_field_website_url),
            placeholder = stringResource(R.string.create_placeholder_url),
            keyboardType = KeyboardType.Uri,
            last = true,
        )
        InfoBanner(stringResource(R.string.create_website_tip), tone = Tone.Primary, icon = AppIcons.Info)
    }
}

/** Plain text (design s33): multi-line field, character counter and the density card. */
@Composable
internal fun TextFields(spec: CreateSpec.Text, density: DensityLevel?, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            FormTextField(
                host = host,
                field = FormField.TEXT,
                value = spec.text,
                onValueChange = { onSpec(spec.copy(text = it)) },
                label = stringResource(R.string.create_field_your_text),
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                singleLine = false,
                minLines = 4,
                last = true,
            )
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.create_text_hint), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary, modifier = Modifier.weight(1f))
                Text(
                    stringResource(R.string.create_text_counter, spec.text.length, Limits.TEXT),
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        }
        if (density != null) DensityCard(density)
    }
}

@Composable
private fun DensityCard(density: DensityLevel) {
    val (detail, badge, tone) = when (density) {
        DensityLevel.LOW -> Triple(R.string.create_density_low, R.string.create_density_good, Tone.Success)
        DensityLevel.MEDIUM -> Triple(R.string.create_density_medium, R.string.create_density_good, Tone.Success)
        DensityLevel.HIGH -> Triple(R.string.create_density_high, R.string.create_density_dense, Tone.Warning)
    }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconTile(AppIcons.QrCode)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.create_density_title), style = AppTheme.type.title, color = AppTheme.colors.ink)
                Text(stringResource(detail), style = AppTheme.type.caption, color = AppTheme.colors.inkSecondary)
            }
            AppBadge(stringResource(badge), tone = tone)
        }
    }
}

/** Phone number (design s34), with a live "Valid number" line. */
@Composable
internal fun PhoneFields(spec: CreateSpec.Phone, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormTextField(
            host = host,
            field = FormField.PHONE,
            value = spec.number,
            onValueChange = { onSpec(spec.copy(number = it)) },
            label = stringResource(R.string.create_field_phone_number),
            helper = stringResource(R.string.create_helper_phone),
            keyboardType = KeyboardType.Phone,
            last = true,
        )
        if (spec.number.isNotBlank() && FormField.PHONE !in host.invalid) ValidLine(stringResource(R.string.create_phone_valid))
    }
}

/** Green check with a short confirmation. */
@Composable
internal fun ValidLine(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            AppIcons.Check,
            contentDescription = null,
            tint = AppTheme.colors.success,
            modifier = Modifier.size(16.dp),
        )
        Text(text, style = AppTheme.type.caption, color = AppTheme.colors.success)
    }
}

/** Contact (design s35): becomes a vCard. */
@Composable
internal fun ContactFields(spec: CreateSpec.Contact, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        FormTextField(
            host, FormField.NAME, spec.name, { onSpec(spec.copy(name = it)) },
            stringResource(R.string.create_field_full_name),
            capitalization = KeyboardCapitalization.Words,
        )
        FormTextField(
            host, FormField.PHONE, spec.phone, { onSpec(spec.copy(phone = it)) },
            stringResource(R.string.create_field_mobile_phone),
            keyboardType = KeyboardType.Phone,
        )
        FormTextField(
            host, FormField.EMAIL, spec.email, { onSpec(spec.copy(email = it)) },
            stringResource(R.string.create_field_email),
            keyboardType = KeyboardType.Email,
        )
        FormTextField(
            host, FormField.ORGANIZATION, spec.organization, { onSpec(spec.copy(organization = it)) },
            stringResource(R.string.create_field_company),
            capitalization = KeyboardCapitalization.Words,
        )
        FormTextField(
            host, FormField.JOB_TITLE, spec.jobTitle, { onSpec(spec.copy(jobTitle = it)) },
            stringResource(R.string.create_field_job_title),
            capitalization = KeyboardCapitalization.Words,
        )
        FormTextField(
            host, FormField.ADDRESS, spec.address, { onSpec(spec.copy(address = it)) },
            stringResource(R.string.create_field_address),
            capitalization = KeyboardCapitalization.Words,
            singleLine = false,
            minLines = 2,
        )
        FormTextField(
            host, FormField.WEBSITE, spec.website, { onSpec(spec.copy(website = it)) },
            stringResource(R.string.create_field_website_optional),
            keyboardType = KeyboardType.Uri,
        )
        FormTextField(
            host, FormField.NOTE, spec.note, { onSpec(spec.copy(note = it)) },
            stringResource(R.string.create_field_note),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            minLines = 2,
            last = true,
        )
        Text(
            stringResource(R.string.create_contact_footnote),
            style = AppTheme.type.caption,
            color = AppTheme.colors.inkSecondary,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

private val WifiSecurities = listOf(WifiSecurity.NONE, WifiSecurity.WEP, WifiSecurity.WPA)

/** Wi-Fi (design s36): SSID, password with show/hide, security, hidden switch. */
@Composable
internal fun WifiFields(spec: CreateSpec.Wifi, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FormTextField(
            host, FormField.SSID, spec.ssid, { onSpec(spec.copy(ssid = it)) },
            stringResource(R.string.create_field_ssid),
            last = spec.security == WifiSecurity.NONE,
        )
        if (spec.security != WifiSecurity.NONE) {
            FormTextField(
                host, FormField.PASSWORD, spec.password, { onSpec(spec.copy(password = it)) },
                stringResource(R.string.create_field_password),
                keyboardType = KeyboardType.Password,
                last = true,
                trailing = { PasswordVisibilityToggle(visible = passwordVisible, onToggle = { passwordVisible = !passwordVisible }) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.create_field_security), style = AppTheme.type.label, color = AppTheme.colors.inkSecondary, modifier = Modifier.padding(horizontal = 4.dp))
            SegmentedControl(
                options = WifiSecurities.map { stringResource(it.labelRes()) },
                selectedIndex = WifiSecurities.indexOf(spec.security),
                onSelect = { onSpec(spec.copy(security = WifiSecurities[it])) },
            )
        }
        CardGroup {
            SettingsSwitchRow(
                title = stringResource(R.string.create_wifi_hidden),
                subtitle = stringResource(R.string.create_wifi_hidden_hint),
                checked = spec.hidden,
                onCheckedChange = { onSpec(spec.copy(hidden = it)) },
            )
        }
        InfoBanner(stringResource(R.string.create_wifi_warning), tone = Tone.Warning, icon = AppIcons.Info)
    }
}

@StringRes
private fun WifiSecurity.labelRes(): Int = when (this) {
    WifiSecurity.NONE -> R.string.create_wifi_security_none
    WifiSecurity.WEP -> R.string.create_wifi_security_wep
    WifiSecurity.WPA -> R.string.create_wifi_security_wpa
}

/** Email: recipient, subject and body become a mailto: link. */
@Composable
internal fun EmailFields(spec: CreateSpec.Email, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        FormTextField(
            host, FormField.EMAIL, spec.to, { onSpec(spec.copy(to = it)) },
            stringResource(R.string.create_field_email_to),
            keyboardType = KeyboardType.Email,
        )
        FormTextField(
            host, FormField.SUBJECT, spec.subject, { onSpec(spec.copy(subject = it)) },
            stringResource(R.string.create_field_subject),
            capitalization = KeyboardCapitalization.Sentences,
        )
        FormTextField(
            host, FormField.BODY, spec.body, { onSpec(spec.copy(body = it)) },
            stringResource(R.string.create_field_message),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            minLines = 3,
            last = true,
        )
    }
}

/** SMS: number and optional message. */
@Composable
internal fun SmsFields(spec: CreateSpec.Sms, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        FormTextField(
            host, FormField.PHONE, spec.number, { onSpec(spec.copy(number = it)) },
            stringResource(R.string.create_field_phone_number),
            helper = stringResource(R.string.create_helper_phone),
            keyboardType = KeyboardType.Phone,
        )
        FormTextField(
            host, FormField.MESSAGE, spec.message, { onSpec(spec.copy(message = it)) },
            stringResource(R.string.create_field_sms_message),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            minLines = 3,
            last = true,
        )
    }
}

/** Location: decimal degrees plus an optional label. Latitude and longitude use a text keyboard so the minus sign is available. */
@Composable
internal fun LocationFields(spec: CreateSpec.Location, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FieldSpacing)) {
        FormTextField(
            host, FormField.LATITUDE, spec.latitude, { onSpec(spec.copy(latitude = it)) },
            stringResource(R.string.create_field_latitude),
            helper = stringResource(R.string.create_helper_latitude),
            placeholder = stringResource(R.string.create_placeholder_latitude),
        )
        FormTextField(
            host, FormField.LONGITUDE, spec.longitude, { onSpec(spec.copy(longitude = it)) },
            stringResource(R.string.create_field_longitude),
            helper = stringResource(R.string.create_helper_longitude),
            placeholder = stringResource(R.string.create_placeholder_longitude),
        )
        FormTextField(
            host, FormField.LABEL, spec.label, { onSpec(spec.copy(label = it)) },
            stringResource(R.string.create_field_place_label),
            capitalization = KeyboardCapitalization.Words,
            last = true,
        )
    }
}

/** App Store / Google Play link. */
@Composable
internal fun AppStoreFields(spec: CreateSpec.AppStore, host: FormFieldHost, onSpec: (CreateSpec) -> Unit) {
    FormTextField(
        host = host,
        field = FormField.URL,
        value = spec.url,
        onValueChange = { onSpec(spec.copy(url = it)) },
        label = stringResource(R.string.create_field_app_link),
        helper = stringResource(R.string.create_helper_app_link),
        placeholder = stringResource(R.string.create_placeholder_app_link),
        keyboardType = KeyboardType.Uri,
        last = true,
    )
}

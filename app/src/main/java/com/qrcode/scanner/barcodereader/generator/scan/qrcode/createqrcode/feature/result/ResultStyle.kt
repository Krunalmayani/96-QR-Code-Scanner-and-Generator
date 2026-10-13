package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.RiskKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure

internal fun ContentType.icon(): ImageVector =
    when (this) {
        ContentType.WEBSITE -> AppIcons.Link
        ContentType.TEXT -> AppIcons.Text
        ContentType.CONTACT -> AppIcons.Person
        ContentType.PHONE -> AppIcons.Phone
        ContentType.EMAIL -> AppIcons.Email
        ContentType.SMS -> AppIcons.Sms
        ContentType.LOCATION -> AppIcons.Location
        ContentType.EVENT -> AppIcons.Event
        ContentType.WIFI -> AppIcons.Wifi
        ContentType.PRODUCT -> AppIcons.Product
        ContentType.UNKNOWN -> AppIcons.QrCode
    }

internal fun ContentType.tone(): Tone =
    when (this) {
        ContentType.TEXT, ContentType.PRODUCT -> Tone.Warning
        ContentType.SMS, ContentType.WIFI -> Tone.Success
        else -> Tone.Primary
    }

@StringRes
internal fun ContentType.titleRes(): Int =
    when (this) {
        ContentType.WEBSITE -> R.string.result_title_website
        ContentType.TEXT -> R.string.result_title_text
        ContentType.CONTACT -> R.string.result_title_contact
        ContentType.PHONE -> R.string.result_title_phone
        ContentType.EMAIL -> R.string.result_title_email
        ContentType.SMS -> R.string.result_title_sms
        ContentType.LOCATION -> R.string.result_title_location
        ContentType.EVENT -> R.string.result_title_event
        ContentType.WIFI -> R.string.result_title_wifi
        ContentType.PRODUCT -> R.string.result_title_product
        ContentType.UNKNOWN -> R.string.result_title_unknown
    }

@StringRes
internal fun ContentType.badgeRes(): Int =
    when (this) {
        ContentType.WEBSITE -> R.string.result_badge_website
        ContentType.TEXT -> R.string.result_badge_text
        ContentType.CONTACT -> R.string.result_badge_contact
        ContentType.PHONE -> R.string.result_badge_phone
        ContentType.EMAIL -> R.string.result_badge_email
        ContentType.SMS -> R.string.result_badge_sms
        ContentType.LOCATION -> R.string.result_badge_location
        ContentType.EVENT -> R.string.result_badge_event
        ContentType.WIFI -> R.string.result_badge_wifi
        ContentType.PRODUCT -> R.string.result_badge_product
        ContentType.UNKNOWN -> R.string.result_badge_unknown
    }

@StringRes
internal fun WifiSecurity.labelRes(): Int =
    when (this) {
        WifiSecurity.NONE -> R.string.result_wifi_security_none
        WifiSecurity.WEP -> R.string.result_wifi_security_wep
        WifiSecurity.WPA -> R.string.result_wifi_security_wpa
    }

@StringRes
internal fun WifiFailure.messageRes(): Int =
    when (this) {
        WifiFailure.UNAVAILABLE -> R.string.result_wifi_failed_unavailable
        WifiFailure.PERMISSION_DENIED -> R.string.result_wifi_failed_permission
        WifiFailure.UNSUPPORTED_SECURITY -> R.string.result_wifi_failed_unsupported
        WifiFailure.INVALID_PASSWORD -> R.string.result_wifi_failed_invalid_password
        WifiFailure.WIFI_DISABLED -> R.string.result_wifi_failed_disabled
        WifiFailure.ERROR -> R.string.result_wifi_failed_error
    }

@StringRes
internal fun ResultMessage.messageRes(): Int =
    when (this) {
        ResultMessage.COPIED -> R.string.common_copied
        ResultMessage.NO_APP_FOUND -> R.string.result_msg_no_app
        ResultMessage.CANNOT_OPEN -> R.string.result_msg_cannot_open
        ResultMessage.STILL_OFFLINE -> R.string.result_msg_still_offline
    }

/** Short chip text of a risk reason in the unsafe-link sheet. */
@StringRes
internal fun RiskKind.chipRes(): Int =
    when (this) {
        RiskKind.NOT_HTTPS -> R.string.result_risk_not_https
        RiskKind.LOOKALIKE_BRAND -> R.string.result_risk_lookalike
        RiskKind.PUNYCODE -> R.string.result_risk_punycode
        RiskKind.SUSPICIOUS_TLD -> R.string.result_risk_tld
        RiskKind.URL_SHORTENER -> R.string.result_risk_shortener
        RiskKind.IP_ADDRESS -> R.string.result_risk_ip
        RiskKind.USER_INFO -> R.string.result_risk_userinfo
        RiskKind.EXCESSIVE_SUBDOMAINS -> R.string.result_risk_subdomains
        RiskKind.NON_WEB_SCHEME -> R.string.result_risk_scheme
        RiskKind.VERY_LONG_URL -> R.string.result_risk_long
    }

/** One-line explanation of a risk reason; the placeholder (if any) is filled with RiskReason.detail. */
@StringRes
internal fun RiskKind.detailRes(): Int =
    when (this) {
        RiskKind.NOT_HTTPS -> R.string.result_risk_not_https_detail
        RiskKind.LOOKALIKE_BRAND -> R.string.result_risk_lookalike_detail
        RiskKind.PUNYCODE -> R.string.result_risk_punycode_detail
        RiskKind.SUSPICIOUS_TLD -> R.string.result_risk_tld_detail
        RiskKind.URL_SHORTENER -> R.string.result_risk_shortener_detail
        RiskKind.IP_ADDRESS -> R.string.result_risk_ip_detail
        RiskKind.USER_INFO -> R.string.result_risk_userinfo_detail
        RiskKind.EXCESSIVE_SUBDOMAINS -> R.string.result_risk_subdomains_detail
        RiskKind.NON_WEB_SCHEME -> R.string.result_risk_scheme_detail
        RiskKind.VERY_LONG_URL -> R.string.result_risk_long_detail
    }

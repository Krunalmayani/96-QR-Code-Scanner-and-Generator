package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType

/** Short name on the hub tile and in the search. */
@StringRes
fun CreateType.labelRes(): Int =
    when (this) {
        CreateType.WEBSITE -> R.string.create_type_website
        CreateType.TEXT -> R.string.create_type_text
        CreateType.CONTACT -> R.string.create_type_contact
        CreateType.WIFI -> R.string.create_type_wifi
        CreateType.EMAIL -> R.string.create_type_email
        CreateType.PHONE -> R.string.create_type_phone
        CreateType.SMS -> R.string.create_type_sms
        CreateType.LOCATION -> R.string.create_type_location
        CreateType.EVENT -> R.string.create_type_event
        CreateType.SOCIAL -> R.string.create_type_social
        CreateType.APP_STORE -> R.string.create_type_app_link
        CreateType.BARCODE_EAN_13 -> R.string.create_type_ean13
        CreateType.BARCODE_EAN_8 -> R.string.create_type_ean8
        CreateType.BARCODE_UPC_A -> R.string.create_type_upca
        CreateType.BARCODE_CODE_128 -> R.string.create_type_code128
        CreateType.BARCODE_CODE_39 -> R.string.create_type_code39
        CreateType.BARCODE_ITF -> R.string.create_type_itf
        CreateType.BARCODE_PDF_417 -> R.string.create_type_pdf417
        CreateType.BARCODE_DATA_MATRIX -> R.string.create_type_data_matrix
        CreateType.BARCODE_AZTEC -> R.string.create_type_aztec
    }

/** Title in the top bar of the form screen. All barcode types share one "Custom data" form. */
@StringRes
fun CreateType.titleRes(): Int =
    when (this) {
        CreateType.WEBSITE -> R.string.create_title_website
        CreateType.TEXT -> R.string.create_title_text
        CreateType.CONTACT -> R.string.create_title_contact
        CreateType.WIFI -> R.string.create_title_wifi
        CreateType.EMAIL -> R.string.create_title_email
        CreateType.PHONE -> R.string.create_title_phone
        CreateType.SMS -> R.string.create_title_sms
        CreateType.LOCATION -> R.string.create_title_location
        CreateType.EVENT -> R.string.create_title_event
        CreateType.SOCIAL -> R.string.create_title_social
        CreateType.APP_STORE -> R.string.create_title_app_link
        else -> R.string.create_title_custom_data
    }

fun CreateType.icon(): ImageVector =
    when (this) {
        CreateType.WEBSITE -> AppIcons.Link
        CreateType.TEXT -> AppIcons.Text
        CreateType.CONTACT -> AppIcons.Person
        CreateType.WIFI -> AppIcons.Wifi
        CreateType.EMAIL -> AppIcons.Email
        CreateType.PHONE -> AppIcons.Phone
        CreateType.SMS -> AppIcons.Sms
        CreateType.LOCATION -> AppIcons.Location
        CreateType.EVENT -> AppIcons.Calendar
        CreateType.SOCIAL -> AppIcons.Share
        CreateType.APP_STORE -> AppIcons.Apps
        else -> AppIcons.Barcode
    }

/** Tile color: mirrors the design's mix of primary, teal, amber and red tiles. */
fun CreateType.tone(): Tone =
    when (this) {
        CreateType.TEXT, CreateType.EMAIL, CreateType.APP_STORE -> Tone.Warning
        CreateType.CONTACT, CreateType.SMS, CreateType.WIFI -> Tone.Success
        CreateType.LOCATION -> Tone.Error
        else -> if (isBarcode) Tone.Success else Tone.Primary
    }

/** Headline and subline above the fields (design s32 to s34, s36); null for forms that start with the fields. */
fun CreateType.introRes(): Pair<Int, Int>? =
    when (this) {
        CreateType.WEBSITE -> R.string.create_intro_website_title to R.string.create_intro_website_text
        CreateType.TEXT -> R.string.create_intro_text_title to R.string.create_intro_text_text
        CreateType.PHONE -> R.string.create_intro_phone_title to R.string.create_intro_phone_text
        CreateType.WIFI -> R.string.create_intro_wifi_title to R.string.create_intro_wifi_text
        CreateType.EMAIL -> R.string.create_intro_email_title to R.string.create_intro_email_text
        CreateType.SMS -> R.string.create_intro_sms_title to R.string.create_intro_sms_text
        CreateType.LOCATION -> R.string.create_intro_location_title to R.string.create_intro_location_text
        CreateType.APP_STORE -> R.string.create_intro_app_link_title to R.string.create_intro_app_link_text
        else -> null
    }

@StringRes
fun CreateType.generateLabelRes(): Int =
    if (isBarcode) R.string.create_generate_barcode else R.string.create_generate_qr

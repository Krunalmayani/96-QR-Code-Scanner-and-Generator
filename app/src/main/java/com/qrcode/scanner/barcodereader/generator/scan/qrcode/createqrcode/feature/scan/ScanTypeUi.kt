package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType

@StringRes
private fun typeLabelRes(type: ContentType): Int =
    when (type) {
        ContentType.WEBSITE -> R.string.scan_type_website
        ContentType.TEXT -> R.string.scan_type_text
        ContentType.CONTACT -> R.string.scan_type_contact
        ContentType.PHONE -> R.string.scan_type_phone
        ContentType.EMAIL -> R.string.scan_type_email
        ContentType.SMS -> R.string.scan_type_sms
        ContentType.LOCATION -> R.string.scan_type_location
        ContentType.EVENT -> R.string.scan_type_event
        ContentType.WIFI -> R.string.scan_type_wifi
        ContentType.PRODUCT -> R.string.scan_type_product
        ContentType.UNKNOWN -> R.string.scan_type_unknown
    }

@Composable
internal fun scanTypeLabel(type: ContentType): String = stringResource(typeLabelRes(type))

internal fun scanTypeIcon(type: ContentType): ImageVector =
    when (type) {
        ContentType.WEBSITE -> AppIcons.Link
        ContentType.TEXT -> AppIcons.Text
        ContentType.CONTACT -> AppIcons.Person
        ContentType.PHONE -> AppIcons.Phone
        ContentType.EMAIL -> AppIcons.Email
        ContentType.SMS -> AppIcons.Sms
        ContentType.LOCATION -> AppIcons.Location
        ContentType.EVENT -> AppIcons.Calendar
        ContentType.WIFI -> AppIcons.Wifi
        ContentType.PRODUCT -> AppIcons.Product
        ContentType.UNKNOWN -> AppIcons.Barcode
    }

/** Tile colour per type, the same mapping as history rows and result headers: teal places and networks, amber text and products. */
internal fun scanTypeTone(type: ContentType): Tone =
    when (type) {
        ContentType.WIFI, ContentType.LOCATION -> Tone.Success
        ContentType.TEXT, ContentType.PRODUCT -> Tone.Warning
        else -> Tone.Primary
    }

/** "Product · EAN-13": type label plus the symbology name (a data name, not translated). */
@Composable
internal fun scanTypeAndFormat(type: ContentType, format: CodeFormat): String =
    stringResource(R.string.scan_row_subtitle, scanTypeLabel(type), format.displayName)

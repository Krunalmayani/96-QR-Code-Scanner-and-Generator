package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Icon of the tile in front of a history row. */
internal fun ContentType.historyIcon(): ImageVector = when (this) {
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
    ContentType.UNKNOWN -> AppIcons.QrCode
}

/** Tile colour: teal for Wi-Fi and places, amber for plain text and products, indigo for the rest (design s45). */
internal fun ContentType.historyTone(): Tone = when (this) {
    ContentType.WIFI, ContentType.LOCATION -> Tone.Success
    ContentType.TEXT, ContentType.PRODUCT -> Tone.Warning
    else -> Tone.Primary
}

@StringRes
internal fun ContentType.historyLabelRes(): Int = when (this) {
    ContentType.WEBSITE -> R.string.hist_type_website
    ContentType.TEXT -> R.string.hist_type_text
    ContentType.CONTACT -> R.string.hist_type_contact
    ContentType.PHONE -> R.string.hist_type_phone
    ContentType.EMAIL -> R.string.hist_type_email
    ContentType.SMS -> R.string.hist_type_sms
    ContentType.LOCATION -> R.string.hist_type_location
    ContentType.EVENT -> R.string.hist_type_event
    ContentType.WIFI -> R.string.hist_type_wifi
    ContentType.PRODUCT -> R.string.hist_type_product
    ContentType.UNKNOWN -> R.string.hist_type_unknown
}

@StringRes
internal fun HistoryChip.labelRes(): Int = when (this) {
    HistoryChip.ALL -> R.string.hist_chip_all
    HistoryChip.FAVORITES -> R.string.hist_chip_favorites
    HistoryChip.LINKS -> R.string.hist_chip_links
    HistoryChip.WIFI -> R.string.hist_chip_wifi
    HistoryChip.CONTACTS -> R.string.hist_chip_contacts
    HistoryChip.TEXT -> R.string.hist_chip_text
    HistoryChip.PHONE -> R.string.hist_chip_phone
    HistoryChip.EMAIL -> R.string.hist_chip_email
    HistoryChip.LOCATION -> R.string.hist_chip_location
    HistoryChip.EVENTS -> R.string.hist_chip_events
    HistoryChip.PRODUCTS -> R.string.hist_chip_products
}

@StringRes
internal fun SearchScope.labelRes(): Int = when (this) {
    SearchScope.ALL -> R.string.hist_chip_all
    SearchScope.SCANNED -> R.string.hist_tab_scanned
    SearchScope.CREATED -> R.string.hist_tab_created
    SearchScope.FAVORITES -> R.string.hist_chip_favorites
}

/** Locale-aware time and date formatting for rows and day headers. */
internal class HistoryFormats(zone: ZoneId) {
    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(zone)
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    fun time(epochMillis: Long): String = timeFormat.format(Instant.ofEpochMilli(epochMillis))

    fun date(day: LocalDate): String = dateFormat.format(day)
}

@Composable
internal fun rememberHistoryFormats(): HistoryFormats = remember { HistoryFormats(ZoneId.systemDefault()) }

@Composable
internal fun DayLabel.displayText(formats: HistoryFormats): String = when (this) {
    DayLabel.Today -> stringResource(R.string.hist_day_today)
    DayLabel.Yesterday -> stringResource(R.string.hist_day_yesterday)
    is DayLabel.Date -> formats.date(date)
}

/** "%d results" style text; the count is both the quantity and the single format argument. */
@Composable
internal fun quantityString(@PluralsRes id: Int, count: Int): String =
    LocalContext.current.resources.getQuantityString(id, count, count)

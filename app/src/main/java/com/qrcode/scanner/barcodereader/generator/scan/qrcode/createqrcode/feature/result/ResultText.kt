package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.CodeKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.CodeKinds
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.DayBucket
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ResultTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** "Today, 4:36 PM", "Yesterday, 5:18 PM" or "Oct 12, 2026 · 6:20 PM", in the device locale and 12/24-hour setting. */
@Composable
internal fun rememberWhenText(createdAt: Long): String {
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }
    val time = remember(createdAt) { DateFormat.getTimeFormat(context).format(Date(createdAt)) }
    val date = remember(createdAt) { DateFormat.getMediumDateFormat(context).format(Date(createdAt)) }
    return when (ResultTime.bucket(createdAt, now)) {
        DayBucket.TODAY -> stringResource(R.string.result_when_today, time)
        DayBucket.YESTERDAY -> stringResource(R.string.result_when_yesterday, time)
        DayBucket.EARLIER -> stringResource(R.string.result_when_date, date, time)
    }
}

/** Header caption: the time plus "QR code", "Barcode" or the symbology name; created codes show the time only. */
@Composable
internal fun headerCaption(record: ScanRecord): String {
    val whenText = rememberWhenText(record.createdAt)
    if (record.source == ScanSource.CREATED) return whenText
    val kind = when (CodeKinds.of(record.format)) {
        CodeKind.QR -> stringResource(R.string.result_kind_qr)
        CodeKind.BARCODE -> stringResource(R.string.result_kind_barcode)
        CodeKind.OTHER -> record.format.displayName
    }
    return stringResource(R.string.result_caption, whenText, kind)
}

@Composable
internal fun sourceLabel(source: ScanSource): String = stringResource(
    when (source) {
        ScanSource.CAMERA -> R.string.result_source_camera
        ScanSource.IMAGE -> R.string.result_source_image
        ScanSource.CREATED -> R.string.result_source_created
    },
)

/** Date part of an event time. All-day events are stored at UTC midnight, so they are formatted in UTC. */
@Composable
internal fun rememberEventDate(millis: Long, allDay: Boolean): String {
    val context = LocalContext.current
    return remember(millis, allDay) {
        val format = DateFormat.getLongDateFormat(context)
        if (allDay) format.timeZone = UTC
        format.format(Date(millis))
    }
}

@Composable
internal fun rememberEventTime(millis: Long): String {
    val context = LocalContext.current
    return remember(millis) { DateFormat.getTimeFormat(context).format(Date(millis)) }
}

/** Month abbreviation and day number for the date tile of an event header. */
@Composable
internal fun rememberEventTile(millis: Long, allDay: Boolean): Pair<String, String> {
    return remember(millis, allDay) {
        val month = SimpleDateFormat("MMM", Locale.getDefault())
        val day = SimpleDateFormat("d", Locale.getDefault())
        if (allDay) {
            month.timeZone = UTC
            day.timeZone = UTC
        }
        val date = Date(millis)
        month.format(date).uppercase(Locale.getDefault()) to day.format(date)
    }
}

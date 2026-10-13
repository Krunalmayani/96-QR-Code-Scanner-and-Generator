package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.CalendarTimes

/** s19: when and where, then the description. The title and date tile are in the header. */
@Composable
internal fun EventContent(content: ScanContent.Event) {
    val lines = ArrayList<DetailLine>()
    val start = content.startMillis
    if (start != null) {
        lines += timeLine(stringResource(R.string.result_event_starts), start, content.allDay)
    }
    val end = CalendarTimes.displayEnd(content.startMillis, content.endMillis, content.allDay)
    if (end != null) {
        lines += timeLine(stringResource(R.string.result_event_ends), end, content.allDay)
    }
    if (content.location.isNotBlank()) {
        lines += DetailLine(stringResource(R.string.result_event_where), content.location)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailsCard(lines)
        if (content.description.isNotBlank()) {
            AppCard(Modifier.fillMaxWidth()) {
                OverlineLabel(stringResource(R.string.result_event_description))
                SelectionContainer {
                    Text(
                        content.description,
                        style = AppTheme.type.body,
                        color = AppTheme.colors.ink,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun timeLine(label: String, millis: Long, allDay: Boolean): DetailLine {
    val date = rememberEventDate(millis, allDay)
    val second = if (allDay) stringResource(R.string.result_event_all_day) else rememberEventTime(millis)
    return DetailLine(label, date, second)
}

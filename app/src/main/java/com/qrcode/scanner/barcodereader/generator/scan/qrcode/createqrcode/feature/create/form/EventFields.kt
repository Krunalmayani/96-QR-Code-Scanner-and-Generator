package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSwitch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.EventTimes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.TimeOfDay
import java.util.TimeZone

/** Which picker dialog is open: the start or the end, the date or the time. */
private data class OpenPicker(val start: Boolean, val time: Boolean)

private val TimeFieldWidth = 132.dp
private const val DEFAULT_PICKER_HOUR = 9

/** Event (design s37): title, all-day switch, start and end with Material3 pickers, location, description. */
@Composable
internal fun EventFields(
    spec: CreateSpec.Event,
    host: FormFieldHost,
    onSpec: (CreateSpec) -> Unit,
    onEvent: (CreateFormEvent) -> Unit,
) {
    var picker by remember { mutableStateOf<OpenPicker?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormTextField(
            host, FormField.TITLE, spec.title, { onSpec(spec.copy(title = it)) },
            stringResource(R.string.create_field_event_title),
            capitalization = KeyboardCapitalization.Sentences,
        )
        AllDayRow(checked = spec.allDay, onCheckedChange = { onEvent(CreateFormEvent.SetAllDay(it)) })
        DateTimeRow(
            heading = stringResource(R.string.create_event_starts),
            millis = spec.startMillis,
            allDay = spec.allDay,
            error = host.errors[FormField.START],
            onPickDate = { picker = OpenPicker(start = true, time = false) },
            onPickTime = { picker = OpenPicker(start = true, time = true) },
        )
        DateTimeRow(
            heading = stringResource(R.string.create_event_ends),
            millis = spec.endMillis,
            allDay = spec.allDay,
            error = host.errors[FormField.END],
            onPickDate = { picker = OpenPicker(start = false, time = false) },
            onPickTime = { picker = OpenPicker(start = false, time = true) },
        )
        FormTextField(
            host, FormField.EVENT_LOCATION, spec.location, { onSpec(spec.copy(location = it)) },
            stringResource(R.string.create_field_location),
            capitalization = KeyboardCapitalization.Words,
        )
        FormTextField(
            host, FormField.DESCRIPTION, spec.description, { onSpec(spec.copy(description = it)) },
            stringResource(R.string.create_field_description),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            minLines = 2,
            last = true,
        )
    }
    EventPickerDialogs(spec = spec, open = picker, onEvent = onEvent, onClose = { picker = null })
}

@Composable
private fun AllDayRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.create_event_all_day),
            style = AppTheme.type.title,
            color = AppTheme.colors.ink,
            modifier = Modifier.weight(1f),
        )
        AppSwitch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun DateTimeRow(
    heading: String,
    millis: Long?,
    allDay: Boolean,
    error: String?,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
) {
    val notSet = stringResource(R.string.create_event_not_set)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(heading.uppercase(), style = AppTheme.type.label, color = AppTheme.colors.inkSecondary, modifier = Modifier.padding(horizontal = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerField(
                label = stringResource(R.string.create_event_date),
                value = millis?.let { EventFormat.date(it, allDay) } ?: notSet,
                icon = AppIcons.Calendar,
                onClick = onPickDate,
                hasError = error != null,
                modifier = Modifier.weight(1f),
            )
            if (!allDay) {
                PickerField(
                    label = stringResource(R.string.create_event_time),
                    value = millis?.let { EventFormat.time(it) } ?: notSet,
                    icon = AppIcons.History,
                    onClick = onPickTime,
                    hasError = error != null,
                    modifier = Modifier.width(TimeFieldWidth),
                )
            }
        }
        if (error != null) {
            Text(error, style = AppTheme.type.caption, color = AppTheme.colors.error, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

/** Looks like a text field but opens a picker when tapped. */
@Composable
private fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    hasError: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.field
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.5.dp, if (hasError) colors.error else colors.outline, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTheme.type.fieldLabel, color = if (hasError) colors.error else colors.inkSecondary)
            Text(value, style = AppTheme.type.input, color = colors.ink, maxLines = 1)
        }
        Icon(icon, contentDescription = null, tint = colors.inkSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EventPickerDialogs(
    spec: CreateSpec.Event,
    open: OpenPicker?,
    onEvent: (CreateFormEvent) -> Unit,
    onClose: () -> Unit,
) {
    if (open == null) return
    val zone = TimeZone.getDefault()
    val current = if (open.start) spec.startMillis else spec.endMillis
    if (open.time) {
        val initial = current?.let { EventTimes.timeOf(it, zone) } ?: TimeOfDay(DEFAULT_PICKER_HOUR, 0)
        EventTimeDialog(
            initial = initial,
            onConfirm = { onEvent(CreateFormEvent.EventTimePicked(open.start, it.hour, it.minute)) },
            onDismiss = onClose,
        )
    } else {
        val initialDate = current?.let { if (spec.allDay) it else EventTimes.dateOf(it, zone) }
        EventDateDialog(
            initialUtcDate = initialDate,
            onConfirm = { onEvent(CreateFormEvent.EventDatePicked(open.start, it)) },
            onDismiss = onClose,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDateDialog(initialUtcDate: Long?, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialUtcDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppTextButton(
                text = stringResource(R.string.common_ok),
                onClick = {
                    state.selectedDateMillis?.let(onConfirm)
                    onDismiss()
                },
                enabled = state.selectedDateMillis != null,
            )
        },
        dismissButton = { AppTextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss) },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimeDialog(initial: TimeOfDay, onConfirm: (TimeOfDay) -> Unit, onDismiss: () -> Unit) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = is24Hour)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppTextButton(
                text = stringResource(R.string.common_ok),
                onClick = {
                    onConfirm(TimeOfDay(state.hour, state.minute))
                    onDismiss()
                },
            )
        },
        dismissButton = { AppTextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss) },
        text = { TimePicker(state = state) },
    )
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField

/**
 * What the field composables need from the screen: the error texts to show (already limited to touched fields or to
 * after a Generate attempt), which fields are currently invalid (shown or not), and the two callbacks.
 */
@Immutable
class FormFieldHost(
    val errors: Map<FormField, String>,
    val invalid: Set<FormField>,
    val onBlur: (FormField) -> Unit,
    val onSubmit: () -> Unit,
)

/**
 * [AppTextField] wired to a [FormField]: shows the field's error, reports blur (focus lost after the field had focus,
 * so a fresh form does not start red), and sets the keyboard type and IME action (Next, or Done on the [last] field).
 */
@Composable
internal fun FormTextField(
    host: FormFieldHost,
    field: FormField,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    last: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else MULTILINE_MAX,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var hadFocus by remember { mutableStateOf(false) }
    val ime = when {
        !singleLine -> ImeAction.Default
        last -> ImeAction.Done
        else -> ImeAction.Next
    }
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        helper = helper,
        error = host.errors[field],
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailing = trailing,
        keyboardOptions = KeyboardOptions(capitalization = capitalization, keyboardType = keyboardType, imeAction = ime),
        keyboardActions = KeyboardActions(onDone = { host.onSubmit() }),
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        onFocusChanged = { focused ->
            if (focused) {
                hadFocus = true
            } else if (hadFocus) {
                host.onBlur(field)
            }
        },
    )
}

private const val MULTILINE_MAX = 8

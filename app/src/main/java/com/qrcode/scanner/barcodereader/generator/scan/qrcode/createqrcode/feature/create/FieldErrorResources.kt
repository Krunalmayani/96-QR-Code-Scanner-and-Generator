package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FieldError
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FieldErrorKey
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FieldErrorMessage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField

/** String resource of each error message. The `when` has no else branch, so a new message fails to compile until mapped. */
@StringRes
fun FieldErrorMessage.stringRes(): Int = when (this) {
    FieldErrorMessage.REQUIRED -> R.string.create_error_required
    FieldErrorMessage.PASSWORD_REQUIRED -> R.string.create_error_password_required
    FieldErrorMessage.START_REQUIRED -> R.string.create_error_start_required
    FieldErrorMessage.INVALID_URL -> R.string.create_error_invalid_url
    FieldErrorMessage.INVALID_EMAIL -> R.string.create_error_invalid_email
    FieldErrorMessage.INVALID_PHONE -> R.string.create_error_invalid_phone
    FieldErrorMessage.TOO_LONG -> R.string.create_error_too_long
    FieldErrorMessage.SSID_REQUIRED -> R.string.create_error_ssid_required
    FieldErrorMessage.PASSWORD_TOO_SHORT -> R.string.create_error_password_too_short
    FieldErrorMessage.END_BEFORE_START -> R.string.create_error_end_before_start
    FieldErrorMessage.INVALID_LATITUDE -> R.string.create_error_invalid_latitude
    FieldErrorMessage.INVALID_LONGITUDE -> R.string.create_error_invalid_longitude
    FieldErrorMessage.INVALID_HANDLE -> R.string.create_error_invalid_handle
    FieldErrorMessage.BARCODE_DIGITS_ONLY -> R.string.create_error_barcode_digits_only
    FieldErrorMessage.BARCODE_WRONG_LENGTH -> R.string.create_error_barcode_wrong_length
    FieldErrorMessage.BARCODE_BAD_CHECK_DIGIT -> R.string.create_error_barcode_bad_check_digit
    FieldErrorMessage.BARCODE_INVALID_CHARS -> R.string.create_error_barcode_invalid_chars
    FieldErrorMessage.ITF_ODD_LENGTH -> R.string.create_error_itf_odd_length
    FieldErrorMessage.DATA_TOO_LONG -> R.string.create_error_data_too_long
}

/** The localised text of [error] for [field]; the numbers of the error are formatted into the string. */
@Composable
fun fieldErrorText(error: FieldError, field: FormField): String {
    val key = FieldErrorKey.of(error, field)
    return stringResource(key.message.stringRes(), *key.args.toTypedArray())
}

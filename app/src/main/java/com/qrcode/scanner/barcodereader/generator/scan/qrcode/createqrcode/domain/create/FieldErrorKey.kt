package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

/**
 * Stable identifiers of the user-facing error messages. The feature layer maps each one to a string resource
 * (`FieldErrorMessage.stringRes()`), so the mapping from [FieldError] to message stays pure and unit-testable.
 */
enum class FieldErrorMessage {
    REQUIRED,
    PASSWORD_REQUIRED,
    START_REQUIRED,
    INVALID_URL,
    INVALID_EMAIL,
    INVALID_PHONE,

    /** args: [maximum length]. */
    TOO_LONG,
    SSID_REQUIRED,

    /** args: [minimum length]. */
    PASSWORD_TOO_SHORT,
    END_BEFORE_START,
    INVALID_LATITUDE,
    INVALID_LONGITUDE,
    INVALID_HANDLE,
    BARCODE_DIGITS_ONLY,

    /** args: [shortest acceptable length, longest acceptable length]. */
    BARCODE_WRONG_LENGTH,

    /** args: [the check digit that would make the number valid]. */
    BARCODE_BAD_CHECK_DIGIT,
    BARCODE_INVALID_CHARS,
    ITF_ODD_LENGTH,

    /** args: [maximum length]. */
    DATA_TOO_LONG,
}

/** A message id plus the numbers to format into it. */
data class FieldErrorKey(
    val message: FieldErrorMessage,
    val args: List<Int> = emptyList(),
) {
    companion object {
        /**
         * Maps [error] to its message. [field] (when known) selects a more specific wording for a few generic
         * codes: a missing Wi-Fi password and a missing event start read differently from "required".
         */
        fun of(error: FieldError, field: FormField? = null): FieldErrorKey =
            when (error) {
                FieldError.Required ->
                    when (field) {
                        FormField.PASSWORD -> FieldErrorKey(FieldErrorMessage.PASSWORD_REQUIRED)
                        FormField.START -> FieldErrorKey(FieldErrorMessage.START_REQUIRED)
                        else -> FieldErrorKey(FieldErrorMessage.REQUIRED)
                    }
                FieldError.InvalidUrl -> FieldErrorKey(FieldErrorMessage.INVALID_URL)
                FieldError.InvalidEmail -> FieldErrorKey(FieldErrorMessage.INVALID_EMAIL)
                FieldError.InvalidPhone -> FieldErrorKey(FieldErrorMessage.INVALID_PHONE)
                is FieldError.TooLong -> FieldErrorKey(FieldErrorMessage.TOO_LONG, listOf(error.max))
                FieldError.SsidRequired -> FieldErrorKey(FieldErrorMessage.SSID_REQUIRED)
                is FieldError.PasswordTooShort -> FieldErrorKey(FieldErrorMessage.PASSWORD_TOO_SHORT, listOf(error.min))
                FieldError.EndBeforeStart -> FieldErrorKey(FieldErrorMessage.END_BEFORE_START)
                FieldError.InvalidLatitude -> FieldErrorKey(FieldErrorMessage.INVALID_LATITUDE)
                FieldError.InvalidLongitude -> FieldErrorKey(FieldErrorMessage.INVALID_LONGITUDE)
                FieldError.InvalidHandle -> FieldErrorKey(FieldErrorMessage.INVALID_HANDLE)
                FieldError.BarcodeDigitsOnly -> FieldErrorKey(FieldErrorMessage.BARCODE_DIGITS_ONLY)
                is FieldError.BarcodeWrongLength -> {
                    val lengths = error.expected.ifEmpty { listOf(0) }
                    FieldErrorKey(FieldErrorMessage.BARCODE_WRONG_LENGTH, listOf(lengths.min(), lengths.max()))
                }
                is FieldError.BarcodeBadCheckDigit -> FieldErrorKey(FieldErrorMessage.BARCODE_BAD_CHECK_DIGIT, listOf(error.expected))
                FieldError.BarcodeInvalidChars -> FieldErrorKey(FieldErrorMessage.BARCODE_INVALID_CHARS)
                FieldError.ItfOddLength -> FieldErrorKey(FieldErrorMessage.ITF_ODD_LENGTH)
                is FieldError.DataTooLong -> FieldErrorKey(FieldErrorMessage.DATA_TOO_LONG, listOf(error.max))
            }
    }
}

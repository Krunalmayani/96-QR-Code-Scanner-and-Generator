package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

/** Error CODES for form fields. Never user-facing text: the UI maps each code to a string resource. */
sealed interface FieldError {
    /** Stable upper-case code, e.g. "INVALID_URL". */
    val code: String

    data object Required : FieldError { override val code = "REQUIRED" }
    data object InvalidUrl : FieldError { override val code = "INVALID_URL" }
    data object InvalidEmail : FieldError { override val code = "INVALID_EMAIL" }
    data object InvalidPhone : FieldError { override val code = "INVALID_PHONE" }
    data class TooLong(val max: Int) : FieldError { override val code = "TOO_LONG" }
    data object SsidRequired : FieldError { override val code = "SSID_REQUIRED" }

    /** [min] is the shortest acceptable length (WPA 8; WEP next valid length 5, 10, 13 or 26). */
    data class PasswordTooShort(val min: Int) : FieldError { override val code = "PASSWORD_TOO_SHORT" }
    data object EndBeforeStart : FieldError { override val code = "END_BEFORE_START" }
    data object InvalidLatitude : FieldError { override val code = "INVALID_LATITUDE" }
    data object InvalidLongitude : FieldError { override val code = "INVALID_LONGITUDE" }
    data object InvalidHandle : FieldError { override val code = "INVALID_HANDLE" }
    data object BarcodeDigitsOnly : FieldError { override val code = "BARCODE_DIGITS_ONLY" }
    data class BarcodeWrongLength(val expected: List<Int>) : FieldError { override val code = "BARCODE_WRONG_LENGTH" }

    /** [expected] is the check digit that would make the number valid. */
    data class BarcodeBadCheckDigit(val expected: Int) : FieldError { override val code = "BARCODE_BAD_CHECK_DIGIT" }
    data object BarcodeInvalidChars : FieldError { override val code = "BARCODE_INVALID_CHARS" }
    data object ItfOddLength : FieldError { override val code = "ITF_ODD_LENGTH" }
    data class DataTooLong(val max: Int) : FieldError { override val code = "DATA_TOO_LONG" }
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val error: FieldError) : ValidationResult

    val isValid: Boolean get() = this is Valid
    fun errorOrNull(): FieldError? = (this as? Invalid)?.error
}

/** Names every input of every Create form, so errors can be shown on the right field. */
enum class FormField {
    URL,
    TEXT,
    NAME,
    ORGANIZATION,
    JOB_TITLE,
    PHONE,
    EMAIL,
    ADDRESS,
    WEBSITE,
    NOTE,
    SSID,
    PASSWORD,
    SUBJECT,
    BODY,
    MESSAGE,
    LATITUDE,
    LONGITUDE,
    LABEL,
    TITLE,
    EVENT_LOCATION,
    DESCRIPTION,
    START,
    END,
    HANDLE,
    BARCODE_DATA,
}

/** Field limits shared by validators and tests. */
object Limits {
    const val URL = 2000
    const val TEXT = 1000
    const val NAME = 100
    const val ORGANIZATION = 100
    const val JOB_TITLE = 100
    const val ADDRESS = 300
    const val NOTE = 500
    const val EMAIL = 254
    const val SSID = 32
    const val WPA_MIN = 8
    const val WPA_MAX = 63
    const val SUBJECT = 200
    const val BODY = 1000
    const val MESSAGE = 500
    const val LABEL = 100
    const val EVENT_TITLE = 150
    const val EVENT_LOCATION = 200
    const val EVENT_DESCRIPTION = 500
    const val HANDLE = 100

    /** Largest payload (UTF-8 bytes) that still fits a QR code at error correction H. */
    const val QR_MAX_BYTES = 1273
    const val BARCODE_1D_MAX = 80
    const val BARCODE_2D_MAX = 1000
}

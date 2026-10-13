package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FieldErrorKeyTest {
    /** One instance of every [FieldError] subtype. The `when` in FieldErrorKey.of is compile-time exhaustive. */
    private val samples: List<FieldError> = listOf(
        FieldError.Required,
        FieldError.InvalidUrl,
        FieldError.InvalidEmail,
        FieldError.InvalidPhone,
        FieldError.TooLong(10),
        FieldError.SsidRequired,
        FieldError.PasswordTooShort(8),
        FieldError.EndBeforeStart,
        FieldError.InvalidLatitude,
        FieldError.InvalidLongitude,
        FieldError.InvalidHandle,
        FieldError.BarcodeDigitsOnly,
        FieldError.BarcodeWrongLength(listOf(12, 13)),
        FieldError.BarcodeBadCheckDigit(7),
        FieldError.BarcodeInvalidChars,
        FieldError.ItfOddLength,
        FieldError.DataTooLong(1000),
    )

    @Test fun everyErrorMapsToItsOwnMessage() {
        val messages = samples.map { FieldErrorKey.of(it).message }
        assertEquals(samples.size, messages.toSet().size, "each error code gets a distinct message: $messages")
    }

    @Test fun everyMessageIsReachable() {
        val reached = samples.map { FieldErrorKey.of(it).message }.toMutableSet()
        reached += FieldErrorKey.of(FieldError.Required, FormField.PASSWORD).message
        reached += FieldErrorKey.of(FieldError.Required, FormField.START).message
        assertEquals(FieldErrorMessage.entries.toSet(), reached)
    }

    @Test fun argsCarryTheNumbers() {
        assertEquals(listOf(10), FieldErrorKey.of(FieldError.TooLong(10)).args)
        assertEquals(listOf(8), FieldErrorKey.of(FieldError.PasswordTooShort(8)).args)
        assertEquals(listOf(7), FieldErrorKey.of(FieldError.BarcodeBadCheckDigit(7)).args)
        assertEquals(listOf(1000), FieldErrorKey.of(FieldError.DataTooLong(1000)).args)
        assertEquals(listOf(12, 13), FieldErrorKey.of(FieldError.BarcodeWrongLength(listOf(13, 12))).args)
        assertEquals(listOf(8, 8), FieldErrorKey.of(FieldError.BarcodeWrongLength(listOf(8))).args)
        assertTrue(FieldErrorKey.of(FieldError.InvalidUrl).args.isEmpty())
    }

    @Test fun requiredReadsDifferentlyForPasswordAndStart() {
        assertEquals(FieldErrorMessage.REQUIRED, FieldErrorKey.of(FieldError.Required).message)
        assertEquals(FieldErrorMessage.REQUIRED, FieldErrorKey.of(FieldError.Required, FormField.URL).message)
        assertEquals(FieldErrorMessage.PASSWORD_REQUIRED, FieldErrorKey.of(FieldError.Required, FormField.PASSWORD).message)
        assertEquals(FieldErrorMessage.START_REQUIRED, FieldErrorKey.of(FieldError.Required, FormField.START).message)
    }

    @Test fun otherErrorsIgnoreTheField() {
        assertEquals(FieldErrorMessage.INVALID_URL, FieldErrorKey.of(FieldError.InvalidUrl, FormField.PASSWORD).message)
    }

    @Test fun realValidatorOutputAlwaysMapsToArgsThatFitTheMessage() {
        val specs = listOf(
            CreateSpec.Website("brewhouse coffee"),
            CreateSpec.Wifi(ssid = "", password = "short"),
            CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "123"),
            CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "5901234123450"),
            CreateSpec.Location("99", "200"),
        )
        for (spec in specs) {
            for ((field, error) in Validators.validate(spec)) {
                val key = FieldErrorKey.of(error, field)
                val expectedArgs = when (key.message) {
                    FieldErrorMessage.BARCODE_WRONG_LENGTH -> 2
                    FieldErrorMessage.TOO_LONG, FieldErrorMessage.PASSWORD_TOO_SHORT,
                    FieldErrorMessage.BARCODE_BAD_CHECK_DIGIT, FieldErrorMessage.DATA_TOO_LONG,
                    -> 1
                    else -> 0
                }
                assertEquals(expectedArgs, key.args.size, "$field $error")
            }
        }
    }
}

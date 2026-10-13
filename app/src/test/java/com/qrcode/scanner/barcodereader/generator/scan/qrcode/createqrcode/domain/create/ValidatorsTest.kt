package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidatorsTest {
    private fun err(r: ValidationResult): FieldError? = r.errorOrNull()

    private fun ok(r: ValidationResult) = assertEquals(ValidationResult.Valid, r)

    private fun bad(expected: FieldError, r: ValidationResult) = assertEquals(ValidationResult.Invalid(expected), r)

    // ---- url ----
    @Test fun urlValid() {
        for (u in listOf(
            "https://example.com",
            "example.com",
            "http://sub.example.co.uk/path?q=1#x",
            "https://example.com:8080/a",
            "HTTPS://Example.COM",
            "192.168.1.10",
            "https://localhost:3000",
            "https://bücher.de",
            "https://user:pw@example.com/",
        )) {
            ok(Validators.url(u))
        }
    }

    @Test fun urlInvalid() {
        for (u in listOf(
            "brewhouse coffee",
            "https://",
            "https://nodot",
            "ftp://example.com",
            "https://exa mple.com",
            "https://example.c",
            "https://.com",
            "https://example.com:abc",
            "javascript://x.y",
        )) {
            bad(FieldError.InvalidUrl, Validators.url(u))
        }
    }

    @Test fun urlRequiredAndOptional() {
        bad(FieldError.Required, Validators.url("  "))
        ok(Validators.url("", required = false))
    }

    @Test fun urlTooLong() = bad(FieldError.TooLong(Limits.URL), Validators.url("https://example.com/" + "a".repeat(Limits.URL)))

    @Test fun appStoreAllowsMarketScheme() {
        ok(Validators.appStoreUrl("market://details?id=com.x.y"))
        ok(Validators.appStoreUrl("itms-apps://apps.apple.com/app/id123"))
        bad(FieldError.InvalidUrl, Validators.appStoreUrl("market://"))
        bad(FieldError.InvalidUrl, Validators.url("market://details.id"))
    }

    // ---- email ----
    @Test fun emailValid() {
        for (e in listOf(
            "a@b.co",
            "sophie.lambert@lumen.studio",
            "first+tag@sub.example.com",
            "o'brien@example.org",
            "x_y-z@ex-ample.io",
        )) {
            ok(Validators.email(e))
        }
    }

    @Test fun emailInvalid() {
        for (e in listOf("plain", "a@b", "a@@b.co", "@b.co", "a@b.c", "a b@c.de", ".a@b.co", "a..b@c.de", "a@-b.co", "a@b.co.")) {
            bad(FieldError.InvalidEmail, Validators.email(e))
        }
    }

    @Test fun emailRequiredOptionalAndLong() {
        bad(FieldError.Required, Validators.email(""))
        ok(Validators.email(" ", required = false))
        bad(FieldError.TooLong(Limits.EMAIL), Validators.email("a".repeat(250) + "@b.co"))
    }

    // ---- phone ----
    @Test fun phoneValid() {
        for (p in listOf(
            "+14155550142",
            "(206) 555-0198",
            "+1 (415) 555-0142",
            "555.0142",
            "1234567",
            "+123456789012345",
            "00 33 1 23 45 67 89",
        )) {
            ok(Validators.phone(p))
        }
    }

    @Test fun phoneInvalid() {
        for (p in listOf("123456", "+1234567890123456", "abc1234567", "555-CALL-NOW", "++14155550142", "+", "12+3456789")) {
            bad(FieldError.InvalidPhone, Validators.phone(p))
        }
    }

    @Test fun phoneRequiredOptional() {
        bad(FieldError.Required, Validators.phone(""))
        ok(Validators.phone("", required = false))
    }

    // ---- wifi ----
    @Test fun ssid() {
        ok(Validators.ssid("CafeNova_Guest"))
        bad(FieldError.SsidRequired, Validators.ssid("   "))
        bad(FieldError.TooLong(32), Validators.ssid("x".repeat(33)))
        ok(Validators.ssid("x".repeat(32)))
        bad(FieldError.TooLong(32), Validators.ssid("é".repeat(17)))
    }

    @Test fun wpaPassword() {
        ok(Validators.wifiPassword("12345678", WifiSecurity.WPA))
        bad(FieldError.PasswordTooShort(8), Validators.wifiPassword("1234567", WifiSecurity.WPA))
        bad(FieldError.Required, Validators.wifiPassword("", WifiSecurity.WPA))
        bad(FieldError.TooLong(63), Validators.wifiPassword("x".repeat(64), WifiSecurity.WPA))
        ok(Validators.wifiPassword("x".repeat(63), WifiSecurity.WPA))
    }

    @Test fun wepPassword() {
        ok(Validators.wifiPassword("abcde", WifiSecurity.WEP))
        ok(Validators.wifiPassword("abcdefghijklm", WifiSecurity.WEP))
        ok(Validators.wifiPassword("0123456789", WifiSecurity.WEP))
        ok(Validators.wifiPassword("0123456789abcdef0123456789", WifiSecurity.WEP))
        bad(FieldError.PasswordTooShort(5), Validators.wifiPassword("abcd", WifiSecurity.WEP))
        bad(FieldError.PasswordTooShort(10), Validators.wifiPassword("abcdef", WifiSecurity.WEP))
        bad(FieldError.PasswordTooShort(13), Validators.wifiPassword("abcdefghij", WifiSecurity.WEP))
        bad(FieldError.PasswordTooShort(26), Validators.wifiPassword("abcdefghijklmn", WifiSecurity.WEP))
        bad(FieldError.TooLong(26), Validators.wifiPassword("a".repeat(27), WifiSecurity.WEP))
    }

    @Test fun openNetworkIgnoresPassword() {
        ok(Validators.wifiPassword("", WifiSecurity.NONE))
        ok(Validators.wifiPassword("x", WifiSecurity.NONE))
    }

    // ---- location ----
    @Test fun latitude() {
        for (v in listOf("0", "90", "-90", "37.7749", "37,7749", "+12.5", ".5", "5.")) ok(Validators.latitude(v))
        for (v in listOf("90.0001", "-91", "abc", "1e2", "NaN", "1.2.3", "--1")) bad(FieldError.InvalidLatitude, Validators.latitude(v))
        bad(FieldError.Required, Validators.latitude(" "))
    }

    @Test fun longitude() {
        for (v in listOf("0", "180", "-180", "-122.4194")) ok(Validators.longitude(v))
        for (v in listOf("180.1", "-181", "x", "Infinity")) bad(FieldError.InvalidLongitude, Validators.longitude(v))
        bad(FieldError.Required, Validators.longitude(""))
    }

    // ---- event ----
    @Test fun eventTimes() {
        val s = utcMillis(2026, 11, 14, 18)
        bad(FieldError.Required, Validators.eventStart(null))
        ok(Validators.eventStart(s))
        ok(Validators.eventEnd(false, s, null))
        ok(Validators.eventEnd(false, s, s))
        ok(Validators.eventEnd(false, s, s + 1))
        bad(FieldError.EndBeforeStart, Validators.eventEnd(false, s, s - 1))
        ok(Validators.eventEnd(false, null, 5L))
    }

    @Test fun allDayComparesByDay() {
        val s = utcMillis(2026, 11, 14, 18)
        ok(Validators.eventEnd(true, s, utcMillis(2026, 11, 14, 1)))
        bad(FieldError.EndBeforeStart, Validators.eventEnd(true, s, utcMillis(2026, 11, 13, 23)))
    }

    // ---- social ----
    @Test fun handles() {
        ok(Validators.handle(SocialPlatform.INSTAGRAM, "@lumen.studio"))
        ok(Validators.handle(SocialPlatform.X, "lumen_hq"))
        ok(Validators.handle(SocialPlatform.TELEGRAM, "lumen_chat"))
        ok(Validators.handle(SocialPlatform.LINKEDIN, "sophie-lambert"))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.X, "has space"))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.X, "x".repeat(16)))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.INSTAGRAM, "a/b"))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.TELEGRAM, "abc"))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.FACEBOOK, "https://facebook.com/x"))
        bad(FieldError.InvalidHandle, Validators.handle(SocialPlatform.YOUTUBE, " @ "))
        bad(FieldError.Required, Validators.handle(SocialPlatform.YOUTUBE, "  "))
    }

    @Test fun whatsappTakesPhone() {
        ok(Validators.handle(SocialPlatform.WHATSAPP, "+1 415 555 0142"))
        bad(FieldError.InvalidPhone, Validators.handle(SocialPlatform.WHATSAPP, "lumen"))
        bad(FieldError.Required, Validators.handle(SocialPlatform.WHATSAPP, ""))
    }

    // ---- barcode: ean13 ----
    @Test fun ean13() {
        ok(Validators.barcode(CodeFormat.EAN_13, "400638133393"))
        ok(Validators.barcode(CodeFormat.EAN_13, "4006381333931"))
        ok(Validators.barcode(CodeFormat.EAN_13, "5901234123457"))
        bad(FieldError.BarcodeBadCheckDigit(1), Validators.barcode(CodeFormat.EAN_13, "4006381333932"))
        bad(FieldError.BarcodeWrongLength(listOf(12, 13)), Validators.barcode(CodeFormat.EAN_13, "40063813339"))
        bad(FieldError.BarcodeWrongLength(listOf(12, 13)), Validators.barcode(CodeFormat.EAN_13, "40063813339311"))
        bad(FieldError.BarcodeDigitsOnly, Validators.barcode(CodeFormat.EAN_13, "40063813339A"))
        bad(FieldError.Required, Validators.barcode(CodeFormat.EAN_13, "  "))
    }

    @Test fun ean8() {
        ok(Validators.barcode(CodeFormat.EAN_8, "9638507"))
        ok(Validators.barcode(CodeFormat.EAN_8, "96385074"))
        bad(FieldError.BarcodeBadCheckDigit(4), Validators.barcode(CodeFormat.EAN_8, "96385073"))
        bad(FieldError.BarcodeWrongLength(listOf(7, 8)), Validators.barcode(CodeFormat.EAN_8, "963850"))
        bad(FieldError.BarcodeDigitsOnly, Validators.barcode(CodeFormat.EAN_8, "9638 507"))
    }

    @Test fun upcA() {
        ok(Validators.barcode(CodeFormat.UPC_A, "03600029145"))
        ok(Validators.barcode(CodeFormat.UPC_A, "036000291452"))
        bad(FieldError.BarcodeBadCheckDigit(2), Validators.barcode(CodeFormat.UPC_A, "036000291453"))
        bad(FieldError.BarcodeWrongLength(listOf(11, 12)), Validators.barcode(CodeFormat.UPC_A, "0360002914521"))
        bad(FieldError.BarcodeDigitsOnly, Validators.barcode(CodeFormat.UPC_A, "-3600029145"))
    }

    @Test fun itf() {
        ok(Validators.barcode(CodeFormat.ITF, "1234567890"))
        ok(Validators.barcode(CodeFormat.ITF, "12"))
        bad(FieldError.ItfOddLength, Validators.barcode(CodeFormat.ITF, "123"))
        bad(FieldError.BarcodeDigitsOnly, Validators.barcode(CodeFormat.ITF, "12AB"))
        bad(FieldError.DataTooLong(80), Validators.barcode(CodeFormat.ITF, "1".repeat(82)))
        ok(Validators.barcode(CodeFormat.ITF, "1".repeat(80)))
    }

    @Test fun code39() {
        ok(Validators.barcode(CodeFormat.CODE_39, "ABC-123 \$/+%."))
        ok(Validators.barcode(CodeFormat.CODE_39, "lower case ok"))
        bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_39, "ABC_123"))
        bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_39, "A*B"))
        bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_39, "café"))
        bad(FieldError.DataTooLong(80), Validators.barcode(CodeFormat.CODE_39, "A".repeat(81)))
    }

    @Test fun code128() {
        ok(Validators.barcode(CodeFormat.CODE_128, "Hello, World! 123 ~"))
        bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_128, "café"))
        bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_128, "tab\there"))
        bad(FieldError.DataTooLong(80), Validators.barcode(CodeFormat.CODE_128, "a".repeat(81)))
    }

    @Test fun twoDimensionalBarcodes() {
        for (f in listOf(CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC)) {
            ok(Validators.barcode(f, "any text, with ünïcode"))
            bad(FieldError.DataTooLong(1000), Validators.barcode(f, "x".repeat(1001)))
            bad(FieldError.Required, Validators.barcode(f, ""))
        }
    }

    @Test fun unsupportedBarcodeFormatIsRejected() = bad(FieldError.BarcodeInvalidChars, Validators.barcode(CodeFormat.CODE_93, "ABC"))

    // ---- check digits ----
    @Test fun knownCheckDigits() {
        assertEquals(1, BarcodeCheckDigit.checkDigit("400638133393"))
        assertEquals(2, BarcodeCheckDigit.checkDigit("03600029145"))
        assertEquals(4, BarcodeCheckDigit.checkDigit("9638507"))
        assertEquals(7, BarcodeCheckDigit.checkDigit("590123412345"))
        assertEquals("4006381333931", BarcodeCheckDigit.ean13("400638133393"))
        assertEquals("96385074", BarcodeCheckDigit.ean8("9638507"))
        assertEquals("036000291452", BarcodeCheckDigit.upcA("03600029145"))
    }

    @Test fun isValidFullNumbers() {
        assertTrue(BarcodeCheckDigit.isValid("4006381333931"))
        assertTrue(BarcodeCheckDigit.isValid("036000291452"))
        assertTrue(BarcodeCheckDigit.isValid("96385074"))
        assertFalse(BarcodeCheckDigit.isValid("4006381333930"))
        assertFalse(BarcodeCheckDigit.isValid("1"))
        assertFalse(BarcodeCheckDigit.isValid("12a4"))
    }

    @Test fun withCheckDigit() {
        assertEquals("4006381333931", BarcodeCheckDigit.withCheckDigit(CodeFormat.EAN_13, "400638133393"))
        assertEquals("4006381333931", BarcodeCheckDigit.withCheckDigit(CodeFormat.EAN_13, "4006381333931"))
        assertEquals("1234", BarcodeCheckDigit.withCheckDigit(CodeFormat.CODE_128, "1234"))
        assertEquals("12345678901x", BarcodeCheckDigit.withCheckDigit(CodeFormat.EAN_13, "12345678901x"))
        assertEquals(null, BarcodeCheckDigit.bodyLength(CodeFormat.QR_CODE))
    }

    @Test fun helpersRejectWrongInput() {
        org.junit.jupiter.api
            .assertThrows<IllegalArgumentException> { BarcodeCheckDigit.ean13("123") }
        org.junit.jupiter.api
            .assertThrows<IllegalArgumentException> { BarcodeCheckDigit.ean8("123") }
        org.junit.jupiter.api
            .assertThrows<IllegalArgumentException> { BarcodeCheckDigit.upcA("123") }
        org.junit.jupiter.api
            .assertThrows<IllegalArgumentException> { BarcodeCheckDigit.checkDigit("12x") }
    }

    // ---- whole-form validate ----
    @Test fun validateWebsite() {
        assertEquals(mapOf(FormField.URL to FieldError.InvalidUrl), Validators.validate(CreateSpec.Website("brewhouse coffee")))
        assertTrue(Validators.isValid(CreateSpec.Website("https://example.com")))
    }

    @Test fun validateText() {
        assertEquals(FieldError.Required, Validators.errorFor(CreateSpec.Text(" "), FormField.TEXT))
        assertEquals(FieldError.TooLong(1000), Validators.errorFor(CreateSpec.Text("x".repeat(1001)), FormField.TEXT))
        assertTrue(Validators.isValid(CreateSpec.Text("x".repeat(1000))))
    }

    @Test fun validateTextTooBigForQr() {
        val errors = Validators.validate(CreateSpec.Text("😀".repeat(400)))
        assertEquals(FieldError.DataTooLong(Limits.QR_MAX_BYTES), errors[FormField.TEXT])
    }

    @Test fun validateContact() {
        val errors = Validators.validate(CreateSpec.Contact(name = "", phone = "12", email = "nope", website = "a b"))
        assertEquals(
            mapOf(
                FormField.NAME to FieldError.Required,
                FormField.PHONE to FieldError.InvalidPhone,
                FormField.EMAIL to FieldError.InvalidEmail,
                FormField.WEBSITE to FieldError.InvalidUrl,
            ),
            errors,
        )
        assertTrue(Validators.isValid(CreateSpec.Contact(name = "Sophie Lambert", phone = "+1 (415) 555-0142", email = "s@l.studio")))
    }

    @Test fun validateWifi() {
        val errors = Validators.validate(CreateSpec.Wifi(ssid = "", password = "short"))
        assertEquals(FieldError.SsidRequired, errors[FormField.SSID])
        assertEquals(FieldError.PasswordTooShort(8), errors[FormField.PASSWORD])
        assertTrue(Validators.isValid(CreateSpec.Wifi("Open", "", WifiSecurity.NONE)))
    }

    @Test fun validateEmailSmsPhone() {
        assertEquals(FieldError.InvalidEmail, Validators.errorFor(CreateSpec.Email("x"), FormField.EMAIL))
        assertEquals(
            FieldError.TooLong(Limits.SUBJECT),
            Validators.errorFor(CreateSpec.Email("a@b.co", "s".repeat(201)), FormField.SUBJECT),
        )
        assertEquals(FieldError.TooLong(Limits.BODY), Validators.errorFor(CreateSpec.Email("a@b.co", "", "s".repeat(1001)), FormField.BODY))
        assertEquals(FieldError.InvalidPhone, Validators.errorFor(CreateSpec.Sms("1"), FormField.PHONE))
        assertEquals(FieldError.TooLong(Limits.MESSAGE), Validators.errorFor(CreateSpec.Sms("5550142", "m".repeat(501)), FormField.MESSAGE))
        assertEquals(FieldError.Required, Validators.errorFor(CreateSpec.Phone(""), FormField.PHONE))
        assertTrue(Validators.isValid(CreateSpec.Sms("555 0142", "hello")))
    }

    @Test fun validateLocation() {
        val errors = Validators.validate(CreateSpec.Location("91", "x"))
        assertEquals(FieldError.InvalidLatitude, errors[FormField.LATITUDE])
        assertEquals(FieldError.InvalidLongitude, errors[FormField.LONGITUDE])
        assertEquals(FieldError.TooLong(Limits.LABEL), Validators.errorFor(CreateSpec.Location("1", "2", "l".repeat(101)), FormField.LABEL))
        assertTrue(Validators.isValid(CreateSpec.Location("37.77", "-122.41")))
    }

    @Test fun validateEvent() {
        val s = utcMillis(2026, 11, 14, 18)
        val errors = Validators.validate(CreateSpec.Event(title = "", startMillis = null, endMillis = 5L))
        assertEquals(FieldError.Required, errors[FormField.TITLE])
        assertEquals(FieldError.Required, errors[FormField.START])
        assertEquals(
            FieldError.EndBeforeStart,
            Validators.errorFor(CreateSpec.Event("T", startMillis = s, endMillis = s - 1), FormField.END),
        )
        assertEquals(
            FieldError.TooLong(Limits.EVENT_LOCATION),
            Validators.errorFor(CreateSpec.Event("T", location = "l".repeat(201), startMillis = s), FormField.EVENT_LOCATION),
        )
        assertEquals(
            FieldError.TooLong(Limits.EVENT_DESCRIPTION),
            Validators.errorFor(CreateSpec.Event("T", description = "d".repeat(501), startMillis = s), FormField.DESCRIPTION),
        )
        assertTrue(Validators.isValid(CreateSpec.Event("T", startMillis = s, endMillis = s + 3_600_000)))
    }

    @Test fun validateSocialAndAppStore() {
        assertEquals(FieldError.InvalidHandle, Validators.errorFor(CreateSpec.Social(SocialPlatform.X, "no spaces"), FormField.HANDLE))
        assertTrue(Validators.isValid(CreateSpec.Social(SocialPlatform.WHATSAPP, "+14155550142")))
        assertEquals(FieldError.InvalidUrl, Validators.errorFor(CreateSpec.AppStore("nope"), FormField.URL))
        assertTrue(Validators.isValid(CreateSpec.AppStore("https://apps.apple.com/app/id123456")))
    }

    @Test fun validateBarcodeSpec() {
        assertEquals(
            mapOf(FormField.BARCODE_DATA to FieldError.BarcodeBadCheckDigit(1)),
            Validators.validate(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "4006381333939")),
        )
        assertTrue(Validators.isValid(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "400638133393")))
    }

    @Test fun errorCodes() {
        assertEquals("REQUIRED", FieldError.Required.code)
        assertEquals("TOO_LONG", FieldError.TooLong(3).code)
        assertEquals("BARCODE_WRONG_LENGTH", FieldError.BarcodeWrongLength(listOf(1)).code)
        assertEquals("ITF_ODD_LENGTH", FieldError.ItfOddLength.code)
        assertTrue(ValidationResult.Valid.isValid)
        assertFalse(ValidationResult.Invalid(FieldError.Required).isValid)
        assertEquals(null, err(ValidationResult.Valid))
    }

    @Test fun primaryFieldCoversEveryType() {
        for (t in CreateType.entries) Validators.primaryField(t)
        assertEquals(FormField.BARCODE_DATA, Validators.primaryField(CreateType.BARCODE_AZTEC))
    }
}

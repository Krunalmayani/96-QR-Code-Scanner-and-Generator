package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

/** Pure validators for the Create forms. They return error CODES, never text. */
object Validators {
    private val OK = ValidationResult.Valid

    private fun fail(error: FieldError): ValidationResult = ValidationResult.Invalid(error)

    // ---- generic ----

    fun required(value: String): ValidationResult = if (value.isBlank()) fail(FieldError.Required) else OK

    fun maxLength(value: String, max: Int): ValidationResult =
        if (value.length > max) fail(FieldError.TooLong(max)) else OK

    /** Required text with a length cap (Text form, event title, ...). */
    fun requiredText(value: String, max: Int): ValidationResult =
        if (value.isBlank()) fail(FieldError.Required) else maxLength(value, max)

    // ---- url ----

    private val schemeRegex = Regex("^([A-Za-z][A-Za-z0-9+.-]*)://(.*)$", RegexOption.DOT_MATCHES_ALL)
    private val hostRegex =
        Regex("^[\\p{L}\\p{N}](?:[\\p{L}\\p{N}_-]*[\\p{L}\\p{N}])?(?:\\.[\\p{L}\\p{N}](?:[\\p{L}\\p{N}_-]*[\\p{L}\\p{N}])?)+$")
    private val marketHostRegex = Regex("^[A-Za-z]+$")
    private val ipv4Regex = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")
    private val webSchemes = setOf("http", "https")
    private val storeSchemes = setOf("http", "https", "market", "itms-apps", "itms-appss")

    /** Website: http(s) only; `https://` is assumed when no scheme is typed. */
    fun url(value: String, required: Boolean = true): ValidationResult = checkUrl(value, required, webSchemes)

    /** App store links additionally allow `market://` and `itms-apps://`. */
    fun appStoreUrl(value: String, required: Boolean = true): ValidationResult = checkUrl(value, required, storeSchemes)

    private fun checkUrl(value: String, required: Boolean, schemes: Set<String>): ValidationResult {
        val t = value.trim()
        if (t.isEmpty()) return if (required) fail(FieldError.Required) else OK
        if (t.length > Limits.URL) return fail(FieldError.TooLong(Limits.URL))
        if (t.any { it.isWhitespace() }) return fail(FieldError.InvalidUrl)
        val match = schemeRegex.matchEntire(PayloadBuilder.normalizeUrl(t)) ?: return fail(FieldError.InvalidUrl)
        val scheme = match.groupValues[1].lowercase()
        if (scheme !in schemes) return fail(FieldError.InvalidUrl)
        val authority = match.groupValues[2].takeWhile { it != '/' && it != '?' && it != '#' }
        val hostPort = authority.substringAfterLast('@')
        val host = hostPort.substringBeforeLast(':', hostPort).trim('[', ']')
        val port = if (hostPort.contains(':')) hostPort.substringAfterLast(':') else ""
        if (port.isNotEmpty() && (port.any { it !in '0'..'9' } || port.length > 5)) return fail(FieldError.InvalidUrl)
        val hostOk =
            host == "localhost" ||
                ipv4Regex.matches(host) ||
                (scheme == "market" && marketHostRegex.matches(host)) ||
                (hostRegex.matches(host) && host.substringAfterLast('.').any { it.isLetter() } && host.substringAfterLast('.').length >= 2)
        return if (hostOk) OK else fail(FieldError.InvalidUrl)
    }

    // ---- email / phone ----

    private val emailRegex =
        Regex(
            "^[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*" +
                "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$",
        )

    fun email(value: String, required: Boolean = true): ValidationResult {
        val t = value.trim()
        if (t.isEmpty()) return if (required) fail(FieldError.Required) else OK
        if (t.length > Limits.EMAIL) return fail(FieldError.TooLong(Limits.EMAIL))
        return if (emailRegex.matches(t)) OK else fail(FieldError.InvalidEmail)
    }

    /** E.164-ish: optional leading +, 7..15 digits; spaces, dashes, dots and parentheses are allowed in the input. */
    fun phone(value: String, required: Boolean = true): ValidationResult {
        val t = value.trim()
        if (t.isEmpty()) return if (required) fail(FieldError.Required) else OK
        val body = t.removePrefix("+")
        if (body.any { it !in '0'..'9' && it != ' ' && it != '-' && it != '(' && it != ')' && it != '.' }) {
            return fail(FieldError.InvalidPhone)
        }
        val digits = body.count { it in '0'..'9' }
        return if (digits in 7..15) OK else fail(FieldError.InvalidPhone)
    }

    // ---- wifi ----

    fun ssid(value: String): ValidationResult =
        when {
            value.isBlank() -> fail(FieldError.SsidRequired)
            value.toByteArray(Charsets.UTF_8).size > Limits.SSID -> fail(FieldError.TooLong(Limits.SSID))
            else -> OK
        }

    private val wepLengths = listOf(5, 10, 13, 26)
    private val hexRegex = Regex("^[0-9A-Fa-f]+$")

    /** WPA 8..63 characters; WEP 5 or 13 characters (or 10 or 26 hex digits); none: ignored. */
    fun wifiPassword(password: String, security: WifiSecurity): ValidationResult {
        if (security == WifiSecurity.NONE) return OK
        if (password.isEmpty()) return fail(FieldError.Required)
        val n = password.length
        return when (security) {
            WifiSecurity.WPA ->
                when {
                    n < Limits.WPA_MIN -> fail(FieldError.PasswordTooShort(Limits.WPA_MIN))
                    n > Limits.WPA_MAX -> fail(FieldError.TooLong(Limits.WPA_MAX))
                    else -> OK
                }
            WifiSecurity.WEP -> {
                val hexOk = (n == 10 || n == 26) && hexRegex.matches(password)
                when {
                    n == 5 || n == 13 || hexOk -> OK
                    n > wepLengths.last() -> fail(FieldError.TooLong(wepLengths.last()))
                    else -> fail(FieldError.PasswordTooShort(wepLengths.first { it > n }))
                }
            }
            WifiSecurity.NONE -> OK
        }
    }

    // ---- location ----

    private val decimalRegex = Regex("^[+-]?(\\d+([.,]\\d*)?|[.,]\\d+)$")

    private fun parseDegrees(value: String): Double? {
        val t = value.trim()
        if (!decimalRegex.matches(t)) return null
        return t.replace(',', '.').toDoubleOrNull()
    }

    fun latitude(value: String): ValidationResult {
        if (value.isBlank()) return fail(FieldError.Required)
        val v = parseDegrees(value) ?: return fail(FieldError.InvalidLatitude)
        return if (v in -90.0..90.0) OK else fail(FieldError.InvalidLatitude)
    }

    fun longitude(value: String): ValidationResult {
        if (value.isBlank()) return fail(FieldError.Required)
        val v = parseDegrees(value) ?: return fail(FieldError.InvalidLongitude)
        return if (v in -180.0..180.0) OK else fail(FieldError.InvalidLongitude)
    }

    // ---- event ----

    /** Start is required; end (optional) must not be before start (for all-day: compared by calendar day). */
    fun eventStart(startMillis: Long?): ValidationResult = if (startMillis == null) fail(FieldError.Required) else OK

    fun eventEnd(allDay: Boolean, startMillis: Long?, endMillis: Long?): ValidationResult {
        if (startMillis == null || endMillis == null) return OK
        val before =
            if (allDay) {
                Math.floorDiv(endMillis, IcsDates.DAY_MILLIS) < Math.floorDiv(startMillis, IcsDates.DAY_MILLIS)
            } else {
                endMillis < startMillis
            }
        return if (before) fail(FieldError.EndBeforeStart) else OK
    }

    // ---- social ----

    private val generalHandle = Regex("^[A-Za-z0-9._-]{1,${Limits.HANDLE}}$")
    private val xHandle = Regex("^[A-Za-z0-9_]{1,15}$")
    private val instagramHandle = Regex("^[A-Za-z0-9._]{1,30}$")
    private val telegramHandle = Regex("^[A-Za-z0-9_]{5,32}$")

    /** Username rules per platform. WhatsApp takes a phone number (same rules as [phone]). */
    fun handle(platform: SocialPlatform, value: String): ValidationResult {
        if (value.isBlank()) return fail(FieldError.Required)
        if (platform.isPhoneBased) return phone(value)
        val h = PayloadBuilder.cleanHandle(value)
        val regex =
            when (platform) {
                SocialPlatform.X -> xHandle
                SocialPlatform.INSTAGRAM -> instagramHandle
                SocialPlatform.TELEGRAM -> telegramHandle
                else -> generalHandle
            }
        return if (regex.matches(h)) OK else fail(FieldError.InvalidHandle)
    }

    // ---- barcodes ----

    private const val CODE39_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ -.\$/+%"

    /** Validates barcode [data] for a barcode [format] (input may be lower-case for Code 39, or lack its check digit). */
    fun barcode(format: CodeFormat, data: String): ValidationResult {
        val t = data.trim()
        if (t.isEmpty()) return fail(FieldError.Required)
        return when (format) {
            CodeFormat.EAN_13 -> gs1(t, 12)
            CodeFormat.EAN_8 -> gs1(t, 7)
            CodeFormat.UPC_A -> gs1(t, 11)
            CodeFormat.ITF -> itf(t)
            CodeFormat.CODE_39 -> code39(t)
            CodeFormat.CODE_128 -> code128(t)
            CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC ->
                if (t.length > Limits.BARCODE_2D_MAX) fail(FieldError.DataTooLong(Limits.BARCODE_2D_MAX)) else OK
            else -> fail(FieldError.BarcodeInvalidChars)
        }
    }

    private fun gs1(t: String, bodyLength: Int): ValidationResult {
        if (t.any { it !in '0'..'9' }) return fail(FieldError.BarcodeDigitsOnly)
        val full = bodyLength + 1
        if (t.length != bodyLength && t.length != full) return fail(FieldError.BarcodeWrongLength(listOf(bodyLength, full)))
        if (t.length == full) {
            val expected = BarcodeCheckDigit.checkDigit(t.dropLast(1))
            if (expected != t.last() - '0') return fail(FieldError.BarcodeBadCheckDigit(expected))
        }
        return OK
    }

    private fun itf(t: String): ValidationResult =
        when {
            t.any { it !in '0'..'9' } -> fail(FieldError.BarcodeDigitsOnly)
            t.length % 2 != 0 -> fail(FieldError.ItfOddLength)
            t.length > Limits.BARCODE_1D_MAX -> fail(FieldError.DataTooLong(Limits.BARCODE_1D_MAX))
            else -> OK
        }

    private fun code39(t: String): ValidationResult =
        when {
            t.uppercase().any { it !in CODE39_CHARS } -> fail(FieldError.BarcodeInvalidChars)
            t.length > Limits.BARCODE_1D_MAX -> fail(FieldError.DataTooLong(Limits.BARCODE_1D_MAX))
            else -> OK
        }

    private fun code128(t: String): ValidationResult =
        when {
            t.any { it.code !in 0x20..0x7E } -> fail(FieldError.BarcodeInvalidChars)
            t.length > Limits.BARCODE_1D_MAX -> fail(FieldError.DataTooLong(Limits.BARCODE_1D_MAX))
            else -> OK
        }

    // ---- whole forms ----

    /** All errors of [spec], keyed by the field to show them on. Empty map means the form is valid. */
    fun validate(spec: CreateSpec): Map<FormField, FieldError> {
        val errors = LinkedHashMap<FormField, FieldError>()

        fun report(field: FormField, result: ValidationResult) {
            result.errorOrNull()?.let { errors[field] = it }
        }
        when (spec) {
            is CreateSpec.Website -> report(FormField.URL, url(spec.url))
            is CreateSpec.Text -> report(FormField.TEXT, requiredText(spec.text, Limits.TEXT))
            is CreateSpec.Contact -> validateContact(spec, ::report)
            is CreateSpec.Wifi -> {
                report(FormField.SSID, ssid(spec.ssid))
                report(FormField.PASSWORD, wifiPassword(spec.password, spec.security))
            }
            is CreateSpec.Email -> {
                report(FormField.EMAIL, email(spec.to))
                report(FormField.SUBJECT, maxLength(spec.subject, Limits.SUBJECT))
                report(FormField.BODY, maxLength(spec.body, Limits.BODY))
            }
            is CreateSpec.Phone -> report(FormField.PHONE, phone(spec.number))
            is CreateSpec.Sms -> {
                report(FormField.PHONE, phone(spec.number))
                report(FormField.MESSAGE, maxLength(spec.message, Limits.MESSAGE))
            }
            is CreateSpec.Location -> {
                report(FormField.LATITUDE, latitude(spec.latitude))
                report(FormField.LONGITUDE, longitude(spec.longitude))
                report(FormField.LABEL, maxLength(spec.label, Limits.LABEL))
            }
            is CreateSpec.Event -> {
                report(FormField.TITLE, requiredText(spec.title, Limits.EVENT_TITLE))
                report(FormField.EVENT_LOCATION, maxLength(spec.location, Limits.EVENT_LOCATION))
                report(FormField.DESCRIPTION, maxLength(spec.description, Limits.EVENT_DESCRIPTION))
                report(FormField.START, eventStart(spec.startMillis))
                report(FormField.END, eventEnd(spec.allDay, spec.startMillis, spec.endMillis))
            }
            is CreateSpec.Social -> report(FormField.HANDLE, handle(spec.platform, spec.handle))
            is CreateSpec.AppStore -> report(FormField.URL, appStoreUrl(spec.url))
            is CreateSpec.Barcode -> report(FormField.BARCODE_DATA, barcode(spec.format, spec.data))
        }
        if (errors.isEmpty() && !spec.type.isBarcode) {
            val bytes = PayloadBuilder.build(spec).toByteArray(Charsets.UTF_8).size
            if (bytes > Limits.QR_MAX_BYTES) errors[primaryField(spec.type)] = FieldError.DataTooLong(Limits.QR_MAX_BYTES)
        }
        return errors
    }

    private fun validateContact(s: CreateSpec.Contact, report: (FormField, ValidationResult) -> Unit) {
        report(FormField.NAME, requiredText(s.name, Limits.NAME))
        report(FormField.ORGANIZATION, maxLength(s.organization, Limits.ORGANIZATION))
        report(FormField.JOB_TITLE, maxLength(s.jobTitle, Limits.JOB_TITLE))
        report(FormField.PHONE, phone(s.phone, required = false))
        report(FormField.EMAIL, email(s.email, required = false))
        report(FormField.ADDRESS, maxLength(s.address, Limits.ADDRESS))
        report(FormField.WEBSITE, url(s.website, required = false))
        report(FormField.NOTE, maxLength(s.note, Limits.NOTE))
    }

    fun isValid(spec: CreateSpec): Boolean = validate(spec).isEmpty()

    /** The error of one [field] of [spec], or null. */
    fun errorFor(spec: CreateSpec, field: FormField): FieldError? = validate(spec)[field]

    /** The field that carries a whole-payload error such as [FieldError.DataTooLong]. */
    fun primaryField(type: CreateType): FormField =
        when (type) {
            CreateType.WEBSITE, CreateType.APP_STORE -> FormField.URL
            CreateType.TEXT -> FormField.TEXT
            CreateType.CONTACT -> FormField.NAME
            CreateType.WIFI -> FormField.SSID
            CreateType.EMAIL -> FormField.EMAIL
            CreateType.PHONE, CreateType.SMS -> FormField.PHONE
            CreateType.LOCATION -> FormField.LATITUDE
            CreateType.EVENT -> FormField.TITLE
            CreateType.SOCIAL -> FormField.HANDLE
            else -> FormField.BARCODE_DATA
        }
}

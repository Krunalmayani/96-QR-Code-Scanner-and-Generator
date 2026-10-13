package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/** How the data field of one barcode format behaves. */
data class BarcodeInputRules(
    /** True when only digits are accepted (a numeric keyboard fits). */
    val numericOnly: Boolean,
    /** Longest useful input; the field stops accepting characters beyond it. */
    val maxLength: Int,
    /** Digits typed before the automatic check digit is appended (EAN-13 12, EAN-8 7, UPC-A 11), else null. */
    val bodyLength: Int?,
)

/** Live state of the barcode data field, shown under it as the user types. */
sealed interface BarcodeStatus {
    data object Empty : BarcodeStatus

    /** Still typing: [count] of [needed] digits so far. */
    data class Incomplete(val count: Int, val needed: Int) : BarcodeStatus

    /** The body is complete; [digit] will be appended automatically. */
    data class CheckDigitWillBeAdded(val digit: Int) : BarcodeStatus

    /** Valid input of [length] characters; [checkDigit] is set for GS1 numbers that carry a verified check digit. */
    data class Complete(val length: Int, val checkDigit: Int?) : BarcodeStatus

    data class Invalid(val error: FieldError) : BarcodeStatus
}

/** The check digit of a finished barcode value, for the details card. */
data class CheckDigitInfo(val digit: Int, val valid: Boolean)

object BarcodeInputs {
    fun rules(format: CodeFormat): BarcodeInputRules = when (format) {
        CodeFormat.EAN_13 -> BarcodeInputRules(numericOnly = true, maxLength = 13, bodyLength = 12)
        CodeFormat.EAN_8 -> BarcodeInputRules(numericOnly = true, maxLength = 8, bodyLength = 7)
        CodeFormat.UPC_A -> BarcodeInputRules(numericOnly = true, maxLength = 12, bodyLength = 11)
        CodeFormat.ITF -> BarcodeInputRules(numericOnly = true, maxLength = Limits.BARCODE_1D_MAX, bodyLength = null)
        CodeFormat.CODE_39, CodeFormat.CODE_128 ->
            BarcodeInputRules(numericOnly = false, maxLength = Limits.BARCODE_1D_MAX, bodyLength = null)
        else -> BarcodeInputRules(numericOnly = false, maxLength = Limits.BARCODE_2D_MAX, bodyLength = null)
    }

    /** Cuts [data] to the format's maximum length. */
    fun clamp(format: CodeFormat, data: String): String = data.take(rules(format).maxLength)

    fun status(format: CodeFormat, data: String): BarcodeStatus {
        val t = data.trim()
        if (t.isEmpty()) return BarcodeStatus.Empty
        val rules = rules(format)
        val body = rules.bodyLength
        val result = Validators.barcode(format, t)
        if (result is ValidationResult.Invalid) {
            val error = result.error
            val stillTyping = body != null && error is FieldError.BarcodeWrongLength && t.length < body && t.all { it in '0'..'9' }
            return if (stillTyping && body != null) BarcodeStatus.Incomplete(t.length, body) else BarcodeStatus.Invalid(error)
        }
        if (body == null) return BarcodeStatus.Complete(t.length, null)
        return if (t.length == body) {
            BarcodeStatus.CheckDigitWillBeAdded(BarcodeCheckDigit.checkDigit(t))
        } else {
            BarcodeStatus.Complete(t.length, t.last() - '0')
        }
    }

    /** Check digit of a finished GS1 [value] (EAN-13, EAN-8, UPC-A), or null for other formats. */
    fun checkDigitInfo(format: CodeFormat, value: String): CheckDigitInfo? {
        val body = BarcodeCheckDigit.bodyLength(format) ?: return null
        if (value.length != body + 1 || value.any { it !in '0'..'9' }) return null
        return CheckDigitInfo(value.last() - '0', BarcodeCheckDigit.isValid(value))
    }
}

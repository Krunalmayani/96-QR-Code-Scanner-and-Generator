package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/** GS1 mod-10 check digits (EAN-13, EAN-8, UPC-A) and per-format input normalisation. */
object BarcodeCheckDigit {
    /** Digit count WITHOUT check digit for the formats that carry one; null for others. */
    fun bodyLength(format: CodeFormat): Int? =
        when (format) {
            CodeFormat.EAN_13 -> 12
            CodeFormat.EAN_8 -> 7
            CodeFormat.UPC_A -> 11
            else -> null
        }

    /** GS1 check digit for [body] (all digits, check digit NOT included). */
    fun checkDigit(body: String): Int {
        require(body.isNotEmpty() && body.all { it in '0'..'9' }) { "digits required" }
        var sum = 0
        // Weights 3,1,3,1... starting from the rightmost digit of the body.
        for ((i, ch) in body.reversed().withIndex()) {
            sum += (ch - '0') * if (i % 2 == 0) 3 else 1
        }
        return (10 - sum % 10) % 10
    }

    /** True when [full] (digits including the final check digit) carries a correct check digit. */
    fun isValid(full: String): Boolean =
        full.length >= 2 && full.all { it in '0'..'9' } && checkDigit(full.dropLast(1)) == full.last() - '0'

    /** [digits12] plus its check digit: the 13-digit EAN-13. */
    fun ean13(digits12: String): String {
        require(digits12.length == 12) { "EAN-13 needs 12 digits" }
        return digits12 + checkDigit(digits12)
    }

    fun ean8(digits7: String): String {
        require(digits7.length == 7) { "EAN-8 needs 7 digits" }
        return digits7 + checkDigit(digits7)
    }

    fun upcA(digits11: String): String {
        require(digits11.length == 11) { "UPC-A needs 11 digits" }
        return digits11 + checkDigit(digits11)
    }

    /**
     * Appends the check digit when [data] has exactly the body length of [format] (EAN-13 12, EAN-8 7, UPC-A 11).
     * Anything else is returned unchanged (it may already be complete, or invalid: see [Validators.barcode]).
     */
    fun withCheckDigit(format: CodeFormat, data: String): String {
        val body = bodyLength(format) ?: return data
        return if (data.length == body && data.all { it in '0'..'9' }) data + checkDigit(data) else data
    }

    /** Trims, upper-cases Code 39, then appends the auto check digit. This is what gets encoded. */
    fun normalize(format: CodeFormat, data: String): String {
        val trimmed = data.trim()
        val cased = if (format == CodeFormat.CODE_39) trimmed.uppercase() else trimmed
        return withCheckDigit(format, cased)
    }
}

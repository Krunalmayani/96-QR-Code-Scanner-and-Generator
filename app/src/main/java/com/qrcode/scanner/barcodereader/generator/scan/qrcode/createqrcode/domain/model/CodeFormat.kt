package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

import kotlinx.serialization.Serializable

/** Symbologies the app can scan and (mostly) create. Pure Kotlin: no Android or ML Kit types. */
@Serializable
enum class CodeFormat(val displayName: String, val isQr: Boolean = false) {
    QR_CODE("QR Code", isQr = true),
    EAN_13("EAN-13"),
    EAN_8("EAN-8"),
    UPC_A("UPC-A"),
    UPC_E("UPC-E"),
    CODE_128("Code 128"),
    CODE_39("Code 39"),
    CODE_93("Code 93"),
    ITF("ITF"),
    CODABAR("Codabar"),
    PDF_417("PDF417"),
    DATA_MATRIX("Data Matrix"),
    AZTEC("Aztec"),
    UNKNOWN("Unknown"),
    ;

    /** True for the 1D retail formats whose digits identify a product. */
    val isProductFormat: Boolean get() = this == EAN_13 || this == EAN_8 || this == UPC_A || this == UPC_E
}

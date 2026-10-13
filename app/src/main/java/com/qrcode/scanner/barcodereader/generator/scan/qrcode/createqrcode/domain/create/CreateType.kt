package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/** Grouping of the Create hub ("All types" grid). */
enum class CreateGroup { CONTENT, SOCIAL, BARCODE }

/**
 * Every kind of code the Create hub can make. [name] is the stable key used in navigation
 * (`CreateForm(typeKey = type.name)`), see [fromKey].
 */
enum class CreateType(
    val group: CreateGroup,
    val defaultFormat: CodeFormat,
) {
    WEBSITE(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    TEXT(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    CONTACT(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    WIFI(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    EMAIL(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    PHONE(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    SMS(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    LOCATION(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    EVENT(CreateGroup.CONTENT, CodeFormat.QR_CODE),
    SOCIAL(CreateGroup.SOCIAL, CodeFormat.QR_CODE),
    APP_STORE(CreateGroup.SOCIAL, CodeFormat.QR_CODE),
    BARCODE_EAN_13(CreateGroup.BARCODE, CodeFormat.EAN_13),
    BARCODE_EAN_8(CreateGroup.BARCODE, CodeFormat.EAN_8),
    BARCODE_UPC_A(CreateGroup.BARCODE, CodeFormat.UPC_A),
    BARCODE_CODE_128(CreateGroup.BARCODE, CodeFormat.CODE_128),
    BARCODE_CODE_39(CreateGroup.BARCODE, CodeFormat.CODE_39),
    BARCODE_ITF(CreateGroup.BARCODE, CodeFormat.ITF),
    BARCODE_PDF_417(CreateGroup.BARCODE, CodeFormat.PDF_417),
    BARCODE_DATA_MATRIX(CreateGroup.BARCODE, CodeFormat.DATA_MATRIX),
    BARCODE_AZTEC(CreateGroup.BARCODE, CodeFormat.AZTEC),
    ;

    val isBarcode: Boolean get() = group == CreateGroup.BARCODE

    /** True for the 1D symbologies (bars of one row). */
    val isLinearBarcode: Boolean
        get() = isBarcode && this != BARCODE_PDF_417 && this != BARCODE_DATA_MATRIX && this != BARCODE_AZTEC

    companion object {
        fun fromKey(key: String?): CreateType? = if (key == null) null else entries.firstOrNull { it.name == key }

        /** Order of the tiles in the Create hub "All types" grid (design s31). */
        val hubOrder: List<CreateType> =
            listOf(
                WEBSITE,
                TEXT,
                CONTACT,
                PHONE,
                SMS,
                EMAIL,
                WIFI,
                EVENT,
                LOCATION,
                SOCIAL,
                APP_STORE,
            )

        /** Types offered by the "Custom data" tile (design s39 QR / Barcode switch). */
        val barcodeTypes: List<CreateType> = entries.filter { it.isBarcode }
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

import kotlinx.serialization.Serializable

/** Coarse category of a code. Drives the history icon tile, filter chips and result screen. */
@Serializable
enum class ContentType {
    WEBSITE,
    TEXT,
    CONTACT,
    PHONE,
    EMAIL,
    SMS,
    LOCATION,
    EVENT,
    WIFI,
    PRODUCT,
    UNKNOWN,
}

@Serializable
enum class ScanSource { CAMERA, IMAGE, CREATED }

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One history row. Enums are stored as their `name` strings and the parsed content / style as JSON,
 * so no Room type converters are needed (see ScanMappers.kt).
 */
@Entity(
    tableName = "scans",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["type"]),
        Index(value = ["isFavorite"]),
    ],
)
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawValue: String,
    /** CodeFormat name. */
    val format: String,
    /** ContentType name. */
    val type: String,
    val displayTitle: String,
    /** ScanContent serialised with kotlinx.serialization. */
    val parsedJson: String,
    val createdAt: Long,
    /** ScanSource name: CAMERA, IMAGE or CREATED. */
    val source: String,
    val isFavorite: Boolean = false,
    val note: String = "",
    /** QrStyle serialised with kotlinx.serialization; null when the record has no style. */
    val styleJson: String? = null,
)

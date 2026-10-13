package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import kotlinx.serialization.json.Json

/** Json used for parsedJson and styleJson. Lenient on read so old rows survive model changes. */
internal val ScanJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun ScanRecord.toEntity(): ScanEntity = ScanEntity(
    id = if (id > 0) id else 0,
    rawValue = rawValue,
    format = format.name,
    type = type.name,
    displayTitle = displayTitle,
    parsedJson = encodeContent(content),
    createdAt = createdAt,
    source = source.name,
    isFavorite = isFavorite,
    note = note,
    styleJson = style?.let { encodeStyle(it) },
)

/** Never throws: corrupt or unknown JSON falls back to [ScanContent.Unknown] / no style. */
fun ScanEntity.toDomain(): ScanRecord {
    val codeFormat = enumOrNull<CodeFormat>(format) ?: CodeFormat.UNKNOWN
    return ScanRecord(
        id = id,
        content = decodeContent(parsedJson, rawValue, codeFormat),
        createdAt = createdAt,
        source = enumOrNull<ScanSource>(source) ?: ScanSource.CAMERA,
        isFavorite = isFavorite,
        note = note,
        style = decodeStyle(styleJson),
    )
}

fun encodeContent(content: ScanContent): String = ScanJson.encodeToString(ScanContent.serializer(), content)

fun encodeStyle(style: QrStyle): String = ScanJson.encodeToString(QrStyle.serializer(), style)

fun decodeContent(json: String, rawValue: String, format: CodeFormat): ScanContent =
    try {
        ScanJson.decodeFromString(ScanContent.serializer(), json)
    } catch (ignored: Exception) {
        ScanContent.Unknown(rawValue, format)
    }

fun decodeStyle(json: String?): QrStyle? {
    if (json.isNullOrBlank()) return null
    return try {
        ScanJson.decodeFromString(QrStyle.serializer(), json)
    } catch (ignored: Exception) {
        null
    }
}

private inline fun <reified E : Enum<E>> enumOrNull(name: String): E? =
    enumValues<E>().firstOrNull { it.name == name }

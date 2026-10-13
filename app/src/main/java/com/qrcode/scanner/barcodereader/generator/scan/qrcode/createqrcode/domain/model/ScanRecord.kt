package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

/**
 * One history row: a scanned or a created code.
 * [id] is a positive Room id for stored rows and negative for transient rows (history switched off).
 */
data class ScanRecord(
    val id: Long = 0,
    val content: ScanContent,
    val createdAt: Long,
    val source: ScanSource,
    val isFavorite: Boolean = false,
    val note: String = "",
    val style: QrStyle? = null,
) {
    val rawValue: String get() = content.rawValue
    val format: CodeFormat get() = content.format
    val type: ContentType get() = content.type
    val displayTitle: String get() = content.title
}

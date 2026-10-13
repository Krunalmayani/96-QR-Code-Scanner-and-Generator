package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource

/** Search and "Recent" row of the Create hub. */
object CreateHubLogic {
    private fun squash(text: String): String = text.lowercase().filter { it.isLetterOrDigit() }

    /**
     * The [types] whose label contains [query]; case, spaces and punctuation are ignored (so "wifi" finds "Wi-Fi").
     * A blank query keeps everything. [labelOf] returns the localised label of a type.
     */
    fun filter(types: List<CreateType>, query: String, labelOf: (CreateType) -> String): List<CreateType> {
        val needle = squash(query)
        if (needle.isEmpty()) return types
        return types.filter { squash(labelOf(it)).contains(needle) }
    }

    /** The create type a stored record was made with, or null when it cannot be told (unknown content). */
    fun typeOf(record: ScanRecord): CreateType? {
        if (!record.format.isQr) return CreateType.entries.firstOrNull { it.isBarcode && it.defaultFormat == record.format }
        return when (record.type) {
            ContentType.WEBSITE -> CreateType.WEBSITE
            ContentType.TEXT -> CreateType.TEXT
            ContentType.CONTACT -> CreateType.CONTACT
            ContentType.PHONE -> CreateType.PHONE
            ContentType.EMAIL -> CreateType.EMAIL
            ContentType.SMS -> CreateType.SMS
            ContentType.LOCATION -> CreateType.LOCATION
            ContentType.EVENT -> CreateType.EVENT
            ContentType.WIFI -> CreateType.WIFI
            ContentType.PRODUCT, ContentType.UNKNOWN -> null
        }
    }

    /** Up to [limit] distinct types from the newest created records of [records] (newest first). */
    fun recent(records: List<ScanRecord>, limit: Int = RECENT_LIMIT): List<CreateType> =
        records.asSequence()
            .filter { it.source == ScanSource.CREATED }
            .mapNotNull { typeOf(it) }
            .distinct()
            .take(limit)
            .toList()

    const val RECENT_LIMIT = 3
}

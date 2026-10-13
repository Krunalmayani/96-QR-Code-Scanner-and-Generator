package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType

/** The two History tabs. Scanned = sources CAMERA and IMAGE, Created = source CREATED. */
enum class HistoryTab { SCANNED, CREATED }

/** Single-select filter chips under the search bar, in display order. */
enum class HistoryChip { ALL, FAVORITES, LINKS, WIFI, CONTACTS, TEXT, PHONE, EMAIL, LOCATION, EVENTS, PRODUCTS }

/** Scope chips shown while searching (design s53). */
enum class SearchScope { ALL, SCANNED, CREATED, FAVORITES }

/** Maps filter chips to the content types they select. */
object HistoryChipMapper {
    /** Types selected by [chip]. Empty means "every type" (ALL and FAVORITES do not narrow by type). */
    fun contentTypes(chip: HistoryChip): Set<ContentType> = when (chip) {
        HistoryChip.ALL, HistoryChip.FAVORITES -> emptySet()
        HistoryChip.LINKS -> setOf(ContentType.WEBSITE)
        HistoryChip.WIFI -> setOf(ContentType.WIFI)
        HistoryChip.CONTACTS -> setOf(ContentType.CONTACT)
        HistoryChip.TEXT -> setOf(ContentType.TEXT)
        HistoryChip.PHONE -> setOf(ContentType.PHONE, ContentType.SMS)
        HistoryChip.EMAIL -> setOf(ContentType.EMAIL)
        HistoryChip.LOCATION -> setOf(ContentType.LOCATION)
        HistoryChip.EVENTS -> setOf(ContentType.EVENT)
        HistoryChip.PRODUCTS -> setOf(ContentType.PRODUCT)
    }

    fun favoritesOnly(chip: HistoryChip): Boolean = chip == HistoryChip.FAVORITES
}

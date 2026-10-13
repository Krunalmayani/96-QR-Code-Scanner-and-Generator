package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

/** Help topics, in display order. The UI maps each topic to its question and answer strings. */
enum class FaqTopic {
    WONT_SCAN,
    OFFLINE,
    COLORS,
    HISTORY_STORAGE,
    FROM_PHOTO,
    AUTO_OPEN,
    WIFI,
    CODE_TYPES,
    EXPORT,
    LANGUAGE,
}

object FaqCatalog {
    /** Every question shown on the Help screen (at least eight). */
    val topics: List<FaqTopic> = FaqTopic.entries.toList()
}

/** Accordion rule for the FAQ list: at most one answer is open at a time. */
object FaqAccordion {
    /** Returns the topic to show open after [tapped] is tapped while [expanded] is open (null = all closed). */
    fun toggle(expanded: FaqTopic?, tapped: FaqTopic): FaqTopic? = if (expanded == tapped) null else tapped
}

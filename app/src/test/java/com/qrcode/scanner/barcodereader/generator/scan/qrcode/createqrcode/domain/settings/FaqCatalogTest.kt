package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaqCatalogTest {
    @Test
    fun `has at least eight unique questions`() {
        assertTrue(FaqCatalog.topics.size >= 8)
        assertEquals(FaqCatalog.topics.size, FaqCatalog.topics.toSet().size)
    }

    @Test
    fun `tapping a closed topic opens it and closes the previous one`() {
        assertEquals(FaqTopic.OFFLINE, FaqAccordion.toggle(null, FaqTopic.OFFLINE))
        assertEquals(FaqTopic.COLORS, FaqAccordion.toggle(FaqTopic.OFFLINE, FaqTopic.COLORS))
    }

    @Test
    fun `tapping the open topic closes it`() {
        assertNull(FaqAccordion.toggle(FaqTopic.WIFI, FaqTopic.WIFI))
    }
}

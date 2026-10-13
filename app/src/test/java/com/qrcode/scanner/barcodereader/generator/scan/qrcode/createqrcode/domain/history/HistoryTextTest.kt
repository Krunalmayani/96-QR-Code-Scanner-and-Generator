package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.Fixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryTextTest {
    @Test
    fun `share text lists raw values one per line`() {
        val text =
            HistoryShareText.build(
                listOf(
                    Fixtures.record(Fixtures.website("https://a.com")),
                    Fixtures.record(Fixtures.text("hello")),
                ),
            )
        assertEquals("https://a.com\nhello", text)
    }

    @Test
    fun `multi line records are separated by a blank line and blanks are skipped`() {
        val contact = ScanContent.Contact("BEGIN:VCARD\nFN:Sophie\nEND:VCARD", CodeFormat.QR_CODE, name = "Sophie")
        val text =
            HistoryShareText.build(
                listOf(Fixtures.record(contact), Fixtures.record(Fixtures.text("   ")), Fixtures.record(Fixtures.text("x"))),
            )
        assertEquals("BEGIN:VCARD\nFN:Sophie\nEND:VCARD\n\nx", text)
    }

    @Test
    fun `share text of nothing is empty`() {
        assertEquals("", HistoryShareText.build(emptyList()))
    }

    @Test
    fun `highlighter finds every case insensitive hit`() {
        assertEquals(listOf(0..3, 13..16), QueryHighlighter.ranges("CafeNova and Cafe", "cafe"))
    }

    @Test
    fun `highlighter ignores blank queries and longer needles`() {
        assertTrue(QueryHighlighter.ranges("abc", "").isEmpty())
        assertTrue(QueryHighlighter.ranges("abc", "   ").isEmpty())
        assertTrue(QueryHighlighter.ranges("abc", "abcd").isEmpty())
        assertTrue(QueryHighlighter.ranges("", "a").isEmpty())
    }

    @Test
    fun `highlighter hits do not overlap`() {
        assertEquals(listOf(0..1, 2..3), QueryHighlighter.ranges("aaaa", "aa"))
    }

    @Test
    fun `highlighter trims the query`() {
        assertEquals(listOf(5..7), QueryHighlighter.ranges("hellocafe", " caf "))
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DuplicateFilterTest {
    private val filter = DuplicateFilter(windowMillis = 2_000L)
    private val qr = CodeFormat.QR_CODE

    @Test
    fun `first sighting is accepted`() {
        assertTrue(filter.shouldAccept("a", qr, 0L))
    }

    @Test
    fun `same value within the window is rejected`() {
        filter.shouldAccept("a", qr, 0L)
        assertFalse(filter.shouldAccept("a", qr, 1_999L))
    }

    @Test
    fun `same value after the window is accepted again`() {
        filter.shouldAccept("a", qr, 0L)
        assertTrue(filter.shouldAccept("a", qr, 2_000L))
    }

    @Test
    fun `a code held in view stays suppressed because every sighting restarts the window`() {
        filter.shouldAccept("a", qr, 0L)
        assertFalse(filter.shouldAccept("a", qr, 1_500L))
        assertFalse(filter.shouldAccept("a", qr, 3_000L))
        assertFalse(filter.shouldAccept("a", qr, 4_500L))
        assertTrue(filter.shouldAccept("a", qr, 6_500L))
    }

    @Test
    fun `different values do not suppress each other`() {
        filter.shouldAccept("a", qr, 0L)
        assertTrue(filter.shouldAccept("b", qr, 10L))
    }

    @Test
    fun `same value in another format is a different code`() {
        filter.shouldAccept("123456789012", CodeFormat.CODE_128, 0L)
        assertTrue(filter.shouldAccept("123456789012", CodeFormat.ITF, 10L))
    }

    @Test
    fun `reset forgets everything`() {
        filter.shouldAccept("a", qr, 0L)
        filter.reset()
        assertTrue(filter.shouldAccept("a", qr, 10L))
    }

    @Test
    fun `a clock that goes backwards does not block a code`() {
        filter.shouldAccept("a", qr, 10_000L)
        assertTrue(filter.shouldAccept("a", qr, 5_000L))
    }

    @Test
    fun `many distinct codes are evicted without breaking later checks`() {
        repeat(100) { assertTrue(filter.shouldAccept("code-$it", qr, it.toLong())) }
        assertTrue(filter.shouldAccept("code-0", qr, 10_000L))
        assertFalse(filter.shouldAccept("code-0", qr, 10_500L))
    }

    @Test
    fun `default window is two seconds`() {
        val default = DuplicateFilter()
        default.shouldAccept("a", qr, 0L)
        assertFalse(default.shouldAccept("a", qr, 1_999L))
        assertTrue(default.shouldAccept("a", qr, 4_000L))
    }
}

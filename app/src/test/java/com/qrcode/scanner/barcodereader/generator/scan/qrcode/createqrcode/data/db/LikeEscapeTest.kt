package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LikeEscapeTest {
    @Test
    fun `plain text is wrapped in wildcards`() {
        assertEquals("%wifi%", escapeLikePattern("wifi"))
    }

    @Test
    fun `percent underscore and backslash are escaped`() {
        assertEquals("%100\\%\\_a\\\\b%", escapeLikePattern("100%_a\\b"))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals("%abc%", escapeLikePattern("  abc "))
    }
}

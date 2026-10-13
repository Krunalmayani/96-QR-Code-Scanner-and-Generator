package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NonFatalExceptionTest {
    private val secret = "WIFI:S:HomeNetwork;P:hunter2;;"

    @Test
    fun `message carries the tag and class name but never the original message`() {
        val original = IllegalArgumentException("bad payload $secret")
        val reported = NonFatalException("parse", original)
        assertEquals("parse: java.lang.IllegalArgumentException", reported.message)
        assertFalse(reported.message.orEmpty().contains("hunter2"))
    }

    @Test
    fun `cause chain is dropped because it may echo content`() {
        val original = IllegalStateException("outer", RuntimeException(secret))
        assertNull(NonFatalException("save", original).cause)
    }

    @Test
    fun `stack trace of the original is kept`() {
        val original = IllegalStateException("x")
        val reported = NonFatalException("save", original)
        assertArrayEquals(original.stackTrace, reported.stackTrace)
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RawValueDecoderTest {
    @Test
    fun `valid utf8 bytes decode as utf8`() {
        assertEquals("café ✓", RawValueDecoder.decodeBytes("café ✓".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun `invalid utf8 falls back to latin1`() {
        val bytes = byteArrayOf(0x63, 0xE9.toByte(), 0x21)
        assertEquals("cé!", RawValueDecoder.decodeBytes(bytes))
    }

    @Test
    fun `null or empty bytes give null`() {
        assertNull(RawValueDecoder.decodeBytes(null))
        assertNull(RawValueDecoder.decodeBytes(ByteArray(0)))
    }

    @Test
    fun `resolve prefers the decoder string`() {
        assertEquals("text", RawValueDecoder.resolve("text", "other".toByteArray()))
    }

    @Test
    fun `resolve falls back to bytes when the string is null or empty`() {
        assertEquals("bytes", RawValueDecoder.resolve(null, "bytes".toByteArray()))
        assertEquals("bytes", RawValueDecoder.resolve("", "bytes".toByteArray()))
    }

    @Test
    fun `resolve gives null when nothing is available`() {
        assertNull(RawValueDecoder.resolve(null, null))
        assertNull(RawValueDecoder.resolve("", ByteArray(0)))
    }
}

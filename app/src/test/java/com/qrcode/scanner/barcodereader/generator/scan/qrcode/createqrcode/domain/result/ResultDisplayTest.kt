package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.TimeZone

class ResultDisplayTest {
    @Test
    fun `url splits into host and path`() {
        assertEquals(UrlParts("brewhouse-coffee.com", "/menu?table=12"), UrlDisplay.split("https://www.brewhouse-coffee.com/menu?table=12"))
        assertEquals(UrlParts("example.com", ""), UrlDisplay.split("https://example.com/"))
        assertEquals(UrlParts("example.com", ""), UrlDisplay.split("http://example.com"))
        assertEquals(UrlParts("example.com:8080", "?a=1"), UrlDisplay.split("http://example.com:8080?a=1"))
    }

    @Test
    fun `user info is not shown as part of the host`() {
        assertEquals("evil.com", UrlDisplay.split("https://google.com@evil.com/x").host)
    }

    @Test
    fun `code kinds`() {
        assertEquals(CodeKind.QR, CodeKinds.of(CodeFormat.QR_CODE))
        assertEquals(CodeKind.BARCODE, CodeKinds.of(CodeFormat.EAN_13))
        assertEquals(CodeKind.BARCODE, CodeKinds.of(CodeFormat.CODE_128))
        assertEquals(CodeKind.OTHER, CodeKinds.of(CodeFormat.DATA_MATRIX))
        assertEquals(CodeKind.OTHER, CodeKinds.of(CodeFormat.UNKNOWN))
    }

    @Test
    fun `day buckets are relative to the local day`() {
        val utc = TimeZone.getTimeZone("UTC")
        val now = 1_700_000_000_000L
        val dayMs = 86_400_000L
        assertEquals(DayBucket.TODAY, ResultTime.bucket(now - 1000, now, utc))
        assertEquals(DayBucket.YESTERDAY, ResultTime.bucket(now - dayMs, now, utc))
        assertEquals(DayBucket.EARLIER, ResultTime.bucket(now - 2 * dayMs, now, utc))
    }

    @Test
    fun `just after midnight in a far zone still counts as today`() {
        val tokyo = TimeZone.getTimeZone("Asia/Tokyo")
        val now = 1_700_000_000_000L
        val midnightTokyo = now - Math.floorMod(now + tokyo.getOffset(now), 86_400_000L)
        assertEquals(DayBucket.TODAY, ResultTime.bucket(midnightTokyo, now, tokyo))
        assertEquals(DayBucket.YESTERDAY, ResultTime.bucket(midnightTokyo - 1, now, tokyo))
    }

    @Test
    fun `barcode numbers are grouped like on the package`() {
        assertEquals("5 449000 000996", BarcodeNumber.group("5449000000996", CodeFormat.EAN_13))
        assertEquals("0 12345 67890 5", BarcodeNumber.group("012345678905", CodeFormat.UPC_A))
        assertEquals("9638 5074", BarcodeNumber.group("96385074", CodeFormat.EAN_8))
        assertEquals("12345", BarcodeNumber.group("12345", CodeFormat.CODE_128))
        assertEquals("54490A0000996", BarcodeNumber.group("54490A0000996", CodeFormat.EAN_13))
    }

    @Test
    fun `hex dump shows offsets and bytes eight per line`() {
        val dump = HexDump.of("Hello world!")
        assertEquals(listOf("0000  48 65 6C 6C 6F 20 77 6F", "0008  72 6C 64 21"), dump.lines)
        assertFalse(dump.truncated)
        assertEquals(12, dump.totalBytes)
    }

    @Test
    fun `hex dump encodes utf-8 and truncates long payloads`() {
        assertEquals(listOf("0000  C3 A9"), HexDump.of("é").lines)
        val long = HexDump.of("a".repeat(1000))
        assertTrue(long.truncated)
        assertEquals(1000, long.totalBytes)
        assertEquals(HexDump.MAX_BYTES / HexDump.BYTES_PER_LINE, long.lines.size)
        assertTrue(HexDump.of("").lines.isEmpty())
    }

    @Test
    fun `initials come from the first two words`() {
        assertEquals("SL", Initials.of("Sophie Lambert"))
        assertEquals("AM", Initials.of("anna maria bell"))
        assertEquals("S", Initials.of("Sophie"))
        assertEquals("", Initials.of("  "))
        assertEquals("JD", Initials.of("  (j) \"d\" "))
    }
}

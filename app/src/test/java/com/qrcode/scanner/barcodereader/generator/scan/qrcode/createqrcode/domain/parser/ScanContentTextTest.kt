package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat.QR_CODE
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ScanContentTextTest {
    private fun ms(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun `simple types copy their main value`() {
        assertEquals("https://x.com", ScanContent.Website("x.com", QR_CODE, "https://x.com").copyText())
        assertEquals("+1555", ScanContent.Phone("tel:+1555", QR_CODE, "+1555").copyText())
        assertEquals("a@b.co", ScanContent.Email("raw", QR_CODE, "a@b.co", "s", "b").copyText())
        assertEquals("555", ScanContent.Sms("raw", QR_CODE, "555", "hi").copyText())
        assertEquals("12345", ScanContent.Product("12345", QR_CODE, "12345").copyText())
        assertEquals("hello", ScanContent.Text("hello", QR_CODE).copyText())
        assertEquals("\u0001", ScanContent.Unknown("\u0001", QR_CODE).copyText())
    }

    @Test
    fun `wifi copies the password or the ssid for an open network`() {
        assertEquals("pw", ScanContent.Wifi("r", QR_CODE, "Net", "pw", WifiSecurity.WPA).copyText())
        assertEquals("Net", ScanContent.Wifi("r", QR_CODE, "Net", "", WifiSecurity.NONE).copyText())
    }

    @Test
    fun `location copies coordinates with a dot decimal separator`() {
        assertEquals("48.858400,2.294500", ScanContent.Location("r", QR_CODE, 48.8584, 2.2945).copyText())
    }

    @Test
    fun `contact is formatted over several lines`() {
        val c = ScanContent.Contact(
            "raw", QR_CODE, name = "John Doe", organization = "Acme", jobTitle = "CEO", phones = listOf("1", "2"),
            emails = listOf("j@d.com"), address = "1 Main St", website = "https://d.com", note = "hi",
        )
        assertEquals("John Doe\nCEO, Acme\n1\n2\nj@d.com\n1 Main St\nhttps://d.com\nhi", c.copyText())
        assertEquals(c.copyText(), c.shareText())
    }

    @Test
    fun `event is formatted with utc dates`() {
        val e = ScanContent.Event(
            "raw", QR_CODE, "Launch", ms("2024-05-01T10:00:00Z"), ms("2024-05-01T11:30:00Z"), false, "HQ", "Cake",
        )
        assertEquals("Launch\n2024-05-01 10:00 UTC - 2024-05-01 11:30 UTC\nHQ\nCake", e.copyText())
        val allDay = ScanContent.Event("raw", QR_CODE, "Holiday", ms("2024-12-25T00:00:00Z"), null, true)
        assertEquals("Holiday\n2024-12-25", allDay.shareText())
    }

    @Test
    fun `share text is richer than copy text`() {
        assertEquals("a@b.co\nSubj\nBody", ScanContent.Email("r", QR_CODE, "a@b.co", "Subj", "Body").shareText())
        assertEquals("a@b.co", ScanContent.Email("r", QR_CODE, "a@b.co").shareText())
        assertEquals("555\nhi", ScanContent.Sms("r", QR_CODE, "555", "hi").shareText())
        assertEquals("555", ScanContent.Sms("r", QR_CODE, "555").shareText())
        assertEquals("Wi-Fi: Net\nPassword: pw", ScanContent.Wifi("r", QR_CODE, "Net", "pw").shareText())
        assertEquals("Wi-Fi: Net", ScanContent.Wifi("r", QR_CODE, "Net", "", WifiSecurity.NONE).shareText())
        assertEquals(
            "Eiffel\nhttps://maps.google.com/?q=48.858400,2.294500",
            ScanContent.Location("r", QR_CODE, 48.8584, 2.2945, "Eiffel").shareText(),
        )
        assertEquals("x", ScanContent.Text("x", QR_CODE).shareText())
        assertEquals("123", ScanContent.Product("123", QR_CODE, "123").shareText())
        assertEquals("+1", ScanContent.Phone("r", QR_CODE, "+1").shareText())
        assertEquals("https://x.com", ScanContent.Website("r", QR_CODE, "https://x.com").shareText())
        assertEquals("z", ScanContent.Unknown("z", QR_CODE).shareText())
    }
}

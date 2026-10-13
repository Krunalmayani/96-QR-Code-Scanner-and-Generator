package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScanContentTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `sealed content survives a json round trip`() {
        val original: ScanContent = ScanContent.Wifi("WIFI:S:Home;T:WPA;P:pw;;", CodeFormat.QR_CODE, "Home", "pw", WifiSecurity.WPA)
        val decoded = json.decodeFromString<ScanContent>(json.encodeToString(original))
        assertEquals(original, decoded)
        assertEquals(ContentType.WIFI, decoded.type)
    }

    @Test
    fun `website title drops scheme and www`() {
        val c = ScanContent.Website("https://www.example.com/menu/", CodeFormat.QR_CODE, "https://www.example.com/menu/")
        assertEquals("example.com/menu", c.title)
    }
}

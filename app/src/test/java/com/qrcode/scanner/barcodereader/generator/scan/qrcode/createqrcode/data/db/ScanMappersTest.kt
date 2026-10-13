package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScanMappersTest {
    private val wifi = ScanContent.Wifi("WIFI:S:Home;T:WPA;P:pw;;", CodeFormat.QR_CODE, "Home", "pw", WifiSecurity.WPA, hidden = true)

    @Test
    fun `record survives an entity round trip with style`() {
        val record =
            ScanRecord(
                id = 7,
                content = wifi,
                createdAt = 1234,
                source = ScanSource.CREATED,
                isFavorite = true,
                note = "home router",
                style = QrStyle(foreground = 0xFF112233, gradientEnd = 0xFF445566, dotShape = DotShape.DOTS, frameText = "Scan me"),
            )
        assertEquals(record, record.toEntity().toDomain())
    }

    @Test
    fun `entity columns mirror the record`() {
        val entity = ScanRecord(content = wifi, createdAt = 5, source = ScanSource.IMAGE).toEntity()
        assertEquals(0L, entity.id)
        assertEquals("WIFI:S:Home;T:WPA;P:pw;;", entity.rawValue)
        assertEquals("QR_CODE", entity.format)
        assertEquals(ContentType.WIFI.name, entity.type)
        assertEquals("Home", entity.displayTitle)
        assertEquals("IMAGE", entity.source)
        assertNull(entity.styleJson)
    }

    @Test
    fun `transient ids are not written to the entity`() {
        assertEquals(0L, ScanRecord(id = -3, content = wifi, createdAt = 1, source = ScanSource.CAMERA).toEntity().id)
    }

    @Test
    fun `corrupt content json falls back to Unknown`() {
        val entity = ScanRecord(content = wifi, createdAt = 1, source = ScanSource.CAMERA).toEntity().copy(parsedJson = "{not json")
        assertEquals(ScanContent.Unknown(wifi.rawValue, CodeFormat.QR_CODE), entity.toDomain().content)
    }

    @Test
    fun `unknown content subtype falls back to Unknown`() {
        val entity =
            ScanRecord(content = wifi, createdAt = 1, source = ScanSource.CAMERA)
                .toEntity()
                .copy(parsedJson = """{"type":"hologram","rawValue":"x"}""")
        assertEquals(ScanContent.Unknown(wifi.rawValue, CodeFormat.QR_CODE), entity.toDomain().content)
    }

    @Test
    fun `unknown keys in stored json are ignored`() {
        val discriminator = ScanContent.Text::class.qualifiedName
        val json = """{"type":"$discriminator","rawValue":"hi","format":"QR_CODE","future":1}"""
        val decoded = decodeContent(json, "hi", CodeFormat.QR_CODE)
        assertEquals(ScanContent.Text("hi", CodeFormat.QR_CODE), decoded)
    }

    @Test
    fun `corrupt style json becomes null`() {
        val entity = ScanRecord(content = wifi, createdAt = 1, source = ScanSource.CAMERA).toEntity().copy(styleJson = "[[")
        assertNull(entity.toDomain().style)
        assertNull(decodeStyle(null))
        assertNull(decodeStyle(""))
    }

    @Test
    fun `unknown enum names fall back instead of crashing`() {
        val entity =
            ScanRecord(content = wifi, createdAt = 1, source = ScanSource.CAMERA)
                .toEntity()
                .copy(format = "HOLOGRAM", source = "TELEPATHY", parsedJson = "oops")
        val record = entity.toDomain()
        assertEquals(CodeFormat.UNKNOWN, record.format)
        assertEquals(ScanSource.CAMERA, record.source)
        assertEquals(ContentType.UNKNOWN, record.type)
    }

    @Test
    fun `default style values are encoded explicitly`() {
        val json = encodeStyle(QrStyle())
        assertEquals(QrStyle(), decodeStyle(json))
        assertTrue(json.contains("exportSizePx"))
    }
}

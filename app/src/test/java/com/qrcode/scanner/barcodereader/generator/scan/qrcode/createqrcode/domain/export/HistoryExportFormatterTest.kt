package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class HistoryExportFormatterTest {
    private val t0 = Instant.parse("2024-05-01T10:00:00Z").toEpochMilli()

    private fun web(id: Long = 1, url: String = "https://example.com", note: String = "", fav: Boolean = false) =
        ScanRecord(id, ScanContent.Website(url, CodeFormat.QR_CODE, url), t0, ScanSource.CAMERA, fav, note)

    private fun text(raw: String, id: Long = 2) = ScanRecord(id, ScanContent.Text(raw, CodeFormat.CODE_128), t0, ScanSource.IMAGE)

    @Test
    fun `enum metadata`() {
        assertEquals("csv", HistoryExportFormat.CSV.extension)
        assertEquals("text/csv", HistoryExportFormat.CSV.mimeType)
        assertEquals("json", HistoryExportFormat.JSON.extension)
        assertEquals("application/json", HistoryExportFormat.JSON.mimeType)
        assertEquals("txt", HistoryExportFormat.TEXT.extension)
        assertEquals("text/plain", HistoryExportFormat.TEXT.mimeType)
    }

    @Test
    fun `csv has header and iso utc dates`() {
        val csv = HistoryExportFormatter.format(listOf(web(fav = true, note = "n")), HistoryExportFormat.CSV)
        val lines = csv.split("\r\n")
        assertEquals("id,date,source,type,format,title,content,favorite,note", lines[0])
        assertEquals("1,2024-05-01T10:00:00Z,CAMERA,WEBSITE,QR_CODE,example.com,https://example.com,true,n", lines[1])
        assertEquals("", lines[2])
    }

    @Test
    fun `csv with no records is only the header`() {
        assertEquals(
            "id,date,source,type,format,title,content,favorite,note\r\n",
            HistoryExportFormatter.format(emptyList(), HistoryExportFormat.CSV),
        )
    }

    @Test
    fun `csv quotes commas, quotes and newlines per rfc 4180`() {
        val csv = HistoryExportFormatter.format(listOf(text("a,b \"c\"\nd")), HistoryExportFormat.CSV)
        assertTrue(csv.contains("\"a,b \"\"c\"\"\nd\""), csv)
    }

    @Test
    fun `csv neutralises formula injection`() {
        for (payload in listOf("=1+1", "+cmd|' /C calc'!A0", "-2+3", "@SUM(A1)", "\t=x")) {
            val csv = HistoryExportFormatter.format(listOf(text(payload)), HistoryExportFormat.CSV)
            val row = csv.split("\r\n")[1]
            assertTrue(row.contains(",'" + payload.replace("\"", "\"\"").take(1)) || row.contains("\"'"), "row: $row")
            assertTrue(!Regex("(^|,)[=+\\-@]").containsMatchIn(row.substringAfter("2024-05-01T10:00:00Z,")), "row: $row")
        }
    }

    @Test
    fun `csv injection also protects note and title`() {
        val csv = HistoryExportFormatter.format(listOf(web(note = "=HYPERLINK(\"x\")")), HistoryExportFormat.CSV)
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""), csv)
    }

    @Test
    fun `safe cell leaves normal text alone`() {
        assertEquals("hello", HistoryExportFormatter.safeCell("hello"))
        assertEquals("", HistoryExportFormatter.safeCell(""))
        assertEquals("'=x", HistoryExportFormatter.safeCell("=x"))
    }

    @Test
    fun `json is a pretty printed array with typed fields`() {
        val out = HistoryExportFormatter.format(listOf(web(fav = true, note = "n"), text("t\"q")), HistoryExportFormat.JSON)
        assertTrue(out.contains("\n  "), "pretty printed")
        val arr = Json.parseToJsonElement(out).jsonArray
        assertEquals(2, arr.size)
        val first = arr[0].jsonObject
        assertEquals(1L, first["id"]!!.jsonPrimitive.long)
        assertEquals("2024-05-01T10:00:00Z", first["date"]!!.jsonPrimitive.content)
        assertEquals(t0, first["createdAt"]!!.jsonPrimitive.long)
        assertEquals("CAMERA", first["source"]!!.jsonPrimitive.content)
        assertEquals("WEBSITE", first["type"]!!.jsonPrimitive.content)
        assertEquals("QR_CODE", first["format"]!!.jsonPrimitive.content)
        assertEquals("https://example.com", first["content"]!!.jsonPrimitive.content)
        assertTrue(first["favorite"]!!.jsonPrimitive.boolean)
        assertEquals("t\"q", arr[1].jsonObject["content"]!!.jsonPrimitive.content)
    }

    @Test
    fun `json keeps content verbatim even when it looks like a formula`() {
        val out = HistoryExportFormatter.format(listOf(text("=1+1")), HistoryExportFormat.JSON)
        assertEquals(
            "=1+1",
            Json
                .parseToJsonElement(out)
                .jsonArray[0]
                .jsonObject["content"]!!
                .jsonPrimitive.content,
        )
    }

    @Test
    fun `json empty list`() {
        assertEquals(JsonArray(emptyList()), Json.parseToJsonElement(HistoryExportFormatter.format(emptyList(), HistoryExportFormat.JSON)))
    }

    @Test
    fun `text export is human readable`() {
        val out = HistoryExportFormatter.format(listOf(web(fav = true, note = "my note"), text("hello")), HistoryExportFormat.TEXT)
        assertTrue(
            out.startsWith(
                "example.com\nType: WEBSITE | Format: QR Code | Source: CAMERA\nDate: 2024-05-01T10:00:00Z\nFavorite: yes\nNote: my note\nContent:\nhttps://example.com",
            ),
            out,
        )
        assertTrue(out.contains("\n\nhello\nType: TEXT | Format: Code 128 | Source: IMAGE"), out)
        assertTrue(out.endsWith("Content:\nhello\n"), out)
    }

    @Test
    fun `text export of nothing is empty`() {
        assertEquals("", HistoryExportFormatter.format(emptyList(), HistoryExportFormat.TEXT))
    }

    @Test
    fun `text export falls back to the type name for a blank title`() {
        val out = HistoryExportFormatter.format(listOf(text("   ")), HistoryExportFormat.TEXT)
        assertTrue(out.startsWith("TEXT\n"), out)
    }
}

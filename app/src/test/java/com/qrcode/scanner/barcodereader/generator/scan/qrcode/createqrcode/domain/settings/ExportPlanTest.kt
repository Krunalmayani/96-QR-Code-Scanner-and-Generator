package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId

class ExportPlanTest {
    private fun record(id: Long, source: ScanSource, favorite: Boolean = false) =
        ScanRecord(
            id = id,
            content = ScanContent.Text("v$id", CodeFormat.QR_CODE),
            createdAt = id,
            source = source,
            isFavorite = favorite,
        )

    private val rows =
        listOf(
            record(1, ScanSource.CAMERA),
            record(2, ScanSource.IMAGE, favorite = true),
            record(3, ScanSource.CREATED),
            record(4, ScanSource.CREATED, favorite = true),
        )

    @Test
    fun `default selection includes everything`() {
        assertEquals(listOf(1L, 2L, 3L, 4L), ExportSelection().select(rows).map { it.id })
    }

    @Test
    fun `source checkboxes filter scanned and created rows`() {
        assertEquals(listOf(1L, 2L), ExportSelection(includeCreated = false).select(rows).map { it.id })
        assertEquals(listOf(3L, 4L), ExportSelection(includeScanned = false).select(rows).map { it.id })
        assertEquals(emptyList<Long>(), ExportSelection(includeScanned = false, includeCreated = false).select(rows).map { it.id })
    }

    @Test
    fun `favorites only combines with the source filter`() {
        assertEquals(listOf(2L, 4L), ExportSelection(favoritesOnly = true).select(rows).map { it.id })
        assertEquals(listOf(2L), ExportSelection(includeCreated = false, favoritesOnly = true).select(rows).map { it.id })
    }

    @Test
    fun `counts split scanned from created`() {
        assertEquals(ExportCounts(scanned = 2, created = 2), ExportCounts.of(rows))
        assertEquals(ExportCounts(0, 0), ExportCounts.of(emptyList()))
    }

    @Test
    fun `file name uses the time stamp and a clean extension`() {
        val millis = 1_776_090_330_000L
        assertEquals("qr-history-20260413-142530.csv", ExportFileName.build("csv", millis))
        assertEquals("qr-history-20260413-142530.json", ExportFileName.build(".json", millis))
        assertEquals("qr-history-20260413-142530.txt", ExportFileName.build("", millis))
    }

    @Test
    fun `file name honours the time zone`() {
        val millis = 1_776_090_330_000L
        assertEquals("qr-history-20260413-195530.txt", ExportFileName.build("txt", millis, ZoneId.of("Asia/Kolkata")))
    }
}

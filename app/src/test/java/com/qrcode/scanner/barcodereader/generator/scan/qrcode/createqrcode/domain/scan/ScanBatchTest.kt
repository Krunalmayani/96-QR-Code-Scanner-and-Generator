package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScanBatchTest {
    private fun entry(id: Long, raw: String, format: CodeFormat = CodeFormat.QR_CODE) = BatchEntry(id, raw, format)

    @Test
    fun `new codes are added newest first`() {
        val batch = ScanBatch().add(entry(1, "a")).add(entry(2, "b")).add(entry(3, "c"))
        assertEquals(listOf("c", "b", "a"), batch.entries.map { it.rawValue })
        assertEquals(3, batch.size)
    }

    @Test
    fun `duplicates are ignored and the batch is returned unchanged`() {
        val batch = ScanBatch().add(entry(1, "a"))
        assertSame(batch, batch.add(entry(2, "a")))
        assertEquals(1, batch.add(entry(2, "a")).size)
    }

    @Test
    fun `same payload in another format is a new code`() {
        val batch = ScanBatch().add(entry(1, "123", CodeFormat.CODE_128)).add(entry(2, "123", CodeFormat.ITF))
        assertEquals(2, batch.size)
    }

    @Test
    fun `contains matches payload and format`() {
        val batch = ScanBatch().add(entry(1, "a"))
        assertTrue(batch.contains("a", CodeFormat.QR_CODE))
        assertEquals(false, batch.contains("a", CodeFormat.AZTEC))
        assertEquals(false, batch.contains("b", CodeFormat.QR_CODE))
    }

    @Test
    fun `clear empties the batch`() {
        val batch = ScanBatch().add(entry(1, "a")).clear()
        assertTrue(batch.isEmpty)
        assertEquals(0, batch.size)
    }

    @Test
    fun `batch stops growing at the maximum size`() {
        var batch = ScanBatch()
        repeat(ScanBatch.MAX_SIZE + 5) { batch = batch.add(entry(it.toLong(), "code-$it")) }
        assertEquals(ScanBatch.MAX_SIZE, batch.size)
        assertTrue(batch.isFull)
    }

    @Test
    fun `distinctCodes keeps the first of each payload and format`() {
        val codes = listOf(
            DetectedCode("a", CodeFormat.QR_CODE),
            DetectedCode("b", CodeFormat.QR_CODE),
            DetectedCode("a", CodeFormat.QR_CODE, PxRect(0f, 0f, 1f, 1f)),
            DetectedCode("a", CodeFormat.AZTEC),
        )
        assertEquals(listOf("a" to CodeFormat.QR_CODE, "b" to CodeFormat.QR_CODE, "a" to CodeFormat.AZTEC), codes.distinctCodes().map { it.rawValue to it.format })
    }
}

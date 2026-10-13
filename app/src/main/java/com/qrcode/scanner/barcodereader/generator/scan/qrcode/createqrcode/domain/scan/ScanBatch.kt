package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat

/** A code collected in batch mode together with the history record it was saved as. */
data class BatchEntry(
    val recordId: Long,
    val rawValue: String,
    val format: CodeFormat,
)

/**
 * Immutable list of the unique codes collected by one batch scan, newest first.
 * Adding a code that is already in the batch (same payload and format) is ignored, so the count badge only
 * grows for new codes. The batch holds at most [MAX_SIZE] codes.
 */
data class ScanBatch(
    val entries: List<BatchEntry> = emptyList(),
) {
    val size: Int get() = entries.size
    val isEmpty: Boolean get() = entries.isEmpty()
    val isFull: Boolean get() = entries.size >= MAX_SIZE

    fun contains(rawValue: String, format: CodeFormat): Boolean =
        entries.any { it.rawValue == rawValue && it.format == format }

    /** Returns this batch unchanged when [entry] is a duplicate or the batch is full. */
    fun add(entry: BatchEntry): ScanBatch =
        if (isFull || contains(entry.rawValue, entry.format)) this else ScanBatch(listOf(entry) + entries)

    fun clear(): ScanBatch = EMPTY

    companion object {
        const val MAX_SIZE = 200
        val EMPTY = ScanBatch()
    }
}

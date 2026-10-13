package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

/**
 * Where native ads go in a list: one ad row after every [everyN] content rows, never after the last row.
 * Row indices are positions in the combined list (content plus ads).
 */
object NativeAdPlacement {
    /** Number of ads inserted for [contentCount] items. */
    fun adCount(contentCount: Int, everyN: Int): Int =
        if (contentCount <= 0 || everyN <= 0) 0 else (contentCount - 1) / everyN

    /** Total rows to render. */
    fun rowCount(contentCount: Int, everyN: Int): Int =
        maxOf(contentCount, 0) + adCount(contentCount, everyN)

    /** True when [rowIndex] is an ad row. */
    fun isAdRow(rowIndex: Int, everyN: Int): Boolean =
        everyN > 0 && rowIndex >= 0 && (rowIndex + 1) % (everyN + 1) == 0

    /** Index into the content list for a non-ad [rowIndex]. */
    fun contentIndex(rowIndex: Int, everyN: Int): Int =
        if (everyN <= 0) rowIndex else rowIndex - rowIndex / (everyN + 1)
}

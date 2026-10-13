package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

object HistoryShareText {
    /**
     * Plain-text list for ACTION_SEND: the raw value of each record, one per line (newest first as given).
     * Records whose content spans several lines (contacts, events) are separated by a blank line instead.
     */
    fun build(records: List<ScanRecord>): String {
        val values = records.map { it.rawValue.trim() }.filter { it.isNotEmpty() }
        val separator = if (values.any { '\n' in it }) "\n\n" else "\n"
        return values.joinToString(separator)
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Which history rows the Export screen includes. "Scanned" means every source except CREATED. */
data class ExportSelection(
    val includeScanned: Boolean = true,
    val includeCreated: Boolean = true,
    val favoritesOnly: Boolean = false,
) {
    fun select(records: List<ScanRecord>): List<ScanRecord> =
        records.filter { record ->
            val sourceOk = if (record.source == ScanSource.CREATED) includeCreated else includeScanned
            sourceOk && (!favoritesOnly || record.isFavorite)
        }
}

/** Row counts shown next to the include checkboxes. */
data class ExportCounts(
    val scanned: Int,
    val created: Int,
) {
    companion object {
        fun of(records: List<ScanRecord>): ExportCounts {
            val created = records.count { it.source == ScanSource.CREATED }
            return ExportCounts(scanned = records.size - created, created = created)
        }
    }
}

object ExportFileName {
    private const val BASE = "qr-history"
    private val STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)

    /** `qr-history-20261013-142530.csv`. [zone] is the user's time zone (UTC by default for tests). */
    fun build(extension: String, nowMillis: Long, zone: ZoneId = ZoneOffset.UTC): String {
        val ext =
            extension
                .trim()
                .trimStart('.')
                .filter { it.isLetterOrDigit() }
                .ifEmpty { "txt" }
        return "$BASE-${STAMP.withZone(zone).format(Instant.ofEpochMilli(nowMillis))}.$ext"
    }
}

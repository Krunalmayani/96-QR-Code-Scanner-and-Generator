package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import kotlinx.coroutines.flow.Flow

data class HistoryFilter(
    val source: ScanSource? = null,
    /** Empty means every type. */
    val types: Set<ContentType> = emptySet(),
    val favoritesOnly: Boolean = false,
    /** Matches display title, raw value and note, case-insensitive. Blank means no search. */
    val query: String = "",
)

data class HistoryCounts(val scanned: Int, val created: Int, val favorites: Int)

/**
 * Single access point for history. Ids <= 0 passed in by callers mean "not stored".
 * Records saved with persist = false get a negative id and live only in memory
 * (used when the user turned "Save history" off); every id-based method accepts them.
 */
interface HistoryRepository {
    fun observe(filter: HistoryFilter = HistoryFilter()): Flow<List<ScanRecord>>

    fun observeById(id: Long): Flow<ScanRecord?>

    suspend fun getById(id: Long): ScanRecord?

    /** Saves [record] and returns it with its final id (positive if persisted, negative if transient). */
    suspend fun save(record: ScanRecord, persist: Boolean = true): ScanRecord

    suspend fun setFavorite(ids: Set<Long>, favorite: Boolean)

    suspend fun updateNote(id: Long, note: String)

    suspend fun updateStyle(id: Long, style: QrStyle)

    suspend fun delete(ids: Set<Long>)

    /** Puts back rows removed with [delete] (undo). Keeps their original ids and timestamps. */
    suspend fun restore(records: List<ScanRecord>)

    /** Deletes history rows; returns how many were removed. [source] null clears both tabs. */
    suspend fun clear(keepFavorites: Boolean, source: ScanSource? = null): Int

    suspend fun counts(): HistoryCounts

    /** All stored rows, newest first, for export. */
    suspend fun all(): List<ScanRecord>
}

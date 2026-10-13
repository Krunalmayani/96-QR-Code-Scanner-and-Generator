package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory [HistoryRepository] for ViewModel and use-case tests. Same semantics as the real one:
 * persisted rows get increasing positive ids (never reused), transient rows (save with persist = false) get
 * -1, -2, ... and are invisible to [observe], [counts] and [all] but work with every id-based method.
 */
class FakeHistoryRepository(
    initial: List<ScanRecord> = emptyList(),
) : HistoryRepository {
    private val persisted = MutableStateFlow<Map<Long, ScanRecord>>(emptyMap())
    private val transient = MutableStateFlow<Map<Long, ScanRecord>>(emptyMap())
    private val lastPersistedId = AtomicLong(0)
    private val lastTransientId = AtomicLong(0)

    init {
        initial.forEach { seed(it) }
    }

    /** Stored rows, newest first (test inspection). */
    val persistedRecords: List<ScanRecord> get() = sorted(persisted.value.values)

    /** Transient rows in creation order (test inspection). */
    val transientRecords: List<ScanRecord> get() = transient.value.values.sortedByDescending { it.id }

    /** Inserts [record] as a stored row without going through [save]; returns it with its id. */
    fun seed(record: ScanRecord): ScanRecord {
        val stored = record.copy(id = nextPersistedId(record.id))
        persisted.update { it + (stored.id to stored) }
        return stored
    }

    override fun observe(filter: HistoryFilter): Flow<List<ScanRecord>> =
        persisted.map { rows -> sorted(rows.values.filter { matches(it, filter) }) }

    override fun observeById(id: Long): Flow<ScanRecord?> =
        storeFor(id)?.map { it[id] }?.distinctUntilChanged() ?: flowOf(null)

    override suspend fun getById(id: Long): ScanRecord? = storeFor(id)?.value?.get(id)

    override suspend fun save(record: ScanRecord, persist: Boolean): ScanRecord {
        if (record.id < 0) transient.update { it - record.id }
        return if (persist) {
            seed(record)
        } else {
            val stored = record.copy(id = lastTransientId.decrementAndGet())
            transient.update { it + (stored.id to stored) }
            stored
        }
    }

    override suspend fun setFavorite(ids: Set<Long>, favorite: Boolean) {
        ids.forEach { id -> modify(id) { it.copy(isFavorite = favorite) } }
    }

    override suspend fun updateNote(id: Long, note: String) = modify(id) { it.copy(note = note) }

    override suspend fun updateStyle(id: Long, style: QrStyle) = modify(id) { it.copy(style = style) }

    override suspend fun delete(ids: Set<Long>) {
        persisted.update { it - ids }
        transient.update { it - ids }
    }

    override suspend fun restore(records: List<ScanRecord>) {
        records.forEach { record ->
            if (record.id < 0) {
                transient.update { it + (record.id to record) }
            } else {
                val stored = record.copy(id = nextPersistedId(record.id))
                persisted.update { it + (stored.id to stored) }
            }
        }
    }

    override suspend fun clear(keepFavorites: Boolean, source: ScanSource?): Int {
        val doomed =
            persisted.value.values
                .filter { (source == null || it.source == source) && !(keepFavorites && it.isFavorite) }
                .map { it.id }
                .toSet()
        persisted.update { it - doomed }
        return doomed.size
    }

    override suspend fun counts(): HistoryCounts {
        val rows = persisted.value.values
        return HistoryCounts(
            scanned = rows.count { it.source != ScanSource.CREATED },
            created = rows.count { it.source == ScanSource.CREATED },
            favorites = rows.count { it.isFavorite },
        )
    }

    override suspend fun all(): List<ScanRecord> = persistedRecords

    private fun storeFor(id: Long): MutableStateFlow<Map<Long, ScanRecord>>? =
        when {
            id > 0 -> persisted
            id < 0 -> transient
            else -> null
        }

    private fun modify(id: Long, change: (ScanRecord) -> ScanRecord) {
        storeFor(id)?.update { rows ->
            val current = rows[id] ?: return@update rows
            rows + (id to change(current))
        }
    }

    /** Keeps [requested] when positive (REPLACE semantics) else hands out the next id; ids are never reused. */
    private fun nextPersistedId(requested: Long): Long {
        if (requested <= 0) return lastPersistedId.incrementAndGet()
        lastPersistedId.updateAndGet { maxOf(it, requested) }
        return requested
    }

    private fun sorted(rows: Collection<ScanRecord>): List<ScanRecord> =
        rows.sortedWith(compareByDescending<ScanRecord> { it.createdAt }.thenByDescending { it.id })

    private fun matches(record: ScanRecord, filter: HistoryFilter): Boolean {
        val query = filter.query.trim()
        return (filter.source == null || record.source == filter.source) &&
            (filter.types.isEmpty() || record.type in filter.types) &&
            (!filter.favoritesOnly || record.isFavorite) &&
            (
                query.isEmpty() ||
                    record.displayTitle.contains(query, ignoreCase = true) ||
                    record.rawValue.contains(query, ignoreCase = true) ||
                    record.note.contains(query, ignoreCase = true)
            )
    }
}

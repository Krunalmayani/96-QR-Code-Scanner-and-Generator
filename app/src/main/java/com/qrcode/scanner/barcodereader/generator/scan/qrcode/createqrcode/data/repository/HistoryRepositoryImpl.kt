package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.repository

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.IoDispatcher
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.HistoryDao
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.encodeStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.escapeLikePattern
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.toDomain
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.toEntity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryCounts
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed history plus an in-memory store for transient records (history switched off).
 * Transient records get negative ids (-1, -2, ...) and are never written to disk; every id-based method
 * accepts them. [observe] and the counts only ever see persisted rows.
 */
@Singleton
class HistoryRepositoryImpl
    @Inject
    constructor(
        private val dao: HistoryDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : HistoryRepository {
        private val lastTransientId = AtomicLong(0)
        private val transientRecords = MutableStateFlow<Map<Long, ScanRecord>>(emptyMap())
        private val transientLock = Mutex()

        override fun observe(filter: HistoryFilter): Flow<List<ScanRecord>> {
            val query = filter.query.trim()
            return dao
                .observeFiltered(
                    source = filter.source?.name,
                    filterTypes = filter.types.isNotEmpty(),
                    // SQLite accepts an empty IN list, but a placeholder keeps the generated SQL trivially valid.
                    types = filter.types.map { it.name }.ifEmpty { listOf("") },
                    favoritesOnly = filter.favoritesOnly,
                    hasQuery = query.isNotEmpty(),
                    pattern = escapeLikePattern(query),
                ).map { rows -> rows.map { it.toDomain() } }
                .flowOn(ioDispatcher)
        }

        override fun observeById(id: Long): Flow<ScanRecord?> =
            when {
                id < 0 -> transientRecords.map { it[id] }.distinctUntilChanged()
                id > 0 -> dao.observeById(id).map { it?.toDomain() }.flowOn(ioDispatcher)
                else -> flowOf(null)
            }

        override suspend fun getById(id: Long): ScanRecord? =
            when {
                id < 0 -> transientRecords.value[id]
                id > 0 -> withContext(ioDispatcher) { dao.getById(id)?.toDomain() }
                else -> null
            }

        override suspend fun save(record: ScanRecord, persist: Boolean): ScanRecord =
            if (persist) persistRecord(record) else saveTransient(record)

        override suspend fun setFavorite(ids: Set<Long>, favorite: Boolean) {
            val (persisted, transient) = splitIds(ids)
            if (persisted.isNotEmpty()) {
                withContext(ioDispatcher) {
                    persisted.chunked(SQL_CHUNK).forEach { dao.setFavorite(it, favorite) }
                }
            }
            mutateTransient(transient) { it.copy(isFavorite = favorite) }
        }

        override suspend fun updateNote(id: Long, note: String) {
            if (id > 0) {
                withContext(ioDispatcher) { dao.updateNote(id, note) }
            } else {
                mutateTransient(setOf(id)) { it.copy(note = note) }
            }
        }

        override suspend fun updateStyle(id: Long, style: QrStyle) {
            if (id > 0) {
                withContext(ioDispatcher) { dao.updateStyle(id, encodeStyle(style)) }
            } else {
                mutateTransient(setOf(id)) { it.copy(style = style) }
            }
        }

        override suspend fun delete(ids: Set<Long>) {
            val (persisted, transient) = splitIds(ids)
            if (persisted.isNotEmpty()) {
                withContext(ioDispatcher) {
                    persisted.chunked(SQL_CHUNK).forEach { dao.deleteByIds(it) }
                }
            }
            if (transient.isNotEmpty()) {
                transientLock.withLock { transientRecords.update { it - transient } }
            }
        }

        override suspend fun restore(records: List<ScanRecord>) {
            val transient = records.filter { it.id < 0 }
            val persisted = records.filter { it.id >= 0 }
            if (persisted.isNotEmpty()) {
                withContext(ioDispatcher) { dao.insertAll(persisted.map { it.toEntity() }) }
            }
            if (transient.isNotEmpty()) {
                transientLock.withLock { transientRecords.update { it + transient.associateBy { r -> r.id } } }
            }
        }

        override suspend fun clear(keepFavorites: Boolean, source: ScanSource?): Int =
            withContext(ioDispatcher) { dao.clear(keepFavorites, source?.name) }

        override suspend fun counts(): HistoryCounts =
            withContext(ioDispatcher) {
                HistoryCounts(
                    scanned = dao.countScanned(),
                    created = dao.countCreated(),
                    favorites = dao.countFavorites(),
                )
            }

        override suspend fun all(): List<ScanRecord> =
            withContext(ioDispatcher) { dao.getAll().map { it.toDomain() } }

        private suspend fun persistRecord(record: ScanRecord): ScanRecord {
            if (record.id < 0) {
                // A transient record being promoted to a stored one: drop the in-memory copy.
                transientLock.withLock { transientRecords.update { it - record.id } }
            }
            val rowId = withContext(ioDispatcher) { dao.insert(record.toEntity()) }
            return record.copy(id = rowId)
        }

        private suspend fun saveTransient(record: ScanRecord): ScanRecord =
            transientLock.withLock {
                val reuseId = record.id < 0 && transientRecords.value.containsKey(record.id)
                val id = if (reuseId) record.id else lastTransientId.decrementAndGet()
                val stored = record.copy(id = id)
                transientRecords.update { it + (id to stored) }
                stored
            }

        private suspend fun mutateTransient(ids: Set<Long>, transform: (ScanRecord) -> ScanRecord) {
            if (ids.isEmpty()) return
            transientLock.withLock {
                transientRecords.update { current ->
                    current.mapValues { (id, record) -> if (id in ids) transform(record) else record }
                }
            }
        }

        /** Splits [ids] into (stored ids > 0, transient ids < 0); id 0 means "not stored" and is dropped. */
        private fun splitIds(ids: Set<Long>): Pair<List<Long>, Set<Long>> =
            ids.filter { it > 0 } to ids.filterTo(HashSet()) { it < 0 }

        private companion object {
            /** Stays far below SQLite's bound-variable limit (999 on old devices). */
            const val SQL_CHUNK = 500
        }
    }

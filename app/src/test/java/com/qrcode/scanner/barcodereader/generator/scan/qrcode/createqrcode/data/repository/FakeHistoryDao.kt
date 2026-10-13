package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.repository

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.HistoryDao
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.ScanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory HistoryDao used to test HistoryRepositoryImpl on the JVM (SQL itself is covered by androidTest). */
class FakeHistoryDao : HistoryDao {
    private val rows = MutableStateFlow<List<ScanEntity>>(emptyList())
    private var lastId = 0L

    val snapshot: List<ScanEntity> get() = rows.value

    private fun sorted(list: List<ScanEntity>) =
        list.sortedWith(compareByDescending<ScanEntity> { it.createdAt }.thenByDescending { it.id })

    override fun observeAll() = rows.map { sorted(it) }

    override fun observeBySource(source: String) = rows.map { l -> sorted(l.filter { it.source == source }) }

    override fun observeByTypes(types: List<String>) = rows.map { l -> sorted(l.filter { it.type in types }) }

    override fun observeFavorites() = rows.map { l -> sorted(l.filter { it.isFavorite }) }

    override fun search(pattern: String) = rows.map { l -> sorted(l.filter { matches(it, pattern) }) }

    override fun observeFiltered(
        source: String?,
        filterTypes: Boolean,
        types: List<String>,
        favoritesOnly: Boolean,
        hasQuery: Boolean,
        pattern: String,
    ): Flow<List<ScanEntity>> = rows.map { l ->
        sorted(
            l.filter {
                (source == null || it.source == source) &&
                    (!filterTypes || it.type in types) &&
                    (!favoritesOnly || it.isFavorite) &&
                    (!hasQuery || matches(it, pattern))
            },
        )
    }

    override fun observeById(id: Long): Flow<ScanEntity?> = rows.map { l -> l.firstOrNull { it.id == id } }

    override suspend fun getById(id: Long): ScanEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun getAll(): List<ScanEntity> = sorted(rows.value)

    override suspend fun insert(entity: ScanEntity): Long {
        val id = if (entity.id > 0) entity.id else lastId + 1
        lastId = maxOf(lastId, id)
        rows.update { l -> l.filterNot { it.id == id } + entity.copy(id = id) }
        return id
    }

    override suspend fun insertAll(entities: List<ScanEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun setFavorite(ids: List<Long>, favorite: Boolean) {
        rows.update { l -> l.map { if (it.id in ids) it.copy(isFavorite = favorite) else it } }
    }

    override suspend fun updateNote(id: Long, note: String) {
        rows.update { l -> l.map { if (it.id == id) it.copy(note = note) else it } }
    }

    override suspend fun updateStyle(id: Long, styleJson: String?) {
        rows.update { l -> l.map { if (it.id == id) it.copy(styleJson = styleJson) else it } }
    }

    override suspend fun deleteByIds(ids: List<Long>): Int {
        val before = rows.value.size
        rows.update { l -> l.filterNot { it.id in ids } }
        return before - rows.value.size
    }

    override suspend fun clear(keepFavorites: Boolean, source: String?): Int {
        val before = rows.value.size
        rows.update { l -> l.filterNot { (source == null || it.source == source) && !(keepFavorites && it.isFavorite) } }
        return before - rows.value.size
    }

    override suspend fun countScanned() = rows.value.count { it.source != "CREATED" }

    override suspend fun countCreated() = rows.value.count { it.source == "CREATED" }

    override suspend fun countFavorites() = rows.value.count { it.isFavorite }

    /** Approximates LIKE '%x%' ESCAPE '\' as a case-insensitive contains on the unescaped middle. */
    private fun matches(e: ScanEntity, pattern: String): Boolean {
        val needle = pattern.removePrefix("%").removeSuffix("%").replace(Regex("\\\\(.)"), "$1")
        return listOf(e.displayTitle, e.rawValue, e.note).any { it.contains(needle, ignoreCase = true) }
    }
}

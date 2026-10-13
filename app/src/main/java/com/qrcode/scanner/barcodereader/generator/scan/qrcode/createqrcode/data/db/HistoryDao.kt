package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * All history queries. Newest first everywhere (createdAt, then id as tiebreaker).
 * LIKE patterns are built by [escapeLikePattern] and use backslash as the ESCAPE character.
 * Lists passed to IN (...) must be kept small by the caller (SQLite variable limit): see HistoryRepositoryImpl.
 */
@Dao
interface HistoryDao {
    @Query("SELECT * FROM scans ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE source = :source ORDER BY createdAt DESC, id DESC")
    fun observeBySource(source: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE type IN (:types) ORDER BY createdAt DESC, id DESC")
    fun observeByTypes(types: List<String>): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isFavorite = 1 ORDER BY createdAt DESC, id DESC")
    fun observeFavorites(): Flow<List<ScanEntity>>

    @Query(
        "SELECT * FROM scans WHERE " +
            "displayTitle LIKE :pattern ESCAPE '\\' OR rawValue LIKE :pattern ESCAPE '\\' OR note LIKE :pattern ESCAPE '\\' " +
            "ORDER BY createdAt DESC, id DESC",
    )
    fun search(pattern: String): Flow<List<ScanEntity>>

    /**
     * Combined filter. Every condition is neutral when its switch is off:
     * [source] null = any source, [filterTypes] false = any type (then [types] is ignored but must not be empty),
     * [favoritesOnly] false = any, [hasQuery] false = no search ([pattern] ignored).
     */
    @Query(
        "SELECT * FROM scans WHERE " +
            "(:source IS NULL OR source = :source) " +
            "AND (:filterTypes = 0 OR type IN (:types)) " +
            "AND (:favoritesOnly = 0 OR isFavorite = 1) " +
            "AND (:hasQuery = 0 OR displayTitle LIKE :pattern ESCAPE '\\' " +
            "OR rawValue LIKE :pattern ESCAPE '\\' OR note LIKE :pattern ESCAPE '\\') " +
            "ORDER BY createdAt DESC, id DESC",
    )
    fun observeFiltered(
        source: String?,
        filterTypes: Boolean,
        types: List<String>,
        favoritesOnly: Boolean,
        hasQuery: Boolean,
        pattern: String,
    ): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    fun observeById(id: Long): Flow<ScanEntity?>

    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: Long): ScanEntity?

    @Query("SELECT * FROM scans ORDER BY createdAt DESC, id DESC")
    suspend fun getAll(): List<ScanEntity>

    /** REPLACE keeps the given id, which makes undo (restore) put rows back exactly where they were. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ScanEntity>)

    @Query("UPDATE scans SET isFavorite = :favorite WHERE id IN (:ids)")
    suspend fun setFavorite(ids: List<Long>, favorite: Boolean)

    @Query("UPDATE scans SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Query("UPDATE scans SET styleJson = :styleJson WHERE id = :id")
    suspend fun updateStyle(id: Long, styleJson: String?)

    @Query("DELETE FROM scans WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    /** [source] null clears every source. Returns the number of deleted rows. */
    @Query(
        "DELETE FROM scans WHERE (:source IS NULL OR source = :source) " +
            "AND (:keepFavorites = 0 OR isFavorite = 0)",
    )
    suspend fun clear(keepFavorites: Boolean, source: String?): Int

    /** Rows scanned with the camera or imported from an image. */
    @Query("SELECT COUNT(*) FROM scans WHERE source != 'CREATED'")
    suspend fun countScanned(): Int

    @Query("SELECT COUNT(*) FROM scans WHERE source = 'CREATED'")
    suspend fun countCreated(): Int

    @Query("SELECT COUNT(*) FROM scans WHERE isFavorite = 1")
    suspend fun countFavorites(): Int
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: HistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.historyDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        raw: String,
        type: String = "TEXT",
        source: String = "CAMERA",
        createdAt: Long = 1L,
        favorite: Boolean = false,
        note: String = "",
        title: String = raw,
    ) = ScanEntity(
        rawValue = raw,
        format = "QR_CODE",
        type = type,
        displayTitle = title,
        parsedJson = "{}",
        createdAt = createdAt,
        source = source,
        isFavorite = favorite,
        note = note,
    )

    private fun filtered(
        source: String? = null,
        types: List<String> = emptyList(),
        favoritesOnly: Boolean = false,
        query: String = "",
    ) = dao.observeFiltered(
        source = source,
        filterTypes = types.isNotEmpty(),
        types = types.ifEmpty { listOf("") },
        favoritesOnly = favoritesOnly,
        hasQuery = query.isNotBlank(),
        pattern = escapeLikePattern(query),
    )

    @Test
    fun insertAssignsIdsAndAllIsNewestFirst() =
        runBlocking {
            val a = dao.insert(entity("a", createdAt = 1))
            val b = dao.insert(entity("b", createdAt = 3))
            val c = dao.insert(entity("c", createdAt = 2))
            assertTrue(a > 0 && b > a && c > b)
            assertEquals(listOf("b", "c", "a"), dao.observeAll().first().map { it.rawValue })
            assertEquals(listOf("b", "c", "a"), dao.getAll().map { it.rawValue })
        }

    @Test
    fun replaceKeepsTheGivenIdForUndo() =
        runBlocking {
            val id = dao.insert(entity("a"))
            val original = dao.getById(id)!!
            dao.deleteByIds(listOf(id))
            assertNull(dao.getById(id))
            dao.insertAll(listOf(original))
            assertEquals(original, dao.getById(id))
        }

    @Test
    fun filtersBySourceTypeAndFavorites() =
        runBlocking {
            dao.insert(entity("w", type = "WEBSITE", source = "CAMERA"))
            dao.insert(entity("t", type = "TEXT", source = "CREATED", favorite = true))
            dao.insert(entity("p", type = "PHONE", source = "IMAGE"))
            assertEquals(listOf("t"), dao.observeBySource("CREATED").first().map { it.rawValue })
            assertEquals(
                listOf("w", "p").toSet(),
                dao
                    .observeByTypes(listOf("WEBSITE", "PHONE"))
                    .first()
                    .map { it.rawValue }
                    .toSet(),
            )
            assertEquals(listOf("t"), dao.observeFavorites().first().map { it.rawValue })
            assertEquals(3, filtered().first().size)
            assertEquals(listOf("p"), filtered(source = "IMAGE").first().map { it.rawValue })
            assertEquals(listOf("w"), filtered(types = listOf("WEBSITE")).first().map { it.rawValue })
            assertEquals(listOf("t"), filtered(favoritesOnly = true, source = "CREATED").first().map { it.rawValue })
            assertTrue(filtered(source = "CAMERA", favoritesOnly = true).first().isEmpty())
        }

    @Test
    fun searchMatchesTitleRawValueAndNoteCaseInsensitively() =
        runBlocking {
            dao.insert(entity("https://example.com", title = "Example Site"))
            dao.insert(entity("plain", note = "Grocery LIST"))
            dao.insert(entity("other"))
            assertEquals(1, filtered(query = "example").first().size)
            assertEquals(1, filtered(query = "grocery").first().size)
            assertEquals(1, dao.search(escapeLikePattern("PLAIN")).first().size)
            assertEquals(3, filtered(query = "   ").first().size)
        }

    @Test
    fun searchTreatsPercentAndUnderscoreLiterally() =
        runBlocking {
            dao.insert(entity("100% cotton"))
            dao.insert(entity("1000 cotton"))
            dao.insert(entity("a_b"))
            dao.insert(entity("axb"))
            assertEquals(listOf("100% cotton"), filtered(query = "100%").first().map { it.rawValue })
            assertEquals(listOf("a_b"), filtered(query = "a_b").first().map { it.rawValue })
        }

    @Test
    fun updatesAffectOnlyTheGivenIds() =
        runBlocking {
            val a = dao.insert(entity("a"))
            val b = dao.insert(entity("b"))
            dao.setFavorite(listOf(a), true)
            dao.updateNote(b, "memo")
            dao.updateStyle(b, "{\"x\":1}")
            assertEquals(true, dao.getById(a)!!.isFavorite)
            assertEquals(false, dao.getById(b)!!.isFavorite)
            assertEquals("memo", dao.observeById(b).first()!!.note)
            assertEquals("{\"x\":1}", dao.getById(b)!!.styleJson)
            assertEquals(1, dao.countFavorites())
        }

    @Test
    fun clearHonoursKeepFavoritesAndSource() =
        runBlocking {
            dao.insert(entity("a", source = "CAMERA", favorite = true))
            dao.insert(entity("b", source = "CAMERA"))
            dao.insert(entity("c", source = "CREATED"))
            assertEquals(1, dao.clear(keepFavorites = true, source = "CAMERA"))
            assertEquals(2, dao.getAll().size)
            assertEquals(2, dao.clear(keepFavorites = false, source = null))
            assertTrue(dao.getAll().isEmpty())
        }

    @Test
    fun countsSplitScannedAndCreated() =
        runBlocking {
            dao.insert(entity("a", source = "CAMERA"))
            dao.insert(entity("b", source = "IMAGE"))
            dao.insert(entity("c", source = "CREATED"))
            assertEquals(2, dao.countScanned())
            assertEquals(1, dao.countCreated())
        }

    @Test
    fun deleteByIdsReturnsTheRemovedCount() =
        runBlocking {
            val a = dao.insert(entity("a"))
            val b = dao.insert(entity("b"))
            assertEquals(2, dao.deleteByIds(listOf(a, b, 999)))
        }
}

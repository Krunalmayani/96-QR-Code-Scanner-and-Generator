package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import android.content.Context
import androidx.core.content.contentValuesOf
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration scaffold. Needs the exported schemas (app/schemas/<db class>/1.json) on the androidTest assets path.
 * When AppDatabase.VERSION becomes 2: add the Migration to [Migrations.ALL], then a test here that
 * createDatabase(name, 1), inserts rows, runMigrationsAndValidate(name, 2, true, MIGRATION_1_2) and checks them.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Test
    fun version1SchemaCreatesAndKeepsRowsWhenReopenedThroughRoom() {
        helper.createDatabase(TEST_DB, 1).apply {
            insert(
                "scans",
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                contentValuesOf(
                    "rawValue" to "hello",
                    "format" to "QR_CODE",
                    "type" to "TEXT",
                    "displayTitle" to "hello",
                    "parsedJson" to "{}",
                    "createdAt" to 1L,
                    "source" to "CAMERA",
                    "isFavorite" to 0,
                    "note" to "",
                ),
            )
            close()
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(*Migrations.ALL)
            .build()
        try {
            val rows = runBlocking { db.historyDao().getAll() }
            assertEquals(1, rows.size)
            assertEquals("hello", rows.first().rawValue)
        } finally {
            db.close()
            context.deleteDatabase(TEST_DB)
        }
    }

    @Test
    fun version1SchemaValidatesAgainstTheEntities() {
        helper.createDatabase(TEST_DB, 1).close()
        helper.runMigrationsAndValidate(TEST_DB, AppDatabase.VERSION, true, *Migrations.ALL).close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}

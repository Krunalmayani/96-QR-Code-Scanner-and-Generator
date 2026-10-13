package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScanEntity::class],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        const val VERSION = 1
        const val NAME = "qr_history.db"
    }
}

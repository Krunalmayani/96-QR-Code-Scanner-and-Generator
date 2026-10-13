package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di

import android.content.Context
import androidx.room.Room
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.AppDatabase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.HistoryDao
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /** No destructive fallback: a missing migration must fail loudly instead of wiping the user's history. */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(*Migrations.ALL)
            .build()

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()
}

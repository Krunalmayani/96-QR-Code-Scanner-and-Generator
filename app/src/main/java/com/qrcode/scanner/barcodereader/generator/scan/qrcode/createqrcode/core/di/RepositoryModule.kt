package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.repository.HistoryRepositoryImpl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.settings.SettingsRepositoryImpl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

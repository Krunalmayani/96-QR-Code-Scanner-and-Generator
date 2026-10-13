package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ClearHistoryUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.DeleteRecordsUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.EnsureFirstLaunchUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Domain use cases are plain Kotlin classes (no javax.inject), so they are provided here. */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideSaveScanUseCase(history: HistoryRepository, settings: SettingsRepository): SaveScanUseCase =
        SaveScanUseCase(history, settings)

    @Provides
    fun provideToggleFavoriteUseCase(history: HistoryRepository): ToggleFavoriteUseCase =
        ToggleFavoriteUseCase(history)

    @Provides
    fun provideDeleteRecordsUseCase(history: HistoryRepository): DeleteRecordsUseCase =
        DeleteRecordsUseCase(history)

    @Provides
    fun provideClearHistoryUseCase(history: HistoryRepository): ClearHistoryUseCase =
        ClearHistoryUseCase(history)

    @Provides
    fun provideEnsureFirstLaunchUseCase(settings: SettingsRepository): EnsureFirstLaunchUseCase =
        EnsureFirstLaunchUseCase(settings)
}

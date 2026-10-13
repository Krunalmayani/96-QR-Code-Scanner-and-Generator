package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.e2e

import android.app.Activity
import android.app.Application
import android.content.Context
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdContentSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdManager
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdModule
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LoadedBanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.LoadedNative
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.NoOpAdController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.RepositoryModule
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.fakes.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system.InAppUpdater
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system.SystemModule
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system.UpdateStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

/**
 * Replaces the Room + DataStore repositories with the in-memory fakes. Tests inject [FakeHistoryRepository] and
 * [FakeSettingsRepository] to seed and inspect state; the app sees the same instances as its repository interfaces.
 * A fresh Hilt component per test means every test starts empty (onboarding already done).
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object FakeRepositoryModule {
    @Provides
    @Singleton
    fun provideFakeHistory(): FakeHistoryRepository = FakeHistoryRepository()

    @Provides
    @Singleton
    fun provideFakeSettings(): FakeSettingsRepository = FakeSettingsRepository(AppSettings(onboardingDone = true))

    @Provides
    fun provideHistoryRepository(fake: FakeHistoryRepository): HistoryRepository = fake

    @Provides
    fun provideSettingsRepository(fake: FakeSettingsRepository): SettingsRepository = fake
}

/** Ads never load or show during tests: no SDK, no consent flow, no interstitials over the UI under test. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AdModule::class])
object NoOpAdModule {
    @Provides
    @Singleton
    fun provideAdManager(): AdManager =
        object : AdManager {
            override fun initialize(application: Application) = Unit

            override fun controller(activity: Activity): AdController = NoOpAdController
        }

    @Provides
    @Singleton
    fun provideAdContentSource(): AdContentSource =
        object : AdContentSource {
            override val adsAvailable: StateFlow<Boolean> = MutableStateFlow(false)

            override suspend fun loadBanner(
                context: Context,
                widthDp: Int,
            ): LoadedBanner? = null

            override suspend fun takeNative(): LoadedNative? = null
        }
}

/** No Play In-App Update checks (and no update prompt that could cover the screen) during tests. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SystemModule::class])
object NoOpSystemModule {
    @Provides
    @Singleton
    fun provideInAppUpdater(): InAppUpdater =
        object : InAppUpdater {
            override val status: StateFlow<UpdateStatus> = MutableStateFlow(UpdateStatus.NONE)

            override fun check() = Unit

            override fun start(activity: Activity) = Unit

            override fun complete() = Unit
        }
}

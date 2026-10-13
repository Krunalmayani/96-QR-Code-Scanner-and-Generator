package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode

import android.app.Application
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.AdManager
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    @Inject lateinit var adManager: AdManager

    @Inject lateinit var analytics: Analytics

    @Inject lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        adManager.initialize(this)
        appScope.launch {
            settingsRepository.update { if (it.firstLaunchAt == 0L) it.copy(firstLaunchAt = System.currentTimeMillis()) else it }
        }
        appScope.launch {
            settingsRepository.settings.map { it.analyticsEnabled }.distinctUntilChanged().collect { analytics.setEnabled(it) }
        }
    }
}

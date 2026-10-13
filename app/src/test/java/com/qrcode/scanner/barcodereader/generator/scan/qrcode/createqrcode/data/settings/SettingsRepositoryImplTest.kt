package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SettingsRepositoryImplTest {
    @TempDir
    lateinit var dir: File

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val store by lazy {
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(dir, "settings.preferences_pb") })
    }
    private val repo by lazy { SettingsRepositoryImpl(store) }

    @AfterEach
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `empty store yields the defaults`() = runTest {
        assertEquals(AppSettings(), repo.current())
    }

    @Test
    fun `update round trips every field`() = runTest {
        val changed = AppSettings(
            vibrate = false,
            beep = true,
            flashOnStart = true,
            autoOpenLinks = true,
            continuousScan = true,
            scanAreaOnly = false,
            productSiteOrder = listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS),
            theme = ThemeMode.DARK,
            languageTag = "pt-BR",
            saveHistory = false,
            analyticsEnabled = false,
            onboardingDone = true,
            successfulScans = 5,
            completedActions = 3,
            lastRatePromptAt = 11L,
            hasRated = true,
            updateLaterUntil = 22L,
            lastInterstitialAt = 33L,
            lastBackgroundAt = 44L,
            firstLaunchAt = 55L,
        )
        repo.update { changed }
        assertEquals(changed, repo.current())
        assertEquals(changed, repo.settings.first())
    }

    @Test
    fun `update transforms the stored value`() = runTest {
        repo.update { it.copy(successfulScans = it.successfulScans + 1) }
        repo.update { it.copy(successfulScans = it.successfulScans + 1) }
        assertEquals(2, repo.current().successfulScans)
    }

    @Test
    fun `ensureFirstLaunch stamps only once`() = runTest {
        repo.ensureFirstLaunch(100L)
        repo.ensureFirstLaunch(200L)
        assertEquals(100L, repo.current().firstLaunchAt)
    }

    @Test
    fun `unknown stored theme and sites fall back safely`() = runTest {
        store.edit {
            it[SettingsKeys.THEME] = "NEON"
            it[SettingsKeys.PRODUCT_SITE_ORDER] = "BING,AMAZON,AMAZON"
        }
        val settings = repo.current()
        assertEquals(ThemeMode.SYSTEM, settings.theme)
        assertEquals(listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS), settings.productSiteOrder)
    }
}

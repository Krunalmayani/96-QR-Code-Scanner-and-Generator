package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class ProductSite { GOOGLE, OPEN_FOOD_FACTS, AMAZON }

data class AppSettings(
    // Scanning
    val vibrate: Boolean = true,
    val beep: Boolean = false,
    val flashOnStart: Boolean = false,
    val autoOpenLinks: Boolean = false,
    val continuousScan: Boolean = false,
    /** true = only the viewfinder square is analysed; false = full screen. */
    val scanAreaOnly: Boolean = true,
    val productSiteOrder: List<ProductSite> = listOf(ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS, ProductSite.AMAZON),
    // Appearance
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** BCP-47 tag such as "hi" or "pt-BR"; empty means follow the system. */
    val languageTag: String = "",
    // Privacy
    val saveHistory: Boolean = true,
    val analyticsEnabled: Boolean = true,
    // Lifecycle bookkeeping
    val onboardingDone: Boolean = false,
    val successfulScans: Int = 0,
    val completedActions: Int = 0,
    val lastRatePromptAt: Long = 0L,
    val hasRated: Boolean = false,
    val updateLaterUntil: Long = 0L,
    val lastInterstitialAt: Long = 0L,
    val lastBackgroundAt: Long = 0L,
    val firstLaunchAt: Long = 0L,
)

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun current(): AppSettings

    /** Atomically applies [transform] to the stored settings. */
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

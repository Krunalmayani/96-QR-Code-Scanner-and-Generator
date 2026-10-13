package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode

/** DataStore keys, one per [AppSettings] field. Never rename a key: it is the on-disk format. */
internal object SettingsKeys {
    val VIBRATE = booleanPreferencesKey("vibrate")
    val BEEP = booleanPreferencesKey("beep")
    val FLASH_ON_START = booleanPreferencesKey("flash_on_start")
    val AUTO_OPEN_LINKS = booleanPreferencesKey("auto_open_links")
    val CONTINUOUS_SCAN = booleanPreferencesKey("continuous_scan")
    val SCAN_AREA_ONLY = booleanPreferencesKey("scan_area_only")
    val PRODUCT_SITE_ORDER = stringPreferencesKey("product_site_order")
    val THEME = stringPreferencesKey("theme")
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    val SAVE_HISTORY = booleanPreferencesKey("save_history")
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val SUCCESSFUL_SCANS = intPreferencesKey("successful_scans")
    val COMPLETED_ACTIONS = intPreferencesKey("completed_actions")
    val LAST_RATE_PROMPT_AT = longPreferencesKey("last_rate_prompt_at")
    val HAS_RATED = booleanPreferencesKey("has_rated")
    val UPDATE_LATER_UNTIL = longPreferencesKey("update_later_until")
    val LAST_INTERSTITIAL_AT = longPreferencesKey("last_interstitial_at")
    val LAST_BACKGROUND_AT = longPreferencesKey("last_background_at")
    val FIRST_LAUNCH_AT = longPreferencesKey("first_launch_at")
}

/** Reads every field, falling back to the [AppSettings] default when a key is missing or unreadable. */
internal fun Preferences.toAppSettings(): AppSettings {
    val d = AppSettings()
    return AppSettings(
        vibrate = this[SettingsKeys.VIBRATE] ?: d.vibrate,
        beep = this[SettingsKeys.BEEP] ?: d.beep,
        flashOnStart = this[SettingsKeys.FLASH_ON_START] ?: d.flashOnStart,
        autoOpenLinks = this[SettingsKeys.AUTO_OPEN_LINKS] ?: d.autoOpenLinks,
        continuousScan = this[SettingsKeys.CONTINUOUS_SCAN] ?: d.continuousScan,
        scanAreaOnly = this[SettingsKeys.SCAN_AREA_ONLY] ?: d.scanAreaOnly,
        productSiteOrder = parseProductSiteOrder(this[SettingsKeys.PRODUCT_SITE_ORDER]),
        theme = parseThemeMode(this[SettingsKeys.THEME]) ?: d.theme,
        languageTag = this[SettingsKeys.LANGUAGE_TAG] ?: d.languageTag,
        saveHistory = this[SettingsKeys.SAVE_HISTORY] ?: d.saveHistory,
        analyticsEnabled = this[SettingsKeys.ANALYTICS_ENABLED] ?: d.analyticsEnabled,
        onboardingDone = this[SettingsKeys.ONBOARDING_DONE] ?: d.onboardingDone,
        successfulScans = this[SettingsKeys.SUCCESSFUL_SCANS] ?: d.successfulScans,
        completedActions = this[SettingsKeys.COMPLETED_ACTIONS] ?: d.completedActions,
        lastRatePromptAt = this[SettingsKeys.LAST_RATE_PROMPT_AT] ?: d.lastRatePromptAt,
        hasRated = this[SettingsKeys.HAS_RATED] ?: d.hasRated,
        updateLaterUntil = this[SettingsKeys.UPDATE_LATER_UNTIL] ?: d.updateLaterUntil,
        lastInterstitialAt = this[SettingsKeys.LAST_INTERSTITIAL_AT] ?: d.lastInterstitialAt,
        lastBackgroundAt = this[SettingsKeys.LAST_BACKGROUND_AT] ?: d.lastBackgroundAt,
        firstLaunchAt = this[SettingsKeys.FIRST_LAUNCH_AT] ?: d.firstLaunchAt,
    )
}

/** Writes every key so a stored snapshot is always complete. */
internal fun MutablePreferences.writeAppSettings(s: AppSettings) {
    this[SettingsKeys.VIBRATE] = s.vibrate
    this[SettingsKeys.BEEP] = s.beep
    this[SettingsKeys.FLASH_ON_START] = s.flashOnStart
    this[SettingsKeys.AUTO_OPEN_LINKS] = s.autoOpenLinks
    this[SettingsKeys.CONTINUOUS_SCAN] = s.continuousScan
    this[SettingsKeys.SCAN_AREA_ONLY] = s.scanAreaOnly
    this[SettingsKeys.PRODUCT_SITE_ORDER] = formatProductSiteOrder(s.productSiteOrder)
    this[SettingsKeys.THEME] = s.theme.name
    this[SettingsKeys.LANGUAGE_TAG] = s.languageTag
    this[SettingsKeys.SAVE_HISTORY] = s.saveHistory
    this[SettingsKeys.ANALYTICS_ENABLED] = s.analyticsEnabled
    this[SettingsKeys.ONBOARDING_DONE] = s.onboardingDone
    this[SettingsKeys.SUCCESSFUL_SCANS] = s.successfulScans
    this[SettingsKeys.COMPLETED_ACTIONS] = s.completedActions
    this[SettingsKeys.LAST_RATE_PROMPT_AT] = s.lastRatePromptAt
    this[SettingsKeys.HAS_RATED] = s.hasRated
    this[SettingsKeys.UPDATE_LATER_UNTIL] = s.updateLaterUntil
    this[SettingsKeys.LAST_INTERSTITIAL_AT] = s.lastInterstitialAt
    this[SettingsKeys.LAST_BACKGROUND_AT] = s.lastBackgroundAt
    this[SettingsKeys.FIRST_LAUNCH_AT] = s.firstLaunchAt
}

internal fun formatProductSiteOrder(order: List<ProductSite>): String =
    order.distinct().joinToString(",") { it.name }

/**
 * Parses a comma-joined list of [ProductSite] names. Unknown names and duplicates are dropped and any site
 * missing from the stored value is appended in enum order, so the result always holds all sites exactly once.
 */
internal fun parseProductSiteOrder(raw: String?): List<ProductSite> {
    val stored =
        raw
            .orEmpty()
            .split(',')
            .mapNotNull { name -> ProductSite.values().firstOrNull { it.name == name.trim() } }
            .distinct()
    return stored + ProductSite.values().filter { it !in stored }
}

internal fun parseThemeMode(raw: String?): ThemeMode? = ThemeMode.values().firstOrNull { it.name == raw }

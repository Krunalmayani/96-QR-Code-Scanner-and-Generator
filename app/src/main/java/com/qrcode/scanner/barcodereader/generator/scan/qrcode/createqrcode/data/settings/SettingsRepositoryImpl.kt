package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Preferences DataStore implementation of [SettingsRepository]. */
@Singleton
class SettingsRepositoryImpl internal constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    @Inject
    constructor(@ApplicationContext context: Context) : this(context.settingsDataStore)

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toAppSettings() }
        .distinctUntilChanged()

    override suspend fun current(): AppSettings = settings.first()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.writeAppSettings(transform(prefs.toAppSettings())) }
    }

    /** Stamps [AppSettings.firstLaunchAt] once, on the first call; later calls change nothing. */
    suspend fun ensureFirstLaunch(nowMillis: Long = System.currentTimeMillis()) {
        update { if (it.firstLaunchAt == 0L) it.copy(firstLaunchAt = nowMillis) else it }
    }
}

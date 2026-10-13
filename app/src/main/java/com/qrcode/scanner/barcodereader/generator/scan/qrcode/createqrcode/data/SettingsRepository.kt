package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val darkKey = booleanPreferencesKey("dark_mode")

    val darkMode: Flow<Boolean?> = context.dataStore.data.map { it[darkKey] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkKey] = enabled }
    }
}

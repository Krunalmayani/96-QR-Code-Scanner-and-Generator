package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Activity-level state: the loaded settings (null until read) and whether the splash may be dismissed. */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val settings: StateFlow<AppSettings?> =
            settingsRepository.settings
                .map<AppSettings, AppSettings?> { it }
                .stateIn(viewModelScope, SharingStarted.Eagerly, null)

        private val minimumSplash =
            flow {
                delay(MIN_SPLASH_MS)
                emit(true)
            }.onStart { emit(false) }

        /** True once settings are loaded and the splash has been visible for [MIN_SPLASH_MS]. */
        val ready: StateFlow<Boolean> =
            combine(settings.map { it != null }, minimumSplash) { loaded, waited -> loaded && waited }
                .stateIn(viewModelScope, SharingStarted.Eagerly, false)

        private companion object {
            const val MIN_SPLASH_MS = 700L
        }
    }

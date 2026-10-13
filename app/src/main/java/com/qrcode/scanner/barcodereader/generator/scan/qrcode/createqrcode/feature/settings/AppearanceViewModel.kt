package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Analytics param key for a picked option such as "DARK" or "hi" (never user content). */
internal const val PARAM_CHOICE = "choice"

data class AppearanceUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** BCP-47 tag; empty means follow the system language. */
    val languageTag: String = "",
)

sealed interface AppearanceEvent {
    data class SelectTheme(val mode: ThemeMode) : AppearanceEvent
    data class SelectLanguage(val tag: String) : AppearanceEvent
}

sealed interface AppearanceEffect {
    /** The screen applies [tag] through AppCompatDelegate right away (the shell does it again from settings). */
    data class ApplyLanguage(val tag: String) : AppearanceEffect
}

/** Backs the theme picker (s57) and the language picker (s58). Changes apply immediately. */
@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: Analytics,
) : ViewModel() {

    private val effectChannel = Channel<AppearanceEffect>(Channel.BUFFERED)

    val effects: Flow<AppearanceEffect> = effectChannel.receiveAsFlow()

    val state: StateFlow<AppearanceUiState> = settingsRepository.settings
        .map { AppearanceUiState(theme = it.theme, languageTag = it.languageTag) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceUiState())

    fun onEvent(event: AppearanceEvent) {
        when (event) {
            is AppearanceEvent.SelectTheme -> selectTheme(event.mode)
            is AppearanceEvent.SelectLanguage -> selectLanguage(event.tag)
        }
    }

    private fun selectTheme(mode: ThemeMode) {
        if (mode == state.value.theme) return
        viewModelScope.launch {
            settingsRepository.update { it.copy(theme = mode) }
            analytics.log(Events.THEME_CHANGED, mapOf(PARAM_CHOICE to mode.name))
        }
    }

    private fun selectLanguage(tag: String) {
        if (tag == state.value.languageTag) return
        viewModelScope.launch {
            try {
                settingsRepository.update { it.copy(languageTag = tag) }
                analytics.log(Events.LANGUAGE_CHANGED, mapOf(PARAM_CHOICE to tag.ifEmpty { "system" }))
                effectChannel.send(AppearanceEffect.ApplyLanguage(tag))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Settings could not be saved; the picker keeps showing the stored value.
            }
        }
    }
}

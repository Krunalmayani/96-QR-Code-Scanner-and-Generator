package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.ProductSiteOrder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ClearHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Counts and the "Keep my favorites" choice shown in the clear-history dialog (s68). */
data class ClearDialogState(
    val scanned: Int,
    val created: Int,
    val keepFavorites: Boolean = true,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    /** Non-null while the clear-history dialog is visible. */
    val clearDialog: ClearDialogState? = null,
)

sealed interface SettingsEvent {
    data class SetVibrate(val on: Boolean) : SettingsEvent
    data class SetBeep(val on: Boolean) : SettingsEvent
    data class SetFlashOnStart(val on: Boolean) : SettingsEvent
    data class SetAutoOpenLinks(val on: Boolean) : SettingsEvent
    data class SetContinuousScan(val on: Boolean) : SettingsEvent
    data class SetScanAreaOnly(val on: Boolean) : SettingsEvent
    data class MoveSite(val site: ProductSite, val up: Boolean) : SettingsEvent
    data class SetSaveHistory(val on: Boolean) : SettingsEvent
    data class SetAnalytics(val on: Boolean) : SettingsEvent
    data object ClearHistoryClicked : SettingsEvent
    data class ClearKeepFavoritesChanged(val keep: Boolean) : SettingsEvent
    data object ClearDismissed : SettingsEvent
    data object ClearConfirmed : SettingsEvent
}

sealed interface SettingsEffect {
    data class HistoryCleared(val count: Int) : SettingsEffect
    data object HistoryAlreadyEmpty : SettingsEffect
    data object Failed : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val clearHistory: ClearHistoryUseCase,
    private val analytics: Analytics,
) : ViewModel() {

    private val clearDialog = MutableStateFlow<ClearDialogState?>(null)
    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)

    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    val state: StateFlow<SettingsUiState> = combine(settingsRepository.settings, clearDialog) { settings, dialog ->
        SettingsUiState(settings = settings, clearDialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetVibrate -> update { it.copy(vibrate = event.on) }
            is SettingsEvent.SetBeep -> update { it.copy(beep = event.on) }
            is SettingsEvent.SetFlashOnStart -> update { it.copy(flashOnStart = event.on) }
            is SettingsEvent.SetAutoOpenLinks -> update { it.copy(autoOpenLinks = event.on) }
            is SettingsEvent.SetContinuousScan -> update { it.copy(continuousScan = event.on) }
            is SettingsEvent.SetScanAreaOnly -> update { it.copy(scanAreaOnly = event.on) }
            is SettingsEvent.MoveSite -> update { it.copy(productSiteOrder = reorder(it.productSiteOrder, event)) }
            is SettingsEvent.SetSaveHistory -> update { it.copy(saveHistory = event.on) }
            is SettingsEvent.SetAnalytics -> update { it.copy(analyticsEnabled = event.on) }
            SettingsEvent.ClearHistoryClicked -> openClearDialog()
            is SettingsEvent.ClearKeepFavoritesChanged ->
                clearDialog.value = clearDialog.value?.copy(keepFavorites = event.keep)
            SettingsEvent.ClearDismissed -> clearDialog.value = null
            SettingsEvent.ClearConfirmed -> confirmClear()
        }
    }

    private fun reorder(order: List<ProductSite>, event: SettingsEvent.MoveSite): List<ProductSite> {
        val current = ProductSiteOrder.normalize(order)
        return if (event.up) ProductSiteOrder.moveUp(current, event.site) else ProductSiteOrder.moveDown(current, event.site)
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.update(transform)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                effectChannel.send(SettingsEffect.Failed)
            }
        }
    }

    private fun openClearDialog() {
        viewModelScope.launch {
            try {
                val counts = historyRepository.counts()
                if (counts.scanned + counts.created == 0) {
                    effectChannel.send(SettingsEffect.HistoryAlreadyEmpty)
                } else {
                    clearDialog.value = ClearDialogState(scanned = counts.scanned, created = counts.created)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                effectChannel.send(SettingsEffect.Failed)
            }
        }
    }

    private fun confirmClear() {
        val dialog = clearDialog.value ?: return
        clearDialog.value = null
        viewModelScope.launch {
            try {
                val removed = clearHistory(keepFavorites = dialog.keepFavorites)
                analytics.log(Events.HISTORY_CLEAR)
                effectChannel.send(SettingsEffect.HistoryCleared(removed))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                effectChannel.send(SettingsEffect.Failed)
            }
        }
    }
}

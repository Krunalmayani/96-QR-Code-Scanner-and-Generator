package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.RatingAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.SystemPrompt
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.SystemPromptPicker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.UpdatePromptPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The overlay currently on screen. */
enum class ActivePrompt { NONE, RATE, UPDATE, UPDATE_READY }

/** One-shot work the host performs because it needs the Activity or the NavController. */
sealed interface SystemPromptsEffect {
    /** Show the Play In-App Review flow. */
    data object LaunchReview : SystemPromptsEffect

    /** Open the feedback form (1 to 3 stars). */
    data object OpenFeedback : SystemPromptsEffect
}

/**
 * Decides which system prompt (rate sheet, update dialog, update-ready dialog) is on screen. The decision itself
 * lives in the pure [SystemPromptPicker]; this class adds the latch (a prompt stays up until the user acts on it,
 * even though showing it changes the settings that triggered it), a short settle delay and persistence.
 */
@HiltViewModel
class SystemPromptsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: Analytics,
    private val updater: InAppUpdater,
) : ViewModel() {

    /** Clock, replaceable in tests. */
    internal var nowProvider: () -> Long = System::currentTimeMillis

    private val active = MutableStateFlow(ActivePrompt.NONE)
    val prompt: StateFlow<ActivePrompt> = active.asStateFlow()

    /** True while the user scans, reads a result or is in onboarding: no prompt may appear. Starts true. */
    private val suppressed = MutableStateFlow(true)

    private val channel = Channel<SystemPromptsEffect>(Channel.BUFFERED)
    val effects: Flow<SystemPromptsEffect> = channel.receiveAsFlow()

    private var readyDismissed = false

    private data class Snapshot(
        val settings: AppSettings,
        val suppressed: Boolean,
        val updateStatus: UpdateStatus,
        val current: ActivePrompt,
    )

    init {
        viewModelScope.launch {
            combine(settingsRepository.settings, suppressed, updater.status, active) { settings, quiet, status, current ->
                Snapshot(settings, quiet, status, current)
            }.collectLatest { snapshot -> evaluate(snapshot) }
        }
    }

    /** The host reports whether prompts must stay hidden (scanning, result, onboarding). */
    fun setSuppressed(value: Boolean) {
        suppressed.value = value
    }

    /** Asks Play for an update; call when the app comes to the foreground. */
    fun refreshUpdate() {
        updater.check()
    }

    fun onRateConfirmed(stars: Int) {
        if (active.value != ActivePrompt.RATE) return
        val action = RatingAction.forStars(stars) ?: return
        active.value = ActivePrompt.NONE
        when (action) {
            RatingAction.IN_APP_REVIEW -> {
                viewModelScope.launch { settingsRepository.update { it.copy(hasRated = true) } }
                channel.trySend(SystemPromptsEffect.LaunchReview)
            }
            RatingAction.FEEDBACK -> channel.trySend(SystemPromptsEffect.OpenFeedback)
        }
    }

    /** "Maybe later", swipe down or back. The 60-day timer already started when the sheet was shown. */
    fun onRateDismissed() {
        if (active.value == ActivePrompt.RATE) active.value = ActivePrompt.NONE
    }

    /** [activity] is null only when the host has no Activity; the prompt then just closes. */
    fun onUpdateConfirmed(activity: Activity?) {
        if (active.value != ActivePrompt.UPDATE) return
        active.value = ActivePrompt.NONE
        if (activity != null) updater.start(activity)
    }

    /** "Later", back or outside tap: hide the update prompt for 3 days. */
    fun onUpdateLater() {
        if (active.value != ActivePrompt.UPDATE) return
        active.value = ActivePrompt.NONE
        val until = UpdatePromptPolicy.laterUntil(nowProvider())
        viewModelScope.launch { settingsRepository.update { it.copy(updateLaterUntil = until) } }
    }

    fun onUpdateReadyConfirmed() {
        if (active.value != ActivePrompt.UPDATE_READY) return
        readyDismissed = true
        active.value = ActivePrompt.NONE
        updater.complete()
    }

    /** The downloaded update installs on a later app start anyway; do not ask again this session. */
    fun onUpdateReadyDismissed() {
        if (active.value != ActivePrompt.UPDATE_READY) return
        readyDismissed = true
        active.value = ActivePrompt.NONE
    }

    private suspend fun evaluate(snapshot: Snapshot) {
        if (snapshot.current != ActivePrompt.NONE) return
        val next = decide(snapshot) ?: return
        // collectLatest drops this wait when any input changes, so the prompt only appears after things settle.
        delay(SETTLE_MILLIS)
        show(next)
    }

    private fun decide(snapshot: Snapshot): ActivePrompt? {
        val settings = snapshot.settings
        val ready = snapshot.updateStatus == UpdateStatus.DOWNLOADED && !readyDismissed &&
            !snapshot.suppressed && settings.onboardingDone
        if (ready) return ActivePrompt.UPDATE_READY
        val pick = SystemPromptPicker.next(
            settings = settings,
            nowMillis = nowProvider(),
            scanningInProgress = snapshot.suppressed,
            updateAvailable = snapshot.updateStatus == UpdateStatus.AVAILABLE,
        )
        return when (pick) {
            SystemPrompt.RATE -> ActivePrompt.RATE
            SystemPrompt.UPDATE -> ActivePrompt.UPDATE
            SystemPrompt.NONE -> null
        }
    }

    private fun show(next: ActivePrompt) {
        active.value = next
        when (next) {
            ActivePrompt.RATE -> {
                val now = nowProvider()
                viewModelScope.launch { settingsRepository.update { it.copy(lastRatePromptAt = now) } }
                analytics.log(Events.RATE_PROMPT_SHOWN)
            }
            ActivePrompt.UPDATE -> analytics.log(Events.UPDATE_PROMPT_SHOWN)
            ActivePrompt.UPDATE_READY, ActivePrompt.NONE -> Unit
        }
    }

    private companion object {
        const val SETTLE_MILLIS = 1_500L
    }
}

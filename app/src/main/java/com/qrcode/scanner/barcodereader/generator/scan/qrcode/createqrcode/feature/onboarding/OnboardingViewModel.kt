package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** How the camera rationale step ended. Only the name is logged. */
enum class CameraStepResult { GRANTED, DENIED, SKIPPED }

sealed interface OnboardingEffect {
    /** Onboarding is stored as done: leave the onboarding screens for Scan. */
    data object Finished : OnboardingEffect
}

/** Completes onboarding after the camera rationale step: persists the flag, logs it, asks the UI to leave. */
@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val analytics: Analytics,
    ) : ViewModel() {
        private val channel = Channel<OnboardingEffect>(Channel.BUFFERED)
        val effects: Flow<OnboardingEffect> = channel.receiveAsFlow()

        private var finishing = false

        /** Safe to call more than once; only the first call has an effect. */
        fun finish(result: CameraStepResult) {
            if (finishing) return
            finishing = true
            viewModelScope.launch {
                settingsRepository.update { it.copy(onboardingDone = true) }
                analytics.log(Events.ONBOARDING_DONE, mapOf(Params.RESULT to result.name))
                channel.send(OnboardingEffect.Finished)
            }
        }
    }

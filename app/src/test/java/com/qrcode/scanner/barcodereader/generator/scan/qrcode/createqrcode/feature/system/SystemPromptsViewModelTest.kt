package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.SystemPromptTiming
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SystemPromptsViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val now = 1_800_000_000_000L
    private val rateReady = AppSettings(onboardingDone = true, successfulScans = 5)
    private val analytics = mockk<Analytics>(relaxed = true)
    private val updater = FakeUpdater()

    private class FakeUpdater : InAppUpdater {
        val state = MutableStateFlow(UpdateStatus.NONE)
        override val status: StateFlow<UpdateStatus> = state
        var checks = 0
        var starts = 0
        var completes = 0

        override fun check() {
            checks++
        }

        override fun start(activity: Activity) {
            starts++
        }

        override fun complete() {
            completes++
        }
    }

    private fun viewModel(settings: FakeSettingsRepository): SystemPromptsViewModel {
        val viewModel = SystemPromptsViewModel(settings, analytics, updater)
        viewModel.nowProvider = { now }
        return viewModel
    }

    private fun TestScope.settle() {
        advanceUntilIdle()
        runCurrent()
    }

    @Test
    fun `nothing is shown while prompts are suppressed`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)

        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
    }

    @Test
    fun `the rate sheet appears after the settle delay and starts the 60 day timer`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)

        viewModel.setSuppressed(false)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)

        advanceTimeBy(600)
        runCurrent()
        assertEquals(ActivePrompt.RATE, viewModel.prompt.value)
        assertEquals(now, settings.value.lastRatePromptAt)
        verify(exactly = 1) { analytics.log(Events.RATE_PROMPT_SHOWN, emptyMap()) }
    }

    @Test
    fun `the rate sheet is cancelled when the user starts scanning during the delay`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)

        viewModel.setSuppressed(false)
        advanceTimeBy(1_000)
        runCurrent()
        viewModel.setSuppressed(true)
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(0L, settings.value.lastRatePromptAt)
    }

    @Test
    fun `the rate sheet does not appear before five scans`() = runTest {
        val settings = FakeSettingsRepository(rateReady.copy(successfulScans = 4))
        val viewModel = viewModel(settings)

        viewModel.setSuppressed(false)
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
    }

    @Test
    fun `four or five stars launch the in-app review and mark the app as rated`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        settle()
        assertEquals(ActivePrompt.RATE, viewModel.prompt.value)

        viewModel.effects.test {
            viewModel.onRateConfirmed(5)
            assertEquals(SystemPromptsEffect.LaunchReview, awaitItem())
        }

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertTrue(settings.value.hasRated)
    }

    @Test
    fun `one to three stars open the feedback form and do not mark the app as rated`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        settle()

        viewModel.effects.test {
            viewModel.onRateConfirmed(2)
            assertEquals(SystemPromptsEffect.OpenFeedback, awaitItem())
        }

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertFalse(settings.value.hasRated)
    }

    @Test
    fun `an invalid star count keeps the sheet open`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        settle()

        viewModel.onRateConfirmed(0)

        assertEquals(ActivePrompt.RATE, viewModel.prompt.value)
    }

    @Test
    fun `maybe later closes the sheet and it does not come back`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        settle()

        viewModel.onRateDismissed()
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertFalse(settings.value.hasRated)
        assertEquals(now, settings.value.lastRatePromptAt)
    }

    @Test
    fun `the update dialog appears when an update is available`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE

        viewModel.setSuppressed(false)
        settle()

        assertEquals(ActivePrompt.UPDATE, viewModel.prompt.value)
        verify(exactly = 1) { analytics.log(Events.UPDATE_PROMPT_SHOWN, emptyMap()) }
    }

    @Test
    fun `nothing is shown when Play reports no update`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)

        viewModel.setSuppressed(false)
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
    }

    @Test
    fun `later hides the update prompt for three days`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE
        viewModel.setSuppressed(false)
        settle()

        viewModel.onUpdateLater()
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(now + SystemPromptTiming.UPDATE_SNOOZE_MILLIS, settings.value.updateLaterUntil)
    }

    @Test
    fun `confirming the update without an activity just closes the dialog`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE
        viewModel.setSuppressed(false)
        settle()

        viewModel.onUpdateConfirmed(null)

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(0, updater.starts)
    }

    @Test
    fun `confirming the update starts the flexible flow`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE
        viewModel.setSuppressed(false)
        settle()

        viewModel.onUpdateConfirmed(mockk<Activity>(relaxed = true))

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(1, updater.starts)
    }

    @Test
    fun `the rate sheet goes first and the update dialog follows once it is closed`() = runTest {
        val settings = FakeSettingsRepository(rateReady)
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE
        viewModel.setSuppressed(false)
        settle()
        assertEquals(ActivePrompt.RATE, viewModel.prompt.value)

        viewModel.onRateDismissed()
        settle()

        assertEquals(ActivePrompt.UPDATE, viewModel.prompt.value)
    }

    @Test
    fun `a downloaded update asks for a restart`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        updater.state.value = UpdateStatus.DOWNLOADED
        settle()
        assertEquals(ActivePrompt.UPDATE_READY, viewModel.prompt.value)

        viewModel.onUpdateReadyConfirmed()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(1, updater.completes)
    }

    @Test
    fun `dismissing the restart prompt does not ask again`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = true))
        val viewModel = viewModel(settings)
        viewModel.setSuppressed(false)
        updater.state.value = UpdateStatus.DOWNLOADED
        settle()

        viewModel.onUpdateReadyDismissed()
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
        assertEquals(0, updater.completes)
    }

    @Test
    fun `nothing is shown before onboarding is done`() = runTest {
        val settings = FakeSettingsRepository(AppSettings(onboardingDone = false, successfulScans = 9))
        val viewModel = viewModel(settings)
        updater.state.value = UpdateStatus.AVAILABLE

        viewModel.setSuppressed(false)
        settle()

        assertEquals(ActivePrompt.NONE, viewModel.prompt.value)
    }

    @Test
    fun `refreshing the update asks the updater to check`() = runTest {
        val viewModel = viewModel(FakeSettingsRepository())

        viewModel.refreshUpdate()

        assertEquals(1, updater.checks)
    }
}

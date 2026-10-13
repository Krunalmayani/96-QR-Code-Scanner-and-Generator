package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import app.cash.turbine.test
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OnboardingViewModelTest {

    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val settings = FakeSettingsRepository()
    private val analytics = mockk<Analytics>(relaxed = true)

    @Test
    fun `finish stores the flag, logs the event and asks the UI to leave`() = runTest {
        val viewModel = OnboardingViewModel(settings, analytics)
        assertFalse(settings.value.onboardingDone)

        viewModel.effects.test {
            viewModel.finish(CameraStepResult.GRANTED)
            assertEquals(OnboardingEffect.Finished, awaitItem())
        }

        assertTrue(settings.value.onboardingDone)
        verify(exactly = 1) { analytics.log(Events.ONBOARDING_DONE, mapOf(Params.RESULT to "GRANTED")) }
    }

    @Test
    fun `skipping the camera step still completes onboarding`() = runTest {
        val viewModel = OnboardingViewModel(settings, analytics)

        viewModel.effects.test {
            viewModel.finish(CameraStepResult.SKIPPED)
            assertEquals(OnboardingEffect.Finished, awaitItem())
        }

        assertTrue(settings.value.onboardingDone)
        verify(exactly = 1) { analytics.log(Events.ONBOARDING_DONE, mapOf(Params.RESULT to "SKIPPED")) }
    }

    @Test
    fun `a second finish call is ignored`() = runTest {
        val viewModel = OnboardingViewModel(settings, analytics)

        viewModel.effects.test {
            viewModel.finish(CameraStepResult.DENIED)
            viewModel.finish(CameraStepResult.GRANTED)
            assertEquals(OnboardingEffect.Finished, awaitItem())
            expectNoEvents()
        }

        verify(exactly = 1) { analytics.log(Events.ONBOARDING_DONE, any()) }
    }
}

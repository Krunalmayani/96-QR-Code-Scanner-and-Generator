package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackTopic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testing.MainDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackViewModelTest {
    @JvmField
    @RegisterExtension
    val main = MainDispatcherExtension()

    private val analytics = mockk<Analytics>(relaxed = true)
    private val send = FeedbackEvent.Send("support@example.com", "1.0.0 (1)", "Pixel, Android 15")

    @Test
    fun `empty form cannot be sent and sends nothing`() = runTest {
        val vm = FeedbackViewModel(analytics)
        assertFalse(vm.state.value.canSend)
        vm.effects.test {
            vm.onEvent(send)
            expectNoEvents()
        }
        verify(exactly = 0) { analytics.log(Events.FEEDBACK_SENT, any()) }
    }

    @Test
    fun `a rating alone is enough and the topic defaults to scanning`() {
        val vm = FeedbackViewModel(analytics)
        vm.onEvent(FeedbackEvent.SetRating(3))
        assertTrue(vm.state.value.canSend)
        assertEquals(FeedbackTopic.SCANNING, vm.state.value.topic)
    }

    @Test
    fun `rating is clamped to zero through five`() {
        val vm = FeedbackViewModel(analytics)
        vm.onEvent(FeedbackEvent.SetRating(9))
        assertEquals(5, vm.state.value.rating)
        vm.onEvent(FeedbackEvent.SetRating(-2))
        assertEquals(0, vm.state.value.rating)
    }

    @Test
    fun `a bad e-mail shows an error after focus is lost and blocks sending`() {
        val vm = FeedbackViewModel(analytics)
        vm.onEvent(FeedbackEvent.MessageChanged("Hello"))
        vm.onEvent(FeedbackEvent.EmailChanged("nope"))
        assertFalse(vm.state.value.emailError)
        vm.onEvent(FeedbackEvent.EmailFocusLost)
        assertTrue(vm.state.value.emailError)
        assertFalse(vm.state.value.canSend)
        vm.onEvent(FeedbackEvent.EmailChanged("me@example.com"))
        assertFalse(vm.state.value.emailError)
        assertTrue(vm.state.value.canSend)
    }

    @Test
    fun `send opens a mailto link with the topic rating and message`() = runTest {
        val vm = FeedbackViewModel(analytics)
        vm.onEvent(FeedbackEvent.SetRating(2))
        vm.onEvent(FeedbackEvent.SelectTopic(FeedbackTopic.DESIGN))
        vm.onEvent(FeedbackEvent.MessageChanged("Too dark"))
        vm.effects.test {
            vm.onEvent(send)
            val uri = (awaitItem() as FeedbackEffect.OpenMail).mailtoUri
            assertTrue(uri.startsWith("mailto:support@example.com?subject=App%20feedback%3A%20Design%20%282%2F5%29&body="))
            assertTrue(uri.contains("Too%20dark"))
            assertTrue(uri.contains("Rating%3A%202%2F5"))
        }
    }

    @Test
    fun `send logs the event with topic and stars but never the text`() = runTest {
        val vm = FeedbackViewModel(analytics)
        vm.onEvent(FeedbackEvent.SetRating(5))
        vm.onEvent(FeedbackEvent.MessageChanged("secret words"))
        vm.effects.test {
            vm.onEvent(send)
            awaitItem()
        }
        verify {
            analytics.log(
                Events.FEEDBACK_SENT,
                mapOf(Params.TYPE to "SCANNING", Params.RESULT to "5"),
            )
        }
    }
}

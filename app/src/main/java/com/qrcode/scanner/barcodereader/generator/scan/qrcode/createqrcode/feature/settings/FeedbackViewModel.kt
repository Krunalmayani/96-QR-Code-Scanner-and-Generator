package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.lifecycle.ViewModel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackMail
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FeedbackUiState(
    /** 0 = not rated, otherwise 1..5. */
    val rating: Int = 0,
    val topic: FeedbackTopic = FeedbackTopic.SCANNING,
    val message: String = "",
    val email: String = "",
    /** True once the e-mail field lost focus or Send was tried with a bad address. */
    val emailChecked: Boolean = false,
) {
    val emailError: Boolean get() = emailChecked && !FeedbackMail.isAcceptableReplyEmail(email)
    val canSend: Boolean get() = FeedbackMail.canSend(rating, message, email)
}

sealed interface FeedbackEvent {
    data class SetRating(
        val stars: Int,
    ) : FeedbackEvent

    data class SelectTopic(
        val topic: FeedbackTopic,
    ) : FeedbackEvent

    data class MessageChanged(
        val text: String,
    ) : FeedbackEvent

    data class EmailChanged(
        val text: String,
    ) : FeedbackEvent

    data object EmailFocusLost : FeedbackEvent

    /** [recipient], [appInfo] and [deviceInfo] come from resources and BuildConfig, which the ViewModel does not read. */
    data class Send(
        val recipient: String,
        val appInfo: String,
        val deviceInfo: String,
    ) : FeedbackEvent
}

sealed interface FeedbackEffect {
    /** The screen opens this `mailto:` link in the user's mail app; nothing is sent without their tap there. */
    data class OpenMail(
        val mailtoUri: String,
    ) : FeedbackEffect
}

@HiltViewModel
class FeedbackViewModel
    @Inject
    constructor(
        private val analytics: Analytics,
    ) : ViewModel() {
        private val _state = MutableStateFlow(FeedbackUiState())
        val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

        private val effectChannel = Channel<FeedbackEffect>(Channel.BUFFERED)
        val effects: Flow<FeedbackEffect> = effectChannel.receiveAsFlow()

        fun onEvent(event: FeedbackEvent) {
            when (event) {
                is FeedbackEvent.SetRating -> _state.update { it.copy(rating = event.stars.coerceIn(0, FeedbackMail.MAX_STARS)) }
                is FeedbackEvent.SelectTopic -> _state.update { it.copy(topic = event.topic) }
                is FeedbackEvent.MessageChanged ->
                    _state.update { it.copy(message = event.text.take(FeedbackMail.MAX_MESSAGE_LENGTH)) }
                is FeedbackEvent.EmailChanged -> _state.update { it.copy(email = event.text) }
                FeedbackEvent.EmailFocusLost -> _state.update { it.copy(emailChecked = true) }
                is FeedbackEvent.Send -> send(event)
            }
        }

        private fun send(event: FeedbackEvent.Send) {
            _state.update { it.copy(emailChecked = true) }
            val current = _state.value
            if (!current.canSend) return
            val uri =
                FeedbackMail.mailtoUri(
                    recipient = event.recipient,
                    subject = FeedbackMail.subject(current.topic, current.rating),
                    body =
                        FeedbackMail.body(
                            message = current.message,
                            rating = current.rating,
                            topic = current.topic,
                            replyEmail = current.email,
                            appInfo = event.appInfo,
                            deviceInfo = event.deviceInfo,
                        ),
                )
            analytics.log(
                Events.FEEDBACK_SENT,
                mapOf(Params.TYPE to current.topic.name, Params.RESULT to current.rating.toString()),
            )
            effectChannel.trySend(FeedbackEffect.OpenMail(uri))
        }
    }

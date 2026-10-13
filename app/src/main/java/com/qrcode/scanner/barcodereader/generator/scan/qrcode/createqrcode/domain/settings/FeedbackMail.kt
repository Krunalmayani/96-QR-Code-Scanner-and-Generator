package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import java.net.URLEncoder

/** Topic chips on the feedback form. [mailLabel] is the English name written into the e-mail for the developer. */
enum class FeedbackTopic(val mailLabel: String) {
    SCANNING("Scanning"),
    CREATING("Creating"),
    DESIGN("Design"),
    OTHER("Other"),
}

/** Builds the `mailto:` link behind "Send feedback". No network, no content from the user's history. */
object FeedbackMail {
    const val MAX_MESSAGE_LENGTH = 2000
    const val MAX_STARS = 5

    private val EMAIL = Regex("^[^@\\s,;<>]+@[^@\\s,;<>]+\\.[^@\\s,;<>]+$")

    /** A blank reply address is fine (it is optional); otherwise it must look like an e-mail address. */
    fun isAcceptableReplyEmail(value: String): Boolean = value.isBlank() || EMAIL.matches(value.trim())

    /** The form can be sent with a star rating, a message, or both. */
    fun canSend(rating: Int, message: String, replyEmail: String): Boolean =
        (rating in 1..MAX_STARS || message.isNotBlank()) && isAcceptableReplyEmail(replyEmail)

    fun subject(topic: FeedbackTopic, rating: Int): String {
        val stars = if (rating in 1..MAX_STARS) " ($rating/$MAX_STARS)" else ""
        return "App feedback: ${topic.mailLabel}$stars"
    }

    fun body(
        message: String,
        rating: Int,
        topic: FeedbackTopic,
        replyEmail: String,
        appInfo: String,
        deviceInfo: String,
    ): String = buildString {
        append(message.trim().take(MAX_MESSAGE_LENGTH))
        append("\n\n---\n")
        append("Topic: ").append(topic.mailLabel).append('\n')
        if (rating in 1..MAX_STARS) append("Rating: ").append(rating).append('/').append(MAX_STARS).append('\n')
        if (replyEmail.isNotBlank()) append("Reply to: ").append(replyEmail.trim()).append('\n')
        append("App: ").append(appInfo).append('\n')
        append("Device: ").append(deviceInfo)
    }

    /** `mailto:` URI string with percent-encoded subject and body (spaces as %20, not +). */
    fun mailtoUri(recipient: String, subject: String, body: String): String {
        val to = recipient.filter { !it.isWhitespace() && it != '?' && it != '&' && it != '#' && it != ',' }
        return "mailto:$to?subject=${encode(subject.replace('\n', ' ').replace('\r', ' '))}&body=${encode(body)}"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}

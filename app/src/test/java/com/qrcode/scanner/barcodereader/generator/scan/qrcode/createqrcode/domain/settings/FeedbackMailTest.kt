package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FeedbackMailTest {
    @Test
    fun `reply e-mail is optional but must look valid when present`() {
        assertTrue(FeedbackMail.isAcceptableReplyEmail(""))
        assertTrue(FeedbackMail.isAcceptableReplyEmail("   "))
        assertTrue(FeedbackMail.isAcceptableReplyEmail("you@example.com"))
        assertTrue(FeedbackMail.isAcceptableReplyEmail(" you@example.com "))
        assertFalse(FeedbackMail.isAcceptableReplyEmail("you@example"))
        assertFalse(FeedbackMail.isAcceptableReplyEmail("you example.com"))
        assertFalse(FeedbackMail.isAcceptableReplyEmail("a@b.com, c@d.com"))
    }

    @Test
    fun `can send needs a rating or a message and a valid reply address`() {
        assertFalse(FeedbackMail.canSend(0, "  ", ""))
        assertTrue(FeedbackMail.canSend(3, "", ""))
        assertTrue(FeedbackMail.canSend(0, "Great", ""))
        assertFalse(FeedbackMail.canSend(5, "Great", "not-an-email"))
        assertFalse(FeedbackMail.canSend(6, "", ""))
    }

    @Test
    fun `subject shows topic and stars only when rated`() {
        assertEquals("App feedback: Scanning (4/5)", FeedbackMail.subject(FeedbackTopic.SCANNING, 4))
        assertEquals("App feedback: Other", FeedbackMail.subject(FeedbackTopic.OTHER, 0))
    }

    @Test
    fun `body contains message and diagnostics but omits empty parts`() {
        val body = FeedbackMail.body("  Slow in the dark ", 2, FeedbackTopic.DESIGN, "", "1.0.0 (1)", "Pixel, Android 15")
        assertEquals(
            "Slow in the dark\n\n---\nTopic: Design\nRating: 2/5\nApp: 1.0.0 (1)\nDevice: Pixel, Android 15",
            body,
        )
        val withEmail = FeedbackMail.body("Hi", 0, FeedbackTopic.OTHER, " me@example.com ", "a", "b")
        assertTrue(withEmail.contains("Reply to: me@example.com\n"))
        assertFalse(withEmail.contains("Rating:"))
    }

    @Test
    fun `body truncates very long messages`() {
        val body = FeedbackMail.body("x".repeat(5000), 0, FeedbackTopic.OTHER, "", "a", "b")
        assertEquals(FeedbackMail.MAX_MESSAGE_LENGTH, body.substringBefore("\n\n---").length)
    }

    @Test
    fun `mailto uri encodes spaces and special characters`() {
        val uri = FeedbackMail.mailtoUri("support@example.com", "A & B", "Line 1\nLine 2 = 100%")
        assertEquals("mailto:support@example.com?subject=A%20%26%20B&body=Line%201%0ALine%202%20%3D%20100%25", uri)
    }

    @Test
    fun `mailto uri cannot be redirected through the recipient or subject`() {
        val uri = FeedbackMail.mailtoUri(" a@b.com?bcc=x@y.com&cc=z ", "Hi\r\nBcc: evil@x.com", "b")
        assertTrue(uri.startsWith("mailto:a@b.combcc=x@y.comcc=z?subject="))
        assertFalse(uri.contains("%0A"))
        assertFalse(uri.contains("%0D"))
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdatePromptPolicyTest {

    private val day = SystemPromptTiming.MILLIS_PER_DAY
    private val now = 1_800_000_000_000L
    private val ready = AppSettings(onboardingDone = true)

    @Test
    fun `shows when an update is available`() {
        assertTrue(UpdatePromptPolicy.shouldShow(ready, now, updateAvailable = true))
    }

    @Test
    fun `shows nothing when no update is available`() {
        assertFalse(UpdatePromptPolicy.shouldShow(ready, now, updateAvailable = false))
    }

    @Test
    fun `hidden until the later deadline`() {
        val settings = ready.copy(updateLaterUntil = now + 1)
        assertFalse(UpdatePromptPolicy.shouldShow(settings, now, true))
    }

    @Test
    fun `shown again exactly at the later deadline`() {
        val settings = ready.copy(updateLaterUntil = now)
        assertTrue(UpdatePromptPolicy.shouldShow(settings, now, true))
    }

    @Test
    fun `shown after the later deadline passed`() {
        val settings = ready.copy(updateLaterUntil = now - day)
        assertTrue(UpdatePromptPolicy.shouldShow(settings, now, true))
    }

    @Test
    fun `later hides the prompt for 3 days`() {
        val until = UpdatePromptPolicy.laterUntil(now)
        assertEquals(now + 3 * day, until)
        val settings = ready.copy(updateLaterUntil = until)
        assertFalse(UpdatePromptPolicy.shouldShow(settings, now, true))
        assertFalse(UpdatePromptPolicy.shouldShow(settings, now + 3 * day - 1, true))
        assertTrue(UpdatePromptPolicy.shouldShow(settings, now + 3 * day, true))
    }

    @Test
    fun `never shows before onboarding is done`() {
        assertFalse(UpdatePromptPolicy.shouldShow(AppSettings(onboardingDone = false), now, true))
    }

    @Test
    fun `later deadline does not matter when nothing is available`() {
        assertFalse(UpdatePromptPolicy.shouldShow(ready.copy(updateLaterUntil = 0L), now, false))
    }
}

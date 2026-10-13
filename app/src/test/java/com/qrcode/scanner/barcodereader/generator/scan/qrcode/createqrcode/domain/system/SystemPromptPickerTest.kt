package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SystemPromptPickerTest {

    private val now = 1_800_000_000_000L
    private val rateReady = AppSettings(onboardingDone = true, successfulScans = 6)
    private val quiet = AppSettings(onboardingDone = true)

    @Test
    fun `nothing to show by default`() {
        assertEquals(SystemPrompt.NONE, SystemPromptPicker.next(quiet, now, false, false))
    }

    @Test
    fun `rate sheet when eligible`() {
        assertEquals(SystemPrompt.RATE, SystemPromptPicker.next(rateReady, now, false, false))
    }

    @Test
    fun `rate wins over update`() {
        assertEquals(SystemPrompt.RATE, SystemPromptPicker.next(rateReady, now, false, true))
    }

    @Test
    fun `update when only an update is available`() {
        assertEquals(SystemPrompt.UPDATE, SystemPromptPicker.next(quiet, now, false, true))
    }

    @Test
    fun `update falls through when the rate sheet is not eligible`() {
        val rated = rateReady.copy(hasRated = true)
        assertEquals(SystemPrompt.UPDATE, SystemPromptPicker.next(rated, now, false, true))
    }

    @Test
    fun `nothing while scanning`() {
        assertEquals(SystemPrompt.NONE, SystemPromptPicker.next(rateReady, now, true, true))
    }

    @Test
    fun `nothing before onboarding is done`() {
        val fresh = AppSettings(onboardingDone = false, successfulScans = 10)
        assertEquals(SystemPrompt.NONE, SystemPromptPicker.next(fresh, now, false, true))
    }

    @Test
    fun `update snoozed`() {
        val snoozed = quiet.copy(updateLaterUntil = now + 1)
        assertEquals(SystemPrompt.NONE, SystemPromptPicker.next(snoozed, now, false, true))
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RatePromptPolicyTest {

    private val day = SystemPromptTiming.MILLIS_PER_DAY
    private val now = 1_800_000_000_000L

    private fun eligible() = AppSettings(onboardingDone = true, successfulScans = 5, hasRated = false, lastRatePromptAt = 0L)

    @Test
    fun `shows after the fifth successful scan`() {
        assertTrue(RatePromptPolicy.shouldShow(eligible(), now, scanningInProgress = false))
    }

    @Test
    fun `does not show before the fifth scan`() {
        for (scans in 0..4) {
            assertFalse(RatePromptPolicy.shouldShow(eligible().copy(successfulScans = scans), now, false), "scans=$scans")
        }
    }

    @Test
    fun `shows with many scans`() {
        assertTrue(RatePromptPolicy.shouldShow(eligible().copy(successfulScans = 500), now, false))
    }

    @Test
    fun `never shows while scanning`() {
        assertFalse(RatePromptPolicy.shouldShow(eligible(), now, scanningInProgress = true))
    }

    @Test
    fun `never shows before onboarding is done`() {
        assertFalse(RatePromptPolicy.shouldShow(eligible().copy(onboardingDone = false), now, false))
    }

    @Test
    fun `never shows after the user rated`() {
        assertFalse(RatePromptPolicy.shouldShow(eligible().copy(hasRated = true), now, false))
    }

    @Test
    fun `does not show again within 60 days`() {
        val settings = eligible().copy(lastRatePromptAt = now - 59 * day)
        assertFalse(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `does not show one millisecond before 60 days`() {
        val settings = eligible().copy(lastRatePromptAt = now - 60 * day + 1)
        assertFalse(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `shows exactly 60 days after the last prompt`() {
        val settings = eligible().copy(lastRatePromptAt = now - 60 * day)
        assertTrue(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `shows long after the last prompt`() {
        val settings = eligible().copy(lastRatePromptAt = now - 400 * day)
        assertTrue(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `does not show right after a prompt`() {
        val settings = eligible().copy(lastRatePromptAt = now)
        assertFalse(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `a prompt time in the future counts as not elapsed`() {
        val settings = eligible().copy(lastRatePromptAt = now + day)
        assertFalse(RatePromptPolicy.shouldShow(settings, now, false))
    }

    @Test
    fun `default settings never qualify`() {
        assertFalse(RatePromptPolicy.shouldShow(AppSettings(), now, false))
    }

    @Test
    fun `every condition must hold`() {
        val ok = eligible()
        assertTrue(RatePromptPolicy.shouldShow(ok, now, false))
        assertFalse(RatePromptPolicy.shouldShow(ok.copy(hasRated = true, successfulScans = 1), now, true))
    }

    @Test
    fun `constants match the PRD`() {
        assertEquals(5, SystemPromptTiming.RATE_MIN_SUCCESSFUL_SCANS)
        assertEquals(60L * 86_400_000L, SystemPromptTiming.RATE_INTERVAL_MILLIS)
        assertEquals(3L * 86_400_000L, SystemPromptTiming.UPDATE_SNOOZE_MILLIS)
    }

    @Test
    fun `4 and 5 stars go to the in-app review`() {
        assertEquals(RatingAction.IN_APP_REVIEW, RatingAction.forStars(4))
        assertEquals(RatingAction.IN_APP_REVIEW, RatingAction.forStars(5))
    }

    @Test
    fun `1 to 3 stars go to feedback`() {
        for (stars in 1..3) assertEquals(RatingAction.FEEDBACK, RatingAction.forStars(stars), "stars=$stars")
    }

    @Test
    fun `stars outside 1 to 5 have no action`() {
        assertNull(RatingAction.forStars(0))
        assertNull(RatingAction.forStars(6))
        assertNull(RatingAction.forStars(-1))
    }
}

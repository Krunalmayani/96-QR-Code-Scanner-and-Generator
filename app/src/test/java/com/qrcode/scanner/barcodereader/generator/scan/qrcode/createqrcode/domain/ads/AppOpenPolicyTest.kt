package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppOpenPolicyTest {
    private val minute = 60_000L
    private val now = 10_000_000_000L
    private val policy = AppOpenPolicy()

    private fun input(
        adsAllowed: Boolean = true,
        onboardingDone: Boolean = true,
        nowMs: Long = now,
        lastBackground: Long = now - 31 * minute,
        ready: Boolean = true,
        showing: Boolean = false,
    ) = AppOpenInput(adsAllowed, onboardingDone, nowMs, lastBackground, ready, showing)

    @Test
    fun `shows after more than 30 minutes away`() {
        assertTrue(policy.shouldShow(input()))
    }

    @Test
    fun `threshold is inclusive at exactly 30 minutes`() {
        assertTrue(policy.shouldShow(input(lastBackground = now - 30 * minute)))
        assertFalse(policy.shouldShow(input(lastBackground = now - 30 * minute + 1)))
    }

    @Test
    fun `short trips to another app do not trigger an ad`() {
        assertFalse(policy.shouldShow(input(lastBackground = now - 29 * minute)))
        assertFalse(policy.shouldShow(input(lastBackground = now - 1_000L)))
    }

    @Test
    fun `not on a true cold start where the app never went to background`() {
        assertFalse(policy.shouldShow(input(lastBackground = 0L)))
    }

    @Test
    fun `not before onboarding is done even after a long absence`() {
        assertFalse(policy.shouldShow(input(onboardingDone = false, lastBackground = now - 600 * minute)))
    }

    @Test
    fun `cold start after onboarding with a long absence is allowed`() {
        assertTrue(policy.shouldShow(input(onboardingDone = true, lastBackground = now - 600 * minute)))
    }

    @Test
    fun `blocked when ads are not allowed`() {
        assertFalse(policy.shouldShow(input(adsAllowed = false)))
    }

    @Test
    fun `blocked when no ad is ready`() {
        assertFalse(policy.shouldShow(input(ready = false)))
    }

    @Test
    fun `blocked while another full screen ad is showing`() {
        assertFalse(policy.shouldShow(input(showing = true)))
    }

    @Test
    fun `background time in the future is ignored`() {
        assertFalse(policy.shouldShow(input(lastBackground = now + 5 * minute)))
    }

    @Test
    fun `away threshold comes from config`() {
        val custom = AppOpenPolicy(AdsConfig(appOpenMinAwayMin = 60))
        assertFalse(custom.shouldShow(input(lastBackground = now - 59 * minute)))
        assertTrue(custom.shouldShow(input(lastBackground = now - 60 * minute)))
        assertEquals(30 * minute, AdsConfig.DEFAULT.appOpenMinAwayMs)
    }
}

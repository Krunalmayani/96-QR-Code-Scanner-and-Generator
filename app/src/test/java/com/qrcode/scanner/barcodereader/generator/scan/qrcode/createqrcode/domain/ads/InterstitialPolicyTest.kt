package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InterstitialPolicyTest {
    private val minute = 60_000L
    private val first = 1_000_000L
    private val now = first + AdDefaults.FIRST_SESSION_MS + minute

    private val policy = InterstitialPolicy()

    private fun input(
        adsAllowed: Boolean = true,
        onboardingDone: Boolean = true,
        results: Int = 4,
        nowMs: Long = now,
        last: Long = 0L,
        firstLaunch: Long = first,
        ready: Boolean = true,
        showing: Boolean = false,
    ) = InterstitialInput(adsAllowed, onboardingDone, results, nowMs, last, firstLaunch, ready, showing)

    @Test
    fun `shows on the fourth result when everything allows it`() {
        assertTrue(policy.shouldShow(input(results = 4)))
    }

    @Test
    fun `does not show before the Nth result`() {
        for (count in 0..3) assertFalse(policy.shouldShow(input(results = count)), "count=$count")
    }

    @Test
    fun `shows when the count overshoots because an earlier attempt was blocked`() {
        assertTrue(policy.shouldShow(input(results = 7)))
    }

    @Test
    fun `respects a custom every-N`() {
        val custom = InterstitialPolicy(AdsConfig(interstitialEveryN = 6))
        assertFalse(custom.shouldShow(input(results = 5)))
        assertTrue(custom.shouldShow(input(results = 6)))
    }

    @Test
    fun `blocked when ads are not allowed`() {
        assertFalse(policy.shouldShow(input(adsAllowed = false)))
    }

    @Test
    fun `blocked before onboarding is done`() {
        assertFalse(policy.shouldShow(input(onboardingDone = false)))
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
    fun `never in the first session`() {
        val duringFirstSession = first + AdDefaults.FIRST_SESSION_MS - 1
        assertFalse(policy.shouldShow(input(nowMs = duringFirstSession)))
        assertFalse(policy.shouldShow(input(nowMs = first)))
    }

    @Test
    fun `allowed exactly when the first session ends`() {
        assertTrue(policy.shouldShow(input(nowMs = first + AdDefaults.FIRST_SESSION_MS)))
    }

    @Test
    fun `unknown first launch counts as first session`() {
        assertFalse(policy.shouldShow(input(firstLaunch = 0L)))
    }

    @Test
    fun `first launch in the future counts as first session`() {
        assertFalse(policy.shouldShow(input(firstLaunch = now + minute)))
    }

    @Test
    fun `isFirstSession boundaries`() {
        assertTrue(policy.isFirstSession(first + AdDefaults.FIRST_SESSION_MS - 1, first))
        assertFalse(policy.isFirstSession(first + AdDefaults.FIRST_SESSION_MS, first))
        assertTrue(policy.isFirstSession(5L, 0L))
    }

    @Test
    fun `minimum interval of 90 seconds between interstitials`() {
        assertFalse(policy.shouldShow(input(last = now - 89_999L)))
        assertTrue(policy.shouldShow(input(last = now - 90_000L)))
        assertTrue(policy.shouldShow(input(last = now - 10 * minute)))
    }

    @Test
    fun `just shown is blocked`() {
        assertFalse(policy.shouldShow(input(last = now)))
    }

    @Test
    fun `never shown before means no interval wait`() {
        assertTrue(policy.shouldShow(input(last = 0L)))
    }

    @Test
    fun `last shown in the future is ignored so ads are not blocked for good`() {
        assertTrue(policy.shouldShow(input(last = now + 365L * 24 * 60 * minute)))
    }

    @Test
    fun `min interval comes from config`() {
        val custom = InterstitialPolicy(AdsConfig(interstitialMinIntervalSec = 300))
        assertFalse(custom.shouldShow(input(last = now - 299_999L)))
        assertTrue(custom.shouldShow(input(last = now - 300_000L)))
    }

    @Test
    fun `config constructor uses defaults of 4 results and 90 seconds`() {
        val fromConfig = InterstitialPolicy(AdsConfig.DEFAULT)
        assertFalse(fromConfig.shouldShow(input(results = 3)))
        assertTrue(fromConfig.shouldShow(input(results = 4, last = now - 90_000L)))
        assertEquals(90_000L, AdsConfig.DEFAULT.interstitialMinIntervalMs)
    }
}

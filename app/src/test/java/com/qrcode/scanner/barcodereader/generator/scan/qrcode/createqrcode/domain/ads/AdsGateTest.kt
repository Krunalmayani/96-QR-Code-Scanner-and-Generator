package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdsGateTest {
    @Test
    fun `allowed only when every condition holds`() {
        assertTrue(AdsGate.canServeAds(buildEnabled = true, remoteEnabled = true, consentAllowsAds = true, adFree = false))
    }

    @Test
    fun `build flag off disables ads`() {
        assertFalse(AdsGate.canServeAds(false, true, true, false))
    }

    @Test
    fun `remote flag off disables ads`() {
        assertFalse(AdsGate.canServeAds(true, false, true, false))
    }

    @Test
    fun `no consent disables ads`() {
        assertFalse(AdsGate.canServeAds(true, true, false, false))
    }

    @Test
    fun `ad free purchase disables ads`() {
        assertFalse(AdsGate.canServeAds(true, true, true, true))
    }

    @Test
    fun `truth table has exactly one allowed row`() {
        val bools = listOf(false, true)
        var allowed = 0
        for (a in bools) {
            for (b in bools) {
                for (c in bools) {
                    for (d in bools) {
                        if (AdsGate.canServeAds(a, b, c, d)) allowed++
                    }
                }
            }
        }
        assertEquals(1, allowed)
    }
}

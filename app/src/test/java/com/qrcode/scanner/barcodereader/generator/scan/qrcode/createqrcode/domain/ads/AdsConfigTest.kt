package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdsConfigTest {
    @Test
    fun `defaults match the PRD`() {
        val config = AdsConfig.DEFAULT
        assertTrue(config.adsEnabled)
        assertEquals(4, config.interstitialEveryN)
        assertEquals(90, config.interstitialMinIntervalSec)
        assertEquals(8, config.nativeEveryNItems)
        assertEquals(30, config.appOpenMinAwayMin)
    }

    @Test
    fun `fromRaw keeps valid values`() {
        val config = AdsConfig.fromRaw(false, 6, 120, 10, 45)
        assertEquals(AdsConfig(false, 6, 120, 10, 45), config)
    }

    @Test
    fun `fromRaw with the default numbers equals DEFAULT`() {
        assertEquals(AdsConfig.DEFAULT, AdsConfig.fromRaw(true, 4, 90, 8, 30))
    }

    @Test
    fun `fromRaw raises too small values to the floor`() {
        val config = AdsConfig.fromRaw(true, 0, -5, 1, 0)
        assertEquals(2, config.interstitialEveryN)
        assertEquals(30, config.interstitialMinIntervalSec)
        assertEquals(4, config.nativeEveryNItems)
        assertEquals(5, config.appOpenMinAwayMin)
    }

    @Test
    fun `fromRaw lowers too large values to the ceiling`() {
        val config = AdsConfig.fromRaw(true, 1_000, 999_999, 5_000, 100_000)
        assertEquals(50, config.interstitialEveryN)
        assertEquals(3600, config.interstitialMinIntervalSec)
        assertEquals(50, config.nativeEveryNItems)
        assertEquals(1440, config.appOpenMinAwayMin)
    }

    @Test
    fun `fromRaw survives Long extremes`() {
        val low = AdsConfig.fromRaw(true, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE)
        val high = AdsConfig.fromRaw(true, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE)
        assertEquals(2, low.interstitialEveryN)
        assertEquals(50, high.interstitialEveryN)
        assertEquals(3600, high.interstitialMinIntervalSec)
    }

    @Test
    fun `range edges are accepted unchanged`() {
        val config = AdsConfig.fromRaw(true, 2, 30, 4, 5)
        assertEquals(2, config.interstitialEveryN)
        assertEquals(30, config.interstitialMinIntervalSec)
        assertEquals(4, config.nativeEveryNItems)
        assertEquals(5, config.appOpenMinAwayMin)
    }

    @Test
    fun `millisecond helpers convert units`() {
        val config = AdsConfig(interstitialMinIntervalSec = 90, appOpenMinAwayMin = 30)
        assertEquals(90_000L, config.interstitialMinIntervalMs)
        assertEquals(1_800_000L, config.appOpenMinAwayMs)
    }

    @Test
    fun `disabled flag survives clamping`() {
        assertFalse(AdsConfig.fromRaw(false, 4, 90, 8, 30).adsEnabled)
    }
}

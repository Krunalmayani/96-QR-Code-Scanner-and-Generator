package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.ads

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdSlotTest {
    private val discarded = mutableListOf<String>()
    private val slot = AdSlot<String>(maxAgeMs = 1_000L, baseRetryDelayMs = 100L, maxRetryDelayMs = 400L) { discarded += it }

    @Test
    fun `starts empty`() {
        assertFalse(slot.isReady(0L))
        assertNull(slot.take(0L))
    }

    @Test
    fun `beginLoad claims once until the load finishes`() {
        assertTrue(slot.beginLoad(0L))
        assertFalse(slot.beginLoad(0L))
        slot.onLoaded("a", 10L)
        assertFalse(slot.beginLoad(11L))
    }

    @Test
    fun `take returns the ad once and empties the slot`() {
        slot.beginLoad(0L)
        slot.onLoaded("a", 0L)
        assertTrue(slot.isReady(500L))
        assertEquals("a", slot.take(500L))
        assertNull(slot.take(500L))
        assertTrue(slot.beginLoad(500L))
    }

    @Test
    fun `ad expires after the max age and is discarded`() {
        slot.beginLoad(0L)
        slot.onLoaded("a", 0L)
        assertTrue(slot.isReady(999L))
        assertFalse(slot.isReady(1_000L))
        assertEquals(listOf("a"), discarded)
        assertTrue(slot.beginLoad(1_000L))
    }

    @Test
    fun `clock moving back makes the ad stale`() {
        slot.beginLoad(5_000L)
        slot.onLoaded("a", 5_000L)
        assertFalse(slot.isReady(100L))
        assertEquals(listOf("a"), discarded)
    }

    @Test
    fun `failure backs off exponentially up to the cap`() {
        assertTrue(slot.beginLoad(0L))
        slot.onLoadFailed(0L)
        assertFalse(slot.beginLoad(99L))
        assertTrue(slot.beginLoad(100L))
        slot.onLoadFailed(100L)
        assertFalse(slot.beginLoad(299L))
        assertTrue(slot.beginLoad(300L))
        slot.onLoadFailed(300L)
        assertFalse(slot.beginLoad(699L))
        assertTrue(slot.beginLoad(700L))
        slot.onLoadFailed(700L)
        assertFalse(slot.beginLoad(1_099L))
        assertTrue(slot.beginLoad(1_100L))
    }

    @Test
    fun `success resets the backoff`() {
        slot.beginLoad(0L)
        slot.onLoadFailed(0L)
        assertTrue(slot.beginLoad(100L))
        slot.onLoaded("a", 100L)
        slot.take(100L)
        assertTrue(slot.beginLoad(101L))
        slot.onLoadFailed(101L)
        assertFalse(slot.beginLoad(200L))
        assertTrue(slot.beginLoad(201L))
    }

    @Test
    fun `loading a second ad discards the first`() {
        slot.onLoaded("a", 0L)
        slot.onLoaded("b", 1L)
        assertEquals(listOf("a"), discarded)
        assertEquals("b", slot.take(2L))
    }

    @Test
    fun `clear discards the held ad and forgets the retry delay`() {
        slot.onLoaded("a", 0L)
        slot.clear()
        assertEquals(listOf("a"), discarded)
        assertFalse(slot.isReady(1L))
        slot.beginLoad(2L)
        slot.onLoadFailed(2L)
        slot.clear()
        assertTrue(slot.beginLoad(3L))
    }
}

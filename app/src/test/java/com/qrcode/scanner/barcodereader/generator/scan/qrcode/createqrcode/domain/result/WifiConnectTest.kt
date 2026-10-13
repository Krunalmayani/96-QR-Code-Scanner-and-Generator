package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WifiConnectTest {
    private fun run(vararg events: WifiConnectEvent): WifiConnectState =
        events.fold(WifiConnectState()) { state, event -> WifiConnectMachine.reduce(state, event) }

    @Test
    fun `happy path goes idle connecting connected`() {
        assertEquals(WifiPhase.CONNECTING, run(WifiConnectEvent.Started).phase)
        val done = run(WifiConnectEvent.Started, WifiConnectEvent.Succeeded())
        assertEquals(WifiPhase.CONNECTED, done.phase)
        assertFalse(done.viaSuggestion)
        assertTrue(done.flowVisible)
    }

    @Test
    fun `suggestion success is flagged`() {
        assertTrue(run(WifiConnectEvent.Started, WifiConnectEvent.Succeeded(viaSuggestion = true)).viaSuggestion)
    }

    @Test
    fun `failure keeps the reason and can be retried`() {
        val failed = run(WifiConnectEvent.Started, WifiConnectEvent.Failed(WifiFailure.UNAVAILABLE))
        assertEquals(WifiPhase.FAILED, failed.phase)
        assertEquals(WifiFailure.UNAVAILABLE, failed.failure)
        val retry = WifiConnectMachine.reduce(failed, WifiConnectEvent.Started)
        assertEquals(WifiPhase.CONNECTING, retry.phase)
        assertNull(retry.failure)
    }

    @Test
    fun `results that arrive outside connecting are ignored`() {
        assertEquals(WifiPhase.IDLE, run(WifiConnectEvent.Succeeded()).phase)
        assertEquals(WifiPhase.IDLE, run(WifiConnectEvent.Failed(WifiFailure.ERROR)).phase)
        assertEquals(WifiPhase.CONNECTED, run(WifiConnectEvent.Started, WifiConnectEvent.Succeeded(), WifiConnectEvent.Failed(WifiFailure.ERROR)).phase)
        assertEquals(WifiPhase.FAILED, run(WifiConnectEvent.Started, WifiConnectEvent.Failed(WifiFailure.ERROR), WifiConnectEvent.Succeeded()).phase)
    }

    @Test
    fun `start is ignored while connecting or connected`() {
        val connecting = run(WifiConnectEvent.Started)
        assertEquals(connecting, WifiConnectMachine.reduce(connecting, WifiConnectEvent.Started))
        val connected = run(WifiConnectEvent.Started, WifiConnectEvent.Succeeded())
        assertEquals(connected, WifiConnectMachine.reduce(connected, WifiConnectEvent.Started))
    }

    @Test
    fun `cancel only affects connecting and dismiss always resets`() {
        assertEquals(WifiPhase.IDLE, run(WifiConnectEvent.Started, WifiConnectEvent.Cancelled).phase)
        assertEquals(WifiPhase.CONNECTED, run(WifiConnectEvent.Started, WifiConnectEvent.Succeeded(), WifiConnectEvent.Cancelled).phase)
        assertEquals(WifiPhase.IDLE, run(WifiConnectEvent.Started, WifiConnectEvent.Failed(WifiFailure.ERROR), WifiConnectEvent.Dismissed).phase)
        assertFalse(run(WifiConnectEvent.Dismissed).flowVisible)
    }

    @Test
    fun `location is needed on android 10 and below only`() {
        assertTrue(WifiConnectPlanner.needsLocation(24))
        assertTrue(WifiConnectPlanner.needsLocation(28))
        assertTrue(WifiConnectPlanner.needsLocation(29))
        assertFalse(WifiConnectPlanner.needsLocation(30))
        assertFalse(WifiConnectPlanner.needsLocation(35))
    }

    @Test
    fun `strategy follows the android version`() {
        assertEquals(WifiStrategy.LEGACY, WifiConnectPlanner.plan(24, WifiSecurity.WPA, "password1").strategy)
        assertEquals(WifiStrategy.LEGACY, WifiConnectPlanner.plan(28, WifiSecurity.NONE, "").strategy)
        assertEquals(WifiStrategy.SPECIFIER, WifiConnectPlanner.plan(29, WifiSecurity.WPA, "password1").strategy)
        assertEquals(WifiStrategy.SPECIFIER, WifiConnectPlanner.plan(34, WifiSecurity.NONE, "").strategy)
    }

    @Test
    fun `wep is unsupported from android 10 but works before`() {
        val modern = WifiConnectPlanner.plan(30, WifiSecurity.WEP, "12345")
        assertEquals(WifiStrategy.UNSUPPORTED, modern.strategy)
        assertEquals(WifiFailure.UNSUPPORTED_SECURITY, modern.failure)
        val legacy = WifiConnectPlanner.plan(27, WifiSecurity.WEP, "12345")
        assertEquals(WifiStrategy.LEGACY, legacy.strategy)
        assertNull(legacy.failure)
    }

    @Test
    fun `invalid passphrases are rejected before any attempt`() {
        assertEquals(WifiFailure.INVALID_PASSWORD, WifiConnectPlanner.plan(33, WifiSecurity.WPA, "short").failure)
        assertEquals(WifiFailure.INVALID_PASSWORD, WifiConnectPlanner.plan(26, WifiSecurity.WPA, "").failure)
        assertNull(WifiConnectPlanner.plan(33, WifiSecurity.WPA, "cafe-nova-2026").failure)
    }

    @Test
    fun `passphrase rules`() {
        val hex64 = "a".repeat(64)
        assertTrue(WifiPassphrase.isValid(WifiSecurity.WPA, "12345678", legacy = false))
        assertTrue(WifiPassphrase.isValid(WifiSecurity.WPA, "x".repeat(63), legacy = false))
        assertFalse(WifiPassphrase.isValid(WifiSecurity.WPA, "x".repeat(64), legacy = false))
        assertFalse(WifiPassphrase.isValid(WifiSecurity.WPA, hex64, legacy = false))
        assertTrue(WifiPassphrase.isValid(WifiSecurity.WPA, hex64, legacy = true))
        assertTrue(WifiPassphrase.isValid(WifiSecurity.NONE, "", legacy = false))
        assertTrue(WifiPassphrase.isValid(WifiSecurity.WEP, "abcde", legacy = true))
        assertTrue(WifiPassphrase.isValid(WifiSecurity.WEP, "0123456789", legacy = true))
        assertFalse(WifiPassphrase.isValid(WifiSecurity.WEP, "abc", legacy = true))
        assertFalse(WifiPassphrase.isValid(WifiSecurity.WEP, "abcde", legacy = false))
    }

    @Test
    fun `legacy keys are quoted unless they are raw hex`() {
        assertEquals("\"cafe-nova-2026\"", WifiPassphrase.legacyWpaKey("cafe-nova-2026"))
        assertEquals("b".repeat(64), WifiPassphrase.legacyWpaKey("b".repeat(64)))
        assertEquals("\"abcde\"", WifiPassphrase.legacyWepKey("abcde"))
        assertEquals("0123456789", WifiPassphrase.legacyWepKey("0123456789"))
    }
}

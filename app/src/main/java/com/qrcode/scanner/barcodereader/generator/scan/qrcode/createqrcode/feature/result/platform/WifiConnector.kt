package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectRequest

/**
 * Android-specific part of joining a Wi-Fi network. The ViewModel only sees this interface so it can be tested with a
 * fake. Implementation: [AndroidWifiConnector].
 */
interface WifiConnector {
    /** Android API level of the device; decides the connection strategy and whether Location is needed. */
    val sdkInt: Int

    fun hasLocationPermission(): Boolean

    /**
     * Tries to join the network and suspends until the attempt succeeds or fails. Cancelling the calling coroutine
     * abandons the attempt (the pending system request is released).
     */
    suspend fun connect(request: WifiConnectRequest): WifiConnectOutcome

    /** Releases a pending connection request, if any. Safe to call at any time. */
    fun cancel()
}

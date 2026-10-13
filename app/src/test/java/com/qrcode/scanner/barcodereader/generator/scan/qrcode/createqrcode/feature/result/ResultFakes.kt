package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectRequest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform.NetworkStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform.WifiConnector
import kotlinx.coroutines.CompletableDeferred

/** Test double for [WifiConnector]: each connect call waits until the test completes [pending]. */
class FakeWifiConnector : WifiConnector {
    override var sdkInt: Int = 34
    var locationGranted: Boolean = true
    var pending: CompletableDeferred<WifiConnectOutcome>? = null
    val requests = mutableListOf<WifiConnectRequest>()
    var cancelCount = 0

    override fun hasLocationPermission(): Boolean = locationGranted

    override suspend fun connect(request: WifiConnectRequest): WifiConnectOutcome {
        requests += request
        val deferred = CompletableDeferred<WifiConnectOutcome>()
        pending = deferred
        return deferred.await()
    }

    override fun cancel() {
        cancelCount++
    }

    fun finish(outcome: WifiConnectOutcome) {
        pending?.complete(outcome)
    }
}

class FakeNetworkStatus(
    var online: Boolean = true,
) : NetworkStatus {
    override fun isOnline(): Boolean = online
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectPlanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectRequest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPassphrase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Joins a Wi-Fi network with the API that fits the Android version:
 * - Android 10+: [WifiNetworkSpecifier] through ConnectivityManager.requestNetwork (the system shows its own
 *   confirmation dialog and the connection is for this app). If the request cannot even be made, a
 *   [WifiNetworkSuggestion] is added instead.
 * - Android 7 to 9: WifiManager.addNetwork / enableNetwork, then polls until the supplicant reports COMPLETED.
 */
@Singleton
class AndroidWifiConnector @Inject constructor(
    @ApplicationContext private val context: Context,
) : WifiConnector {
    private val connectivity: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifi: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var activeCallback: ConnectivityManager.NetworkCallback? = null

    override val sdkInt: Int get() = Build.VERSION.SDK_INT

    override fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override suspend fun connect(request: WifiConnectRequest): WifiConnectOutcome {
        cancel()
        val plan = WifiConnectPlanner.plan(sdkInt, request.security, request.password)
        plan.failure?.let { return WifiConnectOutcome.Failed(it) }
        return when (plan.strategy) {
            WifiStrategy.UNSUPPORTED -> WifiConnectOutcome.Failed(WifiFailure.UNSUPPORTED_SECURITY)
            WifiStrategy.SPECIFIER ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) connectWithSpecifier(request) else WifiConnectOutcome.Failed(WifiFailure.ERROR)
            WifiStrategy.LEGACY -> connectLegacy(request)
        }
    }

    override fun cancel() {
        val callback = activeCallback ?: return
        activeCallback = null
        try {
            connectivity?.unregisterNetworkCallback(callback)
        } catch (e: IllegalArgumentException) {
            // Already unregistered by the system.
        }
    }

    // ---- Android 10+ ------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun connectWithSpecifier(request: WifiConnectRequest): WifiConnectOutcome {
        val manager = connectivity ?: return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        val networkRequest = try {
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(buildSpecifier(request))
                .build()
        } catch (e: IllegalArgumentException) {
            return WifiConnectOutcome.Failed(WifiFailure.INVALID_PASSWORD)
        }
        var requestFailed = false
        val outcome = suspendCancellableCoroutine<WifiConnectOutcome> { cont ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (cont.isActive) cont.resume(WifiConnectOutcome.Connected)
                }

                override fun onUnavailable() {
                    if (cont.isActive) cont.resume(WifiConnectOutcome.Failed(WifiFailure.UNAVAILABLE))
                }
            }
            activeCallback = callback
            cont.invokeOnCancellation { this@AndroidWifiConnector.cancel() }
            try {
                manager.requestNetwork(networkRequest, callback, REQUEST_TIMEOUT_MS)
            } catch (e: RuntimeException) {
                requestFailed = true
                if (cont.isActive) cont.resume(WifiConnectOutcome.Failed(WifiFailure.ERROR))
            }
        }
        if (outcome !is WifiConnectOutcome.Connected) this.cancel()
        return if (requestFailed) suggestNetwork(request) else outcome
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildSpecifier(request: WifiConnectRequest): WifiNetworkSpecifier {
        val builder = WifiNetworkSpecifier.Builder().setSsid(request.ssid)
        if (request.hidden) builder.setIsHiddenSsid(true)
        if (request.security == WifiSecurity.WPA) builder.setWpa2Passphrase(request.password)
        return builder.build()
    }

    /** Fallback when the network request itself throws: save the network so the phone joins it when in range. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun suggestNetwork(request: WifiConnectRequest): WifiConnectOutcome {
        val manager = wifi ?: return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        return try {
            val builder = WifiNetworkSuggestion.Builder().setSsid(request.ssid)
            if (request.hidden) builder.setIsHiddenSsid(true)
            if (request.security == WifiSecurity.WPA) builder.setWpa2Passphrase(request.password)
            val status = manager.addNetworkSuggestions(listOf(builder.build()))
            val added = status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS ||
                status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE
            if (added) WifiConnectOutcome.Suggested else WifiConnectOutcome.Failed(WifiFailure.ERROR)
        } catch (e: IllegalArgumentException) {
            WifiConnectOutcome.Failed(WifiFailure.INVALID_PASSWORD)
        } catch (e: SecurityException) {
            WifiConnectOutcome.Failed(WifiFailure.PERMISSION_DENIED)
        }
    }

    // ---- Android 7 to 9 ---------------------------------------------------------------------------------------

    @Suppress("DEPRECATION")
    private suspend fun connectLegacy(request: WifiConnectRequest): WifiConnectOutcome {
        val manager = wifi ?: return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        if (!manager.isWifiEnabled && !enableWifi(manager)) return WifiConnectOutcome.Failed(WifiFailure.WIFI_DISABLED)
        val networkId = try {
            manager.addNetwork(legacyConfiguration(request))
        } catch (e: SecurityException) {
            return WifiConnectOutcome.Failed(WifiFailure.PERMISSION_DENIED)
        }
        if (networkId < 0) return WifiConnectOutcome.Failed(WifiFailure.ERROR)
        manager.disconnect()
        if (!manager.enableNetwork(networkId, true)) return WifiConnectOutcome.Failed(WifiFailure.UNAVAILABLE)
        manager.reconnect()
        return awaitLegacyConnection(manager, networkId)
    }

    @Suppress("DEPRECATION")
    private suspend fun enableWifi(manager: WifiManager): Boolean {
        if (!manager.setWifiEnabled(true)) return false
        repeat(WIFI_ENABLE_POLLS) {
            if (manager.isWifiEnabled) return true
            delay(POLL_INTERVAL_MS)
        }
        return manager.isWifiEnabled
    }

    @Suppress("DEPRECATION")
    private suspend fun awaitLegacyConnection(manager: WifiManager, networkId: Int): WifiConnectOutcome {
        repeat(CONNECT_POLLS) {
            delay(POLL_INTERVAL_MS)
            val info = manager.connectionInfo
            if (info != null && info.networkId == networkId && info.supplicantState == SupplicantState.COMPLETED) {
                return WifiConnectOutcome.Connected
            }
        }
        return WifiConnectOutcome.Failed(WifiFailure.UNAVAILABLE)
    }

    @Suppress("DEPRECATION")
    private fun legacyConfiguration(request: WifiConnectRequest): WifiConfiguration {
        val config = WifiConfiguration()
        config.SSID = "\"" + request.ssid + "\""
        config.hiddenSSID = request.hidden
        when (request.security) {
            WifiSecurity.NONE -> config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            WifiSecurity.WPA -> {
                config.preSharedKey = WifiPassphrase.legacyWpaKey(request.password)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }
            WifiSecurity.WEP -> {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.wepKeys[0] = WifiPassphrase.legacyWepKey(request.password)
                config.wepTxKeyIndex = 0
            }
        }
        return config
    }

    private companion object {
        const val REQUEST_TIMEOUT_MS = 30_000
        const val POLL_INTERVAL_MS = 500L
        const val CONNECT_POLLS = 40
        const val WIFI_ENABLE_POLLS = 10
    }
}

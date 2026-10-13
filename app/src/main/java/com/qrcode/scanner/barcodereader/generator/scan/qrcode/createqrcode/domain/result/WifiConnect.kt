package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

/** Why a connection attempt did not succeed. The UI maps each value to a sentence under "Could not connect". */
enum class WifiFailure {
    /** The password may be wrong, the network is out of range, or the user dismissed the system prompt. */
    UNAVAILABLE,
    PERMISSION_DENIED,
    UNSUPPORTED_SECURITY,
    INVALID_PASSWORD,
    WIFI_DISABLED,
    ERROR,
}

/** What the connector is asked to join. */
data class WifiConnectRequest(
    val ssid: String,
    val password: String,
    val security: WifiSecurity,
    val hidden: Boolean,
)

/** Result of one attempt. [Suggested] means the network was saved as a suggestion and the phone will join when in range. */
sealed interface WifiConnectOutcome {
    data object Connected : WifiConnectOutcome

    data object Suggested : WifiConnectOutcome

    data class Failed(val reason: WifiFailure) : WifiConnectOutcome
}

enum class WifiStrategy {
    /** Android 10+: WifiNetworkSpecifier through ConnectivityManager.requestNetwork (this app only). */
    SPECIFIER,

    /** Android 7 to 9: WifiManager.addNetwork / enableNetwork. */
    LEGACY,

    /** The security type cannot be joined from an app on this Android version. */
    UNSUPPORTED,
}

/** How to connect on a given Android version. [failure] is set when no attempt should be made. */
data class WifiPlan(val strategy: WifiStrategy, val needsLocation: Boolean, val failure: WifiFailure? = null)

object WifiConnectPlanner {
    const val ANDROID_Q = 29

    /**
     * Location is requested before connecting on Android 10 and below: the legacy path needs it to confirm the
     * connection, and Android 10 needs it for the network-request flow. Android 11+ does not.
     */
    fun needsLocation(sdkInt: Int): Boolean = sdkInt <= ANDROID_Q

    fun plan(sdkInt: Int, security: WifiSecurity, password: String): WifiPlan {
        val modern = sdkInt >= ANDROID_Q
        val location = needsLocation(sdkInt)
        return when {
            modern && security == WifiSecurity.WEP -> WifiPlan(WifiStrategy.UNSUPPORTED, location, WifiFailure.UNSUPPORTED_SECURITY)
            !WifiPassphrase.isValid(security, password, legacy = !modern) ->
                WifiPlan(if (modern) WifiStrategy.SPECIFIER else WifiStrategy.LEGACY, location, WifiFailure.INVALID_PASSWORD)
            modern -> WifiPlan(WifiStrategy.SPECIFIER, location)
            else -> WifiPlan(WifiStrategy.LEGACY, location)
        }
    }
}

object WifiPassphrase {
    private const val WPA_MIN = 8
    private const val WPA_MAX = 63
    private const val WPA_HEX_LENGTH = 64
    private val WEP_ASCII_LENGTHS = setOf(5, 13, 16)
    private val WEP_HEX_LENGTHS = setOf(10, 26, 32)

    /**
     * WPA passphrases are 8 to 63 characters; the legacy API also accepts a raw 64-digit hex key. Open networks
     * need none. WEP keys are 5/13/16 characters or 10/26/32 hex digits (legacy API only).
     */
    fun isValid(security: WifiSecurity, password: String, legacy: Boolean): Boolean = when (security) {
        WifiSecurity.NONE -> true
        WifiSecurity.WPA -> password.length in WPA_MIN..WPA_MAX || (legacy && password.length == WPA_HEX_LENGTH && isHex(password))
        WifiSecurity.WEP -> legacy && (password.length in WEP_ASCII_LENGTHS || (password.length in WEP_HEX_LENGTHS && isHex(password)))
    }

    /** Value for `WifiConfiguration.preSharedKey`: quoted passphrase, or the raw 64-digit hex key. */
    fun legacyWpaKey(password: String): String =
        if (password.length == WPA_HEX_LENGTH && isHex(password)) password else "\"$password\""

    /** Value for `WifiConfiguration.wepKeys[0]`: hex keys are passed raw, ASCII keys quoted. */
    fun legacyWepKey(password: String): String =
        if (password.length in WEP_HEX_LENGTHS && isHex(password)) password else "\"$password\""

    private fun isHex(text: String): Boolean = text.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

enum class WifiPhase { IDLE, CONNECTING, CONNECTED, FAILED }

/** [viaSuggestion] is set when CONNECTED came from a saved suggestion instead of a live connection. */
data class WifiConnectState(
    val phase: WifiPhase = WifiPhase.IDLE,
    val failure: WifiFailure? = null,
    val viaSuggestion: Boolean = false,
) {
    /** True while one of the s26 to s28 screens replaces the normal Wi-Fi result. */
    val flowVisible: Boolean get() = phase != WifiPhase.IDLE
}

sealed interface WifiConnectEvent {
    data object Started : WifiConnectEvent

    data class Succeeded(val viaSuggestion: Boolean = false) : WifiConnectEvent

    data class Failed(val reason: WifiFailure) : WifiConnectEvent

    /** User pressed Cancel while connecting. */
    data object Cancelled : WifiConnectEvent

    /** Back from the connect screens to the normal result. */
    data object Dismissed : WifiConnectEvent
}

/** Idle -> Connecting -> Connected | Failed. Late or duplicate callbacks are ignored instead of corrupting the state. */
object WifiConnectMachine {
    fun reduce(state: WifiConnectState, event: WifiConnectEvent): WifiConnectState = when (event) {
        WifiConnectEvent.Started ->
            if (state.phase == WifiPhase.IDLE || state.phase == WifiPhase.FAILED) WifiConnectState(WifiPhase.CONNECTING) else state
        is WifiConnectEvent.Succeeded ->
            if (state.phase == WifiPhase.CONNECTING) WifiConnectState(WifiPhase.CONNECTED, viaSuggestion = event.viaSuggestion) else state
        is WifiConnectEvent.Failed ->
            if (state.phase == WifiPhase.CONNECTING) WifiConnectState(WifiPhase.FAILED, failure = event.reason) else state
        WifiConnectEvent.Cancelled ->
            if (state.phase == WifiPhase.CONNECTING) WifiConnectState() else state
        WifiConnectEvent.Dismissed -> WifiConnectState()
    }
}

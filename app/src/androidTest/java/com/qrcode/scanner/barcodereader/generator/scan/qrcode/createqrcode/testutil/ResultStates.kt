package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.testutil

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.LinkRiskReasons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultPhase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.ResultUiState

/** Sample [ResultUiState]s built from real domain code (the safety checker), for the stateless Result screens. */
object ResultStates {
    const val UNSAFE_URL = "http://paypal.com@login-check.xyz/verify"
    const val SAFE_URL = "https://www.brewhouse-coffee.com/menu"
    const val WIFI_SSID = "CafeNova_Guest"
    const val WIFI_PASSWORD = "guestpass123"

    private val checker = LinkSafetyChecker()

    private fun now(): Long = System.currentTimeMillis()

    fun website(
        url: String,
        showUnsafeSheet: Boolean = false,
        fromHistory: Boolean = false,
    ): ResultUiState {
        val safety = checker.check(url)
        return ResultUiState(
            phase = ResultPhase.READY,
            record =
                ScanRecord(
                    id = 1,
                    content = ScanContent.Website(url, CodeFormat.QR_CODE, url),
                    createdAt = now(),
                    source = ScanSource.CAMERA,
                ),
            fromHistory = fromHistory,
            safety = safety,
            riskReasons = LinkRiskReasons.of(safety),
            unsafeSheetVisible = showUnsafeSheet,
        )
    }

    fun wifi(
        wifi: WifiConnectState = WifiConnectState(),
        security: WifiSecurity = WifiSecurity.WPA,
        password: String = WIFI_PASSWORD,
        locationRationaleVisible: Boolean = false,
    ): ResultUiState =
        ResultUiState(
            phase = ResultPhase.READY,
            record =
                ScanRecord(
                    id = 2,
                    content =
                        ScanContent.Wifi(
                            rawValue = "WIFI:T:WPA;S:$WIFI_SSID;P:$password;;",
                            format = CodeFormat.QR_CODE,
                            ssid = WIFI_SSID,
                            password = password,
                            security = security,
                        ),
                    createdAt = now(),
                    source = ScanSource.CAMERA,
                ),
            wifi = wifi,
            locationRationaleVisible = locationRationaleVisible,
        )
}

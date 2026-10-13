package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

/** Parses the de-facto `WIFI:T:WPA;S:ssid;P:pass;H:true;;` format. */
internal object WifiParser {
    private const val PREFIX = "WIFI:"

    fun parse(text: String, raw: String, format: CodeFormat): ScanContent.Wifi? {
        val fields = FieldParsing.parseKeyValue(text.substring(PREFIX.length))
        val ssid = fields["S"]?.firstOrNull().orEmpty()
        if (ssid.isEmpty()) return null
        val type = fields["T"]?.firstOrNull().orEmpty()
        val password = fields["P"]?.firstOrNull().orEmpty()
        val security = securityOf(type, password)
        return ScanContent.Wifi(
            rawValue = raw,
            format = format,
            ssid = ssid,
            password = if (security == WifiSecurity.NONE) "" else password,
            security = security,
            hidden = fields["H"]?.firstOrNull()?.trim().equals("true", ignoreCase = true),
        )
    }

    /** WPA, WPA2, WPA3, SAE and EAP variants -> WPA; WEP -> WEP; nopass or an empty type with no password -> NONE. */
    private fun securityOf(type: String, password: String): WifiSecurity {
        val t = type.trim().uppercase()
        return when {
            t == "WEP" -> WifiSecurity.WEP
            t == "NOPASS" || t == "NONE" || t == "OPEN" -> WifiSecurity.NONE
            t.isEmpty() -> if (password.isEmpty()) WifiSecurity.NONE else WifiSecurity.WPA
            else -> WifiSecurity.WPA
        }
    }
}

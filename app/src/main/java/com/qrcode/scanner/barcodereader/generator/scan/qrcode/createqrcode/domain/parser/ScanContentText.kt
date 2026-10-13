package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_TIME_UTC: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC)
private val DATE_UTC: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US).withZone(ZoneOffset.UTC)

/** What the "Copy" button should put on the clipboard: the URL, the number, the password, a formatted contact, ... */
fun ScanContent.copyText(): String =
    when (this) {
        is ScanContent.Website -> url
        is ScanContent.Phone -> number
        is ScanContent.Email -> address
        is ScanContent.Sms -> number
        is ScanContent.Location -> coordinates()
        is ScanContent.Wifi -> password.ifEmpty { ssid }
        is ScanContent.Contact -> formatted()
        is ScanContent.Event -> formatted()
        is ScanContent.Product -> number
        is ScanContent.Text -> rawValue
        is ScanContent.Unknown -> rawValue
    }

/** What the system share sheet should send. Labels are plain English data, not UI strings. */
fun ScanContent.shareText(): String =
    when (this) {
        is ScanContent.Website -> url
        is ScanContent.Phone -> number
        is ScanContent.Email -> listOf(address, subject, body).filter { it.isNotBlank() }.joinToString("\n")
        is ScanContent.Sms -> if (message.isBlank()) number else "$number\n$message"
        is ScanContent.Location ->
            listOf(
                label,
                "https://maps.google.com/?q=${coordinates()}",
            ).filter { it.isNotBlank() }.joinToString("\n")
        is ScanContent.Wifi -> wifiShare()
        is ScanContent.Contact -> formatted()
        is ScanContent.Event -> formatted()
        is ScanContent.Product -> number
        is ScanContent.Text -> rawValue
        is ScanContent.Unknown -> rawValue
    }

private fun ScanContent.Location.coordinates(): String =
    String.format(Locale.US, "%.6f,%.6f", latitude, longitude)

private fun ScanContent.Wifi.wifiShare(): String {
    val lines = mutableListOf("Wi-Fi: $ssid")
    if (security != WifiSecurity.NONE && password.isNotEmpty()) lines += "Password: $password"
    return lines.joinToString("\n")
}

private fun ScanContent.Contact.formatted(): String =
    buildList {
        add(name)
        add(listOf(jobTitle, organization).filter { it.isNotBlank() }.joinToString(", "))
        addAll(phones)
        addAll(emails)
        add(address)
        add(website)
        add(note)
    }.filter { it.isNotBlank() }.joinToString("\n")

private fun ScanContent.Event.formatted(): String {
    val fmt = if (allDay) DATE_UTC else DATE_TIME_UTC
    val whenText =
        listOfNotNull(startMillis, endMillis)
            .joinToString(" - ") { fmt.format(Instant.ofEpochMilli(it)) }
    return listOf(summary, whenText, location, description).filter { it.isNotBlank() }.joinToString("\n")
}

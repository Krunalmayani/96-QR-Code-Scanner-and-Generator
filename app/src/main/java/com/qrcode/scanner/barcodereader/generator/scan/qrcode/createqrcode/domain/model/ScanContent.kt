package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WifiSecurity { NONE, WEP, WPA }

/**
 * Parsed meaning of a scanned or created code.
 * Produced by [com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser] from the raw payload text.
 * Times are epoch millis (UTC); null when unknown.
 */
@Serializable
sealed interface ScanContent {
    val rawValue: String
    val format: CodeFormat
    val type: ContentType

    /** Short human title for history rows and result headers (no localisation: it is data). */
    val title: String

    @Serializable
    data class Website(
        override val rawValue: String,
        override val format: CodeFormat,
        /** Normalised absolute URL (scheme added when the payload had none). */
        val url: String,
    ) : ScanContent {
        override val type get() = ContentType.WEBSITE
        override val title get() = url.substringAfter("://").removePrefix("www.").trimEnd('/')
    }

    @Serializable
    data class Text(
        override val rawValue: String,
        override val format: CodeFormat,
    ) : ScanContent {
        override val type get() = ContentType.TEXT
        override val title get() = rawValue.lineSequence().firstOrNull().orEmpty().take(60)
    }

    @Serializable
    data class Contact(
        override val rawValue: String,
        override val format: CodeFormat,
        val name: String = "",
        val organization: String = "",
        val jobTitle: String = "",
        val phones: List<String> = emptyList(),
        val emails: List<String> = emptyList(),
        val address: String = "",
        val website: String = "",
        val note: String = "",
    ) : ScanContent {
        override val type get() = ContentType.CONTACT
        override val title get() = name.ifBlank { organization }.ifBlank { phones.firstOrNull() ?: emails.firstOrNull() ?: "Contact" }
    }

    @Serializable
    data class Phone(
        override val rawValue: String,
        override val format: CodeFormat,
        val number: String,
    ) : ScanContent {
        override val type get() = ContentType.PHONE
        override val title get() = number
    }

    @Serializable
    data class Email(
        override val rawValue: String,
        override val format: CodeFormat,
        val address: String,
        val subject: String = "",
        val body: String = "",
    ) : ScanContent {
        override val type get() = ContentType.EMAIL
        override val title get() = address
    }

    @Serializable
    data class Sms(
        override val rawValue: String,
        override val format: CodeFormat,
        val number: String,
        val message: String = "",
    ) : ScanContent {
        override val type get() = ContentType.SMS
        override val title get() = number
    }

    @Serializable
    data class Location(
        override val rawValue: String,
        override val format: CodeFormat,
        val latitude: Double,
        val longitude: Double,
        /** Optional label or query from a geo: URI (?q=). */
        val label: String = "",
    ) : ScanContent {
        override val type get() = ContentType.LOCATION
        override val title get() = label.ifBlank { "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}" }
    }

    @Serializable
    data class Event(
        override val rawValue: String,
        override val format: CodeFormat,
        val summary: String = "",
        val startMillis: Long? = null,
        val endMillis: Long? = null,
        val allDay: Boolean = false,
        val location: String = "",
        val description: String = "",
    ) : ScanContent {
        override val type get() = ContentType.EVENT
        override val title get() = summary.ifBlank { "Event" }
    }

    @Serializable
    data class Wifi(
        override val rawValue: String,
        override val format: CodeFormat,
        val ssid: String,
        val password: String = "",
        val security: WifiSecurity = WifiSecurity.WPA,
        val hidden: Boolean = false,
    ) : ScanContent {
        override val type get() = ContentType.WIFI
        override val title get() = ssid
    }

    @Serializable
    data class Product(
        override val rawValue: String,
        override val format: CodeFormat,
        /** Digits only. */
        val number: String,
    ) : ScanContent {
        override val type get() = ContentType.PRODUCT
        override val title get() = number
    }

    @Serializable
    data class Unknown(
        override val rawValue: String,
        override val format: CodeFormat,
    ) : ScanContent {
        override val type get() = ContentType.UNKNOWN
        override val title get() = rawValue.take(40).ifBlank { format.displayName }
    }
}

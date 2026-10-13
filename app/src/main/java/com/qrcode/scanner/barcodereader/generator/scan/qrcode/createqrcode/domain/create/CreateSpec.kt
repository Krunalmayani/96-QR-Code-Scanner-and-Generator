package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity

enum class SocialPlatform {
    INSTAGRAM,
    FACEBOOK,
    X,
    LINKEDIN,
    YOUTUBE,

    /** Takes a phone number instead of a handle (wa.me link). */
    WHATSAPP,
    TELEGRAM,
    ;

    val isPhoneBased: Boolean get() = this == WHATSAPP
}

/**
 * Raw (unvalidated) form inputs of one Create form. Strings are exactly what the user typed;
 * [PayloadBuilder] trims and normalises, [Validators] checks them.
 */
sealed interface CreateSpec {
    val type: CreateType

    data class Website(
        val url: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.WEBSITE
    }

    data class Text(
        val text: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.TEXT
    }

    data class Contact(
        val name: String = "",
        val organization: String = "",
        val jobTitle: String = "",
        val phone: String = "",
        val email: String = "",
        val address: String = "",
        val website: String = "",
        val note: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.CONTACT
    }

    data class Wifi(
        val ssid: String = "",
        val password: String = "",
        val security: WifiSecurity = WifiSecurity.WPA,
        val hidden: Boolean = false,
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.WIFI
    }

    data class Email(
        val to: String = "",
        val subject: String = "",
        val body: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.EMAIL
    }

    data class Phone(
        val number: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.PHONE
    }

    data class Sms(
        val number: String = "",
        val message: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.SMS
    }

    /** [latitude] and [longitude] are decimal degrees as typed (dot or comma separator). */
    data class Location(
        val latitude: String = "",
        val longitude: String = "",
        val label: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.LOCATION
    }

    /**
     * Times are epoch millis (UTC). For [allDay] events only the UTC calendar date of each value matters
     * (a Material3 date picker already returns UTC midnight); [endMillis] is then the LAST day (inclusive).
     */
    data class Event(
        val title: String = "",
        val location: String = "",
        val description: String = "",
        val allDay: Boolean = false,
        val startMillis: Long? = null,
        val endMillis: Long? = null,
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.EVENT
    }

    /** [handle] is the username (a leading @ is ignored) or, for WhatsApp, a phone number. */
    data class Social(
        val platform: SocialPlatform = SocialPlatform.INSTAGRAM,
        val handle: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.SOCIAL
    }

    data class AppStore(
        val url: String = "",
    ) : CreateSpec {
        override val type: CreateType get() = CreateType.APP_STORE
    }

    /** [type] must be one of the `BARCODE_*` [CreateType]s. */
    data class Barcode(
        override val type: CreateType,
        val data: String = "",
    ) : CreateSpec {
        init {
            require(type.isBarcode) { "Barcode spec needs a BARCODE_* type, got $type" }
        }

        val format: CodeFormat get() = type.defaultFormat
    }

    companion object {
        /** An empty form for [type] (initial state of a Create form). */
        fun blank(type: CreateType): CreateSpec =
            when (type) {
                CreateType.WEBSITE -> Website()
                CreateType.TEXT -> Text()
                CreateType.CONTACT -> Contact()
                CreateType.WIFI -> Wifi()
                CreateType.EMAIL -> Email()
                CreateType.PHONE -> Phone()
                CreateType.SMS -> Sms()
                CreateType.LOCATION -> Location()
                CreateType.EVENT -> Event()
                CreateType.SOCIAL -> Social()
                CreateType.APP_STORE -> AppStore()
                else -> Barcode(type)
            }
    }
}

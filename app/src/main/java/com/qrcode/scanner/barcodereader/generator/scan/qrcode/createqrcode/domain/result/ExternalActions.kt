package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import java.math.BigDecimal
import java.math.RoundingMode

/** Builds the [ExternalAction] behind each button of the result screen. Returns null when there is nothing usable to launch. */
object ExternalActions {
    private const val COORDINATE_SCALE = 6
    private const val MAX_SEARCH_LENGTH = 200
    private const val SEARCH_URL = "https://www.google.com/search?q="

    fun dial(number: String): ExternalAction.Dial? =
        if (PhoneNumbers.isDialable(number)) ExternalAction.Dial(PhoneNumbers.telUri(number)) else null

    fun composeEmail(address: String, subject: String = "", body: String = ""): ExternalAction.ComposeEmail? {
        val to = address.trim()
        if (to.isEmpty()) return null
        return ExternalAction.ComposeEmail(
            uri = "mailto:" + UriEncoding.encode(to, extraSafe = "@+,"),
            address = to,
            subject = subject,
            body = body,
        )
    }

    fun composeSms(number: String, message: String = ""): ExternalAction.ComposeSms? =
        if (PhoneNumbers.isDialable(number)) ExternalAction.ComposeSms(PhoneNumbers.smsUri(number), message) else null

    fun openInMaps(latitude: Double, longitude: Double, label: String = ""): ExternalAction.OpenMap =
        ExternalAction.OpenMap(geoUri(latitude, longitude, label))

    /** Opens a free-text place (an event venue) in a maps app. */
    fun searchPlace(place: String): ExternalAction.OpenMap? {
        val query = cleanLabel(place)
        return if (query.isEmpty()) null else ExternalAction.OpenMap("geo:0,0?q=" + UriEncoding.encode(query))
    }

    /** "Share by email": an empty compose window whose body is [text]; the user picks the recipient. */
    fun shareByEmail(text: String): ExternalAction.ComposeEmail? =
        if (text.isBlank()) null else ExternalAction.ComposeEmail(uri = "mailto:", address = "", subject = "", body = text)

    /** "Share by message": an empty compose window whose body is [text]; the user picks the recipient. */
    fun shareBySms(text: String): ExternalAction.ComposeSms? =
        if (text.isBlank()) null else ExternalAction.ComposeSms(uri = "smsto:", body = text)

    /** Web search page for [query] (whitespace collapsed, at most 200 characters); null when there is nothing to search. */
    fun webSearchUrl(query: String): String? {
        val clean =
            query
                .trim()
                .replace(Regex("\\s+"), " ")
                .take(MAX_SEARCH_LENGTH)
                .trim()
        return if (clean.isEmpty()) null else SEARCH_URL + UriEncoding.encode(clean)
    }

    fun addToCalendar(event: ScanContent.Event): ExternalAction.AddToCalendar {
        val window = CalendarTimes.window(event.startMillis, event.endMillis, event.allDay)
        return ExternalAction.AddToCalendar(
            title = event.summary,
            beginMillis = window.beginMillis,
            endMillis = window.endMillis,
            allDay = event.allDay,
            location = event.location,
            description = event.description,
        )
    }

    fun addToContacts(contact: ScanContent.Contact): ExternalAction.AddToContacts =
        ExternalAction.AddToContacts(
            ContactInsertData(
                name = contact.name,
                company = contact.organization,
                jobTitle = contact.jobTitle,
                phone = contact.phones.getOrNull(0).orEmpty(),
                secondaryPhone = contact.phones.getOrNull(1).orEmpty(),
                email = contact.emails.getOrNull(0).orEmpty(),
                secondaryEmail = contact.emails.getOrNull(1).orEmpty(),
                postal = contact.address,
                notes = listOf(contact.website, contact.note).filter { it.isNotBlank() }.joinToString("\n"),
            ),
        )

    fun addNumberToContacts(number: String): ExternalAction.AddToContacts =
        ExternalAction.AddToContacts(ContactInsertData(phone = number.trim()))

    fun addEmailToContacts(address: String): ExternalAction.AddToContacts =
        ExternalAction.AddToContacts(ContactInsertData(email = address.trim()))

    /** `geo:lat,lng?q=lat,lng(label)`: the `q=` form makes maps apps drop a labelled pin. */
    fun geoUri(latitude: Double, longitude: Double, label: String): String {
        val point = "${formatCoordinate(latitude)},${formatCoordinate(longitude)}"
        val name = cleanLabel(label)
        return if (name.isEmpty()) "geo:$point?q=$point" else "geo:$point?q=$point(${UriEncoding.encode(name)})"
    }

    /** Up to six decimals, no trailing zeros, always a dot. */
    fun formatCoordinate(value: Double): String {
        if (!value.isFinite()) return "0"
        val text = BigDecimal(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        return if (text == "-0") "0" else text
    }

    private fun cleanLabel(label: String): String =
        label
            .replace('(', ' ')
            .replace(')', ' ')
            .trim()
            .replace(Regex("\\s+"), " ")
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

/**
 * Pure description of an intent the result screen can launch after a tap. The Android layer turns each one into a
 * real `Intent`; keeping the data here makes every URI and extra unit-testable.
 */
sealed interface ExternalAction {
    /** ACTION_DIAL with a `tel:` URI. Never places the call by itself. */
    data class Dial(
        val uri: String,
    ) : ExternalAction

    /** ACTION_SENDTO with a `mailto:` URI; [subject] and [body] go in the intent extras. */
    data class ComposeEmail(
        val uri: String,
        val address: String,
        val subject: String,
        val body: String,
    ) : ExternalAction

    /** ACTION_SENDTO with an `smsto:` URI; [body] goes in the `sms_body` extra. */
    data class ComposeSms(
        val uri: String,
        val body: String,
    ) : ExternalAction

    /** ACTION_VIEW with a `geo:` URI. */
    data class OpenMap(
        val uri: String,
    ) : ExternalAction

    /** ACTION_INSERT on `CalendarContract.Events.CONTENT_URI`. Null times are left out of the intent. */
    data class AddToCalendar(
        val title: String,
        val beginMillis: Long?,
        val endMillis: Long?,
        val allDay: Boolean,
        val location: String,
        val description: String,
    ) : ExternalAction

    /** `ContactsContract.Intents.Insert.ACTION` with the prefilled [contact]. */
    data class AddToContacts(
        val contact: ContactInsertData,
    ) : ExternalAction
}

/** Values for the `ContactsContract.Intents.Insert` extras. Blank strings are not sent. */
data class ContactInsertData(
    val name: String = "",
    val company: String = "",
    val jobTitle: String = "",
    val phone: String = "",
    val secondaryPhone: String = "",
    val email: String = "",
    val secondaryEmail: String = "",
    val postal: String = "",
    val notes: String = "",
)

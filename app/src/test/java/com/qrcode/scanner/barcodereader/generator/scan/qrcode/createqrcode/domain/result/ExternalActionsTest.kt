package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExternalActionsTest {
    private val qr = CodeFormat.QR_CODE

    @Test
    fun `dial sanitises the number`() {
        assertEquals(ExternalAction.Dial("tel:+12065550198"), ExternalActions.dial("+1 (206) 555-0198"))
        assertNull(ExternalActions.dial("call me"))
    }

    @Test
    fun `email puts subject and body in the action and keeps the address in the uri`() {
        val action = ExternalActions.composeEmail("hello@lumen.studio", "Booking inquiry", "Hi there")
        assertEquals("mailto:hello@lumen.studio", action?.uri)
        assertEquals("Booking inquiry", action?.subject)
        assertEquals("Hi there", action?.body)
        assertEquals("hello@lumen.studio", action?.address)
    }

    @Test
    fun `email address is encoded but plus and comma survive`() {
        assertEquals("mailto:a+b@x.org", ExternalActions.composeEmail("a+b@x.org")?.uri)
        assertEquals("mailto:a%20b@x.org", ExternalActions.composeEmail("a b@x.org")?.uri)
        assertNull(ExternalActions.composeEmail("   "))
    }

    @Test
    fun `sms uses smsto and carries the body`() {
        val action = ExternalActions.composeSms("+44 7700 900123", "Reply YES")
        assertEquals("smsto:+447700900123", action?.uri)
        assertEquals("Reply YES", action?.body)
        assertNull(ExternalActions.composeSms("abc"))
    }

    @Test
    fun `geo uri has the pin query with the label in parentheses`() {
        assertEquals(
            "geo:37.7749,-122.4194?q=37.7749,-122.4194(San%20Francisco%2C%20CA)",
            ExternalActions.geoUri(37.7749, -122.4194, "San Francisco, CA"),
        )
    }

    @Test
    fun `geo uri without a label is a plain pin`() {
        assertEquals("geo:48.8584,2.2945?q=48.8584,2.2945", ExternalActions.geoUri(48.8584, 2.2945, ""))
        assertEquals("geo:48.8584,2.2945?q=48.8584,2.2945", ExternalActions.geoUri(48.8584, 2.2945, " () "))
    }

    @Test
    fun `parentheses in a label cannot break out of the query`() {
        assertEquals("geo:1,2?q=1,2(Cafe%20Nova)", ExternalActions.geoUri(1.0, 2.0, "Cafe (Nova)"))
    }

    @Test
    fun `coordinates have at most six decimals and no trailing zeros`() {
        assertEquals("37.7749", ExternalActions.formatCoordinate(37.7749))
        assertEquals("-0.000001", ExternalActions.formatCoordinate(-0.000001))
        assertEquals("1.234568", ExternalActions.formatCoordinate(1.23456789))
        assertEquals("0", ExternalActions.formatCoordinate(0.0))
        assertEquals("0", ExternalActions.formatCoordinate(-0.0000001))
        assertEquals("12", ExternalActions.formatCoordinate(12.0))
        assertEquals("0", ExternalActions.formatCoordinate(Double.NaN))
    }

    @Test
    fun `place search encodes the query`() {
        assertEquals("geo:0,0?q=Harbor%20Hall%20120%20Pier%20Ave", ExternalActions.searchPlace("Harbor Hall 120 Pier Ave")?.uri)
        assertNull(ExternalActions.searchPlace("  "))
    }

    @Test
    fun `calendar action carries the event fields and a normalised window`() {
        val start = 1_700_000_000_000L
        val event = ScanContent.Event("raw", qr, "Launch", start, null, false, "Harbor Hall", "Doors 5:30")
        val action = ExternalActions.addToCalendar(event)
        assertEquals("Launch", action.title)
        assertEquals(start, action.beginMillis)
        assertEquals(start + CalendarTimes.HOUR_MILLIS, action.endMillis)
        assertEquals("Harbor Hall", action.location)
        assertEquals("Doors 5:30", action.description)
    }

    @Test
    fun `calendar action without times passes none`() {
        val action = ExternalActions.addToCalendar(ScanContent.Event("raw", qr, summary = "Open day"))
        assertNull(action.beginMillis)
        assertNull(action.endMillis)
    }

    @Test
    fun `contact insert uses first and second phone and email and joins notes`() {
        val contact =
            ScanContent.Contact(
                rawValue = "raw",
                format = qr,
                name = "Sophie Lambert",
                organization = "Lumen Studio",
                jobTitle = "Designer",
                phones = listOf("+1 415 555 0142", "+1 415 555 0143", "third"),
                emails = listOf("sophie@lumen.studio"),
                address = "48 Market Street",
                website = "https://lumen.studio",
                note = "Met at the expo",
            )
        val data = ExternalActions.addToContacts(contact).contact
        assertEquals("Sophie Lambert", data.name)
        assertEquals("Lumen Studio", data.company)
        assertEquals("Designer", data.jobTitle)
        assertEquals("+1 415 555 0142", data.phone)
        assertEquals("+1 415 555 0143", data.secondaryPhone)
        assertEquals("sophie@lumen.studio", data.email)
        assertEquals("", data.secondaryEmail)
        assertEquals("48 Market Street", data.postal)
        assertEquals("https://lumen.studio\nMet at the expo", data.notes)
    }

    @Test
    fun `add number and add email prefill one field`() {
        assertEquals(ContactInsertData(phone = "+1 206"), ExternalActions.addNumberToContacts(" +1 206 ").contact)
        assertEquals(ContactInsertData(email = "a@b.c"), ExternalActions.addEmailToContacts("a@b.c").contact)
        assertNotNull(ExternalActions.addNumberToContacts("1"))
    }

    @Test
    fun `sharing by email or message only fills the body`() {
        val email = ExternalActions.shareByEmail("WIFI:T:WPA;S:Cafe;;")
        assertEquals("mailto:", email?.uri)
        assertEquals("", email?.address)
        assertEquals("WIFI:T:WPA;S:Cafe;;", email?.body)
        val sms = ExternalActions.shareBySms("hello")
        assertEquals("smsto:", sms?.uri)
        assertEquals("hello", sms?.body)
        assertNull(ExternalActions.shareByEmail("  "))
        assertNull(ExternalActions.shareBySms(""))
    }

    @Test
    fun `web search url encodes the query, collapses whitespace and caps the length`() {
        assertEquals(
            "https://www.google.com/search?q=ship%20v2.4%20on%20Friday",
            ExternalActions.webSearchUrl("  ship v2.4\n on   Friday "),
        )
        assertNull(ExternalActions.webSearchUrl("   "))
        val long = ExternalActions.webSearchUrl("a".repeat(500))
        assertEquals("https://www.google.com/search?q=".length + 200, long?.length)
    }
}

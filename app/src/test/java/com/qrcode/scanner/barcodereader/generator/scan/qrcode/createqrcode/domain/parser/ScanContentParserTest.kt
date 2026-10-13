package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat.QR_CODE
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.time.Instant
import java.time.ZoneOffset
import java.util.Random

/** Table driven: every row is one realistic payload with the expected parse. */
class ScanContentParserTest {
    private val parser = ScanContentParser(ZoneOffset.UTC)

    private class Case(
        val name: String,
        val raw: String,
        val format: CodeFormat,
        val check: (ScanContent) -> Unit,
    )

    private fun case(name: String, raw: String, format: CodeFormat = QR_CODE, check: (ScanContent) -> Unit) =
        Case(name, raw, format, check)

    private fun website(url: String): (ScanContent) -> Unit = { c ->
        assertTrue(c is ScanContent.Website, "expected Website but was $c")
        assertEquals(url, (c as ScanContent.Website).url)
    }

    private fun type(t: ContentType): (ScanContent) -> Unit = { c -> assertEquals(t, c.type, "was $c") }

    private inline fun <reified T : ScanContent> typed(crossinline body: (T) -> Unit): (ScanContent) -> Unit = { c ->
        assertTrue(c is T, "expected ${T::class.simpleName} but was $c")
        body(c as T)
    }

    private fun ms(iso: String) = Instant.parse(iso).toEpochMilli()

    private val vcard30 =
        "BEGIN:VCARD\nVERSION:3.0\nN:Doe;John;Q;Dr.;Jr.\nFN:John Q. Doe\nORG:Acme Corp;Research\nTITLE:Chief Scientist\n" +
            "TEL;TYPE=WORK,VOICE:+1-555-0100\nTEL;TYPE=CELL:+1-555-0101\nEMAIL;TYPE=INTERNET:john@acme.com\nEMAIL:jd@home.org\n" +
            "ADR;TYPE=WORK:;;1 Main St;Springfield;IL;62701;USA\nURL:https://acme.com\nNOTE:Met at conf\\nCall back\nEND:VCARD"

    private val cases: List<Case> =
        listOf(
            // ---- websites ----
            case("https url", "https://example.com/path?a=1#frag", check = website("https://example.com/path?a=1#frag")),
            case("http scheme is kept", "http://example.com", check = website("http://example.com")),
            case("uppercase scheme is kept", "HTTPS://EXAMPLE.COM/A", check = website("HTTPS://EXAMPLE.COM/A")),
            case("www without scheme gets https", "www.example.com", check = website("https://www.example.com")),
            case("bare domain with path gets https", "example.com/menu/today", check = website("https://example.com/menu/today")),
            case("bare domain with port", "example.org:8080/x", check = website("https://example.org:8080/x")),
            case("surrounding whitespace is trimmed", "  \n https://example.com/a \r\n", check = website("https://example.com/a")),
            case(
                "unicode path",
                "https://example.com/caf\u00E9/\u4F60\u597D",
                check = website("https://example.com/caf\u00E9/\u4F60\u597D"),
            ),
            case("URLTO wrapper", "URLTO:Example:https://example.com/x", check = website("https://example.com/x")),
            case("URLTO with bare domain", "URLTO:Site:example.org", check = website("https://example.org")),
            case(
                "MEBKM wrapper with escaped colon",
                "MEBKM:TITLE:Foo;URL:http\\://example.com/a;;",
                check = website("http://example.com/a"),
            ),
            case("ip host url", "http://192.168.1.1:8080/admin", check = website("http://192.168.1.1:8080/admin")),
            case("sentence containing a url is text", "visit https://example.com now", check = type(ContentType.TEXT)),
            case("scheme without host is text", "https://", check = type(ContentType.TEXT)),
            case("file name is not a domain", "notes.txt", check = type(ContentType.TEXT)),
            case("markdown file name is not a domain", "readme.md", check = type(ContentType.TEXT)),
            case("version number is text", "version 1.2.3", check = type(ContentType.TEXT)),
            case("javascript scheme is text", "javascript:alert(1)", check = type(ContentType.TEXT)),
            case("ftp scheme is text", "ftp://example.com/file", check = type(ContentType.TEXT)),
            case(
                "google maps coordinates become a location",
                "https://maps.google.com/?q=48.8584,2.2945",
                check =
                    typed<ScanContent.Location> {
                        assertEquals(48.8584, it.latitude, 1e-9)
                        assertEquals(2.2945, it.longitude, 1e-9)
                    },
            ),
            case(
                "google maps @ view becomes a location",
                "https://www.google.com/maps/@37.4219,-122.0840,15z",
                check =
                    typed<ScanContent.Location> {
                        assertEquals(-122.0840, it.longitude, 1e-9)
                    },
            ),
            case(
                "google maps place name stays a website",
                "https://www.google.com/maps/place/Eiffel+Tower/@48.8584,2.2945,17z",
                check = website("https://www.google.com/maps/place/Eiffel+Tower/@48.8584,2.2945,17z"),
            ),
            case("google maps text query stays a website", "https://maps.google.com/?q=pizza+near+me", check = type(ContentType.WEBSITE)),
            case("maps short link stays a website", "https://maps.app.goo.gl/AbCdEf123", check = type(ContentType.WEBSITE)),
            case("apple maps ll becomes a location", "https://maps.apple.com/?ll=51.5007,-0.1246", check = type(ContentType.LOCATION)),
            // ---- wifi ----
            case(
                "wifi wpa",
                "WIFI:T:WPA;S:HomeNet;P:secret123;;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals("HomeNet", it.ssid)
                        assertEquals("secret123", it.password)
                        assertEquals(WifiSecurity.WPA, it.security)
                        assertEquals(false, it.hidden)
                    },
            ),
            case(
                "wifi escapes",
                "WIFI:T:WPA;S:My\\;Net\\:5G;P:pa\\\\ss\\,word;;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals("My;Net:5G", it.ssid)
                        assertEquals("pa\\ss,word", it.password)
                    },
            ),
            case(
                "wifi nopass drops the password",
                "WIFI:T:nopass;S:Cafe;P:ignored;;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals(WifiSecurity.NONE, it.security)
                        assertEquals("", it.password)
                    },
            ),
            case("wifi wep", "WIFI:T:WEP;S:Old;P:12345;;", check = typed<ScanContent.Wifi> { assertEquals(WifiSecurity.WEP, it.security) }),
            case(
                "wifi wpa2 maps to wpa",
                "WIFI:T:WPA2;S:A;P:b;;",
                check = typed<ScanContent.Wifi> { assertEquals(WifiSecurity.WPA, it.security) },
            ),
            case(
                "wifi sae maps to wpa",
                "WIFI:T:SAE;S:A;P:b;;",
                check = typed<ScanContent.Wifi> { assertEquals(WifiSecurity.WPA, it.security) },
            ),
            case("wifi hidden", "WIFI:S:Ghost;T:WPA;P:pw;H:true;;", check = typed<ScanContent.Wifi> { assertTrue(it.hidden) }),
            case(
                "wifi quoted hex-looking values",
                "WIFI:T:WPA;S:\"1234\";P:\"deadbeef\";;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals("1234", it.ssid)
                        assertEquals("deadbeef", it.password)
                    },
            ),
            case("wifi password first", "WIFI:P:pw;T:WPA;S:Net;;", check = typed<ScanContent.Wifi> { assertEquals("Net", it.ssid) }),
            case(
                "wifi without type but with password is wpa",
                "WIFI:S:Net;P:pw;;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals(WifiSecurity.WPA, it.security)
                    },
            ),
            case(
                "wifi without type or password is open",
                "WIFI:S:Net;;",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals(WifiSecurity.NONE, it.security)
                    },
            ),
            case(
                "wifi lower-case prefix, no terminator, CRLF",
                "wifi:T:WPA;S:Net;P:pw\r\n",
                check =
                    typed<ScanContent.Wifi> {
                        assertEquals("pw", it.password)
                    },
            ),
            case("wifi missing ssid is text", "WIFI:T:WPA;P:pw;;", check = type(ContentType.TEXT)),
            case("wifi garbage is text", "WIFI:", check = type(ContentType.TEXT)),
            // ---- contacts ----
            case(
                "vcard 3.0 full",
                vcard30,
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("John Q. Doe", it.name)
                        assertEquals("Acme Corp, Research", it.organization)
                        assertEquals("Chief Scientist", it.jobTitle)
                        assertEquals(listOf("+1-555-0100", "+1-555-0101"), it.phones)
                        assertEquals(listOf("john@acme.com", "jd@home.org"), it.emails)
                        assertEquals("1 Main St, Springfield, IL, 62701, USA", it.address)
                        assertEquals("https://acme.com", it.website)
                        assertEquals("Met at conf\nCall back", it.note)
                    },
            ),
            case(
                "vcard with CRLF line endings",
                vcard30.replace("\n", "\r\n"),
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("John Q. Doe", it.name)
                        assertEquals(2, it.phones.size)
                    },
            ),
            case(
                "vcard 2.1 bare type params",
                "BEGIN:VCARD\nVERSION:2.1\nN:Smith;Anna;;;\nTEL;CELL:555 1234\nTEL;HOME;VOICE:555 9999\nEMAIL;INTERNET:anna@x.io\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("Anna Smith", it.name)
                        assertEquals(2, it.phones.size)
                        assertEquals("anna@x.io", it.emails.single())
                    },
            ),
            case(
                "vcard 4.0 tel uri and group prefix",
                "BEGIN:VCARD\nVERSION:4.0\nFN:Lee Wong\nitem1.TEL;VALUE=uri;TYPE=cell:tel:+65-9123-4567\nEMAIL:lee@sg.com\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("Lee Wong", it.name)
                        assertEquals(listOf("+65-9123-4567"), it.phones)
                    },
            ),
            case(
                "vcard folded lines",
                "BEGIN:VCARD\nVERSION:3.0\nFN:Very Long\n  Name Here\nNOTE:line one\n  and two\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("Very Long Name Here", it.name)
                        assertEquals("line one and two", it.note)
                    },
            ),
            case(
                "vcard N only is First Last",
                "BEGIN:VCARD\nN:Doe;John;;;\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("John Doe", it.name)
                    },
            ),
            case(
                "vcard quoted-printable utf8",
                "BEGIN:VCARD\nVERSION:2.1\nFN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:Ren=C3=A9 Dupont\nNOTE;ENCODING=QUOTED-PRINTABLE:a=\nb\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("Ren\u00E9 Dupont", it.name)
                        assertEquals("ab", it.note)
                    },
            ),
            case(
                "vcard without END is still parsed",
                "BEGIN:VCARD\nFN:Open Ended\nTEL:123456",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("Open Ended", it.name)
                    },
            ),
            case("empty vcard is text", "BEGIN:VCARD\nVERSION:3.0\nEND:VCARD", check = type(ContentType.TEXT)),
            case(
                "duplicate phones are collapsed",
                "BEGIN:VCARD\nFN:A\nTEL:111\nTEL:111\nEND:VCARD",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals(1, it.phones.size)
                    },
            ),
            case(
                "mecard basic",
                "MECARD:N:Doe,John;TEL:5551234;EMAIL:j@d.com;ORG:Acme;URL:https\\://d.com;NOTE:hi;;",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals("John Doe", it.name)
                        assertEquals(listOf("5551234"), it.phones)
                        assertEquals("https://d.com", it.website)
                        assertEquals("Acme", it.organization)
                    },
            ),
            case(
                "mecard multiple tel and address",
                "MECARD:N:Roe,Jane;TEL:111;TEL:222;ADR:,,5 High St,Leeds,,LS1,UK;;",
                check =
                    typed<ScanContent.Contact> {
                        assertEquals(listOf("111", "222"), it.phones)
                        assertEquals("5 High St, Leeds, LS1, UK", it.address)
                    },
            ),
            // ---- events ----
            case(
                "event utc",
                "BEGIN:VEVENT\nSUMMARY:Launch\nDTSTART:20240501T100000Z\nDTEND:20240501T113000Z\nLOCATION:HQ\nDESCRIPTION:Cake\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertEquals("Launch", it.summary)
                        assertEquals(ms("2024-05-01T10:00:00Z"), it.startMillis)
                        assertEquals(ms("2024-05-01T11:30:00Z"), it.endMillis)
                        assertEquals("HQ", it.location)
                        assertEquals("Cake", it.description)
                        assertEquals(false, it.allDay)
                    },
            ),
            case(
                "event all day",
                "BEGIN:VEVENT\nSUMMARY:Holiday\nDTSTART;VALUE=DATE:20241225\nDTEND;VALUE=DATE:20241226\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertTrue(it.allDay)
                        assertEquals(ms("2024-12-25T00:00:00Z"), it.startMillis)
                        assertEquals(ms("2024-12-26T00:00:00Z"), it.endMillis)
                    },
            ),
            case(
                "event floating uses the default zone",
                "BEGIN:VEVENT\nSUMMARY:Local\nDTSTART:20240501T090000\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertEquals(ms("2024-05-01T09:00:00Z"), it.startMillis)
                    },
            ),
            case(
                "event valid TZID is honoured",
                "BEGIN:VEVENT\nSUMMARY:NY\nDTSTART;TZID=America/New_York:20240501T090000\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertEquals(ms("2024-05-01T13:00:00Z"), it.startMillis)
                    },
            ),
            case(
                "event unknown TZID falls back to the default zone",
                "BEGIN:VEVENT\nSUMMARY:Win\nDTSTART;TZID=Eastern Standard Time:20240501T090000\nEND:VEVENT",
                check = typed<ScanContent.Event> { assertEquals(ms("2024-05-01T09:00:00Z"), it.startMillis) },
            ),
            case(
                "event inside vcalendar with CRLF and escapes",
                "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nSUMMARY:Lunch\\, then walk\r\nLOCATION:Cafe\\; Main St\r\nDESCRIPTION:Line one\\nLine\r\n  two\r\nDTSTART:20240601T120000Z\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n",
                check =
                    typed<ScanContent.Event> {
                        assertEquals("Lunch, then walk", it.summary)
                        assertEquals("Cafe; Main St", it.location)
                        assertEquals("Line one\nLine two", it.description)
                    },
            ),
            case(
                "event duration gives an end",
                "BEGIN:VEVENT\nSUMMARY:D\nDTSTART:20240501T100000Z\nDURATION:PT1H30M\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertEquals(ms("2024-05-01T11:30:00Z"), it.endMillis)
                    },
            ),
            case(
                "event with impossible date keeps the summary",
                "BEGIN:VEVENT\nSUMMARY:Bad\nDTSTART:20241399T100000Z\nEND:VEVENT",
                check =
                    typed<ScanContent.Event> {
                        assertNull(it.startMillis)
                        assertEquals("Bad", it.summary)
                    },
            ),
            case("event missing END still parses", "BEGIN:VEVENT\nSUMMARY:Open\nDTSTART:20240501T100000Z", check = type(ContentType.EVENT)),
            case("empty vevent is text", "BEGIN:VEVENT\nEND:VEVENT", check = type(ContentType.TEXT)),
            case("vcalendar without event is text", "BEGIN:VCALENDAR\nVERSION:2.0\nEND:VCALENDAR", check = type(ContentType.TEXT)),
            // ---- phone ----
            case("tel uri", "tel:+14155550123", check = typed<ScanContent.Phone> { assertEquals("+14155550123", it.number) }),
            case(
                "tel uri percent encoded",
                "TEL:%2B1%20555%200100",
                check = typed<ScanContent.Phone> { assertEquals("+1 555 0100", it.number) },
            ),
            case(
                "plain international number",
                "+1 (415) 555-0123",
                check = typed<ScanContent.Phone> { assertEquals("+1 (415) 555-0123", it.number) },
            ),
            case("plain ten digit number", "4155550123", check = type(ContentType.PHONE)),
            case("six digits is not a phone", "123456", check = type(ContentType.TEXT)),
            case("long digit run is not a phone", "1234567890123456789", check = type(ContentType.TEXT)),
            case("date is not a phone", "2024-05-01", check = type(ContentType.TEXT)),
            case("number with extension letters is text", "+1 555 123 4567 ext 5", check = type(ContentType.TEXT)),
            case("empty tel is text", "tel:", check = type(ContentType.TEXT)),
            case("digits on code 128 are not a phone", "4155550123", CodeFormat.CODE_128, type(ContentType.TEXT)),
            // ---- e-mail ----
            case(
                "mailto simple",
                "mailto:ann@example.com",
                check = typed<ScanContent.Email> { assertEquals("ann@example.com", it.address) },
            ),
            case(
                "mailto with subject and body",
                "mailto:ann@example.com?subject=Hello%20World&body=Line%201%0ALine%202",
                check =
                    typed<ScanContent.Email> {
                        assertEquals("Hello World", it.subject)
                        assertEquals("Line 1\nLine 2", it.body)
                    },
            ),
            case(
                "mailto percent decodes utf8",
                "mailto:a@b.co?subject=Caf%C3%A9",
                check =
                    typed<ScanContent.Email> {
                        assertEquals("Caf\u00E9", it.subject)
                    },
            ),
            case("mailto upper case", "MAILTO:A@B.CO", check = typed<ScanContent.Email> { assertEquals("A@B.CO", it.address) }),
            case("mailto without at is text", "mailto:nobody", check = type(ContentType.TEXT)),
            case(
                "matmsg",
                "MATMSG:TO:bob@x.com;SUB:Hi\\;there;BODY:See you;;",
                check =
                    typed<ScanContent.Email> {
                        assertEquals("bob@x.com", it.address)
                        assertEquals("Hi;there", it.subject)
                        assertEquals("See you", it.body)
                    },
            ),
            case(
                "bare email",
                "first.last+tag@sub.example.co.uk",
                check =
                    typed<ScanContent.Email> {
                        assertEquals("first.last+tag@sub.example.co.uk", it.address)
                    },
            ),
            // ---- sms ----
            case(
                "SMSTO number and message",
                "SMSTO:+15551234567:Hello there: friend",
                check =
                    typed<ScanContent.Sms> {
                        assertEquals("+15551234567", it.number)
                        assertEquals("Hello there: friend", it.message)
                    },
            ),
            case(
                "sms with body query",
                "sms:+15551234567?body=hi%20there",
                check =
                    typed<ScanContent.Sms> {
                        assertEquals("+15551234567", it.number)
                        assertEquals("hi there", it.message)
                    },
            ),
            case("smsto number only", "smsto:5551234", check = typed<ScanContent.Sms> { assertEquals("", it.message) }),
            case("mms prefix", "mms:+1555?body=pic", check = type(ContentType.SMS)),
            case("sms with ampersand in message", "smsto:555:a & b", check = typed<ScanContent.Sms> { assertEquals("a & b", it.message) }),
            case("sms without number is text", "sms:", check = type(ContentType.TEXT)),
            // ---- geo ----
            case(
                "geo plain",
                "geo:37.786971,-122.399677",
                check =
                    typed<ScanContent.Location> {
                        assertEquals(37.786971, it.latitude, 1e-9)
                        assertEquals(-122.399677, it.longitude, 1e-9)
                    },
            ),
            case(
                "geo with altitude and crs",
                "geo:48.2010,16.3695,183;crs=wgs84;u=40",
                check =
                    typed<ScanContent.Location> {
                        assertEquals(16.3695, it.longitude, 1e-9)
                    },
            ),
            case(
                "geo zero zero with query label",
                "geo:0,0?q=Eiffel+Tower",
                check =
                    typed<ScanContent.Location> {
                        assertEquals("Eiffel Tower", it.label)
                    },
            ),
            case(
                "geo q with coordinates and label",
                "geo:0,0?q=34.99,-106.61(Treasure)",
                check =
                    typed<ScanContent.Location> {
                        assertEquals(34.99, it.latitude, 1e-9)
                        assertEquals("Treasure", it.label)
                    },
            ),
            case("geo out of range is text", "geo:91.0,10.0", check = type(ContentType.TEXT)),
            case("geo without coordinates is text", "geo:?q=pizza", check = type(ContentType.TEXT)),
            // ---- product ----
            case("ean13", "5901234123457", CodeFormat.EAN_13, typed<ScanContent.Product> { assertEquals("5901234123457", it.number) }),
            case("ean8", "96385074", CodeFormat.EAN_8, type(ContentType.PRODUCT)),
            case("upc e", "01234565", CodeFormat.UPC_E, type(ContentType.PRODUCT)),
            case("upc a", "036000291452", CodeFormat.UPC_A, type(ContentType.PRODUCT)),
            case("code128 with 13 digits is a product", "4006381333931", CodeFormat.CODE_128, type(ContentType.PRODUCT)),
            case("code39 letters are text", "ABC-123", CodeFormat.CODE_39, type(ContentType.TEXT)),
            case("ean13 with letters is text", "ABC", CodeFormat.EAN_13, type(ContentType.TEXT)),
            case("itf 14 digits is text", "12345678901231", CodeFormat.ITF, type(ContentType.TEXT)),
            case("qr with 13 digits is a phone number, not a product", "5901234123457", QR_CODE, type(ContentType.PHONE)),
            // ---- text / unknown ----
            case("plain text", "Hello, world", check = type(ContentType.TEXT)),
            case(
                "multi line text with CRLF",
                "line one\r\nline two\r\n",
                check =
                    typed<ScanContent.Text> {
                        assertEquals("line one\r\nline two\r\n", it.rawValue)
                    },
            ),
            case("unicode and emoji", "\u4F60\u597D \uD83D\uDE00 caf\u00E9", check = type(ContentType.TEXT)),
            case("note with colon is text", "Note: buy milk", check = type(ContentType.TEXT)),
            case("empty", "", check = type(ContentType.UNKNOWN)),
            case("whitespace only", " \t\r\n ", check = type(ContentType.UNKNOWN)),
            case("control characters", "ab\u0000\u0001cd", check = type(ContentType.UNKNOWN)),
            case("replacement character", "ab\uFFFDcd", check = type(ContentType.UNKNOWN)),
            case("unknown format with binary", "\u0002\u0003\u0004", CodeFormat.UNKNOWN, type(ContentType.UNKNOWN)),
            case("unknown format with text", "hello", CodeFormat.UNKNOWN, type(ContentType.TEXT)),
            case("BOM before url", "\uFEFFhttps://example.com", check = website("https://example.com")),
            case("huge text", "a".repeat(1_000_000), check = type(ContentType.TEXT)),
            case("huge url", "https://example.com/" + "a".repeat(200_000), check = type(ContentType.WEBSITE)),
            case("huge digit run on qr", "1".repeat(100_000), check = type(ContentType.TEXT)),
            case("huge vcard", "BEGIN:VCARD\nFN:Big\n" + "NOTE:x\n".repeat(50_000) + "END:VCARD", check = type(ContentType.CONTACT)),
        )

    @TestFactory
    fun `payload table`(): List<DynamicTest> =
        cases.map { c ->
            dynamicTest(c.name) {
                val parsed = parser.parse(c.raw, c.format)
                assertEquals(c.raw, parsed.rawValue, "raw value must be preserved")
                assertEquals(c.format, parsed.format)
                c.check(parsed)
            }
        }

    @Test
    fun `table has at least sixty cases`() {
        assertTrue(cases.size >= 60, "only ${cases.size} cases")
        assertEquals(cases.size, cases.map { it.name }.toSet().size, "case names must be unique")
    }

    @Test
    fun `floating times use the configured zone`() {
        val plus2 = ScanContentParser(ZoneOffset.ofHours(2))
        val c = plus2.parse("BEGIN:VEVENT\nSUMMARY:S\nDTSTART:20240501T100000\nEND:VEVENT", QR_CODE) as ScanContent.Event
        assertEquals(ms("2024-05-01T08:00:00Z"), c.startMillis)
    }

    @Test
    fun `default constructor works`() {
        assertEquals(ContentType.TEXT, ScanContentParser().parse("hi", QR_CODE).type)
    }

    @Test
    fun `never throws on random input`() {
        val rnd = Random(42)
        val alphabet = "ABCabc012:;,\\\"=?&%@./\n\r +-()[]{}<>!#$*_\u00E9\u4F60\u0000"
        val prefixes =
            listOf(
                "",
                "WIFI:",
                "BEGIN:VCARD\n",
                "MECARD:",
                "BEGIN:VEVENT\n",
                "mailto:",
                "sms:",
                "geo:",
                "tel:",
                "URLTO:",
                "MEBKM:",
                "https://",
                "MATMSG:",
            )
        repeat(3000) {
            val body = buildString { repeat(rnd.nextInt(60)) { append(alphabet[rnd.nextInt(alphabet.length)]) } }
            val raw = prefixes[rnd.nextInt(prefixes.size)] + body
            val format = CodeFormat.values()[rnd.nextInt(CodeFormat.values().size)]
            val parsed = parser.parse(raw, format)
            assertEquals(raw, parsed.rawValue)
        }
    }

    @Test
    fun `parser handles type detection order`() {
        // A wifi payload that looks like a url inside must still be wifi.
        assertEquals(ContentType.WIFI, parser.parse("WIFI:S:http://x.com;T:nopass;;", QR_CODE).type)
        // An email address inside a url wrapper stays a website.
        assertEquals(ContentType.WEBSITE, parser.parse("https://example.com/?to=a@b.com", QR_CODE).type)
    }
}

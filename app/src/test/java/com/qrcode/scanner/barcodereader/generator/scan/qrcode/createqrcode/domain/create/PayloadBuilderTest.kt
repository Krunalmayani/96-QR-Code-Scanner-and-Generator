package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PayloadBuilderTest {
    private fun build(spec: CreateSpec) = PayloadBuilder.build(spec)

    // ---- website / text / app ----
    @Test fun websiteAddsHttps() = assertEquals("https://example.com", build(CreateSpec.Website("example.com")))

    @Test fun websiteKeepsScheme() = assertEquals("http://example.com/a?b=1", build(CreateSpec.Website("http://example.com/a?b=1")))

    @Test fun websiteTrims() = assertEquals("https://example.com", build(CreateSpec.Website("  https://example.com \n")))

    @Test fun textIsVerbatim() = assertEquals("Hello\nWorld, ;: ", build(CreateSpec.Text("Hello\nWorld, ;: ")))

    @Test fun appStoreAsIs() =
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.x",
            build(CreateSpec.AppStore("https://play.google.com/store/apps/details?id=com.x")),
        )

    @Test fun appStoreMarketSchemeKept() =
        assertEquals(
            "market://details?id=com.x",
            build(CreateSpec.AppStore("market://details?id=com.x")),
        )

    // ---- phone / sms ----
    @Test fun phoneStripsFormatting() = assertEquals("tel:2065550198", build(CreateSpec.Phone("(206) 555-0198")))

    @Test fun phoneKeepsPlus() = assertEquals("tel:+14155550142", build(CreateSpec.Phone(" +1 (415) 555-0142 ")))

    @Test fun smsWithMessage() = assertEquals("SMSTO:+14155550142:Hi there", build(CreateSpec.Sms("+1 415 555 0142", "Hi there")))

    @Test fun smsWithoutMessage() = assertEquals("SMSTO:5550142", build(CreateSpec.Sms("555-0142")))

    @Test fun smsMessageKeepsColon() = assertEquals("SMSTO:123:a:b", build(CreateSpec.Sms("123", "a:b")))

    // ---- email ----
    @Test fun emailPlain() = assertEquals("mailto:a@b.co", build(CreateSpec.Email("a@b.co")))

    @Test fun emailSubjectBody() =
        assertEquals(
            "mailto:a@b.co?subject=Hello%20World&body=Line%201%0ALine%202",
            build(CreateSpec.Email("a@b.co", "Hello World", "Line 1\nLine 2")),
        )

    @Test fun emailOnlySubject() = assertEquals("mailto:a@b.co?subject=Hi", build(CreateSpec.Email(" a@b.co ", "Hi")))

    @Test fun emailEncodesReservedAndUnicode() =
        assertEquals(
            "mailto:a@b.co?subject=caf%C3%A9%20%26%20co%3D%3F",
            build(CreateSpec.Email("a@b.co", "café & co=?")),
        )

    // ---- wifi ----
    @Test fun wifiWpa() = assertEquals("WIFI:T:WPA;S:CafeNova_Guest;P:secret123;;", build(CreateSpec.Wifi("CafeNova_Guest", "secret123")))

    @Test fun wifiHidden() =
        assertEquals(
            "WIFI:T:WPA;S:Net;P:pw123456;H:true;;",
            build(CreateSpec.Wifi("Net", "pw123456", WifiSecurity.WPA, true)),
        )

    @Test fun wifiWep() = assertEquals("WIFI:T:WEP;S:Old;P:12345;;", build(CreateSpec.Wifi("Old", "12345", WifiSecurity.WEP)))

    @Test fun wifiOpenIgnoresPassword() =
        assertEquals(
            "WIFI:T:nopass;S:Open;;",
            build(CreateSpec.Wifi("Open", "ignored", WifiSecurity.NONE)),
        )

    @Test fun wifiEscapesSpecials() =
        assertEquals(
            "WIFI:T:WPA;S:a\\;b\\,c\\:d;P:p\\\\w\\\"x;;",
            build(CreateSpec.Wifi("a;b,c:d", "p\\w\"x")),
        )

    // ---- location ----
    @Test fun geoPlain() = assertEquals("geo:37.7749,-122.4194", build(CreateSpec.Location("37.7749", "-122.4194")))

    @Test fun geoLabel() = assertEquals("geo:1.5,2?q=Golden%20Gate", build(CreateSpec.Location("1.50", "2", "Golden Gate")))

    @Test fun geoCommaDecimals() = assertEquals("geo:37.5,-122.25", build(CreateSpec.Location("37,5", "-122,25")))

    @Test fun geoNegativeZero() = assertEquals("geo:0,0", build(CreateSpec.Location("-0.0", "0.000")))

    // ---- social ----
    @Test fun socialInstagram() =
        assertEquals(
            "https://instagram.com/lumen.studio",
            build(CreateSpec.Social(SocialPlatform.INSTAGRAM, "@lumen.studio")),
        )

    @Test fun socialFacebook() = assertEquals("https://facebook.com/lumen", build(CreateSpec.Social(SocialPlatform.FACEBOOK, " lumen ")))

    @Test fun socialX() = assertEquals("https://x.com/lumen_hq", build(CreateSpec.Social(SocialPlatform.X, "lumen_hq")))

    @Test fun socialLinkedin() =
        assertEquals(
            "https://linkedin.com/in/sophie-lambert",
            build(CreateSpec.Social(SocialPlatform.LINKEDIN, "sophie-lambert")),
        )

    @Test fun socialYoutube() =
        assertEquals(
            "https://youtube.com/@lumenstudio",
            build(CreateSpec.Social(SocialPlatform.YOUTUBE, "@lumenstudio")),
        )

    @Test fun socialWhatsapp() =
        assertEquals(
            "https://wa.me/14155550142",
            build(CreateSpec.Social(SocialPlatform.WHATSAPP, "+1 (415) 555-0142")),
        )

    @Test fun socialTelegram() = assertEquals("https://t.me/lumen_chat", build(CreateSpec.Social(SocialPlatform.TELEGRAM, "@lumen_chat")))

    // ---- contact ----
    @Test fun contactFull() {
        val spec =
            CreateSpec.Contact(
                name = "Sophie Lambert",
                organization = "Lumen Studio",
                jobTitle = "Creative Director",
                phone = "+1 (415) 555-0142",
                email = "sophie.lambert@lumen.studio",
                address = "48 Market Street, Suite 6\nAustin, TX 78701",
                website = "lumen.studio",
                note = "Met at; the fair",
            )
        val expected =
            listOf(
                "BEGIN:VCARD",
                "VERSION:3.0",
                "N:Lambert;Sophie;;;",
                "FN:Sophie Lambert",
                "ORG:Lumen Studio",
                "TITLE:Creative Director",
                "TEL;TYPE=CELL:+14155550142",
                "EMAIL:sophie.lambert@lumen.studio",
                "ADR;TYPE=WORK:;;48 Market Street\\, Suite 6\\nAustin\\, TX 78701;;;;",
                "URL:https://lumen.studio",
                "NOTE:Met at\\; the fair",
                "END:VCARD",
            ).joinToString("\n")
        assertEquals(expected, build(spec))
    }

    @Test fun contactMinimalSingleName() =
        assertEquals("BEGIN:VCARD\nVERSION:3.0\nN:;Prince;;;\nFN:Prince\nEND:VCARD", build(CreateSpec.Contact(name = "Prince")))

    @Test fun contactMultiWordGivenName() =
        assertEquals(
            "BEGIN:VCARD\nVERSION:3.0\nN:Watson;Mary Jane;;;\nFN:Mary Jane Watson\nEND:VCARD",
            build(CreateSpec.Contact(name = "Mary Jane Watson")),
        )

    @Test fun contactOrganizationOnlyUsesOrgAsFn() {
        val out = build(CreateSpec.Contact(organization = "Lumen"))
        assertEquals("BEGIN:VCARD\nVERSION:3.0\nN:;;;;\nFN:Lumen\nORG:Lumen\nEND:VCARD", out)
    }

    // ---- event ----
    @Test fun eventTimed() {
        val spec =
            CreateSpec.Event(
                title = "Product Launch Meetup",
                location = "Harbor Hall, 120 Pier Ave",
                description = "Doors open at 5:30 PM.",
                startMillis = utcMillis(2026, 11, 14, 18, 0),
                endMillis = utcMillis(2026, 11, 14, 20, 30),
            )
        val expected =
            listOf(
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//QR Code Scanner and Generator//EN",
                "BEGIN:VEVENT",
                "SUMMARY:Product Launch Meetup",
                "DTSTART:20261114T180000Z",
                "DTEND:20261114T203000Z",
                "LOCATION:Harbor Hall\\, 120 Pier Ave",
                "DESCRIPTION:Doors open at 5:30 PM.",
                "END:VEVENT",
                "END:VCALENDAR",
            ).joinToString("\n")
        assertEquals(expected, build(spec))
    }

    @Test fun eventTimedWithoutEnd() {
        val out = build(CreateSpec.Event(title = "T", startMillis = utcMillis(2024, 2, 29, 23, 59, 59)))
        assertEquals(true, out.contains("DTSTART:20240229T235959Z"))
        assertEquals(false, out.contains("DTEND"))
    }

    @Test fun eventAllDaySingleDay() {
        val d = utcMillis(2026, 11, 14)
        val out = build(CreateSpec.Event(title = "Holiday", allDay = true, startMillis = d, endMillis = d))
        assertEquals(true, out.contains("DTSTART;VALUE=DATE:20261114\n"))
        assertEquals(true, out.contains("DTEND;VALUE=DATE:20261115\n"))
    }

    @Test fun eventAllDayRollsOverYear() {
        val out =
            build(
                CreateSpec.Event(title = "NYE", allDay = true, startMillis = utcMillis(2026, 12, 30), endMillis = utcMillis(2026, 12, 31)),
            )
        assertEquals(true, out.contains("DTSTART;VALUE=DATE:20261230\n"))
        assertEquals(true, out.contains("DTEND;VALUE=DATE:20270101\n"))
    }

    @Test fun eventEscapesTitle() {
        val out = build(CreateSpec.Event(title = "A; B, C\nD", startMillis = 0L))
        assertEquals(true, out.contains("SUMMARY:A\\; B\\, C\\nD\n"))
        assertEquals(true, out.contains("DTSTART:19700101T000000Z"))
    }

    @Test fun datesBeforeEpoch() = assertEquals("19691231T235959Z", IcsDates.utc(-1L))

    // ---- barcodes ----
    @Test fun barcodeEan13AddsCheckDigit() =
        assertEquals(
            "4006381333931",
            build(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, "400638133393")),
        )

    @Test fun barcodeEan13FullKept() =
        assertEquals(
            "4006381333931",
            build(CreateSpec.Barcode(CreateType.BARCODE_EAN_13, " 4006381333931 ")),
        )

    @Test fun barcodeEan8AddsCheckDigit() = assertEquals("96385074", build(CreateSpec.Barcode(CreateType.BARCODE_EAN_8, "9638507")))

    @Test fun barcodeUpcaAddsCheckDigit() = assertEquals("036000291452", build(CreateSpec.Barcode(CreateType.BARCODE_UPC_A, "03600029145")))

    @Test fun barcodeCode39Uppercased() = assertEquals("ABC-123", build(CreateSpec.Barcode(CreateType.BARCODE_CODE_39, "abc-123")))

    @Test fun barcodeCode128Untouched() = assertEquals("Hello 123", build(CreateSpec.Barcode(CreateType.BARCODE_CODE_128, "Hello 123")))

    // ---- format / spec ----
    @Test fun formatOfQrTypes() {
        assertEquals(CodeFormat.QR_CODE, PayloadBuilder.formatOf(CreateSpec.Website("x.com")))
        assertEquals(CodeFormat.QR_CODE, PayloadBuilder.formatOf(CreateSpec.Social()))
    }

    @Test fun formatOfBarcode() {
        assertEquals(CodeFormat.ITF, PayloadBuilder.formatOf(CreateSpec.Barcode(CreateType.BARCODE_ITF, "12")))
        assertEquals(CodeFormat.AZTEC, PayloadBuilder.formatOf(CreateSpec.Barcode(CreateType.BARCODE_AZTEC, "x")))
    }

    @Test fun barcodeSpecRejectsNonBarcodeType() {
        assertThrows<IllegalArgumentException> { CreateSpec.Barcode(CreateType.WEBSITE, "x") }
    }

    @Test fun percentEncodeKeepsUnreserved() = assertEquals("Az09-_.~%20%2F", PayloadBuilder.percentEncode("Az09-_.~ /"))
}

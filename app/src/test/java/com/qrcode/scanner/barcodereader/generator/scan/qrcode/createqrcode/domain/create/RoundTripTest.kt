package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/** Build payload, encode, rasterise, decode with ZXing: a created code must scan back to its payload. */
class RoundTripTest {
    private fun sample(type: CreateType): CreateSpec =
        when (type) {
            CreateType.WEBSITE -> CreateSpec.Website("brewhouse-coffee.com/menu?table=12&x=café")
            CreateType.TEXT -> CreateSpec.Text("Meeting notes: ship v2.4 on Friday.\nGrüße 日本語 😀; done,")
            CreateType.CONTACT ->
                CreateSpec.Contact(
                    name = "Sophie Lambert",
                    organization = "Lumen Studio",
                    jobTitle = "Creative Director",
                    phone = "+1 (415) 555-0142",
                    email = "sophie.lambert@lumen.studio",
                    address = "48 Market Street, Suite 6\nAustin, TX 78701",
                    website = "lumen.studio",
                    note = "Met at the fair; call back",
                )
            CreateType.WIFI -> CreateSpec.Wifi("Cafe;Nova:Guest", "p\"ass,w\\ord1", WifiSecurity.WPA, hidden = true)
            CreateType.EMAIL -> CreateSpec.Email("hello@lumen.studio", "Hello & welcome", "Line 1\nLine 2 café")
            CreateType.PHONE -> CreateSpec.Phone("(206) 555-0198")
            CreateType.SMS -> CreateSpec.Sms("+1 415 555 0142", "Running late: see you at 6")
            CreateType.LOCATION -> CreateSpec.Location("37.7749", "-122.4194", "Golden Gate Bridge")
            CreateType.EVENT ->
                CreateSpec.Event(
                    title = "Product Launch Meetup",
                    location = "Harbor Hall, 120 Pier Ave, San Francisco",
                    description = "Meet the team and see the live demo. Doors open at 5:30 PM.",
                    startMillis = utcMillis(2026, 11, 14, 18, 0),
                    endMillis = utcMillis(2026, 11, 14, 20, 30),
                )
            CreateType.SOCIAL -> CreateSpec.Social(SocialPlatform.INSTAGRAM, "@lumen.studio")
            CreateType.APP_STORE -> CreateSpec.AppStore("https://play.google.com/store/apps/details?id=com.lumen.studio")
            CreateType.BARCODE_EAN_13 -> CreateSpec.Barcode(type, "400638133393")
            CreateType.BARCODE_EAN_8 -> CreateSpec.Barcode(type, "9638507")
            CreateType.BARCODE_UPC_A -> CreateSpec.Barcode(type, "03600029145")
            CreateType.BARCODE_CODE_128 -> CreateSpec.Barcode(type, "Hello-123 abc")
            CreateType.BARCODE_CODE_39 -> CreateSpec.Barcode(type, "code-39 ok")
            CreateType.BARCODE_ITF -> CreateSpec.Barcode(type, "1234567890")
            CreateType.BARCODE_PDF_417 -> CreateSpec.Barcode(type, "PDF417 sample text 1234567890")
            CreateType.BARCODE_DATA_MATRIX -> CreateSpec.Barcode(type, "DataMatrix 2026")
            CreateType.BARCODE_AZTEC -> CreateSpec.Barcode(type, "Aztec code sample")
        }

    @ParameterizedTest
    @EnumSource(CreateType::class)
    fun everyCreateTypeScansBackToItsPayload(type: CreateType) {
        val spec = sample(type)
        assertEquals(emptyMap<FormField, FieldError>(), Validators.validate(spec), "sample for $type must be valid")
        val payload = PayloadBuilder.build(spec)
        val format = PayloadBuilder.formatOf(spec)
        val matrix = CodeEncoder.encode(spec, ErrorCorrection.M, heightPx = 40)
        assertEquals(payload, Raster.roundTrip(format, matrix), "round trip of $type")
    }

    @Test fun barcodePayloadsIncludeCheckDigits() {
        assertEquals("4006381333931", PayloadBuilder.build(sample(CreateType.BARCODE_EAN_13)))
        assertEquals("96385074", PayloadBuilder.build(sample(CreateType.BARCODE_EAN_8)))
        assertEquals("036000291452", PayloadBuilder.build(sample(CreateType.BARCODE_UPC_A)))
        assertEquals("CODE-39 OK", PayloadBuilder.build(sample(CreateType.BARCODE_CODE_39)))
    }

    @ParameterizedTest
    @EnumSource(ErrorCorrection::class)
    fun qrRoundTripAtEveryErrorCorrectionLevel(level: ErrorCorrection) {
        for (type in listOf(CreateType.WEBSITE, CreateType.TEXT, CreateType.CONTACT, CreateType.EVENT)) {
            val payload = PayloadBuilder.build(sample(type))
            val matrix = CodeEncoder.encodeQr(payload, level)
            assertEquals(payload, Raster.roundTrip(CodeFormat.QR_CODE, matrix), "$type at $level")
        }
    }

    @Test fun longestAllowedTextScans() {
        val payload = "Lorem ipsum dolor sit amet. ".repeat(40).take(Limits.TEXT)
        val matrix = CodeEncoder.encodeQr(payload, ErrorCorrection.H)
        assertEquals(payload, Raster.roundTrip(CodeFormat.QR_CODE, matrix))
    }

    @Test fun allDayEventAndFreeTextScan() {
        val spec = CreateSpec.Event("Holiday", allDay = true, startMillis = utcMillis(2026, 12, 31), endMillis = utcMillis(2026, 12, 31))
        val payload = PayloadBuilder.build(spec)
        assertEquals(payload, Raster.roundTrip(CodeFormat.QR_CODE, CodeEncoder.encodeQr(payload, ErrorCorrection.M)))
    }

    @Test fun unicodeInTwoDimensionalBarcodes() {
        for (f in listOf(CodeFormat.PDF_417, CodeFormat.AZTEC, CodeFormat.DATA_MATRIX)) {
            val text = "Café über 123"
            val decoded = Raster.roundTrip(f, CodeEncoder.encodeBarcode(f, text))
            assertEquals(text, decoded, "unicode in $f")
        }
    }

    @Test fun pdf417AndDataMatrixLongText() {
        val text = "0123456789 ABCDEFGHIJ ".repeat(20)
        for (f in listOf(CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC)) {
            assertEquals(text, Raster.roundTrip(f, CodeEncoder.encodeBarcode(f, text)), "$f")
        }
    }

    // ---- the styled vector picture must still scan ----
    @ParameterizedTest
    @EnumSource(DotShape::class)
    fun styledVectorQrScansForEveryDotShapeAndEyeShape(dot: DotShape) {
        val payload = "https://www.brewhouse-coffee.com/menu?table=12"
        for (eye in EyeShape.entries) {
            val style = QrStyle(dotShape = dot, eyeShape = eye, errorCorrection = ErrorCorrection.Q, foreground = 0xFF000000)
            val matrix = CodeEncoder.encodeQr(payload, QrVectorBuilder.effectiveErrorCorrection(style))
            val image = QrVectorBuilder.build(matrix, style, quietZoneModules = 4)
            val (px, w, h) = Raster.render(image, 8)
            assertEquals(payload, Raster.decode(px, w, h, CodeFormat.QR_CODE), "$dot / $eye")
        }
    }

    @Test fun styledVectorQrWithLogoAreaStillScans() {
        val payload = "https://www.brewhouse-coffee.com/menu?table=12&utm_source=poster&utm_campaign=autumn"
        val style = QrStyle(errorCorrection = ErrorCorrection.L, logoUri = "content://logo", foreground = 0xFF000000)
        assertEquals(ErrorCorrection.Q, QrVectorBuilder.effectiveErrorCorrection(style))
        val matrix = CodeEncoder.encodeQr(payload, QrVectorBuilder.effectiveErrorCorrection(style))
        val image = QrVectorBuilder.build(matrix, style, quietZoneModules = 4)
        val (px, w, h) = Raster.render(image, 8)
        assertEquals(payload, Raster.decode(px, w, h, CodeFormat.QR_CODE))
    }

    @Test fun styledVectorQrWithFrameScans() {
        val payload = "https://example.com/frame"
        val style = QrStyle(frameText = "Scan me", foreground = 0xFF000000)
        val image = QrVectorBuilder.build(CodeEncoder.encodeQr(payload, ErrorCorrection.M), style, quietZoneModules = 4)
        val (px, w, h) = Raster.render(image, 8)
        assertEquals(payload, Raster.decode(px, w, h, CodeFormat.QR_CODE))
    }

    @Test fun styledVectorBarcodesScan() {
        for ((format, data) in listOf(
            CodeFormat.EAN_13 to "4006381333931",
            CodeFormat.CODE_128 to "Hello-123",
            CodeFormat.ITF to "1234567890",
            CodeFormat.CODE_39 to "ABC-123",
            CodeFormat.AZTEC to "Aztec vector",
            CodeFormat.DATA_MATRIX to "DM vector",
            CodeFormat.PDF_417 to "PDF vector text",
        )) {
            val image = BarcodeVectorBuilder.build(CodeEncoder.encodeBarcode(format, data), QrStyle(foreground = 0xFF000000), data, format)
            val (px, w, h) = Raster.render(image, 8)
            assertEquals(data, Raster.decode(px, w, h, format), "$format")
        }
    }

    @Test fun rasterHelperSanity() {
        val m = CodeEncoder.encodeQr("x")
        val (px, w, h) = Raster.rasterize(m, 2, 2, 4, 4)
        assertEquals((m.width + 8) * 2, w)
        assertEquals(w * h, px.size)
        assertTrue(px.any { it == 0xFF000000.toInt() })
    }
}

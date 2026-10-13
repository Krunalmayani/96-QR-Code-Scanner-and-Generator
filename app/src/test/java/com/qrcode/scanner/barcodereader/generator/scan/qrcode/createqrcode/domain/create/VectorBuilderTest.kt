package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class VectorBuilderTest {
    private val matrix = CodeEncoder.encodeQr("https://www.brewhouse-coffee.com/menu?table=12", ErrorCorrection.M)

    private fun bounds(shape: VectorShape): FloatArray? = when (shape) {
        is VectorShape.Rect -> floatArrayOf(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height)
        is VectorShape.Circle -> floatArrayOf(shape.cx - shape.r, shape.cy - shape.r, shape.cx + shape.r, shape.cy + shape.r)
        is VectorShape.Path -> {
            val xs = ArrayList<Float>()
            val ys = ArrayList<Float>()
            for (c in shape.commands) when (c) {
                is PathCommand.MoveTo -> { xs += c.x; ys += c.y }
                is PathCommand.LineTo -> { xs += c.x; ys += c.y }
                is PathCommand.QuadTo -> { xs += c.x; ys += c.y }
                is PathCommand.ArcTo -> { xs += c.cx - c.r; xs += c.cx + c.r; ys += c.cy - c.r; ys += c.cy + c.r }
                PathCommand.Close -> Unit
            }
            floatArrayOf(xs.min(), ys.min(), xs.max(), ys.max())
        }
        is VectorShape.Text -> null
    }

    private fun assertInside(img: QrVectorImage) {
        val eps = 0.001f
        for (item in img.items) {
            val b = bounds(item.shape) ?: continue
            assertTrue(b[0] >= -eps && b[1] >= -eps && b[2] <= img.width + eps && b[3] <= img.height + eps, "out of bounds: ${item.shape}")
        }
    }

    private fun eyeItems(img: QrVectorImage, q: Int, size: Int): List<StyledShape> =
        img.items.filter { s ->
            val b = bounds(s.shape)!!
            QrGeometry.finderOrigins(size).any { (ox, oy) ->
                b[0] >= ox + q - 0.01f && b[1] >= oy + q - 0.01f && b[2] <= ox + q + 7.01f && b[3] <= oy + q + 7.01f
            }
        }

    @Test fun dimensionsIncludeQuietZone() {
        val img = QrVectorBuilder.build(matrix, QrStyle(), quietZoneModules = 2)
        assertEquals((matrix.width + 4).toFloat(), img.width)
        assertEquals(img.width, img.height)
        assertEquals(QrStyle().background, img.background)
        val none = QrVectorBuilder.build(matrix, QrStyle(), quietZoneModules = 0)
        assertEquals(matrix.width.toFloat(), none.width)
        assertNull(img.logoRect)
    }

    @Test fun everyShapeCombinationStaysInBoundsAndHasThreeEyes() {
        for (dot in DotShape.entries) for (eye in EyeShape.entries) {
            val style = QrStyle(dotShape = dot, eyeShape = eye)
            val img = QrVectorBuilder.build(matrix, style)
            assertInside(img)
            val eyes = eyeItems(img, 2, matrix.width)
            assertEquals(6, eyes.size, "ring + pupil per eye for $dot/$eye")
            assertEquals(3, eyes.count { it.shape is VectorShape.Path && (it.shape as VectorShape.Path).evenOdd })
            assertTrue(img.items.isNotEmpty())
        }
    }

    @Test fun squareDotsMergeRunsAndSkipFinders() {
        val img = QrVectorBuilder.build(matrix, QrStyle(), quietZoneModules = 0)
        val rects = img.items.map { it.shape }.filterIsInstance<VectorShape.Rect>()
        var darkOutsideEyes = 0
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            if (matrix[x, y] && !QrGeometry.isFinderModule(x, y, matrix.width)) darkOutsideEyes++
        }
        val covered = rects.filter { it.height == 1f && it.cornerRadius == 0f && it.width >= 1f && it.x + it.width <= matrix.width }
        // pupils (3x3) are not 1 high, so what remains are the merged data runs
        assertEquals(darkOutsideEyes, covered.sumOf { it.width.toInt() })
        assertTrue(covered.size < darkOutsideEyes)
    }

    @Test fun dotsAreCirclesAndRoundedAreRoundRects() {
        val dots = QrVectorBuilder.build(matrix, QrStyle(dotShape = DotShape.DOTS))
        assertTrue(dots.items.any { it.shape is VectorShape.Circle && (it.shape as VectorShape.Circle).r == 0.42f })
        val rounded = QrVectorBuilder.build(matrix, QrStyle(dotShape = DotShape.ROUNDED))
        assertTrue(rounded.items.any { (it.shape as? VectorShape.Rect)?.cornerRadius == 0.35f })
        val classy = QrVectorBuilder.build(matrix, QrStyle(dotShape = DotShape.CLASSY))
        assertTrue(classy.items.any { it.shape is VectorShape.Path && !(it.shape as VectorShape.Path).evenOdd })
    }

    @Test fun eyeShapesProduceMatchingPrimitives() {
        val circle = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.CIRCLE))
        assertEquals(3, circle.items.count { (it.shape as? VectorShape.Circle)?.r == 1.5f })
        val rounded = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.ROUNDED))
        assertEquals(3, rounded.items.count { (it.shape as? VectorShape.Rect)?.cornerRadius == 0.9f })
        val square = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.SQUARE))
        assertEquals(3, square.items.count { (it.shape as? VectorShape.Rect)?.let { r -> r.width == 3f && r.height == 3f } == true })
    }

    @Test fun solidAndGradientPaints() {
        val solid = QrVectorBuilder.build(matrix, QrStyle(foreground = 0xFF112233))
        assertTrue(solid.items.all { it.paint == ShapePaint.Solid(0xFF112233) })
        val grad = QrVectorBuilder.build(matrix, QrStyle(foreground = 0xFF112233, gradientEnd = 0xFF445566), gradientAngleDegrees = 90f)
        assertTrue(grad.items.all { it.paint == ShapePaint.LinearGradient(0xFF112233, 0xFF445566, 90f) })
    }

    @Test fun gradientEndpoints() {
        val g = ShapePaint.LinearGradient(0, 0, 0f)
        val h = g.endpoints(100f, 50f)
        assertEquals(0f, h.x0, 0.001f); assertEquals(25f, h.y0, 0.001f)
        assertEquals(100f, h.x1, 0.001f); assertEquals(25f, h.y1, 0.001f)
        val v = ShapePaint.LinearGradient(0, 0, 90f).endpoints(100f, 50f)
        assertEquals(50f, v.x0, 0.01f); assertEquals(0f, v.y0, 0.01f)
        assertEquals(50f, v.y1, 0.01f)
        val d = ShapePaint.LinearGradient(0, 0, 45f).endpoints(100f, 100f)
        assertEquals(0f, d.x0, 0.01f); assertEquals(0f, d.y0, 0.01f)
        assertEquals(100f, d.x1, 0.01f); assertEquals(100f, d.y1, 0.01f)
    }

    // ---- logo ----
    @Test fun logoSkipsModulesInCentreAndExposesLogoRect() {
        val withLogo = QrVectorBuilder.build(matrix, QrStyle(logoUri = "content://logo"))
        val without = QrVectorBuilder.build(matrix, QrStyle())
        val logo = withLogo.logoRect
        assertNotNull(logo)
        assertTrue(withLogo.items.size < without.items.size)
        val side = matrix.width * QrVectorBuilder.LOGO_CLEAR_FRACTION
        val c = withLogo.width / 2f
        val half = side / 2f
        for (item in withLogo.items) {
            val b = bounds(item.shape)!!
            val inside = b[0] > c - half + 0.001f && b[2] < c + half - 0.001f && b[1] > c - half + 0.001f && b[3] < c + half - 0.001f
            assertFalse(inside, "module fully inside the clear area: ${item.shape}")
            val overlapX = b[0] < c + half - 0.001f && b[2] > c - half + 0.001f
            val overlapY = b[1] < c + half - 0.001f && b[3] > c - half + 0.001f
            // merged square runs may start left of the area, but never cross into it
            if (item.shape is VectorShape.Rect && (item.shape as VectorShape.Rect).height == 1f) assertFalse(overlapX && overlapY)
        }
        assertEquals(matrix.width * QrVectorBuilder.LOGO_IMAGE_FRACTION, logo!!.width, 0.001f)
        assertEquals(c, logo.x + logo.width / 2f, 0.001f)
        assertEquals(c, logo.y + logo.height / 2f, 0.001f)
        assertTrue(logo.width < side)
    }

    @Test fun logoReserveCanBeForcedAndDisabled() {
        assertNotNull(QrVectorBuilder.build(matrix, QrStyle(), logoReserve = true).logoRect)
        assertNull(QrVectorBuilder.build(matrix, QrStyle(logoUri = "x"), logoReserve = false).logoRect)
    }

    @Test fun logoNeverRemovesFinders() {
        val img = QrVectorBuilder.build(matrix, QrStyle(logoUri = "x"))
        assertEquals(6, eyeItems(img, 2, matrix.width).size)
    }

    @Test fun effectiveErrorCorrection() {
        assertEquals(ErrorCorrection.M, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.M)))
        assertEquals(ErrorCorrection.Q, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.L, logoUri = "x")))
        assertEquals(ErrorCorrection.Q, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.M, logoUri = "x")))
        assertEquals(ErrorCorrection.Q, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.Q, logoUri = "x")))
        assertEquals(ErrorCorrection.H, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.H, logoUri = "x")))
        assertEquals(ErrorCorrection.L, QrVectorBuilder.effectiveErrorCorrection(QrStyle(errorCorrection = ErrorCorrection.L)))
    }

    // ---- frame ----
    @Test fun frameAddsHeightBandAndText() {
        val plain = QrVectorBuilder.build(matrix, QrStyle())
        val framed = QrVectorBuilder.build(matrix, QrStyle(frameText = "Scan me"))
        assertEquals(plain.width, framed.width)
        assertEquals(plain.height * (1 + QrVectorBuilder.FRAME_BAND_FRACTION), framed.height, 0.001f)
        val text = framed.items.map { it.shape }.filterIsInstance<VectorShape.Text>().single()
        assertEquals("Scan me", text.text)
        assertTrue(text.y > framed.width && text.y < framed.height)
        assertTrue(text.size > 0f)
        assertInside(framed)
        assertEquals(ShapePaint.Solid(QrStyle().background), framed.items.last().paint)
    }

    @Test fun blankFrameTextMeansNoFrame() {
        val img = QrVectorBuilder.build(matrix, QrStyle(frameText = "   "))
        assertEquals(img.width, img.height)
        assertTrue(img.items.none { it.shape is VectorShape.Text })
    }

    @Test fun longFrameTextShrinksToFit() {
        val short = QrVectorBuilder.build(matrix, QrStyle(frameText = "Hi")).items.map { it.shape }.filterIsInstance<VectorShape.Text>().single()
        val long = QrVectorBuilder.build(matrix, QrStyle(frameText = "Scan this code for the full menu please")).items.map { it.shape }.filterIsInstance<VectorShape.Text>().single()
        assertTrue(long.size < short.size)
    }

    // ---- barcode ----
    @Test fun barcodeBarsAreRectsWithMergedRunsAndNumber() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.EAN_13, "4006381333931")
        val img = BarcodeVectorBuilder.build(m, QrStyle(showNumber = true, barHeightPercent = 100), "4006381333931")
        assertEquals((95 + 2 * BarcodeVectorBuilder.QUIET_1D).toFloat(), img.width)
        val rects = img.items.map { it.shape }.filterIsInstance<VectorShape.Rect>()
        val barModules = rects.sumOf { it.width.toInt() }
        assertEquals(m.darkCount(), barModules)
        assertTrue(rects.all { it.x >= BarcodeVectorBuilder.QUIET_1D && it.x + it.width <= img.width - BarcodeVectorBuilder.QUIET_1D })
        val text = img.items.map { it.shape }.filterIsInstance<VectorShape.Text>().single()
        assertEquals("4006381333931", text.text)
        assertTrue(text.y < img.height)
        assertInside(img)
    }

    @Test fun barcodeHeightFollowsPercentAndNumberToggle() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.CODE_128, "ABC123")
        val tall = BarcodeVectorBuilder.build(m, QrStyle(barHeightPercent = 200), "ABC123")
        val short = BarcodeVectorBuilder.build(m, QrStyle(barHeightPercent = 40), "ABC123")
        assertTrue(tall.height > short.height)
        val noNumber = BarcodeVectorBuilder.build(m, QrStyle(showNumber = false), "ABC123")
        assertTrue(noNumber.items.none { it.shape is VectorShape.Text })
        val nullNumber = BarcodeVectorBuilder.build(m, QrStyle(), null)
        assertTrue(nullNumber.items.none { it.shape is VectorShape.Text })
        assertTrue(nullNumber.height < BarcodeVectorBuilder.build(m, QrStyle(), "ABC123").height)
        val clamped = BarcodeVectorBuilder.build(m, QrStyle(barHeightPercent = 5), null)
        assertEquals(short.width * 0.4f + 8f, clamped.height, 0.01f)
    }

    @Test fun barcodeGradient() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.CODE_39, "AB")
        val img = BarcodeVectorBuilder.build(m, QrStyle(gradientEnd = 0xFF0000FF), "AB", CodeFormat.CODE_39)
        assertTrue(img.items.filter { it.shape is VectorShape.Rect }.all { it.paint is ShapePaint.LinearGradient })
    }

    @Test fun twoDimensionalBarcodeIsAGridWithoutNumber() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.AZTEC, "Hello Aztec")
        val img = BarcodeVectorBuilder.build(m, QrStyle(), "Hello Aztec", CodeFormat.AZTEC)
        assertEquals((m.width + 2 * BarcodeVectorBuilder.QUIET_2D).toFloat(), img.width)
        assertEquals((m.height + 2 * BarcodeVectorBuilder.QUIET_2D).toFloat(), img.height)
        assertTrue(img.items.none { it.shape is VectorShape.Text })
        assertEquals(m.darkCount(), img.items.map { it.shape }.filterIsInstance<VectorShape.Rect>().sumOf { it.width.toInt() })
        assertInside(img)
    }

    @Test fun linearDetectionWithoutFormat() {
        val oneD = BarcodeVectorBuilder.build(CodeEncoder.encodeBarcode(CodeFormat.ITF, "1234", 20), QrStyle(), "1234")
        assertTrue(oneD.items.any { it.shape is VectorShape.Text })
        val twoD = BarcodeVectorBuilder.build(CodeEncoder.encodeBarcode(CodeFormat.PDF_417, "hello"), QrStyle(), "hello")
        assertTrue(twoD.items.none { it.shape is VectorShape.Text })
    }

    // ---- renderer ----
    @Test fun codeRendererBuildsEveryKind() {
        val qr = CodeRenderer.render(CreateSpec.Website("example.com"), QrStyle(logoUri = "x"))
        assertNotNull(qr.logoRect)
        val bar = CodeRenderer.render(CreateSpec.Barcode(CreateType.BARCODE_EAN_8, "9638507"), QrStyle())
        assertEquals("96385074", bar.items.map { it.shape }.filterIsInstance<VectorShape.Text>().single().text)
        val aztec = CodeRenderer.render(CodeFormat.AZTEC, "x", QrStyle())
        assertTrue(aztec.items.isNotEmpty())
    }

    @Test fun pathCommandsStartWithMoveToAndRingsHaveTwoSubpaths() {
        val img = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.ROUNDED))
        val ring = img.items.map { it.shape }.filterIsInstance<VectorShape.Path>().first { it.evenOdd }
        assertTrue(ring.commands.first() is PathCommand.MoveTo)
        assertEquals(2, ring.commands.count { it is PathCommand.MoveTo })
        assertEquals(2, ring.commands.count { it == PathCommand.Close })
        assertTrue(abs(ring.commands.filterIsInstance<PathCommand.ArcTo>().first().sweepAngleDegrees) == 90f)
    }
}

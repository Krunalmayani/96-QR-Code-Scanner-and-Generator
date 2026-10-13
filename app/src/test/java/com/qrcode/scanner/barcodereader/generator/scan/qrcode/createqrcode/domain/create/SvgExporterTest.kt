package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class SvgExporterTest {
    private val matrix = CodeEncoder.encodeQr("https://example.com/a?b=1&c=2", ErrorCorrection.M)

    private fun parse(svg: String): Document {
        val f = DocumentBuilderFactory.newInstance()
        f.isNamespaceAware = true
        f.isValidating = false
        return f.newDocumentBuilder().parse(ByteArrayInputStream(svg.toByteArray(Charsets.UTF_8)))
    }

    private fun count(doc: Document, tag: String) = doc.getElementsByTagName(tag).length

    @Test fun wellFormedForEveryStyleCombination() {
        for (dot in DotShape.entries) for (eye in EyeShape.entries) for (grad in listOf<Long?>(null, 0xFF0044AA)) {
            val img = QrVectorBuilder.build(matrix, QrStyle(dotShape = dot, eyeShape = eye, gradientEnd = grad, frameText = "Scan me <&>"))
            val doc = parse(SvgExporter.toSvg(img))
            assertEquals("svg", doc.documentElement.localName)
            assertEquals("http://www.w3.org/2000/svg", doc.documentElement.namespaceURI)
        }
    }

    @Test fun rootAttributes() {
        val img = QrVectorBuilder.build(matrix, QrStyle())
        val root = parse(SvgExporter.toSvg(img)).documentElement
        assertEquals("1.1", root.getAttribute("version"))
        assertEquals("0 0 ${SvgExporter.num(img.width)} ${SvgExporter.num(img.height)}", root.getAttribute("viewBox"))
        assertEquals(SvgExporter.num(img.width), root.getAttribute("width"))
    }

    @Test fun widthPxScalesDocumentKeepingViewBox() {
        val img = QrVectorBuilder.build(matrix, QrStyle(frameText = "Scan me"))
        val root = parse(SvgExporter.toSvg(img, widthPx = 1024)).documentElement
        assertEquals("1024", root.getAttribute("width"))
        val h = root.getAttribute("height").toFloat()
        assertEquals(1024f * img.height / img.width, h, 0.01f)
        assertEquals("0 0 ${SvgExporter.num(img.width)} ${SvgExporter.num(img.height)}", root.getAttribute("viewBox"))
    }

    @Test fun backgroundRectComesFirstAndUsesBackgroundColor() {
        val img = QrVectorBuilder.build(matrix, QrStyle(background = 0xFFF0F4FF))
        val doc = parse(SvgExporter.toSvg(img))
        val first = doc.getElementsByTagName("rect").item(0) as Element
        assertEquals("#F0F4FF", first.getAttribute("fill"))
        assertEquals("0", first.getAttribute("x"))
    }

    @Test fun solidColorsAreHex() {
        val svg = SvgExporter.toSvg(QrVectorBuilder.build(matrix, QrStyle(foreground = 0xFF13142B)))
        assertTrue(svg.contains("fill=\"#13142B\""))
        assertFalse(svg.contains("linearGradient"))
    }

    @Test fun gradientBecomesLinearGradientDefinedOnce() {
        val img = QrVectorBuilder.build(matrix, QrStyle(foreground = 0xFF112233, gradientEnd = 0xFF445566))
        val doc = parse(SvgExporter.toSvg(img))
        assertEquals(1, count(doc, "linearGradient"))
        assertEquals(2, count(doc, "stop"))
        val g = doc.getElementsByTagName("linearGradient").item(0) as Element
        assertEquals("userSpaceOnUse", g.getAttribute("gradientUnits"))
        assertEquals("g0", g.getAttribute("id"))
        val stops = doc.getElementsByTagName("stop")
        assertEquals("#112233", (stops.item(0) as Element).getAttribute("stop-color"))
        assertEquals("#445566", (stops.item(1) as Element).getAttribute("stop-color"))
        val gradientFills = (0 until doc.getElementsByTagName("rect").length)
            .map { doc.getElementsByTagName("rect").item(it) as Element }
            .count { it.getAttribute("fill") == "url(#g0)" }
        assertTrue(gradientFills > 0)
    }

    @Test fun translucentColorsUseOpacity() {
        val img = QrVectorImage(10f, 10f, 0xFFFFFFFF, listOf(StyledShape(VectorShape.Rect(1f, 1f, 2f, 2f), ShapePaint.Solid(0x80FF0000))))
        val svg = SvgExporter.toSvg(img)
        assertTrue(svg.contains("fill=\"#FF0000\" fill-opacity=\"0.502\""))
        parse(svg)
    }

    @Test fun evenOddPathsAndArcs() {
        val img = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.CIRCLE, dotShape = DotShape.CLASSY))
        val svg = SvgExporter.toSvg(img)
        assertTrue(svg.contains("fill-rule=\"evenodd\""))
        assertTrue(svg.contains(" A3.5 3.5 0 0 1 "))
        parse(svg)
    }

    @Test fun textIsEscaped() {
        val img = QrVectorBuilder.build(matrix, QrStyle(frameText = "A&B <\"x\"> 'y'"))
        val svg = SvgExporter.toSvg(img)
        assertTrue(svg.contains("A&amp;B &lt;&quot;x&quot;&gt; &apos;y&apos;"))
        val doc = parse(svg)
        assertEquals("A&B <\"x\"> 'y'", doc.getElementsByTagName("text").item(0).textContent)
        val t = doc.getElementsByTagName("text").item(0) as Element
        assertEquals("middle", t.getAttribute("text-anchor"))
        assertEquals("bold", t.getAttribute("font-weight"))
    }

    @Test fun controlCharactersAreDropped() {
        assertEquals("ab", SvgExporter.escapeXml("a\u0000b"))
        assertEquals("a\tb\n", SvgExporter.escapeXml("a\tb\n"))
    }

    @Test fun barcodeSvgHasBarsAndNumber() {
        val m = CodeEncoder.encodeBarcode(CodeFormat.EAN_13, "4006381333931")
        val img = BarcodeVectorBuilder.build(m, QrStyle(), "4006381333931")
        val doc = parse(SvgExporter.toSvg(img))
        assertEquals("4006381333931", doc.getElementsByTagName("text").item(0).textContent)
        assertTrue(count(doc, "rect") > 20)
    }

    @Test fun logoImageEmbeddedInsideLogoRect() {
        val img = QrVectorBuilder.build(matrix, QrStyle(logoUri = "x"))
        val svg = SvgExporter.toSvg(img, logoHref = "data:image/png;base64,AAAA&x")
        val doc = parse(svg)
        val image = doc.getElementsByTagName("image").item(0) as Element
        assertEquals(SvgExporter.num(img.logoRect!!.width), image.getAttribute("width"))
        assertEquals("data:image/png;base64,AAAA&x", image.getAttributeNS("http://www.w3.org/1999/xlink", "href"))
        assertEquals(0, count(parse(SvgExporter.toSvg(img)), "image"))
    }

    @Test fun numberFormatting() {
        assertEquals("0", SvgExporter.num(0f))
        assertEquals("1", SvgExporter.num(1.0f))
        assertEquals("0.35", SvgExporter.num(0.35f))
        assertEquals("12.346", SvgExporter.num(12.3456f))
        assertEquals("-2.5", SvgExporter.num(-2.5f))
        assertEquals("1000000", SvgExporter.num(1_000_000f))
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> { SvgExporter.num(Float.NaN) }
    }

    @Test fun quadAndNegativeSweepAndStartAnchor() {
        val img = QrVectorImage(
            20f, 20f, 0xFFFFFFFF,
            listOf(
                StyledShape(
                    VectorShape.Path(
                        listOf(
                            PathCommand.MoveTo(1f, 1f),
                            PathCommand.QuadTo(5f, 0f, 9f, 1f),
                            PathCommand.ArcTo(5f, 5f, 4f, 0f, -90f),
                            PathCommand.Close,
                        ),
                    ),
                    ShapePaint.Solid(0xFF000000),
                ),
                StyledShape(VectorShape.Text(1f, 15f, "x", 3f, TextAnchor.START), ShapePaint.Solid(0xFF000000)),
                StyledShape(VectorShape.Text(19f, 15f, "y", 3f, TextAnchor.END), ShapePaint.Solid(0xFF000000)),
            ),
        )
        val svg = SvgExporter.toSvg(img)
        assertTrue(svg.contains("Q5 0 9 1"))
        assertTrue(svg.contains("A4 4 0 0 0 5 1"))
        assertTrue(svg.contains("text-anchor=\"start\"") && svg.contains("text-anchor=\"end\""))
        parse(svg)
    }
}

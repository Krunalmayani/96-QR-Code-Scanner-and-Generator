package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EyeLeafTest {
    private val payload = "https://www.brewhouse-coffee.com/menu?table=12"
    private val matrix = CodeEncoder.encodeQr(payload, ErrorCorrection.M)

    private fun eyePaths(img: QrVectorImage): List<VectorShape.Path> {
        val size = matrix.width
        val q = 2
        return img.items.map { it.shape }.filterIsInstance<VectorShape.Path>().filter { p ->
            val first = p.commands.first() as PathCommand.MoveTo
            QrGeometry.finderOrigins(size).any { (ox, oy) ->
                first.x >= ox + q && first.x <= ox + q + 7 && first.y >= oy + q && first.y <= oy + q + 7
            }
        }
    }

    @Test fun leafEyeHasRingAndPupilPerFinder() {
        val img = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.LEAF))
        val paths = eyePaths(img)
        assertEquals(3, paths.count { it.evenOdd }, "one ring per eye")
        assertEquals(3, paths.count { !it.evenOdd }, "one pupil per eye")
    }

    @Test fun ringHasTwoClosedLeafOutlinesWithTwoRoundedCornersEach() {
        val img = QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.LEAF))
        val ring = eyePaths(img).first { it.evenOdd }
        assertEquals(2, ring.commands.count { it is PathCommand.MoveTo })
        assertEquals(2, ring.commands.count { it == PathCommand.Close })
        val arcs = ring.commands.filterIsInstance<PathCommand.ArcTo>()
        assertEquals(4, arcs.size)
        assertTrue(arcs.all { it.sweepAngleDegrees == 90f })
        // outer outline: radius 3 and 2 on the inner one; top-left (180) and bottom-right (0) corners
        assertEquals(listOf(0f, 180f, 0f, 180f), arcs.map { it.startAngleDegrees })
        assertEquals(listOf(3f, 3f, 2f, 2f), arcs.map { it.r })
    }

    @Test fun leafPathIsAsymmetric() {
        val cmds = PathShapes.leaf(10f, 20f, 7f, 7f, 3f)
        assertEquals(PathCommand.MoveTo(13f, 20f), cmds.first())
        assertEquals(PathCommand.Close, cmds.last())
        // sharp corners: top-right (17,20) and bottom-left (10,27) are plain line points
        assertTrue(cmds.contains(PathCommand.LineTo(17f, 20f)))
        assertTrue(cmds.contains(PathCommand.LineTo(10f, 27f)))
        val arcs = cmds.filterIsInstance<PathCommand.ArcTo>()
        assertEquals(2, arcs.size)
        assertEquals(PathCommand.ArcTo(14f, 24f, 3f, 0f, 90f), arcs[0])
        assertEquals(PathCommand.ArcTo(13f, 23f, 3f, 180f, 90f), arcs[1])
    }

    @Test fun arcsStartWhereThePreviousSegmentEnds() {
        val cmds = PathShapes.leaf(0f, 0f, 7f, 7f, 3f)
        var x = 0f
        var y = 0f
        for (c in cmds) {
            when (c) {
                is PathCommand.MoveTo -> { x = c.x; y = c.y }
                is PathCommand.LineTo -> { x = c.x; y = c.y }
                is PathCommand.ArcTo -> {
                    val a = Math.toRadians(c.startAngleDegrees.toDouble())
                    assertEquals(x, c.cx + c.r * Math.cos(a).toFloat(), 0.001f)
                    assertEquals(y, c.cy + c.r * Math.sin(a).toFloat(), 0.001f)
                    val e = Math.toRadians((c.startAngleDegrees + c.sweepAngleDegrees).toDouble())
                    x = c.cx + c.r * Math.cos(e).toFloat()
                    y = c.cy + c.r * Math.sin(e).toFloat()
                }
                else -> Unit
            }
        }
    }

    @Test fun leafEyedCodeStillScans() {
        for (dot in com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape.entries) {
            val style = QrStyle(eyeShape = EyeShape.LEAF, dotShape = dot, errorCorrection = ErrorCorrection.Q, foreground = 0xFF000000)
            val image = QrVectorBuilder.build(CodeEncoder.encodeQr(payload, ErrorCorrection.Q), style, quietZoneModules = 4)
            val (px, w, h) = Raster.render(image, 8)
            assertEquals(payload, Raster.decode(px, w, h, CodeFormat.QR_CODE), "leaf / $dot")
        }
    }

    @Test fun leafEyesExportToSvgWithArcs() {
        val svg = SvgExporter.toSvg(QrVectorBuilder.build(matrix, QrStyle(eyeShape = EyeShape.LEAF)))
        assertTrue(svg.contains("fill-rule=\"evenodd\""))
        assertTrue(svg.contains(" A3 3 0 0 1 "))
    }
}

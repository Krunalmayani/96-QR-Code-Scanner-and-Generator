package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExportPlanningTest {
    @Test fun snapPointsCoverTheRange() {
        assertEquals(ExportSizes.MIN, ExportSizes.points.first())
        assertEquals(ExportSizes.MAX, ExportSizes.points.last())
        assertTrue(ExportSizes.DEFAULT in ExportSizes.points)
        assertEquals(ExportSizes.points.sorted(), ExportSizes.points)
    }

    @Test fun snapGoesToTheNearestPoint() {
        assertEquals(512, ExportSizes.snap(0))
        assertEquals(512, ExportSizes.snap(600))
        assertEquals(768, ExportSizes.snap(650))
        assertEquals(1024, ExportSizes.snap(1000))
        assertEquals(1536, ExportSizes.snap(1400))
        assertEquals(2048, ExportSizes.snap(5000))
        for (p in ExportSizes.points) assertEquals(p, ExportSizes.snap(p))
    }

    @Test fun tiesGoToTheLargerSize() {
        assertEquals(768, ExportSizes.snap(640))
    }

    @Test fun indexAndAtAreInverse() {
        for ((i, p) in ExportSizes.points.withIndex()) {
            assertEquals(i, ExportSizes.indexOf(p))
            assertEquals(p, ExportSizes.at(i))
        }
        assertEquals(512, ExportSizes.at(-3))
        assertEquals(2048, ExportSizes.at(99))
        assertEquals(2, ExportSizes.indexOf(1100))
    }

    @Test fun dimensionsKeepTheAspectRatio() {
        val square = QrVectorImage(33f, 33f, 0xFFFFFFFF, emptyList())
        assertEquals(PixelSize(1024, 1024), ExportDimensions.of(square, 1024))
        val framed = QrVectorImage(30f, 36f, 0xFFFFFFFF, emptyList())
        assertEquals(PixelSize(1000, 1200), ExportDimensions.of(framed, 1000))
        val wide = QrVectorImage(100f, 40f, 0xFFFFFFFF, emptyList())
        assertEquals(PixelSize(512, 205), ExportDimensions.of(wide, 512))
    }

    @Test fun dimensionsOfRealCodes() {
        val qr = CodeRenderer.render(CodeFormat.QR_CODE, "hello", QrStyle())
        val size = ExportDimensions.of(qr, 2048)
        assertEquals(2048, size.width)
        assertEquals(2048, size.height)
    }

    @Test fun pdfPageIsTheSizeAt300Dpi() {
        assertEquals(245, ExportDimensions.pdfPageWidthPoints(1024))
        assertEquals(144, ExportDimensions.pdfPageWidthPoints(512))
        assertEquals(491, ExportDimensions.pdfPageWidthPoints(2048))
    }

    @Test fun fileNames() {
        val millis = utcMillis(2026, 10, 13, 14, 25, 1)
        assertEquals("qr-website-20261013-142501.png", ExportNaming.fileName(true, "Website", millis, ExportFormat.PNG))
        assertEquals("barcode-ean-13-20261013-142501.jpg", ExportNaming.fileName(false, "EAN-13", millis, ExportFormat.JPG))
        assertEquals("qr-20261013-142501.svg", ExportNaming.fileName(true, "", millis, ExportFormat.SVG))
        assertEquals("qr-wi-fi-20261013-142501.pdf", ExportNaming.fileName(true, "Wi-Fi", millis, ExportFormat.PDF))
    }

    @Test fun fileNameUsesTheOffset() {
        val millis = utcMillis(2026, 12, 31, 23, 30, 0)
        assertEquals("qr-text-20270101-050000.png", ExportNaming.fileName(true, "Text", millis, ExportFormat.PNG, utcOffsetMillis = 5 * 3_600_000 + 1_800_000))
    }

    @Test fun slugIsSafeForFileNames() {
        assertEquals("a-b-c", ExportNaming.slug("  A / B ** C  "))
        assertEquals("", ExportNaming.slug("///"))
        assertEquals("caf", ExportNaming.slug("Café"))
        assertEquals(24, ExportNaming.slug("x".repeat(100)).length)
        assertEquals("abc", ExportNaming.slug("abc-"))
    }
}

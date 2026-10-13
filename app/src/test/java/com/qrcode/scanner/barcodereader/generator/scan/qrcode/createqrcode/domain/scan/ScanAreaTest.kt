package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScanAreaTest {
    private val delta = 0.001f

    @Test
    fun `upright size swaps sides for 90 and 270 degrees`() {
        assertEquals(480 to 640, ScanArea.uprightSize(640, 480, 90))
        assertEquals(480 to 640, ScanArea.uprightSize(640, 480, 270))
        assertEquals(640 to 480, ScanArea.uprightSize(640, 480, 0))
        assertEquals(640 to 480, ScanArea.uprightSize(640, 480, 180))
    }

    @Test
    fun `view rect maps one to one when the view and image have the same size`() {
        val rect = ScanArea.viewRectToImage(PxRect(10f, 20f, 110f, 120f), 400f, 800f, 400f, 800f)!!
        assertEquals(10f, rect.left, delta)
        assertEquals(20f, rect.top, delta)
        assertEquals(110f, rect.right, delta)
        assertEquals(120f, rect.bottom, delta)
    }

    @Test
    fun `image taller than the view is cropped top and bottom and scaled down`() {
        // view 400x400, image 200x400 -> scale 2 (covers width), image shown 400x800, offsetY = -200
        val rect = ScanArea.viewRectToImage(PxRect(100f, 100f, 300f, 300f), 400f, 400f, 200f, 400f)!!
        assertEquals(50f, rect.left, delta)
        assertEquals(150f, rect.right, delta)
        assertEquals(150f, rect.top, delta)
        assertEquals(250f, rect.bottom, delta)
    }

    @Test
    fun `image wider than the view is cropped left and right`() {
        // view 400x400, image 800x400 -> scale 1, offsetX = -200
        val rect = ScanArea.viewRectToImage(PxRect(0f, 0f, 400f, 400f), 400f, 400f, 800f, 400f)!!
        assertEquals(200f, rect.left, delta)
        assertEquals(600f, rect.right, delta)
        assertEquals(0f, rect.top, delta)
        assertEquals(400f, rect.bottom, delta)
    }

    @Test
    fun `result is clamped to the image`() {
        val rect = ScanArea.viewRectToImage(PxRect(-50f, -50f, 500f, 900f), 400f, 800f, 400f, 800f)!!
        assertEquals(0f, rect.left, delta)
        assertEquals(0f, rect.top, delta)
        assertEquals(400f, rect.right, delta)
        assertEquals(800f, rect.bottom, delta)
    }

    @Test
    fun `degenerate sizes give null`() {
        assertNull(ScanArea.viewRectToImage(PxRect(0f, 0f, 1f, 1f), 0f, 800f, 400f, 800f))
        assertNull(ScanArea.viewRectToImage(PxRect(0f, 0f, 1f, 1f), 400f, 800f, 0f, 800f))
        assertNotNull(ScanArea.viewRectToImage(PxRect(0f, 0f, 1f, 1f), 400f, 800f, 400f, 800f))
    }

    @Test
    fun `code is inside when its centre is in the area`() {
        val area = PxRect(100f, 100f, 300f, 300f)
        val inside = DetectedCode("a", CodeFormat.QR_CODE, PxRect(250f, 250f, 330f, 330f))
        val outside = DetectedCode("b", CodeFormat.QR_CODE, PxRect(290f, 290f, 400f, 400f))
        assertTrue(ScanArea.isInside(inside, area))
        assertFalse(ScanArea.isInside(outside, area))
    }

    @Test
    fun `no area or no bounds means the code counts`() {
        val withBounds = DetectedCode("a", CodeFormat.QR_CODE, PxRect(0f, 0f, 10f, 10f))
        assertTrue(ScanArea.isInside(withBounds, null))
        assertTrue(ScanArea.isInside(DetectedCode("a", CodeFormat.QR_CODE), PxRect(100f, 100f, 200f, 200f)))
    }

    @Test
    fun `rect helpers`() {
        val rect = PxRect(10f, 20f, 50f, 80f)
        assertEquals(40f, rect.width, delta)
        assertEquals(60f, rect.height, delta)
        assertEquals(30f, rect.centerX, delta)
        assertEquals(50f, rect.centerY, delta)
    }
}

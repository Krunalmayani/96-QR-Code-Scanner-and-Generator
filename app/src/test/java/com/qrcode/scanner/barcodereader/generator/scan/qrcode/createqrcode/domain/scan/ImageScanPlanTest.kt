package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageScanPlanTest {
    @Test
    fun `the plain image is tried first`() {
        assertEquals(ImageVariant.ORIGINAL, ImageScanPlan.variants.first())
    }

    @Test
    fun `plan covers contrast, inversion and all three extra rotations`() {
        val variants = ImageScanPlan.variants
        assertTrue(variants.any { it.contrast })
        assertTrue(variants.any { it.invert })
        assertEquals(setOf(0, 90, 180, 270), variants.map { it.rotationDegrees }.toSet())
    }

    @Test
    fun `stretch maps a washed out range to full range`() {
        val histogram = IntArray(256)
        histogram[100] = 500
        histogram[150] = 500
        val stretch = ContrastStretch.forHistogram(histogram, clipFraction = 0.0)
        assertNotNull(stretch)
        assertEquals(255f / 50f, stretch!!.gain, 0.001f)
        assertEquals(0f, 100 * stretch.gain + stretch.offset, 0.001f)
        assertEquals(255f, 150 * stretch.gain + stretch.offset, 0.001f)
    }

    @Test
    fun `stretch ignores clipped outliers`() {
        val histogram = IntArray(256)
        histogram[0] = 5
        histogram[100] = 495
        histogram[150] = 495
        histogram[255] = 5
        val stretch = ContrastStretch.forHistogram(histogram, clipFraction = 0.01)!!
        assertEquals(255f / 50f, stretch.gain, 0.001f)
    }

    @Test
    fun `no stretch for flat, full range, empty or malformed histograms`() {
        val flat = IntArray(256).also { it[128] = 1000 }
        assertNull(ContrastStretch.forHistogram(flat))
        val full =
            IntArray(256).also {
                it[0] = 500
                it[255] = 500
            }
        assertNull(ContrastStretch.forHistogram(full, clipFraction = 0.0))
        assertNull(ContrastStretch.forHistogram(IntArray(256)))
        assertNull(ContrastStretch.forHistogram(IntArray(10)))
    }

    @Test
    fun `sample size halves until the longer side would drop below the target`() {
        assertEquals(1, ImageSampling.sampleSize(1000, 800, 2048))
        assertEquals(1, ImageSampling.sampleSize(2048, 1000, 2048))
        assertEquals(2, ImageSampling.sampleSize(4096, 3000, 2048))
        assertEquals(2, ImageSampling.sampleSize(8000, 6000, 2048))
        assertEquals(4, ImageSampling.sampleSize(16000, 12000, 2048))
    }

    @Test
    fun `sample size is one for invalid input`() {
        assertEquals(1, ImageSampling.sampleSize(0, 100, 2048))
        assertEquals(1, ImageSampling.sampleSize(100, 100, 0))
    }
}

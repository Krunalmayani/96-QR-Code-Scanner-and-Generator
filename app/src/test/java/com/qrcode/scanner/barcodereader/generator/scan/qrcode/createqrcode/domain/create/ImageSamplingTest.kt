package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageSamplingTest {
    @Test fun smallImagesAreNotSampled() {
        assertEquals(1, ImageSampling.sampleSize(100))
        assertEquals(1, ImageSampling.sampleSize(512))
        assertEquals(1, ImageSampling.sampleSize(1023))
    }

    @Test fun bigImagesAreHalvedUntilTheyAreJustAboveTheTarget() {
        assertEquals(2, ImageSampling.sampleSize(1024))
        assertEquals(2, ImageSampling.sampleSize(1500))
        assertEquals(4, ImageSampling.sampleSize(2048))
        assertEquals(4, ImageSampling.sampleSize(4000))
        assertEquals(16, ImageSampling.sampleSize(8192))
    }

    @Test fun resultKeepsAtLeastTheTargetSize() {
        for (side in listOf(513, 700, 1024, 1800, 3000, 4032, 6000, 12000)) {
            val sample = ImageSampling.sampleSize(side)
            assertTrue(side / sample >= ImageSampling.LOGO_MAX_SIDE, "side=$side sample=$sample")
            assertTrue(sample and (sample - 1) == 0, "power of two: $sample")
        }
    }

    @Test fun degenerateInputIsSafe() {
        assertEquals(1, ImageSampling.sampleSize(0))
        assertEquals(1, ImageSampling.sampleSize(-5))
        assertEquals(1, ImageSampling.sampleSize(1000, maxSide = 0))
        assertEquals(4, ImageSampling.sampleSize(1000, maxSide = 200))
    }
}

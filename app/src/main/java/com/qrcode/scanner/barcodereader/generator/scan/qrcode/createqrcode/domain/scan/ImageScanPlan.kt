package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import kotlin.math.max

/**
 * One attempt at reading a picked image. [contrast] stretches the brightness range, [invert] flips light and dark
 * (light-on-dark codes), [rotationDegrees] is the rotation handed to the decoder.
 */
enum class ImageVariant(
    val rotationDegrees: Int,
    val contrast: Boolean,
    val invert: Boolean,
) {
    ORIGINAL(0, contrast = false, invert = false),
    CONTRAST_STRETCHED(0, contrast = true, invert = false),
    INVERTED(0, contrast = false, invert = true),
    ROTATED_90(90, contrast = false, invert = false),
    ROTATED_180(180, contrast = false, invert = false),
    ROTATED_270(270, contrast = false, invert = false),
}

/** The order in which variants are tried; reading stops at the first one that finds a code. */
object ImageScanPlan {
    val variants: List<ImageVariant> = ImageVariant.entries
}

/** Linear brightness correction `out = in * gain + offset` (0..255 scale). */
data class ContrastStretch(
    val gain: Float,
    val offset: Float,
) {
    companion object {
        private const val LEVELS = 256
        private const val MIN_SPAN = 8
        private const val FULL_RANGE_SPAN = 240

        /**
         * Stretch that maps the visible brightness range of [histogram] (256 luminance buckets) to 0..255,
         * ignoring the darkest and brightest [clipFraction] of pixels. Null when the image is empty, flat, or
         * already uses (nearly) the full range, i.e. stretching would not help.
         */
        fun forHistogram(histogram: IntArray, clipFraction: Double = 0.01): ContrastStretch? {
            if (histogram.size != LEVELS) return null
            val total = histogram.sum()
            if (total <= 0) return null
            val clip = (total * clipFraction).toInt()
            val low = percentileLevel(histogram, clip, fromTop = false)
            val high = percentileLevel(histogram, clip, fromTop = true)
            val span = high - low
            if (span < MIN_SPAN || span >= FULL_RANGE_SPAN) return null
            val gain = (LEVELS - 1).toFloat() / span
            return ContrastStretch(gain, -low * gain)
        }

        private fun percentileLevel(histogram: IntArray, skip: Int, fromTop: Boolean): Int {
            var seen = 0
            val range = if (fromTop) (LEVELS - 1 downTo 0) else (0 until LEVELS)
            for (level in range) {
                seen += histogram[level]
                if (seen > skip) return level
            }
            return if (fromTop) 0 else LEVELS - 1
        }
    }
}

/** Decoding size math for picked images. */
object ImageSampling {
    /** Largest power-of-two `inSampleSize` that keeps the longer side at or above [maxSide] (never below 1). */
    fun sampleSize(width: Int, height: Int, maxSide: Int): Int {
        if (width <= 0 || height <= 0 || maxSide <= 0) return 1
        var sample = 1
        val longest = max(width, height)
        while (longest / (sample * 2) >= maxSide) sample *= 2
        return sample
    }
}

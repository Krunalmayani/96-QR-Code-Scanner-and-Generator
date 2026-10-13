package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ContrastStretch
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ImageVariant
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/** Bitmap preparation for the retry attempts of [ImageVariant]. Rotation is not applied here: the decoder takes it as a parameter. */
internal object ImageVariants {
    private const val HISTOGRAM_SAMPLES = 65_536.0
    private const val LEVELS = 256

    /**
     * The bitmap to scan for [variant]: [source] itself when no pixel change is needed, a new bitmap otherwise
     * (the caller recycles it when it is not [source]), or null when the variant would not change anything.
     */
    fun prepare(source: Bitmap, variant: ImageVariant): Bitmap? {
        if (!variant.contrast && !variant.invert) return source
        val matrix =
            when {
                variant.contrast -> contrastMatrix(source) ?: return null
                else -> INVERT_MATRIX
            }
        return filtered(source, matrix)
    }

    private val INVERT_MATRIX =
        ColorMatrix(
            floatArrayOf(
                -1f,
                0f,
                0f,
                0f,
                255f,
                0f,
                -1f,
                0f,
                0f,
                255f,
                0f,
                0f,
                -1f,
                0f,
                255f,
                0f,
                0f,
                0f,
                1f,
                0f,
            ),
        )

    private fun contrastMatrix(source: Bitmap): ColorMatrix? {
        val stretch = ContrastStretch.forHistogram(histogram(source)) ?: return null
        val g = stretch.gain
        val o = stretch.offset
        return ColorMatrix(
            floatArrayOf(
                g,
                0f,
                0f,
                0f,
                o,
                0f,
                g,
                0f,
                0f,
                o,
                0f,
                0f,
                g,
                0f,
                o,
                0f,
                0f,
                0f,
                1f,
                0f,
            ),
        )
    }

    private fun histogram(bitmap: Bitmap): IntArray {
        val counts = IntArray(LEVELS)
        val stride = max(1, ceil(sqrt(bitmap.width.toDouble() * bitmap.height / HISTOGRAM_SAMPLES)).toInt())
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val luma = (299 * Color.red(pixel) + 587 * Color.green(pixel) + 114 * Color.blue(pixel)) / 1000
                counts[luma.coerceIn(0, LEVELS - 1)]++
                x += stride
            }
            y += stride
        }
        return counts
    }

    private fun filtered(source: Bitmap, matrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        Canvas(result).drawBitmap(source, 0f, 0f, paint)
        return result
    }
}

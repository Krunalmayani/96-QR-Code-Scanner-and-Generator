package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import kotlin.math.abs
import kotlin.math.roundToInt

/** The export sizes the slider can land on (PRD 4.6: 512 to 2048 px, snap points). */
object ExportSizes {
    const val MIN = 512
    const val MAX = 2048
    const val DEFAULT = 1024

    val points: List<Int> = listOf(512, 768, 1024, 1536, 2048)

    /** The snap point closest to [px] (ties go to the larger size). */
    fun snap(px: Int): Int = points.minWith(compareBy<Int> { abs(it - px) }.thenByDescending { it })

    /** Slider position (0 until [points].size) of the snap point closest to [px]. */
    fun indexOf(px: Int): Int = points.indexOf(snap(px))

    /** The size at slider position [index]; out-of-range values are clamped. */
    fun at(index: Int): Int = points[index.coerceIn(0, points.lastIndex)]
}

data class PixelSize(val width: Int, val height: Int)

object ExportDimensions {
    /** Pixel size of [image] exported [widthPx] wide; the height keeps the picture's aspect ratio. */
    fun of(image: QrVectorImage, widthPx: Int): PixelSize {
        val w = widthPx.coerceAtLeast(1)
        val h = (w * image.height / image.width).roundToInt().coerceAtLeast(1)
        return PixelSize(w, h)
    }

    /** Page width in PDF points for an export of [sizePx]: the size printed at 300 dpi (72 points per inch). */
    fun pdfPageWidthPoints(sizePx: Int): Int = (sizePx * 72 / 300).coerceAtLeast(MIN_PDF_POINTS)

    private const val MIN_PDF_POINTS = 144
}

object ExportNaming {
    private const val MAX_LABEL = 24

    /**
     * File name such as `qr-website-20261013-142501.png`. [label] is slugified (letters, digits, dashes);
     * [utcOffsetMillis] shifts the timestamp to local time.
     */
    fun fileName(isQr: Boolean, label: String, millis: Long, format: ExportFormat, utcOffsetMillis: Int = 0): String {
        val stamp = IcsDates.utc(millis + utcOffsetMillis).removeSuffix("Z").replace("T", "-")
        val parts = ArrayList<String>(3)
        parts += if (isQr) "qr" else "barcode"
        slug(label).takeIf { it.isNotEmpty() }?.let { parts += it }
        parts += stamp
        return parts.joinToString("-") + "." + format.extension
    }

    /** Lower-case letters and digits separated by single dashes, at most 24 characters. */
    fun slug(label: String): String {
        val sb = StringBuilder()
        for (ch in label.trim().lowercase()) {
            if (ch in 'a'..'z' || ch in '0'..'9') {
                sb.append(ch)
            } else if (sb.isNotEmpty() && sb.last() != '-') {
                sb.append('-')
            }
        }
        return sb.toString().take(MAX_LABEL).trim('-')
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import kotlin.math.cos
import kotlin.math.sin

/** Epoch millis of a UTC date/time (days-from-civil, no java.time). */
fun utcMillis(y: Int, m: Int, d: Int, h: Int = 0, mi: Int = 0, s: Int = 0): Long {
    val yy = if (m <= 2) y - 1 else y
    val era = Math.floorDiv(yy, 400)
    val yoe = yy - era * 400
    val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    val days = era.toLong() * 146097 + doe - 719468
    return days * 86_400_000L + h * 3_600_000L + mi * 60_000L + s * 1000L
}

object Raster {
    private const val WHITE = -1
    private const val BLACK = 0xFF000000.toInt()

    fun decode(pixels: IntArray, w: Int, h: Int, format: CodeFormat): String {
        val zx = format.toZxingFormat() ?: error("no zxing format for $format")
        return decode(pixels, w, h, zx)
    }

    fun decode(pixels: IntArray, w: Int, h: Int, format: BarcodeFormat): String {
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(w, h, pixels)))
        val hints = HashMap<DecodeHintType, Any>()
        hints[DecodeHintType.TRY_HARDER] = true
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(format)
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        return MultiFormatReader().decode(bitmap, hints).text
    }

    /** Scales every module to scaleX x scaleY pixels and adds a white quiet zone. Returns (pixels, width, height). */
    fun rasterize(m: ModuleMatrix, scaleX: Int, scaleY: Int, quietX: Int, quietY: Int): Triple<IntArray, Int, Int> {
        val w = (m.width + 2 * quietX) * scaleX
        val h = (m.height + 2 * quietY) * scaleY
        val px = IntArray(w * h) { WHITE }
        for (y in 0 until m.height) for (x in 0 until m.width) {
            if (!m[x, y]) continue
            for (dy in 0 until scaleY) for (dx in 0 until scaleX) {
                px[((y + quietY) * scaleY + dy) * w + (x + quietX) * scaleX + dx] = BLACK
            }
        }
        return Triple(px, w, h)
    }

    fun roundTrip(format: CodeFormat, m: ModuleMatrix): String {
        val linear = format != CodeFormat.QR_CODE && format != CodeFormat.PDF_417 && format != CodeFormat.DATA_MATRIX && format != CodeFormat.AZTEC
        val (px, w, h) = if (linear) rasterize(m, 4, 2, 10, 4) else rasterize(m, 8, 8, 4, 4)
        return decode(px, w, h, format)
    }

    /** Samples a [QrVectorImage] at pixel centres (solid paints use their start color) and returns ARGB pixels. */
    fun render(image: QrVectorImage, pxPerUnit: Int): Triple<IntArray, Int, Int> {
        val w = (image.width * pxPerUnit).toInt()
        val h = (image.height * pxPerUnit).toInt()
        val px = IntArray(w * h) { image.background.toInt() }
        val polys = image.items.map { it to flatten(it.shape) }
        for (y in 0 until h) for (x in 0 until w) {
            val ux = (x + 0.5f) / pxPerUnit
            val uy = (y + 0.5f) / pxPerUnit
            for ((item, poly) in polys) {
                if (item.shape is VectorShape.Text) continue
                if (covers(item.shape, poly, ux, uy)) {
                    px[y * w + x] = when (val p = item.paint) {
                        is ShapePaint.Solid -> p.argb.toInt()
                        is ShapePaint.LinearGradient -> p.startArgb.toInt()
                    }
                }
            }
        }
        return Triple(px, w, h)
    }

    private fun flatten(shape: VectorShape): List<List<Pair<Float, Float>>> {
        if (shape !is VectorShape.Path) return emptyList()
        val subs = ArrayList<MutableList<Pair<Float, Float>>>()
        var cur: MutableList<Pair<Float, Float>>? = null
        for (c in shape.commands) {
            when (c) {
                is PathCommand.MoveTo -> { cur = mutableListOf(c.x to c.y)\\; subs += cur }
                is PathCommand.LineTo -> cur!!.add(c.x to c.y)
                is PathCommand.QuadTo -> cur!!.add(c.x to c.y)
                is PathCommand.ArcTo -> {
                    for (i in 1..16) {
                        val a = Math.toRadians((c.startAngleDegrees + c.sweepAngleDegrees * i / 16).toDouble())
                        cur!!.add((c.cx + c.r * cos(a)).toFloat() to (c.cy + c.r * sin(a)).toFloat())
                    }
                }
                PathCommand.Close -> Unit
            }
        }
        return subs
    }

    private fun covers(shape: VectorShape, poly: List<List<Pair<Float, Float>>>, x: Float, y: Float): Boolean = when (shape) {
        is VectorShape.Rect -> inRoundRect(shape, x, y)
        is VectorShape.Circle -> (x - shape.cx) * (x - shape.cx) + (y - shape.cy) * (y - shape.cy) <= shape.r * shape.r
        is VectorShape.Path -> inPath(poly, shape.evenOdd, x, y)
        is VectorShape.Text -> false
    }

    private fun inRoundRect(r: VectorShape.Rect, x: Float, y: Float): Boolean {
        if (x < r.x || x > r.x + r.width || y < r.y || y > r.y + r.height) return false
        val rad = r.cornerRadius
        if (rad <= 0f) return true
        val cx = x.coerceIn(r.x + rad, r.x + r.width - rad)
        val cy = y.coerceIn(r.y + rad, r.y + r.height - rad)
        return (x - cx) * (x - cx) + (y - cy) * (y - cy) <= rad * rad
    }

    private fun inPath(subs: List<List<Pair<Float, Float>>>, evenOdd: Boolean, x: Float, y: Float): Boolean {
        var crossings = 0
        var winding = 0
        for (pts in subs) {
            for (i in pts.indices) {
                val (x0, y0) = pts[i]
                val (x1, y1) = pts[(i + 1) % pts.size]
                if ((y0 <= y) != (y1 <= y)) {
                    val t = (y - y0) / (y1 - y0)
                    if (x0 + t * (x1 - x0) > x) {
                        crossings++
                        winding += if (y1 > y0) 1 else -1
                    }
                }
            }
        }
        return if (evenOdd) crossings % 2 == 1 else winding != 0
    }

}

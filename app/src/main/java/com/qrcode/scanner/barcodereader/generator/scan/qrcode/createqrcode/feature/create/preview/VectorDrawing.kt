package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.PathCommand
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ShapePaint
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.StyledShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.TextAnchor
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.VectorShape
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Share of the logo tile left empty around the logo bitmap. */
private const val LOGO_PADDING_FRACTION = 0.1f

private fun argbColor(argb: Long): Color = Color(argb.toInt())

/**
 * The one renderer behind the on-screen preview, the PNG/JPG export and the PDF export: draws [image] scaled so its
 * width fills this scope's width. [logo] (when given) goes into [QrVectorImage.logoRect] on a tile in the background color.
 */
fun DrawScope.drawVectorImage(image: QrVectorImage, logo: ImageBitmap?) {
    val scale = size.width / image.width
    drawRect(argbColor(image.background), Offset.Zero, Size(image.width * scale, image.height * scale))
    val brushes = HashMap<ShapePaint, Brush>()
    for (item in image.items) {
        val brush = brushes.getOrPut(item.paint) { brushFor(item.paint, image, scale) }
        drawItem(item, brush, scale)
    }
    drawLogo(image, logo, scale)
}

private fun brushFor(paint: ShapePaint, image: QrVectorImage, scale: Float): Brush = when (paint) {
    is ShapePaint.Solid -> SolidColor(argbColor(paint.argb))
    is ShapePaint.LinearGradient -> {
        val line = paint.endpoints(image.width, image.height)
        Brush.linearGradient(
            colors = listOf(argbColor(paint.startArgb), argbColor(paint.endArgb)),
            start = Offset(line.x0 * scale, line.y0 * scale),
            end = Offset(line.x1 * scale, line.y1 * scale),
        )
    }
}

private fun DrawScope.drawItem(item: StyledShape, brush: Brush, s: Float) {
    when (val shape = item.shape) {
        is VectorShape.Rect -> {
            val topLeft = Offset(shape.x * s, shape.y * s)
            val rectSize = Size(shape.width * s, shape.height * s)
            if (shape.cornerRadius > 0f) {
                drawRoundRect(brush, topLeft, rectSize, CornerRadius(shape.cornerRadius * s))
            } else {
                drawRect(brush, topLeft, rectSize)
            }
        }
        is VectorShape.Circle -> drawCircle(brush, shape.r * s, Offset(shape.cx * s, shape.cy * s))
        is VectorShape.Path -> drawPath(shape.toComposePath(s), brush)
        is VectorShape.Text -> drawVectorText(shape, item.paint, s)
    }
}

/** Converts a platform-free outline to a Compose [Path] scaled by [s]; quadratic curves become cubics. */
internal fun VectorShape.Path.toComposePath(s: Float): Path {
    val path = Path()
    path.fillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero
    var x = 0f
    var y = 0f
    var startX = 0f
    var startY = 0f
    for (command in commands) {
        when (command) {
            is PathCommand.MoveTo -> {
                path.moveTo(command.x * s, command.y * s)
                x = command.x
                y = command.y
                startX = x
                startY = y
            }
            is PathCommand.LineTo -> {
                path.lineTo(command.x * s, command.y * s)
                x = command.x
                y = command.y
            }
            is PathCommand.QuadTo -> {
                val c1x = x + QUAD_TO_CUBIC * (command.cx - x)
                val c1y = y + QUAD_TO_CUBIC * (command.cy - y)
                val c2x = command.x + QUAD_TO_CUBIC * (command.cx - command.x)
                val c2y = command.y + QUAD_TO_CUBIC * (command.cy - command.y)
                path.cubicTo(c1x * s, c1y * s, c2x * s, c2y * s, command.x * s, command.y * s)
                x = command.x
                y = command.y
            }
            is PathCommand.ArcTo -> {
                val oval = Rect((command.cx - command.r) * s, (command.cy - command.r) * s, (command.cx + command.r) * s, (command.cy + command.r) * s)
                path.arcTo(oval, command.startAngleDegrees, command.sweepAngleDegrees, false)
                val end = Math.toRadians((command.startAngleDegrees + command.sweepAngleDegrees).toDouble())
                x = command.cx + command.r * cos(end).toFloat()
                y = command.cy + command.r * sin(end).toFloat()
            }
            PathCommand.Close -> {
                path.close()
                x = startX
                y = startY
            }
        }
    }
    return path
}

private const val QUAD_TO_CUBIC = 2f / 3f

private fun DrawScope.drawVectorText(text: VectorShape.Text, paint: ShapePaint, s: Float) {
    val argb = when (paint) {
        is ShapePaint.Solid -> paint.argb
        is ShapePaint.LinearGradient -> paint.startArgb
    }
    drawIntoCanvas { canvas ->
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = argb.toInt()
        p.textSize = text.size * s
        p.typeface = if (text.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        p.textAlign = when (text.anchor) {
            TextAnchor.START -> Paint.Align.LEFT
            TextAnchor.MIDDLE -> Paint.Align.CENTER
            TextAnchor.END -> Paint.Align.RIGHT
        }
        canvas.nativeCanvas.drawText(text.text, text.x * s, text.y * s, p)
    }
}

private fun DrawScope.drawLogo(image: QrVectorImage, logo: ImageBitmap?, s: Float) {
    val tile = image.logoRect ?: return
    drawRoundRect(
        argbColor(image.background),
        Offset(tile.x * s, tile.y * s),
        Size(tile.width * s, tile.height * s),
        CornerRadius(tile.cornerRadius * s),
    )
    if (logo == null || logo.width <= 0 || logo.height <= 0) return
    val box = tile.width * s * (1f - 2f * LOGO_PADDING_FRACTION)
    val ratio = logo.width.toFloat() / logo.height.toFloat()
    val w = if (ratio >= 1f) box else box * ratio
    val h = if (ratio >= 1f) box / ratio else box
    val left = tile.x * s + (tile.width * s - w) / 2f
    val top = tile.y * s + (tile.height * s - h) / 2f
    drawImage(
        image = logo,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(logo.width, logo.height),
        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
        dstSize = IntSize(w.roundToInt().coerceAtLeast(1), h.roundToInt().coerceAtLeast(1)),
    )
}

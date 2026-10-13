package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Serialises a [QrVectorImage] as a standalone SVG 1.1 document. */
object SvgExporter {
    /**
     * @param widthPx optional pixel width of the document (height keeps the aspect ratio); without it the size is in image units.
     * @param logoHref optional image reference (usually a `data:` URI) drawn inside [QrVectorImage.logoRect].
     */
    fun toSvg(image: QrVectorImage, widthPx: Int? = null, logoHref: String? = null): String {
        val outW = widthPx?.toFloat() ?: image.width
        val outH = if (widthPx != null) image.height * widthPx / image.width else image.height
        val gradients = LinkedHashMap<ShapePaint.LinearGradient, String>()
        for (item in image.items) {
            val p = item.paint
            if (p is ShapePaint.LinearGradient && p !in gradients) gradients[p] = "g${gradients.size}"
        }
        val sb = StringBuilder(4096)
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\"")
        sb
            .append(" width=\"")
            .append(num(outW))
            .append("\" height=\"")
            .append(num(outH))
            .append('"')
        sb
            .append(" viewBox=\"0 0 ")
            .append(num(image.width))
            .append(' ')
            .append(num(image.height))
            .append("\">\n")
        if (gradients.isNotEmpty()) {
            sb.append("<defs>\n")
            for ((g, id) in gradients) {
                val line = g.endpoints(image.width, image.height)
                sb.append("<linearGradient id=\"").append(id).append("\" gradientUnits=\"userSpaceOnUse\"")
                sb
                    .append(" x1=\"")
                    .append(num(line.x0))
                    .append("\" y1=\"")
                    .append(num(line.y0))
                sb
                    .append("\" x2=\"")
                    .append(num(line.x1))
                    .append("\" y2=\"")
                    .append(num(line.y1))
                    .append("\">")
                sb.append("<stop offset=\"0\"").append(stopColor(g.startArgb)).append("/>")
                sb.append("<stop offset=\"1\"").append(stopColor(g.endArgb)).append("/></linearGradient>\n")
            }
            sb.append("</defs>\n")
        }
        sb
            .append("<rect x=\"0\" y=\"0\" width=\"")
            .append(num(image.width))
            .append("\" height=\"")
            .append(num(image.height))
        sb.append('"').append(fill(ShapePaint.Solid(image.background), gradients)).append("/>\n")
        for (item in image.items) appendShape(sb, item, gradients)
        val logo = image.logoRect
        if (logoHref != null && logo != null) {
            sb
                .append("<image x=\"")
                .append(num(logo.x))
                .append("\" y=\"")
                .append(num(logo.y))
            sb
                .append("\" width=\"")
                .append(num(logo.width))
                .append("\" height=\"")
                .append(num(logo.height))
            sb.append("\" preserveAspectRatio=\"xMidYMid meet\" xlink:href=\"").append(escapeXml(logoHref)).append("\"/>\n")
        }
        sb.append("</svg>\n")
        return sb.toString()
    }

    private fun appendShape(sb: StringBuilder, item: StyledShape, gradients: Map<ShapePaint.LinearGradient, String>) {
        val paint = fill(item.paint, gradients)
        when (val s = item.shape) {
            is VectorShape.Rect -> {
                sb
                    .append("<rect x=\"")
                    .append(num(s.x))
                    .append("\" y=\"")
                    .append(num(s.y))
                sb
                    .append("\" width=\"")
                    .append(num(s.width))
                    .append("\" height=\"")
                    .append(num(s.height))
                    .append('"')
                if (s.cornerRadius >
                    0f
                ) {
                    sb
                        .append(" rx=\"")
                        .append(num(s.cornerRadius))
                        .append("\" ry=\"")
                        .append(num(s.cornerRadius))
                        .append('"')
                }
                sb.append(paint).append("/>\n")
            }
            is VectorShape.Circle -> {
                sb
                    .append("<circle cx=\"")
                    .append(num(s.cx))
                    .append("\" cy=\"")
                    .append(num(s.cy))
                sb
                    .append("\" r=\"")
                    .append(num(s.r))
                    .append('"')
                    .append(paint)
                    .append("/>\n")
            }
            is VectorShape.Path -> {
                sb.append("<path d=\"").append(pathData(s.commands)).append('"')
                if (s.evenOdd) sb.append(" fill-rule=\"evenodd\"")
                sb.append(paint).append("/>\n")
            }
            is VectorShape.Text -> {
                sb
                    .append("<text x=\"")
                    .append(num(s.x))
                    .append("\" y=\"")
                    .append(num(s.y))
                sb.append("\" font-size=\"").append(num(s.size)).append("\" font-family=\"sans-serif\" text-anchor=\"")
                sb
                    .append(
                        when (s.anchor) {
                            TextAnchor.START -> "start"
                            TextAnchor.MIDDLE -> "middle"
                            TextAnchor.END -> "end"
                        },
                    ).append('"')
                if (s.bold) sb.append(" font-weight=\"bold\"")
                sb
                    .append(paint)
                    .append('>')
                    .append(escapeXml(s.text))
                    .append("</text>\n")
            }
        }
    }

    private fun pathData(commands: List<PathCommand>): String {
        val sb = StringBuilder()
        for (c in commands) {
            if (sb.isNotEmpty()) sb.append(' ')
            when (c) {
                is PathCommand.MoveTo ->
                    sb
                        .append("M")
                        .append(num(c.x))
                        .append(' ')
                        .append(num(c.y))
                is PathCommand.LineTo ->
                    sb
                        .append("L")
                        .append(num(c.x))
                        .append(' ')
                        .append(num(c.y))
                is PathCommand.QuadTo ->
                    sb
                        .append("Q")
                        .append(num(c.cx))
                        .append(' ')
                        .append(num(c.cy))
                        .append(' ')
                        .append(num(c.x))
                        .append(' ')
                        .append(num(c.y))
                is PathCommand.ArcTo -> appendArc(sb, c)
                PathCommand.Close -> sb.append('Z')
            }
        }
        return sb.toString()
    }

    /** Centre-parameterised arc to SVG endpoint arcs; sweeps of 180 degrees or more are split because an arc cannot be 360. */
    private fun appendArc(sb: StringBuilder, a: PathCommand.ArcTo) {
        val parts = if (abs(a.sweepAngleDegrees) > 180f) 2 else 1
        val step = a.sweepAngleDegrees / parts
        for (i in 1..parts) {
            val end = Math.toRadians((a.startAngleDegrees + step * i).toDouble())
            val ex = a.cx + a.r * cos(end).toFloat()
            val ey = a.cy + a.r * sin(end).toFloat()
            if (i > 1) sb.append(' ')
            sb
                .append("A")
                .append(num(a.r))
                .append(' ')
                .append(num(a.r))
                .append(" 0 0 ")
            sb
                .append(if (step >= 0f) '1' else '0')
                .append(' ')
                .append(num(ex))
                .append(' ')
                .append(num(ey))
        }
    }

    private fun fill(paint: ShapePaint, gradients: Map<ShapePaint.LinearGradient, String>): String =
        when (paint) {
            is ShapePaint.Solid -> " fill=\"${hex(paint.argb)}\"" + opacityAttr("fill-opacity", paint.argb)
            is ShapePaint.LinearGradient -> " fill=\"url(#${gradients.getValue(paint)})\""
        }

    private fun stopColor(argb: Long): String = " stop-color=\"${hex(argb)}\"" + opacityAttr("stop-opacity", argb)

    private fun opacityAttr(name: String, argb: Long): String {
        val a = ((argb shr 24) and 0xFF).toInt()
        return if (a == 255) "" else " $name=\"${num(a / 255f)}\""
    }

    private fun hex(argb: Long): String {
        val rgb = (argb and 0xFFFFFF).toInt()
        return "#" + rgb.toString(16).padStart(6, '0').uppercase()
    }

    /** Float to plain decimal text with at most 3 decimals and no exponent. */
    internal fun num(v: Float): String {
        require(v.isFinite()) { "non-finite coordinate" }
        return BigDecimal(v.toDouble()).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    /** XML escaping of text and attribute values; characters that XML 1.0 forbids are dropped. */
    internal fun escapeXml(s: String): String {
        val sb = StringBuilder(s.length + 8)
        for (ch in s) {
            when {
                ch == '&' -> sb.append("&amp;")
                ch == '<' -> sb.append("&lt;")
                ch == '>' -> sb.append("&gt;")
                ch == '"' -> sb.append("&quot;")
                ch == '\'' -> sb.append("&apos;")
                ch.code < 0x20 && ch != '\n' && ch != '\t' && ch != '\r' -> Unit
                ch.code == 0xFFFE || ch.code == 0xFFFF -> Unit
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}

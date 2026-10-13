package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import kotlin.math.min

internal fun paintOf(style: QrStyle, angleDegrees: Float): ShapePaint {
    val end = style.gradientEnd
    return if (end != null) ShapePaint.LinearGradient(style.foreground, end, angleDegrees) else ShapePaint.Solid(style.foreground)
}

/** Turns a QR [ModuleMatrix] plus a [QrStyle] into a [QrVectorImage] (1 module = 1 unit). */
object QrVectorBuilder {
    /** Side of the module-free square behind a logo, as a fraction of the QR width. */
    const val LOGO_CLEAR_FRACTION = 0.22f

    /** Side of the logo image itself, as a fraction of the QR width (design: "auto-sized to 18%"). */
    const val LOGO_IMAGE_FRACTION = 0.18f

    /** Frame band height as a fraction of the image width. */
    const val FRAME_BAND_FRACTION = 0.2f

    /** Error correction to encode with: a centre logo forces at least Q so the covered modules can be recovered. */
    fun effectiveErrorCorrection(style: QrStyle): ErrorCorrection =
        if (style.logoUri != null && style.errorCorrection < ErrorCorrection.Q) ErrorCorrection.Q else style.errorCorrection

    fun build(
        matrix: ModuleMatrix,
        style: QrStyle,
        quietZoneModules: Int = 2,
        logoReserve: Boolean = style.logoUri != null,
        gradientAngleDegrees: Float = 45f,
    ): QrVectorImage {
        require(matrix.width == matrix.height) { "QR matrix must be square" }
        val size = matrix.width
        val q = quietZoneModules.coerceAtLeast(0)
        val codeSide = (size + 2 * q).toFloat()
        val paint = paintOf(style, gradientAngleDegrees)
        val items = ArrayList<StyledShape>()
        val clear = if (logoReserve) clearArea(size) else null

        for ((ox, oy) in QrGeometry.finderOrigins(size)) addEye(items, (ox + q).toFloat(), (oy + q).toFloat(), style.eyeShape, paint)
        addModules(items, matrix, style.dotShape, q, clear, paint)

        val frame = style.frameText?.trim().orEmpty()
        val height = if (frame.isEmpty()) codeSide else codeSide + codeSide * FRAME_BAND_FRACTION
        if (frame.isNotEmpty()) addFrame(items, frame, codeSide, q, style)

        val logo = if (logoReserve) {
            val side = size * LOGO_IMAGE_FRACTION
            val c = codeSide / 2f
            VectorShape.Rect(c - side / 2f, c - side / 2f, side, side, side * 0.2f)
        } else {
            null
        }
        return QrVectorImage(codeSide, height, style.background, items, logo)
    }

    /** Module-space square (left, top, right, bottom) where no modules are drawn. */
    private fun clearArea(size: Int): FloatArray {
        val side = size * LOGO_CLEAR_FRACTION
        val c = size / 2f
        return floatArrayOf(c - side / 2f, c - side / 2f, c + side / 2f, c + side / 2f)
    }

    private fun intersects(x: Int, y: Int, clear: FloatArray?): Boolean =
        clear != null && x + 1f > clear[0] && x < clear[2] && y + 1f > clear[1] && y < clear[3]

    // ---- eyes ----

    private fun addEye(items: MutableList<StyledShape>, x: Float, y: Float, shape: EyeShape, paint: ShapePaint) {
        when (shape) {
            EyeShape.SQUARE -> {
                items += StyledShape(
                    VectorShape.Path(PathShapes.rect(x, y, 7f, 7f) + PathShapes.rect(x + 1, y + 1, 5f, 5f), evenOdd = true),
                    paint,
                )
                items += StyledShape(VectorShape.Rect(x + 2, y + 2, 3f, 3f), paint)
            }
            EyeShape.ROUNDED -> {
                items += StyledShape(
                    VectorShape.Path(
                        PathShapes.roundedRect(x, y, 7f, 7f, 2f) + PathShapes.roundedRect(x + 1, y + 1, 5f, 5f, 1f),
                        evenOdd = true,
                    ),
                    paint,
                )
                items += StyledShape(VectorShape.Rect(x + 2, y + 2, 3f, 3f, 0.9f), paint)
            }
            EyeShape.CIRCLE -> {
                items += StyledShape(
                    VectorShape.Path(PathShapes.circle(x + 3.5f, y + 3.5f, 3.5f) + PathShapes.circle(x + 3.5f, y + 3.5f, 2.5f), evenOdd = true),
                    paint,
                )
                items += StyledShape(VectorShape.Circle(x + 3.5f, y + 3.5f, 1.5f), paint)
            }
        }
    }

    // ---- data modules ----

    private fun addModules(
        items: MutableList<StyledShape>,
        m: ModuleMatrix,
        shape: DotShape,
        q: Int,
        clear: FloatArray?,
        paint: ShapePaint,
    ) {
        val size = m.width
        fun drawn(x: Int, y: Int): Boolean =
            x in 0 until size && y in 0 until size && m[x, y] && !QrGeometry.isFinderModule(x, y, size) && !intersects(x, y, clear)
        for (y in 0 until size) {
            var x = 0
            while (x < size) {
                if (!drawn(x, y)) {
                    x++
                    continue
                }
                if (shape == DotShape.SQUARE) {
                    var end = x
                    while (drawn(end + 1, y)) end++
                    items += StyledShape(VectorShape.Rect((x + q).toFloat(), (y + q).toFloat(), (end - x + 1).toFloat(), 1f), paint)
                    x = end + 1
                    continue
                }
                val fx = (x + q).toFloat()
                val fy = (y + q).toFloat()
                items += StyledShape(
                    when (shape) {
                        DotShape.ROUNDED -> VectorShape.Rect(fx, fy, 1f, 1f, 0.35f)
                        DotShape.DOTS -> VectorShape.Circle(fx + 0.5f, fy + 0.5f, 0.42f)
                        else -> classy(fx, fy, tl = !drawn(x - 1, y) && !drawn(x, y - 1), br = !drawn(x + 1, y) && !drawn(x, y + 1))
                    },
                    paint,
                )
                x++
            }
        }
    }

    /** A square with the top-left and/or bottom-right corner rounded into a leaf-like shape. */
    private fun classy(x: Float, y: Float, tl: Boolean, br: Boolean): VectorShape.Path {
        val r = 0.5f
        val cmds = ArrayList<PathCommand>()
        cmds += PathCommand.MoveTo(if (tl) x + r else x, y)
        cmds += PathCommand.LineTo(x + 1, y)
        if (br) {
            cmds += PathCommand.LineTo(x + 1, y + 1 - r)
            cmds += PathCommand.ArcTo(x + 1 - r, y + 1 - r, r, 0f, 90f)
        } else {
            cmds += PathCommand.LineTo(x + 1, y + 1)
        }
        cmds += PathCommand.LineTo(x, y + 1)
        if (tl) {
            cmds += PathCommand.LineTo(x, y + r)
            cmds += PathCommand.ArcTo(x + r, y + r, r, 180f, 90f)
        }
        cmds += PathCommand.Close
        return VectorShape.Path(cmds)
    }

    // ---- frame ----

    private fun addFrame(items: MutableList<StyledShape>, text: String, codeSide: Float, q: Int, style: QrStyle) {
        val band = codeSide * FRAME_BAND_FRACTION
        val inset = q / 2f
        val bandHeight = band - inset
        items += StyledShape(
            VectorShape.Rect(inset, codeSide, codeSide - 2 * inset, bandHeight, bandHeight * 0.3f),
            ShapePaint.Solid(style.foreground),
        )
        val fit = (codeSide - 2 * inset) * 0.9f / (text.length * 0.6f)
        val fontSize = min(bandHeight * 0.45f, fit)
        items += StyledShape(
            VectorShape.Text(codeSide / 2f, codeSide + bandHeight / 2f + fontSize * 0.35f, text, fontSize, TextAnchor.MIDDLE, bold = true),
            ShapePaint.Solid(style.background),
        )
    }
}

/** Builds the picture of a barcode: 1D bars (plus the number under them) or a 2D symbol (PDF417, Data Matrix, Aztec). */
object BarcodeVectorBuilder {
    const val QUIET_1D = 10
    const val QUIET_2D = 3

    /**
     * [matrix] from [CodeEncoder.encodeBarcode]. [digitsText] is drawn under 1D bars when not null (ignored for 2D).
     * Bar height is `style.barHeightPercent` percent of the image width. Pass [format] to force 1D/2D detection,
     * otherwise a matrix whose rows are all identical is treated as 1D.
     */
    fun build(
        matrix: ModuleMatrix,
        style: QrStyle,
        digitsText: String?,
        format: CodeFormat? = null,
        gradientAngleDegrees: Float = 0f,
    ): QrVectorImage {
        val linear = when (format) {
            null -> isLinear(matrix)
            CodeFormat.PDF_417, CodeFormat.DATA_MATRIX, CodeFormat.AZTEC, CodeFormat.QR_CODE -> false
            else -> true
        }
        val paint = paintOf(style, gradientAngleDegrees)
        return if (linear) build1d(matrix, style, digitsText, paint) else build2d(matrix, style, paint)
    }

    private fun isLinear(m: ModuleMatrix): Boolean {
        for (y in 1 until m.height) for (x in 0 until m.width) if (m[x, y] != m[x, 0]) return false
        return true
    }

    private fun build1d(m: ModuleMatrix, style: QrStyle, digitsText: String?, paint: ShapePaint): QrVectorImage {
        val width = (m.width + 2 * QUIET_1D).toFloat()
        val pad = 4f
        val barHeight = width * style.barHeightPercent.coerceIn(40, 200) / 100f
        val text = digitsText?.takeIf { it.isNotBlank() && style.showNumber }
        val textZone = if (text != null) width * 0.14f else 0f
        val height = pad + barHeight + textZone + pad
        val items = ArrayList<StyledShape>()
        var x = 0
        while (x < m.width) {
            if (!m[x, 0]) {
                x++
                continue
            }
            var end = x
            while (end + 1 < m.width && m[end + 1, 0]) end++
            items += StyledShape(VectorShape.Rect((x + QUIET_1D).toFloat(), pad, (end - x + 1).toFloat(), barHeight), paint)
            x = end + 1
        }
        if (text != null) {
            val fit = width * 0.9f / (text.length * 0.62f)
            val fontSize = min(textZone * 0.7f, fit)
            items += StyledShape(
                VectorShape.Text(width / 2f, pad + barHeight + textZone * 0.8f, text, fontSize, TextAnchor.MIDDLE),
                ShapePaint.Solid(style.foreground),
            )
        }
        return QrVectorImage(width, height, style.background, items)
    }

    private fun build2d(m: ModuleMatrix, style: QrStyle, paint: ShapePaint): QrVectorImage {
        val q = QUIET_2D
        val items = ArrayList<StyledShape>()
        for (y in 0 until m.height) {
            var x = 0
            while (x < m.width) {
                if (!m[x, y]) {
                    x++
                    continue
                }
                var end = x
                while (end + 1 < m.width && m[end + 1, y]) end++
                items += StyledShape(VectorShape.Rect((x + q).toFloat(), (y + q).toFloat(), (end - x + 1).toFloat(), 1f), paint)
                x = end + 1
            }
        }
        return QrVectorImage((m.width + 2 * q).toFloat(), (m.height + 2 * q).toFloat(), style.background, items)
    }
}

/** One call from form to picture: payload, encoding with the right error correction, styled vector image. */
object CodeRenderer {
    /** The encoded payload and the finished picture for [spec]. */
    fun render(spec: CreateSpec, style: QrStyle): QrVectorImage =
        render(PayloadBuilder.formatOf(spec), PayloadBuilder.build(spec), style)

    fun render(format: CodeFormat, payload: String, style: QrStyle): QrVectorImage =
        if (format == CodeFormat.QR_CODE) {
            val matrix = CodeEncoder.encodeQr(payload, QrVectorBuilder.effectiveErrorCorrection(style))
            QrVectorBuilder.build(matrix, style)
        } else {
            val matrix = CodeEncoder.encodeBarcode(format, payload)
            BarcodeVectorBuilder.build(matrix, style, payload, format)
        }
}

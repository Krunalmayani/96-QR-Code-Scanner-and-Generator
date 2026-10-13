package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Platform-free drawing model shared by the Compose preview, bitmap export, SVG export and PDF export.
 * Coordinates are Floats in "image units" (for QR codes: 1 unit = 1 module), origin top-left, y down.
 */
sealed interface VectorShape {
    data class Rect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val cornerRadius: Float = 0f,
    ) : VectorShape

    data class Circle(val cx: Float, val cy: Float, val r: Float) : VectorShape

    /**
     * A filled outline made of one or more sub-paths (each starts with [PathCommand.MoveTo]).
     * With [evenOdd] true the fill uses the even-odd rule, so a nested sub-path punches a hole (eye rings).
     */
    data class Path(val commands: List<PathCommand>, val evenOdd: Boolean = false) : VectorShape

    /** Text drawn with its baseline at [y]; [x] is the anchor (see [anchor]). */
    data class Text(
        val x: Float,
        val y: Float,
        val text: String,
        val size: Float,
        val anchor: TextAnchor = TextAnchor.MIDDLE,
        val bold: Boolean = false,
    ) : VectorShape
}

enum class TextAnchor { START, MIDDLE, END }

sealed interface PathCommand {
    data class MoveTo(val x: Float, val y: Float) : PathCommand
    data class LineTo(val x: Float, val y: Float) : PathCommand
    data class QuadTo(val cx: Float, val cy: Float, val x: Float, val y: Float) : PathCommand

    /**
     * Circular arc on the circle centred ([cx],[cy]) with radius [r], starting at [startAngleDegrees] and sweeping
     * [sweepAngleDegrees] (positive = clockwise on screen). Angle 0 points right, 90 points down.
     * Maps 1:1 to Compose/Android `arcTo(Rect(cx-r, cy-r, cx+r, cy+r), start, sweep, false)`.
     * The current point is expected to be the arc start; afterwards it is the arc end.
     */
    data class ArcTo(
        val cx: Float,
        val cy: Float,
        val r: Float,
        val startAngleDegrees: Float,
        val sweepAngleDegrees: Float,
    ) : PathCommand

    data object Close : PathCommand
}

sealed interface ShapePaint {
    data class Solid(val argb: Long) : ShapePaint

    /**
     * Linear gradient laid across the WHOLE image rectangle. [angleDegrees]: 0 = left to right, 90 = top to bottom,
     * 45 = top-left to bottom-right. Use [endpoints] so every renderer draws the same gradient.
     */
    data class LinearGradient(val startArgb: Long, val endArgb: Long, val angleDegrees: Float) : ShapePaint {
        /** Start and end points of the gradient line for an image of [width] x [height]. */
        fun endpoints(width: Float, height: Float): GradientLine {
            val rad = Math.toRadians(angleDegrees.toDouble())
            val dx = cos(rad)
            val dy = sin(rad)
            val half = (abs(width * dx) + abs(height * dy)) / 2.0
            val cx = width / 2.0
            val cy = height / 2.0
            return GradientLine(
                (cx - half * dx).toFloat(),
                (cy - half * dy).toFloat(),
                (cx + half * dx).toFloat(),
                (cy + half * dy).toFloat(),
            )
        }
    }
}

data class GradientLine(val x0: Float, val y0: Float, val x1: Float, val y1: Float)

data class StyledShape(val shape: VectorShape, val paint: ShapePaint)

/**
 * A finished, renderer-independent picture. Draw [background] over the full [width] x [height], then [items] in order.
 * [logoRect] (when not null) is where the centre logo image must be drawn; the modules behind it were already skipped.
 */
data class QrVectorImage(
    val width: Float,
    val height: Float,
    val background: Long,
    val items: List<StyledShape>,
    val logoRect: VectorShape.Rect? = null,
)

/** Small helpers that build [VectorShape.Path] outlines. */
internal object PathShapes {
    fun rect(x: Float, y: Float, w: Float, h: Float): List<PathCommand> = listOf(
        PathCommand.MoveTo(x, y),
        PathCommand.LineTo(x + w, y),
        PathCommand.LineTo(x + w, y + h),
        PathCommand.LineTo(x, y + h),
        PathCommand.Close,
    )

    fun roundedRect(x: Float, y: Float, w: Float, h: Float, r: Float): List<PathCommand> = listOf(
        PathCommand.MoveTo(x + r, y),
        PathCommand.LineTo(x + w - r, y),
        PathCommand.ArcTo(x + w - r, y + r, r, 270f, 90f),
        PathCommand.LineTo(x + w, y + h - r),
        PathCommand.ArcTo(x + w - r, y + h - r, r, 0f, 90f),
        PathCommand.LineTo(x + r, y + h),
        PathCommand.ArcTo(x + r, y + h - r, r, 90f, 90f),
        PathCommand.LineTo(x, y + r),
        PathCommand.ArcTo(x + r, y + r, r, 180f, 90f),
        PathCommand.Close,
    )

    /**
     * A "leaf": a rectangle whose top-left and bottom-right corners are rounded with radius [r] while the other two
     * stay sharp. [r] must not exceed half of the smaller side.
     */
    fun leaf(x: Float, y: Float, w: Float, h: Float, r: Float): List<PathCommand> = listOf(
        PathCommand.MoveTo(x + r, y),
        PathCommand.LineTo(x + w, y),
        PathCommand.LineTo(x + w, y + h - r),
        PathCommand.ArcTo(x + w - r, y + h - r, r, 0f, 90f),
        PathCommand.LineTo(x, y + h),
        PathCommand.LineTo(x, y + r),
        PathCommand.ArcTo(x + r, y + r, r, 180f, 90f),
        PathCommand.Close,
    )

    fun circle(cx: Float, cy: Float, r: Float): List<PathCommand> = listOf(
        PathCommand.MoveTo(cx + r, cy),
        PathCommand.ArcTo(cx, cy, r, 0f, 180f),
        PathCommand.ArcTo(cx, cy, r, 180f, 180f),
        PathCommand.Close,
    )
}

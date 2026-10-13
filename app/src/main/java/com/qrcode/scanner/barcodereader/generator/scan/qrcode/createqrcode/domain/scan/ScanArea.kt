package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import kotlin.math.max
import kotlin.math.min

/**
 * Geometry for "scan area only": maps the viewfinder frame, drawn on the preview, to image coordinates so
 * detections outside the frame can be ignored. Assumes the preview is shown with FILL_CENTER (scaled to cover the
 * view, centered, overflow cropped), which is how the Scan screen configures PreviewView.
 */
object ScanArea {
    /** Size of the upright image for a camera frame of [width] x [height] that must be rotated by [rotationDegrees]. */
    fun uprightSize(width: Int, height: Int, rotationDegrees: Int): Pair<Int, Int> =
        if ((rotationDegrees % 180 + 180) % 180 == 90) height to width else width to height

    /**
     * Converts [viewRect] (view pixels, view is [viewWidth] x [viewHeight]) to the coordinates of an upright image
     * of [imageWidth] x [imageHeight]. The result is clamped to the image. Returns null for degenerate sizes.
     */
    fun viewRectToImage(
        viewRect: PxRect,
        viewWidth: Float,
        viewHeight: Float,
        imageWidth: Float,
        imageHeight: Float,
    ): PxRect? {
        if (viewWidth <= 0f || viewHeight <= 0f || imageWidth <= 0f || imageHeight <= 0f) return null
        val scale = max(viewWidth / imageWidth, viewHeight / imageHeight)
        val offsetX = (viewWidth - imageWidth * scale) / 2f
        val offsetY = (viewHeight - imageHeight * scale) / 2f
        val left = ((viewRect.left - offsetX) / scale).coerceIn(0f, imageWidth)
        val right = ((viewRect.right - offsetX) / scale).coerceIn(0f, imageWidth)
        val top = ((viewRect.top - offsetY) / scale).coerceIn(0f, imageHeight)
        val bottom = ((viewRect.bottom - offsetY) / scale).coerceIn(0f, imageHeight)
        return PxRect(min(left, right), min(top, bottom), max(left, right), max(top, bottom))
    }

    /**
     * True when [code] counts for [area]: no area means the whole image, a code without bounds always counts,
     * otherwise the centre of its bounding box must lie inside the area.
     */
    fun isInside(code: DetectedCode, area: PxRect?): Boolean {
        if (area == null) return true
        val bounds = code.bounds ?: return true
        return area.contains(bounds.centerX, bounds.centerY)
    }
}

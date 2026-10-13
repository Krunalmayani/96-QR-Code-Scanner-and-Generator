package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

/** Zoom helpers. Zoom is a linear value 0..1 (CameraControl.setLinearZoom). */
object ZoomMath {
    private const val PINCH_GAIN = 0.6f

    /** New linear zoom after a pinch gesture whose incremental scale factor is [scale] (>1 spreads fingers = zoom in). */
    fun applyPinch(current: Float, scale: Float): Float {
        val base = if (current.isNaN()) 0f else current.coerceIn(0f, 1f)
        if (scale.isNaN() || scale <= 0f || scale.isInfinite()) return base
        return (base + (scale - 1f) * PINCH_GAIN).coerceIn(0f, 1f)
    }

    /** Label for the right end of the zoom slider, e.g. 5.0 -> "5", capped to a readable 1..20. */
    fun maxRatioLabel(maxZoomRatio: Float): Int {
        if (maxZoomRatio.isNaN() || maxZoomRatio.isInfinite()) return DEFAULT_MAX_LABEL
        return maxZoomRatio.toInt().coerceIn(2, 20)
    }

    private const val DEFAULT_MAX_LABEL = 5
}

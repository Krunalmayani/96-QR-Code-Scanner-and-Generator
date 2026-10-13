package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

enum class ReliabilityReason {
    /** Contrast ratio between code and background below 3:1. */
    LOW_CONTRAST,

    /** Code lighter than its background: many scanners cannot read inverted codes. */
    INVERTED,

    /** The gradient end color fails the contrast test. */
    GRADIENT_LOW_CONTRAST,

    /** Dot modules with error correction L or M. */
    DOTS_LOW_EC,
}

/** Result of [ContrastChecker.scanReliability]. [reasons] is empty when [isGood]. */
data class ScanReliability(
    val reasons: List<ReliabilityReason>,
    /** Contrast ratio of the code color against the background (the worst of both gradient colors). */
    val contrastRatio: Double,
) {
    val isGood: Boolean get() = reasons.isEmpty()
    val isLow: Boolean get() = reasons.isNotEmpty()
    val level: Level get() = if (isGood) Level.GOOD else Level.LOW

    enum class Level { GOOD, LOW }
}

object ContrastChecker {
    /** Minimum WCAG contrast ratio for a code to be considered readable. */
    const val MIN_RATIO = 3.0

    /** WCAG relative luminance (0 = black, 1 = white) of an 0xAARRGGBB color; alpha is ignored. */
    fun relativeLuminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    /** WCAG contrast ratio, 1.0 (identical) to 21.0 (black on white). Symmetric. */
    fun contrastRatio(fgArgb: Long, bgArgb: Long): Double {
        val a = relativeLuminance(fgArgb)
        val b = relativeLuminance(bgArgb)
        return (max(a, b) + 0.05) / (min(a, b) + 0.05)
    }

    fun scanReliability(style: QrStyle): ScanReliability {
        val reasons = ArrayList<ReliabilityReason>()
        val bgLum = relativeLuminance(style.background)
        val ratio = contrastRatio(style.foreground, style.background)
        var worst = ratio
        if (ratio < MIN_RATIO) reasons += ReliabilityReason.LOW_CONTRAST
        if (relativeLuminance(style.foreground) > bgLum) reasons += ReliabilityReason.INVERTED
        style.gradientEnd?.let { end ->
            val endRatio = contrastRatio(end, style.background)
            worst = min(worst, endRatio)
            if (endRatio < MIN_RATIO) reasons += ReliabilityReason.GRADIENT_LOW_CONTRAST
            if (relativeLuminance(end) > bgLum && ReliabilityReason.INVERTED !in reasons) reasons += ReliabilityReason.INVERTED
        }
        val ec = QrVectorBuilder.effectiveErrorCorrection(style)
        if (style.dotShape == DotShape.DOTS && (ec == ErrorCorrection.L || ec == ErrorCorrection.M)) {
            reasons += ReliabilityReason.DOTS_LOW_EC
        }
        return ScanReliability(reasons, worst)
    }
}

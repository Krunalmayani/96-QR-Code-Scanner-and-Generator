package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.DotShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.EyeShape
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle

/** One change the customize editor can make to a [QrStyle]. Applied with [applying]. */
sealed interface StyleEdit {
    data class Foreground(val argb: Long) : StyleEdit

    data class Background(val argb: Long) : StyleEdit

    /** Turns the gradient on ([endArgb] set) or off (null). */
    data class GradientEnd(val endArgb: Long?) : StyleEdit

    /** Sets both gradient colors from a preset. */
    data class Gradient(val preset: GradientPreset) : StyleEdit

    data class Dots(val shape: DotShape) : StyleEdit

    data class Eyes(val shape: EyeShape) : StyleEdit

    data class Correction(val level: ErrorCorrection) : StyleEdit

    /** Sets (a stored `file://` Uri string) or removes (null) the center logo. */
    data class Logo(val uri: String?) : StyleEdit

    /** Text of the frame under the code; blank removes the frame. */
    data class Frame(val text: String?) : StyleEdit

    data class Size(val px: Int) : StyleEdit

    data class Format(val format: ExportFormat) : StyleEdit

    data class ShowNumber(val show: Boolean) : StyleEdit

    data class BarHeight(val percent: Int) : StyleEdit
}

object StyleRules {
    const val FRAME_TEXT_MAX = 24
    const val BAR_HEIGHT_MIN = 40
    const val BAR_HEIGHT_MAX = 200
    private const val OPAQUE = 0xFF000000

    /** The smallest error correction a center logo allows. */
    val logoMinimum: ErrorCorrection = ErrorCorrection.Q

    fun opaque(argb: Long): Long = OPAQUE or (argb and 0xFFFFFF)

    /** [level] raised to the logo minimum when [hasLogo]. */
    fun correctionFor(level: ErrorCorrection, hasLogo: Boolean): ErrorCorrection =
        if (hasLogo && level < logoMinimum) logoMinimum else level
}

/** Applies [edit] and keeps the result consistent (opaque colors, logo needs error correction Q or better, clamped numbers). */
fun QrStyle.applying(edit: StyleEdit): QrStyle = when (edit) {
    is StyleEdit.Foreground -> copy(foreground = StyleRules.opaque(edit.argb))
    is StyleEdit.Background -> copy(background = StyleRules.opaque(edit.argb))
    is StyleEdit.GradientEnd -> copy(gradientEnd = edit.endArgb?.let { StyleRules.opaque(it) })
    is StyleEdit.Gradient -> copy(foreground = edit.preset.start, gradientEnd = edit.preset.end)
    is StyleEdit.Dots -> copy(dotShape = edit.shape)
    is StyleEdit.Eyes -> copy(eyeShape = edit.shape)
    is StyleEdit.Correction -> copy(errorCorrection = StyleRules.correctionFor(edit.level, logoUri != null))
    is StyleEdit.Logo -> copy(
        logoUri = edit.uri,
        errorCorrection = StyleRules.correctionFor(errorCorrection, edit.uri != null),
    )
    is StyleEdit.Frame -> copy(frameText = edit.text?.trim()?.take(StyleRules.FRAME_TEXT_MAX)?.takeIf { it.isNotBlank() })
    is StyleEdit.Size -> copy(exportSizePx = ExportSizes.snap(edit.px))
    is StyleEdit.Format -> copy(exportFormat = edit.format)
    is StyleEdit.ShowNumber -> copy(showNumber = edit.show)
    is StyleEdit.BarHeight -> copy(barHeightPercent = edit.percent.coerceIn(StyleRules.BAR_HEIGHT_MIN, StyleRules.BAR_HEIGHT_MAX))
}

/** Hints shown under the style controls. */
object StyleAdvice {
    /** True when the style mixes dots or leaf eyes with error correction L or M (design s42 hint). */
    fun needsStrongerCorrection(style: QrStyle): Boolean {
        val effective = QrVectorBuilder.effectiveErrorCorrection(style)
        val fragile = style.dotShape == DotShape.DOTS || style.eyeShape == EyeShape.LEAF
        return fragile && effective < ErrorCorrection.Q
    }

    /** Share of damaged modules a code can survive at [level], in percent. */
    fun recoveryPercent(level: ErrorCorrection): Int = when (level) {
        ErrorCorrection.L -> 7
        ErrorCorrection.M -> 15
        ErrorCorrection.Q -> 25
        ErrorCorrection.H -> 30
    }
}

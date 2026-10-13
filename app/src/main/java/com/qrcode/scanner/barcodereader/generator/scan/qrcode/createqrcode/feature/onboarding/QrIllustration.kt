package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import kotlin.math.abs

/** Number of modules per side of the decorative code. */
internal const val QR_ILLUSTRATION_GRID = 21

private const val FINDER_SIZE = 7

/**
 * Decorative (not scannable) QR-like pattern: three finder squares plus a fixed pseudo-random fill.
 * Pure function so the shape of the illustration is testable.
 */
internal fun isIllustrationModuleDark(row: Int, col: Int): Boolean {
    val last = QR_ILLUSTRATION_GRID - 1
    val inTop = row < FINDER_SIZE
    val inBottom = row > last - FINDER_SIZE
    val inLeft = col < FINDER_SIZE
    val inRight = col > last - FINDER_SIZE
    return when {
        inTop && inLeft -> finderModule(row, col)
        inTop && inRight -> finderModule(row, col - (last - FINDER_SIZE + 1))
        inBottom && inLeft -> finderModule(row - (last - FINDER_SIZE + 1), col)
        isSeparator(row, col) -> false
        else -> (row * 31 + col * 17 + row * col * 7) % 5 < 2
    }
}

/** Dark ring, light ring, dark 3x3 center of a 7x7 finder pattern. */
private fun finderModule(row: Int, col: Int): Boolean {
    val ring = maxOf(abs(row - 3), abs(col - 3))
    return ring != 2
}

/** The light margin that sits next to each finder pattern. */
private fun isSeparator(row: Int, col: Int): Boolean {
    val last = QR_ILLUSTRATION_GRID - 1
    val nearTop = row <= FINDER_SIZE
    val nearBottom = row >= last - FINDER_SIZE
    val nearLeft = col <= FINDER_SIZE
    val nearRight = col >= last - FINDER_SIZE
    return (nearTop && nearLeft) || (nearTop && nearRight) || (nearBottom && nearLeft)
}

/**
 * Draws the decorative code in a square of [dimension]. [styled] uses round dots in the brand color
 * (slide 2); otherwise square modules in ink (slide 1).
 */
@Composable
internal fun QrIllustration(dimension: Dp, styled: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(dimension)) {
        val cell = this.size.minDimension / QR_ILLUSTRATION_GRID
        val color = if (styled) OnboardingColors.qrAccent else OnboardingColors.qrInk
        for (row in 0 until QR_ILLUSTRATION_GRID) {
            for (col in 0 until QR_ILLUSTRATION_GRID) {
                if (!isIllustrationModuleDark(row, col)) continue
                val topLeft = Offset(col * cell, row * cell)
                if (styled) {
                    drawRoundRect(
                        color = color,
                        topLeft = topLeft,
                        size = Size(cell, cell),
                        cornerRadius = CornerRadius(cell / 2f, cell / 2f),
                    )
                } else {
                    drawRect(color = color, topLeft = topLeft, size = Size(cell, cell))
                }
            }
        }
    }
}

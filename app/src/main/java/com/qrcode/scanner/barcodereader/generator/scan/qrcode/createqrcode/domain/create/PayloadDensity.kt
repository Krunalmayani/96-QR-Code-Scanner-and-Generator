package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection

/** How dense (hard to scan) the QR code for a payload will be. */
enum class DensityLevel { LOW, MEDIUM, HIGH }

object PayloadDensity {
    private const val MEDIUM_FROM_MODULES = 34
    private const val HIGH_FROM_MODULES = 58

    /** Density from the number of modules per side of the QR code for [payload]; too long to encode counts as HIGH. */
    fun of(payload: String): DensityLevel {
        if (payload.isEmpty()) return DensityLevel.LOW
        val side =
            try {
                CodeEncoder.encodeQr(payload, ErrorCorrection.M).width
            } catch (e: CodeEncodeException) {
                return DensityLevel.HIGH
            }
        return when {
            side >= HIGH_FROM_MODULES -> DensityLevel.HIGH
            side >= MEDIUM_FROM_MODULES -> DensityLevel.MEDIUM
            else -> DensityLevel.LOW
        }
    }
}

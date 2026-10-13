package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

/** Down-sampling of a picked logo image: big photos are loaded at a fraction of their size to bound memory. */
object ImageSampling {
    /** Longest side a logo ever needs: the logo is drawn at 18% of an export that is at most 2048 px wide. */
    const val LOGO_MAX_SIDE = 512

    /**
     * Power-of-two `inSampleSize` that brings [longestSide] down to at most twice [maxSide] but never below [maxSide]
     * (so the image stays sharp enough for the size it is drawn at). 1 when the image is already small.
     */
    fun sampleSize(longestSide: Int, maxSide: Int = LOGO_MAX_SIDE): Int {
        if (longestSide <= 0 || maxSide <= 0) return 1
        var sample = 1
        while (longestSide / (sample * 2) >= maxSide) sample *= 2
        return sample
    }
}

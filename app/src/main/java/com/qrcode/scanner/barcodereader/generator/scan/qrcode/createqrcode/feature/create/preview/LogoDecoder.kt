package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

/** Loads the center logo the editor stored in `filesDir/logos` (a `file://` Uri string). Blocking: call off the main thread. */
object LogoDecoder {
    /** Longest side the preview and exports ever need. */
    const val MAX_SIDE = 512

    fun decode(uriString: String, maxSide: Int = MAX_SIDE): Bitmap? {
        val path = Uri.parse(uriString).path ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSizeFor(max(bounds.outWidth, bounds.outHeight), maxSide) }
        return BitmapFactory.decodeFile(path, options)
    }

    /** Power-of-two sample size that brings [longestSide] to at most [maxSide] (at least 1). */
    fun sampleSizeFor(longestSide: Int, maxSide: Int): Int {
        var sample = 1
        while (longestSide / (sample * 2) >= maxSide) sample *= 2
        return sample
    }
}

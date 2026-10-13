package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ImageSampling
import kotlin.math.max

/** Loads the center logo the editor stored in `filesDir/logos` (a `file://` Uri string). Blocking: call off the main thread. */
object LogoDecoder {
    fun decode(uriString: String, maxSide: Int = ImageSampling.LOGO_MAX_SIDE): Bitmap? {
        val path = Uri.parse(uriString).path ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = ImageSampling.sampleSize(max(bounds.outWidth, bounds.outHeight), maxSide)
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, options)
    }
}

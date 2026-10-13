package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ImageScanPlan
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ImageVariant
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.distinctCodes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scannerOptionsForAll
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.toDetectedCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads a picked image with ML Kit. Tries the image as is first; if nothing is found it retries on a
 * contrast-stretched copy, an inverted copy and the three other rotations ([ImageScanPlan]), stopping at the first hit.
 */
class MlKitImageCodeReader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ImageCodeReader {
        override suspend fun read(imageUri: String): ImageReadResult =
            withContext(Dispatchers.Default) {
                val bitmap =
                    ImageDecoding.decode(context, Uri.parse(imageUri), MAX_SIDE_PX)
                        ?: return@withContext ImageReadResult.Unreadable
                val scanner = BarcodeScanning.getClient(scannerOptionsForAll())
                try {
                    for (variant in ImageScanPlan.variants) {
                        val codes = scanVariant(scanner, bitmap, variant)
                        if (codes.isNotEmpty()) return@withContext ImageReadResult.Found(codes.distinctCodes())
                    }
                    ImageReadResult.NoCode
                } finally {
                    scanner.close()
                    bitmap.recycle()
                }
            }

        private suspend fun scanVariant(scanner: BarcodeScanner, bitmap: Bitmap, variant: ImageVariant): List<DetectedCode> {
            val prepared = ImageVariants.prepare(bitmap, variant) ?: return emptyList()
            return try {
                val image = InputImage.fromBitmap(prepared, variant.rotationDegrees)
                scanner.process(image).await().mapNotNull { it.toDetectedCode() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            } finally {
                if (prepared !== bitmap) prepared.recycle()
            }
        }

        private companion object {
            /** Longer side after downsampling; large enough for small codes in screenshots and photos. */
            const val MAX_SIDE_PX = 2048
        }
    }

private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }

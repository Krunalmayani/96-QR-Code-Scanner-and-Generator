package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.PxRect
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanArea

/** The viewfinder frame in view pixels plus the size of the view it was measured in. */
data class ScanAreaSpec(val frame: PxRect, val viewWidth: Float, val viewHeight: Float)

/**
 * CameraX analyzer that runs ML Kit on each frame and reports the codes inside [area] on the main thread.
 * Frames arriving while one is processed are dropped by CameraX (STRATEGY_KEEP_ONLY_LATEST).
 * [enabled] and [area] may be changed from any thread.
 */
class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onCodes: (List<DetectedCode>) -> Unit,
) : ImageAnalysis.Analyzer {
    @Volatile var enabled: Boolean = true

    /** null analyses the whole frame. */
    @Volatile var area: ScanAreaSpec? = null

    @Suppress("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !enabled) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val crop = cropInImage(imageProxy.width, imageProxy.height, rotation)
        try {
            scanner.process(InputImage.fromMediaImage(mediaImage, rotation))
                .addOnSuccessListener { barcodes -> deliver(barcodes.mapNotNull { it.toDetectedCode() }, crop) }
                .addOnCompleteListener { imageProxy.close() }
        } catch (e: RuntimeException) {
            imageProxy.close()
        }
    }

    private fun cropInImage(width: Int, height: Int, rotation: Int): PxRect? {
        val spec = area ?: return null
        val (upright, uprightHeight) = ScanArea.uprightSize(width, height, rotation)
        return ScanArea.viewRectToImage(spec.frame, spec.viewWidth, spec.viewHeight, upright.toFloat(), uprightHeight.toFloat())
    }

    private fun deliver(codes: List<DetectedCode>, crop: PxRect?) {
        val inside = codes.filter { ScanArea.isInside(it, crop) }
        if (inside.isNotEmpty() && enabled) onCodes(inside)
    }
}

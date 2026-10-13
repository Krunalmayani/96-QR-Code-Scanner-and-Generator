package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ErrorCorrection

/** Thrown when a payload cannot be encoded (too long for the symbology, illegal characters). */
class CodeEncodeException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/** A grid of modules, `true` = dark. No quiet zone. For 1D codes every row is identical. */
class ModuleMatrix(
    val width: Int,
    val height: Int,
    private val bits: BooleanArray,
) {
    init {
        require(width > 0 && height > 0) { "empty matrix" }
        require(bits.size == width * height) { "bits size ${bits.size} != $width x $height" }
    }

    operator fun get(x: Int, y: Int): Boolean {
        require(x in 0 until width && y in 0 until height) { "($x,$y) outside ${width}x$height" }
        return bits[y * width + x]
    }

    /** Number of dark modules. */
    fun darkCount(): Int = bits.count { it }

    /** A copy without the all-light outer rows and columns. */
    fun cropped(): ModuleMatrix {
        var minX = width
        var maxX = -1
        var minY = height
        var maxY = -1
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (bits[y * width + x]) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < 0) return this
        val w = maxX - minX + 1
        val h = maxY - minY + 1
        val out = BooleanArray(w * h)
        for (y in 0 until h) for (x in 0 until w) out[y * w + x] = bits[(minY + y) * width + (minX + x)]
        return ModuleMatrix(w, h, out)
    }

    override fun equals(other: Any?): Boolean =
        other is ModuleMatrix && other.width == width && other.height == height && other.bits.contentEquals(bits)

    override fun hashCode(): Int = 31 * (31 * width + height) + bits.contentHashCode()

    companion object {
        fun fromBitMatrix(m: BitMatrix): ModuleMatrix {
            val bits = BooleanArray(m.width * m.height)
            for (y in 0 until m.height) for (x in 0 until m.width) bits[y * m.width + x] = m[x, y]
            return ModuleMatrix(m.width, m.height, bits)
        }
    }
}

/** Maps our [CodeFormat] to ZXing, or null for formats that cannot be created. */
fun CodeFormat.toZxingFormat(): BarcodeFormat? =
    when (this) {
        CodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
        CodeFormat.EAN_13 -> BarcodeFormat.EAN_13
        CodeFormat.EAN_8 -> BarcodeFormat.EAN_8
        CodeFormat.UPC_A -> BarcodeFormat.UPC_A
        CodeFormat.CODE_128 -> BarcodeFormat.CODE_128
        CodeFormat.CODE_39 -> BarcodeFormat.CODE_39
        CodeFormat.ITF -> BarcodeFormat.ITF
        CodeFormat.PDF_417 -> BarcodeFormat.PDF_417
        CodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        CodeFormat.AZTEC -> BarcodeFormat.AZTEC
        else -> null
    }

/** ZXing based encoder producing platform independent [ModuleMatrix] grids (quiet zone 0). */
object CodeEncoder {
    fun zxingLevel(level: ErrorCorrection): ErrorCorrectionLevel =
        when (level) {
            ErrorCorrection.L -> ErrorCorrectionLevel.L
            ErrorCorrection.M -> ErrorCorrectionLevel.M
            ErrorCorrection.Q -> ErrorCorrectionLevel.Q
            ErrorCorrection.H -> ErrorCorrectionLevel.H
        }

    private fun isAscii(s: String): Boolean = s.all { it.code < 128 }

    /** QR code without quiet zone (callers add the margin). Non-ASCII text is encoded as UTF-8 with an ECI header. */
    fun encodeQr(payload: String, level: ErrorCorrection = ErrorCorrection.M): ModuleMatrix {
        if (payload.isEmpty()) throw CodeEncodeException("Empty payload")
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = zxingLevel(level)
        hints[EncodeHintType.MARGIN] = 0
        if (!isAscii(payload)) hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        return try {
            ModuleMatrix.fromBitMatrix(QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 0, 0, hints))
        } catch (e: Exception) {
            throw CodeEncodeException("Cannot encode QR code: ${e.message}", e)
        }
    }

    /** True when [payload] fits a QR code at [level]. */
    fun canEncodeQr(payload: String, level: ErrorCorrection): Boolean =
        payload.isNotEmpty() &&
            try {
                encodeQr(payload, level)
                true
            } catch (e: CodeEncodeException) {
                false
            }

    /**
     * 1D formats and the 2D barcodes (PDF417, Data Matrix, Aztec), no quiet zone. [data] must already carry its check digit
     * (see [BarcodeCheckDigit.normalize]). For 1D formats the matrix has [heightPx] identical rows (pass 1 for vector output).
     */
    fun encodeBarcode(format: CodeFormat, data: String, heightPx: Int = 1): ModuleMatrix {
        val zx = format.toZxingFormat() ?: throw CodeEncodeException("Unsupported format $format")
        if (zx == BarcodeFormat.QR_CODE) return encodeQr(data, ErrorCorrection.M)
        if (data.isEmpty()) throw CodeEncodeException("Empty data")
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 0
        if (!isAscii(data) && (zx == BarcodeFormat.PDF_417 || zx == BarcodeFormat.AZTEC || zx == BarcodeFormat.DATA_MATRIX)) {
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        }
        return try {
            val m = ModuleMatrix.fromBitMatrix(MultiFormatWriter().encode(data, zx, 0, heightPx.coerceAtLeast(1), hints))
            if (zx == BarcodeFormat.DATA_MATRIX) m.cropped() else m
        } catch (e: Exception) {
            throw CodeEncodeException("Cannot encode $format: ${e.message}", e)
        }
    }

    /** Encodes [payload] in [format] (QR uses [level]; barcodes use [heightPx]). */
    fun encode(format: CodeFormat, payload: String, level: ErrorCorrection = ErrorCorrection.M, heightPx: Int = 1): ModuleMatrix =
        if (format == CodeFormat.QR_CODE) encodeQr(payload, level) else encodeBarcode(format, payload, heightPx)

    /** Builds the payload of [spec] and encodes it. */
    fun encode(spec: CreateSpec, level: ErrorCorrection = ErrorCorrection.M, heightPx: Int = 1): ModuleMatrix =
        encode(PayloadBuilder.formatOf(spec), PayloadBuilder.build(spec), level, heightPx)
}

/** Position helpers for the three QR finder patterns ("eyes"), each 7x7 modules. */
object QrGeometry {
    const val FINDER_SIZE = 7

    /** Top-left corners of the top-left, top-right and bottom-left finder patterns of a [size]x[size] QR code. */
    fun finderOrigins(size: Int): List<Pair<Int, Int>> =
        listOf(0 to 0, (size - FINDER_SIZE) to 0, 0 to (size - FINDER_SIZE))

    /** True when module ([x], [y]) is part of one of the three 7x7 finder squares. */
    fun isFinderModule(x: Int, y: Int, size: Int): Boolean {
        val far = size - FINDER_SIZE
        val inX = x < FINDER_SIZE
        val inY = y < FINDER_SIZE
        return (inX && inY) || (x >= far && inY && x < size) || (inX && y >= far && y < size)
    }
}

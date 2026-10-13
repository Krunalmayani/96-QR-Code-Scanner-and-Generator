package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.util.Base64
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.IoDispatcher
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ExportDimensions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.SvgExporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.LogoDecoder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.drawVectorImage
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Produces export files with the same Compose renderer as the on-screen preview ([drawVectorImage]):
 * PNG/JPG through an off-screen [ImageBitmap], PDF through a [PdfDocument] page canvas, SVG through the pure [SvgExporter].
 */
class AndroidCodeBitmapRenderer @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher,
) : CodeBitmapRenderer {

    override suspend fun render(job: ExportJob): ExportedFile = withContext(io) {
        val style = job.style
        val image = CodeImageFactory(job.format, job.payload).image(style)
        val logoBitmap = if (image.logoRect != null) style.logoUri?.let { LogoDecoder.decode(it) } else null
        val bytes = when (style.exportFormat) {
            ExportFormat.PNG -> rasterBytes(image, logoBitmap, style.exportSizePx, Bitmap.CompressFormat.PNG, PNG_QUALITY)
            ExportFormat.JPG -> rasterBytes(image, logoBitmap, style.exportSizePx, Bitmap.CompressFormat.JPEG, JPEG_QUALITY)
            ExportFormat.SVG -> svgBytes(image, logoBitmap, style.exportSizePx)
            ExportFormat.PDF -> pdfBytes(image, logoBitmap, style.exportSizePx)
        }
        ExportedFile(bytes, style.exportFormat)
    }

    private fun rasterBytes(
        image: QrVectorImage,
        logo: Bitmap?,
        widthPx: Int,
        compress: Bitmap.CompressFormat,
        quality: Int,
    ): ByteArray {
        val pixels = ExportDimensions.of(image, widthPx)
        val target = ImageBitmap(pixels.width, pixels.height)
        paint(Canvas(target), image, logo?.asImageBitmap(), pixels.width.toFloat(), pixels.height.toFloat())
        val out = ByteArrayOutputStream()
        target.asAndroidBitmap().compress(compress, quality, out)
        return out.toByteArray()
    }

    private fun pdfBytes(image: QrVectorImage, logo: Bitmap?, sizePx: Int): ByteArray {
        val pageWidth = ExportDimensions.pdfPageWidthPoints(sizePx)
        val pageHeight = (pageWidth * image.height / image.width).roundToInt().coerceAtLeast(1)
        val document = PdfDocument()
        try {
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
            paint(Canvas(page.canvas), image, logo?.asImageBitmap(), pageWidth.toFloat(), pageHeight.toFloat())
            document.finishPage(page)
            val out = ByteArrayOutputStream()
            document.writeTo(out)
            return out.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun svgBytes(image: QrVectorImage, logo: Bitmap?, widthPx: Int): ByteArray {
        val href = logo?.let { "data:image/png;base64," + Base64.encodeToString(pngOf(it), Base64.NO_WRAP) }
        return SvgExporter.toSvg(image, widthPx, href).toByteArray(Charsets.UTF_8)
    }

    private fun pngOf(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, out)
        return out.toByteArray()
    }

    private fun paint(canvas: Canvas, image: QrVectorImage, logo: ImageBitmap?, width: Float, height: Float) {
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(width, height)) {
            drawVectorImage(image, logo)
        }
    }

    private companion object {
        const val PNG_QUALITY = 100
        const val JPEG_QUALITY = 95
    }
}

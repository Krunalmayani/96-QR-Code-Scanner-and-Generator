package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.IoDispatcher
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.ImageSampling
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.max

/** Keeps picked logos in `filesDir/logos` as down-scaled PNGs (strips metadata, bounds memory). */
class AndroidLogoStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : LogoStore {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun import(source: String): String? =
            withContext(io) {
                try {
                    val bitmap = decodeScaled(Uri.parse(source)) ?: return@withContext null
                    val dir = File(context.filesDir, DIR).apply { mkdirs() }
                    val target = File(dir, "logo_${System.currentTimeMillis()}.png")
                    FileOutputStream(target).use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
                    Uri.fromFile(target).toString()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.w(e, "Importing the logo failed")
                    null
                }
            }

        override suspend fun discard(uri: String?) {
            if (uri == null) return
            withContext(io) {
                val path = Uri.parse(uri).path ?: return@withContext
                val file = File(path)
                val logos = File(context.filesDir, DIR)
                if (file.parentFile == logos) file.delete()
            }
        }

        private fun decodeScaled(source: Uri): Bitmap? {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = ImageSampling.sampleSize(max(bounds.outWidth, bounds.outHeight))
                }
            return resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
        }

        private companion object {
            const val DIR = "logos"
            const val PNG_QUALITY = 100
        }
    }

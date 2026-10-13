package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di.IoDispatcher
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Gallery and share-sheet output of the Generated screen.
 *
 * Gallery: API 29+ inserts through MediaStore (Pictures/QR Codes, IS_PENDING while writing, no permission). API 24-28 writes
 * into the public Pictures folder, which needs WRITE_EXTERNAL_STORAGE (declared with maxSdkVersion 28 in the manifest).
 * PDFs cannot live in the image collection, so they go to Downloads/QR Codes.
 * Share: the file is written to cacheDir/shared and handed out through the FileProvider `${packageName}.fileprovider`.
 */
class AndroidCodeExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val renderer: CodeBitmapRenderer,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CodeExporter {

    override fun needsStoragePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveToGallery(job: ExportJob, fileName: String): SaveOutcome = withContext(io) {
        try {
            val file = renderer.render(job)
            val toDownloads = file.format == ExportFormat.PDF
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                insertWithMediaStore(file, fileName, toDownloads)
            } else {
                writeToPublicFolder(file, fileName, toDownloads)
            }
            SaveOutcome.Saved(toDownloads)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Saving the export failed")
            SaveOutcome.Failed
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createShareFile(job: ExportJob, fileName: String): ShareFile? = withContext(io) {
        try {
            val file = renderer.render(job)
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            deleteOldShareFiles(dir)
            val target = File(dir, fileName)
            target.writeBytes(file.bytes)
            val uri = FileProvider.getUriForFile(context, context.packageName + FILE_PROVIDER_SUFFIX, target)
            ShareFile(uri.toString(), file.format.mimeType)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Preparing the share file failed")
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun insertWithMediaStore(file: ExportedFile, fileName: String, toDownloads: Boolean) {
        val resolver = context.contentResolver
        val collection = if (toDownloads) MediaStore.Downloads.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val root = if (toDownloads) Environment.DIRECTORY_DOWNLOADS else Environment.DIRECTORY_PICTURES
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, file.format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$root/$FOLDER")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: throw IOException("MediaStore insert returned no Uri")
        try {
            val stream = resolver.openOutputStream(uri) ?: throw IOException("No output stream for $uri")
            stream.use { it.write(file.bytes) }
            val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            resolver.update(uri, done, null, null)
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION")
    private fun writeToPublicFolder(file: ExportedFile, fileName: String, toDownloads: Boolean) {
        val root = Environment.getExternalStoragePublicDirectory(
            if (toDownloads) Environment.DIRECTORY_DOWNLOADS else Environment.DIRECTORY_PICTURES,
        )
        val dir = File(root, FOLDER)
        if (!dir.exists() && !dir.mkdirs()) throw IOException("Cannot create $dir")
        val target = File(dir, fileName)
        target.writeBytes(file.bytes)
        MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf(file.format.mimeType), null)
    }

    private fun deleteOldShareFiles(dir: File) {
        val cutoff = System.currentTimeMillis() - SHARE_FILE_MAX_AGE_MS
        dir.listFiles()?.forEach { if (it.lastModified() < cutoff) it.delete() }
    }

    private companion object {
        const val FOLDER = "QR Codes"
        const val SHARE_DIR = "shared"
        const val FILE_PROVIDER_SUFFIX = ".fileprovider"
        const val SHARE_FILE_MAX_AGE_MS = 24L * 60 * 60 * 1000
    }
}

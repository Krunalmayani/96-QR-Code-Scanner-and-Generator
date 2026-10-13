package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export.HistoryExportFormat
import java.io.File
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Hands history text and CSV files to the system share sheet. Every function returns false when sharing failed. */
internal object HistoryShare {
    private const val SHARED_DIR = "shared"
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    fun shareText(context: Context, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return launchChooser(context, send, context.getString(R.string.hist_share_chooser_title))
    }

    /** Writes [csv] to cache/shared/ and offers it through a FileProvider uri. */
    suspend fun exportCsv(context: Context, csv: String): Boolean {
        val uri = try {
            withContext(Dispatchers.IO) { writeCsv(context, csv) }
        } catch (e: IOException) {
            return false
        } catch (e: IllegalArgumentException) {
            return false
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = HistoryExportFormat.CSV.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return launchChooser(context, send, context.getString(R.string.hist_export_chooser_title))
    }

    private fun writeCsv(context: Context, csv: String): Uri {
        val dir = File(context.cacheDir, SHARED_DIR)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Cannot create ${dir.path}")
        val file = File(dir, "qr-history-${LocalDate.now()}.${HistoryExportFormat.CSV.extension}")
        file.writeText(csv, Charsets.UTF_8)
        return FileProvider.getUriForFile(context, context.packageName + FILE_PROVIDER_SUFFIX, file)
    }

    private fun launchChooser(context: Context, send: Intent, title: String): Boolean {
        val chooser = Intent.createChooser(send, title)
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(chooser)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackMail
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.StoreLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import android.provider.Settings as AndroidSettings

/** Android-side helpers for the settings screens: every function returns false (or does nothing) instead of crashing. */
internal object SettingsIntents {
    fun appInfo(): String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    fun deviceInfo(): String =
        "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    /** Opens [url] in a Custom Tab (or the default browser when no Custom Tabs provider exists). */
    fun openUrl(context: Context, url: String): Boolean =
        try {
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    /** Opens the user's mail app on a `mailto:` link. */
    fun openMail(context: Context, mailtoUri: String): Boolean =
        try {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(mailtoUri)))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    /** Opens a pre-addressed e-mail to the support address (no feedback form details). */
    fun openSupportMail(context: Context): Boolean {
        val body = "\n\n---\nApp: ${appInfo()}\nDevice: ${deviceInfo()}"
        val uri = FeedbackMail.mailtoUri(context.getString(R.string.settings_support_email), "App support", body)
        return openMail(context, uri)
    }

    /** System app-info page where the user manages permissions. */
    fun openAppSettings(context: Context): Boolean =
        try {
            val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    /** Share sheet with [text] (name and Play link). */
    fun shareText(context: Context, text: String, chooserTitle: String): Boolean =
        try {
            val send =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            context.startActivity(Intent.createChooser(send, chooserTitle))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    fun playWebLink(): String = StoreLinks.webUrl(BuildConfig.APPLICATION_ID)

    /** Opens the Play listing, preferring the Play Store app (market://) and falling back to the web page. */
    fun openStoreListing(context: Context): Boolean {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse(StoreLinks.marketUrl(BuildConfig.APPLICATION_ID)))
        return try {
            context.startActivity(market)
            true
        } catch (e: ActivityNotFoundException) {
            openUrl(context, playWebLink())
        }
    }

    /**
     * Play In-App Review; opens the store listing when the review flow cannot be requested or launched.
     * [onUnavailable] runs when neither works.
     */
    fun rateApp(context: Context, onUnavailable: () -> Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            if (!openStoreListing(context)) onUnavailable()
            return
        }
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                manager.launchReviewFlow(activity, request.result).addOnFailureListener {
                    if (!openStoreListing(context)) onUnavailable()
                }
            } else if (!openStoreListing(context)) {
                onUnavailable()
            }
        }
    }

    /** Open source licenses screen from play-services-oss-licenses. */
    fun openLicenses(context: Context, title: String): Boolean =
        try {
            OssLicensesMenuActivity.setActivityTitle(title)
            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    /** Share sheet for an exported history file already written by [ExportFiles.writeSharedFile]. */
    fun shareFile(context: Context, uri: Uri, payload: ExportPayload, chooserTitle: String): Boolean =
        try {
            val send =
                Intent(Intent.ACTION_SEND).apply {
                    type = payload.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(payload.fileName, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(Intent.createChooser(send, chooserTitle))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
}

/** File work for "Export history": writes to a user-picked Uri, or to the app cache for sharing. */
internal object ExportFiles {
    private const val SHARED_DIR = "shared"
    private const val FILE_PREFIX = "qr-history-"

    suspend fun writeToUri(context: Context, uri: Uri, text: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val stream = context.contentResolver.openOutputStream(uri, "wt")
                if (stream == null) {
                    false
                } else {
                    stream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                    true
                }
            } catch (e: IOException) {
                false
            } catch (e: SecurityException) {
                false
            }
        }

    /** Writes [payload] under cache/shared/ and returns a FileProvider Uri, or null on failure. Old exports are removed first. */
    suspend fun writeSharedFile(context: Context, payload: ExportPayload): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.cacheDir, SHARED_DIR)
                dir.mkdirs()
                dir.listFiles()?.filter { it.name.startsWith(FILE_PREFIX) }?.forEach { it.delete() }
                val file = File(dir, payload.fileName)
                file.writeText(payload.text, Charsets.UTF_8)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: IOException) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }
        }
}

internal tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

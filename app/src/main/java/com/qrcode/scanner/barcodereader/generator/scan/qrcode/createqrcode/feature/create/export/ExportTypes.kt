package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ExportFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle

/** Everything needed to produce one export file: what to encode ([format], [payload]) and how it looks ([style]). */
data class ExportJob(val format: CodeFormat, val payload: String, val style: QrStyle)

/** The bytes of a finished export in [format] (`style.exportFormat` of the job). */
class ExportedFile(val bytes: ByteArray, val format: ExportFormat)

/** A file the share sheet can read: a `content://` Uri string from our FileProvider and its MIME type. */
data class ShareFile(val uri: String, val mimeType: String)

sealed interface SaveOutcome {
    /** Written to the gallery, or to Downloads when [toDownloads] (PDF cannot live in the image gallery). */
    data class Saved(val toDownloads: Boolean) : SaveOutcome

    data object Failed : SaveOutcome
}

/** Draws a code into the bytes of PNG, JPG, SVG or PDF. All Android drawing sits behind this interface. */
interface CodeBitmapRenderer {
    /** Throws `CodeEncodeException` (domain.create) when the payload cannot be encoded in the job's format. */
    suspend fun render(job: ExportJob): ExportedFile
}

/** Gets finished exports out of the app: into the gallery or to the share sheet. */
interface CodeExporter {
    /** True on Android 9 and older while WRITE_EXTERNAL_STORAGE has not been granted. */
    fun needsStoragePermission(): Boolean

    suspend fun saveToGallery(job: ExportJob, fileName: String): SaveOutcome

    /** Writes the export into the share cache and returns its Uri, or null when it could not be produced. */
    suspend fun createShareFile(job: ExportJob, fileName: String): ShareFile?
}

/** Copies a picked center logo into app storage, because Photo Picker Uris are only temporary. */
interface LogoStore {
    /** Copies the image behind [source] (a content Uri string) into `filesDir/logos`; returns a `file://` Uri string or null. */
    suspend fun import(source: String): String?

    /** Deletes a logo file previously returned by [import]; other Uris are ignored. */
    suspend fun discard(uri: String?)
}

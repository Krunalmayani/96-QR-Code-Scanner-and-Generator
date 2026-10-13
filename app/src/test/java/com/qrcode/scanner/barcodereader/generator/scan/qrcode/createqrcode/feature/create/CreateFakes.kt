package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.CodeExporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.ExportJob
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.LogoStore
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.SaveOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.ShareFile

/** Scriptable [CodeExporter]: records every call and answers with the configured outcomes. */
class FakeCodeExporter : CodeExporter {
    var permissionNeeded = false
    var saveOutcome: SaveOutcome = SaveOutcome.Saved(toDownloads = false)
    var shareFile: ShareFile? = ShareFile("content://test.fileprovider/shared/code.png", "image/png")

    val saved = mutableListOf<Pair<ExportJob, String>>()
    val shared = mutableListOf<Pair<ExportJob, String>>()

    override fun needsStoragePermission(): Boolean = permissionNeeded

    override suspend fun saveToGallery(job: ExportJob, fileName: String): SaveOutcome {
        saved += job to fileName
        return saveOutcome
    }

    override suspend fun createShareFile(job: ExportJob, fileName: String): ShareFile? {
        shared += job to fileName
        return shareFile
    }
}

/** [LogoStore] that "imports" every source to a fixed file Uri (or fails when [result] is null). */
class FakeLogoStore(
    var result: String? = "file:///data/logos/logo_1.png",
) : LogoStore {
    val imported = mutableListOf<String>()
    val discarded = mutableListOf<String?>()

    override suspend fun import(source: String): String? {
        imported += source
        return result
    }

    override suspend fun discard(uri: String?) {
        discarded += uri
    }
}

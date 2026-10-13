package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource

/** Small builders shared by repository, use-case and ViewModel tests. */
object Fixtures {
    fun text(raw: String = "hello", format: CodeFormat = CodeFormat.QR_CODE): ScanContent =
        ScanContent.Text(raw, format)

    fun website(url: String = "https://example.com"): ScanContent =
        ScanContent.Website(url, CodeFormat.QR_CODE, url)

    fun record(
        content: ScanContent = text(),
        source: ScanSource = ScanSource.CAMERA,
        createdAt: Long = 1_000L,
        id: Long = 0,
        isFavorite: Boolean = false,
        note: String = "",
    ): ScanRecord =
        ScanRecord(
            id = id,
            content = content,
            createdAt = createdAt,
            source = source,
            isFavorite = isFavorite,
            note = note,
        )
}

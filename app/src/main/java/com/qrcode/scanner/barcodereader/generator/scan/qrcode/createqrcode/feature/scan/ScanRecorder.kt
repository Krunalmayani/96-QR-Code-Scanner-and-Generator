package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import javax.inject.Inject

/**
 * Turns a decoded code into a stored history record: parse, save, log the analytics event (enum names only,
 * never the content). Shared by the live camera and scan-from-image.
 */
class ScanRecorder(
    private val saveScan: SaveScanUseCase,
    private val analytics: Analytics,
    private val parser: ScanContentParser,
) {
    @Inject
    constructor(saveScan: SaveScanUseCase, analytics: Analytics) : this(saveScan, analytics, ScanContentParser())

    /** Parsed meaning of [code], without saving it (used to label rows before the user picks one). */
    fun parse(code: DetectedCode): ScanContent = parser.parse(code.rawValue, code.format)

    suspend fun record(code: DetectedCode, source: ScanSource): ScanRecord {
        val record = saveScan(parse(code), source)
        analytics.log(
            Events.SCAN_SUCCESS,
            mapOf(
                Params.TYPE to record.type.name,
                Params.SOURCE to source.name,
                Params.FORMAT to record.format.name,
            ),
        )
        return record
    }
}

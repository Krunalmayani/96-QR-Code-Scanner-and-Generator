package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import javax.inject.Inject

/**
 * Reads the payload of a freshly built form back into a typed [ScanContent], exactly like a scan would
 * (so the created record shows the same title and type as the scanned one). Open so tests can replace it.
 */
open class CreatedContentParser @Inject constructor() {
    private val parser by lazy { ScanContentParser() }

    open fun parse(payload: String, format: CodeFormat): ScanContent = parser.parse(payload, format)
}

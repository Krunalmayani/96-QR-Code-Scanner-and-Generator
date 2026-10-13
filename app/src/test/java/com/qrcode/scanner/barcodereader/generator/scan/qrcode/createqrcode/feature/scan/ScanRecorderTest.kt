package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.ScanContentParser
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.DetectedCode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeHistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing.FakeSettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScanRecorderTest {
    private val history = FakeHistoryRepository()
    private val analytics = mockk<Analytics>(relaxed = true)
    private val recorder = ScanRecorder(SaveScanUseCase(history, FakeSettingsRepository()) { 5L }, analytics, ScanContentParser())

    @Test
    fun `record parses saves and logs enum names only`() =
        runTest {
            val record = recorder.record(DetectedCode("tel:+4915112345678", CodeFormat.QR_CODE), ScanSource.CAMERA)

            assertEquals(ContentType.PHONE, record.type)
            assertEquals(listOf(record), history.all())
            verify {
                analytics.log(
                    Events.SCAN_SUCCESS,
                    mapOf("type" to "PHONE", "source" to "CAMERA", "format" to "QR_CODE"),
                )
            }
        }

    @Test
    fun `parse labels a code without saving it`() =
        runTest {
            val content = recorder.parse(DetectedCode("5901234123457", CodeFormat.EAN_13))

            assertEquals(ContentType.PRODUCT, content.type)
            assertEquals(emptyList<Any>(), history.all())
        }
}

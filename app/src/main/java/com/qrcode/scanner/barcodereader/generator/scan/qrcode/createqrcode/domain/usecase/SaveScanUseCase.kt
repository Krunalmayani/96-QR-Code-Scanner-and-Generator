package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository

/**
 * Records a scanned or created code.
 *
 * The record is always returned and usable by Result/Generated screens: when "Save history" is off it is kept
 * in memory only (negative id), otherwise it is stored in the database. Also bumps the counters that drive
 * the rate prompt: successfulScans for CAMERA/IMAGE, completedActions for CREATED.
 */
class SaveScanUseCase(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    suspend operator fun invoke(content: ScanContent, source: ScanSource, style: QrStyle? = null): ScanRecord {
        val persist = settingsRepository.current().saveHistory
        val record = ScanRecord(content = content, createdAt = clock(), source = source, style = style)
        val saved = historyRepository.save(record, persist)
        settingsRepository.update { it.withCounted(source) }
        return saved
    }

    private fun AppSettings.withCounted(source: ScanSource) =
        when (source) {
            ScanSource.CREATED -> copy(completedActions = completedActions + 1)
            ScanSource.CAMERA, ScanSource.IMAGE -> copy(successfulScans = successfulScans + 1)
        }
}

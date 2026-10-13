package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository

class DeleteRecordsUseCase(private val historyRepository: HistoryRepository) {
    /** Deletes [ids] and returns the records that were removed, to hand back to [undo]. Unknown ids are skipped. */
    suspend operator fun invoke(ids: Set<Long>): List<ScanRecord> {
        val removed = ids.mapNotNull { historyRepository.getById(it) }
        if (removed.isNotEmpty()) historyRepository.delete(removed.map { it.id }.toSet())
        return removed
    }

    /** Puts back records returned by [invoke], keeping ids and timestamps. */
    suspend fun undo(records: List<ScanRecord>) {
        if (records.isNotEmpty()) historyRepository.restore(records)
    }
}

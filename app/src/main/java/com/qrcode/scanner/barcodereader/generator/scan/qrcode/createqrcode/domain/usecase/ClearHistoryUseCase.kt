package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository

class ClearHistoryUseCase(
    private val historyRepository: HistoryRepository,
) {
    /** Clears history ([source] null = every source) and returns how many rows were removed. */
    suspend operator fun invoke(keepFavorites: Boolean, source: ScanSource? = null): Int =
        historyRepository.clear(keepFavorites, source)
}

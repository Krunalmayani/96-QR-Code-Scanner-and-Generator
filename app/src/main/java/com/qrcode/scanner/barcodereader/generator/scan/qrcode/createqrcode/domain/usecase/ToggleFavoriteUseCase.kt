package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository

class ToggleFavoriteUseCase(
    private val historyRepository: HistoryRepository,
) {
    /** Flips the favorite flag of [id]. Returns the new state, or null when the record does not exist. */
    suspend operator fun invoke(id: Long): Boolean? {
        val record = historyRepository.getById(id) ?: return null
        val favorite = !record.isFavorite
        historyRepository.setFavorite(setOf(id), favorite)
        return favorite
    }

    /** Sets the same favorite state on many records (multi-select). */
    suspend fun set(ids: Set<Long>, favorite: Boolean) {
        if (ids.isNotEmpty()) historyRepository.setFavorite(ids, favorite)
    }
}

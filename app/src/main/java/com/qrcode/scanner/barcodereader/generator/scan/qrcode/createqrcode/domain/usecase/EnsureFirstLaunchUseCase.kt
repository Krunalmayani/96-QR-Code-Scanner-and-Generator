package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository

/** Stamps AppSettings.firstLaunchAt once (the first call); call it on app start. */
class EnsureFirstLaunchUseCase(
    private val settingsRepository: SettingsRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    suspend operator fun invoke() {
        val now = clock()
        settingsRepository.update { if (it.firstLaunchAt == 0L) it.copy(firstLaunchAt = now) else it }
    }
}

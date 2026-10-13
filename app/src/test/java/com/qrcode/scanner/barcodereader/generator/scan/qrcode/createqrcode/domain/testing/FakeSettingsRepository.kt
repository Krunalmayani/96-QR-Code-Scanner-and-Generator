package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.testing

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [SettingsRepository] for tests. [value] reads or replaces the current settings directly. */
class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    var value: AppSettings
        get() = state.value
        set(newValue) {
            state.value = newValue
        }

    override val settings: Flow<AppSettings> = state

    override suspend fun current(): AppSettings = state.value

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        state.update(transform)
    }
}

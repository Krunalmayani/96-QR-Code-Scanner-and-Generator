package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateHubLogic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryFilter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** [recent] are the types of the newest created codes (design "Recent" chips); empty when nothing was created yet. */
data class CreateHubUiState(
    val query: String = "",
    val recent: List<CreateType> = emptyList(),
)

sealed interface CreateHubEvent {
    data class QueryChanged(
        val query: String,
    ) : CreateHubEvent
}

@HiltViewModel
class CreateHubViewModel
    @Inject
    constructor(
        historyRepository: HistoryRepository,
    ) : ViewModel() {
        private val query = MutableStateFlow("")

        val state: StateFlow<CreateHubUiState> =
            combine(
                query,
                historyRepository.observe(HistoryFilter(source = ScanSource.CREATED)),
            ) { q, created -> CreateHubUiState(q, CreateHubLogic.recent(created)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CreateHubUiState())

        fun onEvent(event: CreateHubEvent) {
            when (event) {
                is CreateHubEvent.QueryChanged -> query.value = event.query
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }

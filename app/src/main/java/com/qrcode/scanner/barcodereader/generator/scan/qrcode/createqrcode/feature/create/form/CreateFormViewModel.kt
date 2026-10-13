package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeInputs
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.BarcodeStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateSpec
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.DensityLevel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.EventTimes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FieldError
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.FormField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.PayloadBuilder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.PayloadDensity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.TimeOfDay
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.Validators
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.SaveScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * State of one Create form. [spec] is null only when the route carried an unknown type key.
 * Errors appear on blur ([touched]) and after a Generate attempt ([showAllErrors]); [errors] always holds all of them,
 * so [isValid] (which enables the Generate button) does not depend on what is currently shown.
 */
data class CreateFormUiState(
    val spec: CreateSpec? = null,
    val errors: Map<FormField, FieldError> = emptyMap(),
    val touched: Set<FormField> = emptySet(),
    val showAllErrors: Boolean = false,
    val generating: Boolean = false,
    /** Text form only: how dense the code will be. */
    val density: DensityLevel? = null,
    /** Barcode forms only: live state of the data field. */
    val barcodeStatus: BarcodeStatus = BarcodeStatus.Empty,
    /** Barcode forms only: live preview of a valid value. */
    val barcodePreview: QrVectorImage? = null,
) {
    val type: CreateType? get() = spec?.type
    val isValid: Boolean get() = spec != null && errors.isEmpty()

    val visibleErrors: Map<FormField, FieldError>
        get() = if (showAllErrors) errors else errors.filterKeys { it in touched }
}

sealed interface CreateFormEvent {
    /** The user edited a field: [spec] is the whole form with the change applied. */
    data class Edit(val spec: CreateSpec) : CreateFormEvent

    /** Focus left [field] after it had focus. */
    data class Blur(val field: FormField) : CreateFormEvent

    /** Barcode form: another symbology; the typed data is kept. */
    data class SelectBarcode(val type: CreateType) : CreateFormEvent

    data class SetAllDay(val allDay: Boolean) : CreateFormEvent

    /** [utcDateMillis] is the UTC midnight a Material3 date picker returns. */
    data class EventDatePicked(val start: Boolean, val utcDateMillis: Long) : CreateFormEvent

    data class EventTimePicked(val start: Boolean, val hour: Int, val minute: Int) : CreateFormEvent

    data object Generate : CreateFormEvent
}

sealed interface CreateFormEffect {
    data class OpenGenerated(val recordId: Long) : CreateFormEffect

    data object GenerateFailed : CreateFormEffect
}

@HiltViewModel
class CreateFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveScan: SaveScanUseCase,
    private val parser: CreatedContentParser,
    private val analytics: Analytics,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateFormUiState())
    val state: StateFlow<CreateFormUiState> = _state.asStateFlow()

    private val _effects = Channel<CreateFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        val type = CreateType.fromKey(savedStateHandle.get<String>(KEY_TYPE))
        if (type != null) _state.value = derive(CreateFormUiState(), initialSpec(type))
    }

    fun onEvent(event: CreateFormEvent) {
        when (event) {
            is CreateFormEvent.Edit -> edit(event.spec)
            is CreateFormEvent.Blur -> _state.update { it.copy(touched = it.touched + event.field) }
            is CreateFormEvent.SelectBarcode -> selectBarcode(event.type)
            is CreateFormEvent.SetAllDay -> updateEvent { EventTimes.withAllDay(it, event.allDay, TimeZone.getDefault()) }
            is CreateFormEvent.EventDatePicked -> pickDate(event)
            is CreateFormEvent.EventTimePicked -> pickTime(event)
            CreateFormEvent.Generate -> generate()
        }
    }

    private fun initialSpec(type: CreateType): CreateSpec =
        if (type == CreateType.EVENT) EventTimes.blankEvent(System.currentTimeMillis(), TimeZone.getDefault()) else CreateSpec.blank(type)

    private fun edit(spec: CreateSpec) {
        val current = _state.value.spec ?: return
        if (spec.type != current.type) return
        _state.update { derive(it, clamped(spec)) }
    }

    /** Barcode data is cut to the symbology's maximum length. */
    private fun clamped(spec: CreateSpec): CreateSpec =
        if (spec is CreateSpec.Barcode) spec.copy(data = BarcodeInputs.clamp(spec.format, spec.data)) else spec

    private fun selectBarcode(type: CreateType) {
        val current = _state.value.spec as? CreateSpec.Barcode ?: return
        if (!type.isBarcode) return
        _state.update { derive(it, clamped(CreateSpec.Barcode(type, current.data))) }
    }

    private fun updateEvent(touch: FormField? = null, change: (CreateSpec.Event) -> CreateSpec.Event) {
        val current = _state.value.spec as? CreateSpec.Event ?: return
        _state.update { derive(it, change(current)).let { next -> if (touch != null) next.copy(touched = next.touched + touch) else next } }
    }

    private fun pickDate(event: CreateFormEvent.EventDatePicked) {
        val zone = TimeZone.getDefault()
        updateEvent(pickedField(event.start)) { e ->
            val existing = if (event.start) e.startMillis else e.endMillis
            val picked = when {
                e.allDay -> event.utcDateMillis
                existing != null -> EventTimes.combine(event.utcDateMillis, EventTimes.timeOf(existing, zone), zone)
                else -> EventTimes.combine(event.utcDateMillis, TimeOfDay(DEFAULT_HOUR, 0), zone)
            }
            applyPicked(e, event.start, picked)
        }
    }

    private fun pickTime(event: CreateFormEvent.EventTimePicked) {
        val zone = TimeZone.getDefault()
        updateEvent(pickedField(event.start)) { e ->
            val existing = (if (event.start) e.startMillis else e.endMillis) ?: System.currentTimeMillis()
            val picked = EventTimes.combine(EventTimes.dateOf(existing, zone), TimeOfDay(event.hour, event.minute), zone)
            applyPicked(e, event.start, picked)
        }
    }

    private fun pickedField(start: Boolean): FormField = if (start) FormField.START else FormField.END

    private fun applyPicked(event: CreateSpec.Event, start: Boolean, picked: Long): CreateSpec.Event =
        if (start) EventTimes.withStart(event, picked) else event.copy(endMillis = picked)

    /** Recomputes everything derived from [spec]: errors, the Text density, the barcode status and preview. */
    private fun derive(base: CreateFormUiState, spec: CreateSpec): CreateFormUiState {
        val errors = Validators.validate(spec)
        val barcode = spec as? CreateSpec.Barcode
        val status = if (barcode != null) BarcodeInputs.status(barcode.format, barcode.data) else BarcodeStatus.Empty
        val preview = if (barcode != null && errors.isEmpty()) {
            CodeImageFactory(barcode.format, PayloadBuilder.build(barcode)).imageOrNull(QrStyle())
        } else {
            null
        }
        val density = if (spec is CreateSpec.Text && spec.text.isNotBlank()) PayloadDensity.of(spec.text) else null
        return base.copy(spec = spec, errors = errors, density = density, barcodeStatus = status, barcodePreview = preview)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun generate() {
        val current = _state.value
        val spec = current.spec ?: return
        if (current.generating) return
        if (!Validators.isValid(spec)) {
            _state.update { it.copy(showAllErrors = true) }
            return
        }
        _state.update { it.copy(generating = true) }
        viewModelScope.launch {
            try {
                val payload = PayloadBuilder.build(spec)
                val content = parser.parse(payload, PayloadBuilder.formatOf(spec))
                val record = saveScan(content, ScanSource.CREATED, QrStyle())
                analytics.log(Events.CREATE_SUCCESS, mapOf(Params.TYPE to spec.type.name))
                _effects.send(CreateFormEffect.OpenGenerated(record.id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Creating the code failed")
                _effects.send(CreateFormEffect.GenerateFailed)
            } finally {
                _state.update { it.copy(generating = false) }
            }
        }
    }

    companion object {
        /** Name of the navigation argument of `CreateForm(typeKey)`. */
        const val KEY_TYPE = "typeKey"
        private const val DEFAULT_HOUR = 9
    }
}

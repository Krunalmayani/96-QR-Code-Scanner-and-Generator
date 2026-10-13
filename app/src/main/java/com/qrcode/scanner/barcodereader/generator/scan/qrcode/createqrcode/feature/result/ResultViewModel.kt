package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Analytics
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Events
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.analytics.Params
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.copyText
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser.shareText
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.product.ProductSearchUrls
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.AppSettings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.HistoryRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.SettingsRepository
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalActions
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.LinkRiskReasons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectEvent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectMachine
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectOutcome
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectPlanner
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectRequest
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.DeleteRecordsUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.usecase.ToggleFavoriteUseCase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform.NetworkStatus
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform.WifiConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Result screen for a fresh scan and the history detail ([ResultViewModel.ARG_FROM_HISTORY]). The record is observed by
 * id, so transient (negative id) records work too. Every external action is an effect the UI performs after a tap;
 * nothing is launched automatically except the opt-in "auto open links" setting for a SAFE link of a new scan.
 *
 * The primary constructor is for tests (it takes the note debounce); Hilt uses the secondary one.
 */
@HiltViewModel
class ResultViewModel internal constructor(
    private val savedState: SavedStateHandle,
    private val history: HistoryRepository,
    private val settings: SettingsRepository,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val deleteRecords: DeleteRecordsUseCase,
    private val wifiConnector: WifiConnector,
    private val networkStatus: NetworkStatus,
    private val analytics: Analytics,
    private val noteDebounceMs: Long,
) : ViewModel() {
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        history: HistoryRepository,
        settings: SettingsRepository,
        toggleFavorite: ToggleFavoriteUseCase,
        deleteRecords: DeleteRecordsUseCase,
        wifiConnector: WifiConnector,
        networkStatus: NetworkStatus,
        analytics: Analytics,
    ) : this(
        savedStateHandle,
        history,
        settings,
        toggleFavorite,
        deleteRecords,
        wifiConnector,
        networkStatus,
        analytics,
        NOTE_DEBOUNCE_MS,
    )

    private val recordId: Long = savedState.get<Long>(ARG_RECORD_ID) ?: 0L
    private val fromHistory: Boolean = savedState.get<Boolean>(ARG_FROM_HISTORY) ?: false
    private val linkChecker = LinkSafetyChecker()

    private val _state = MutableStateFlow(ResultUiState(fromHistory = fromHistory))
    val uiState: StateFlow<ResultUiState> = _state.asStateFlow()

    private val _effects = Channel<ResultEffect>(Channel.BUFFERED)
    val effects: Flow<ResultEffect> = _effects.receiveAsFlow()

    private var productOrder: List<ProductSite> = AppSettings().productSiteOrder
    private var noteInitialised = false
    private var deleting = false
    private var noteJob: Job? = null
    private var wifiJob: Job? = null

    /** True when the Location sheet was opened by Connect, so a permission answer should continue the connection. */
    private var connectAfterPermission = false

    init {
        viewModelScope.launch {
            settings.settings.map { it.productSiteOrder }.distinctUntilChanged().collect {
                productOrder = it
                refreshProductSites()
            }
        }
        viewModelScope.launch { history.observeById(recordId).collect { onRecord(it) } }
    }

    fun onEvent(event: ResultEvent) {
        when (event) {
            ResultEvent.PrimaryAction -> onPrimaryAction()
            ResultEvent.CopyClicked -> onCopy()
            ResultEvent.ShareClicked -> onShare()
            ResultEvent.ShareAsText -> onShareAsText()
            ResultEvent.ShareAsImage -> onShareAsImage()
            ResultEvent.ShareSheetDismissed -> _state.update { it.copy(shareSheetVisible = false) }
            ResultEvent.FavoriteClicked -> onFavorite()
            is ResultEvent.DialClicked -> launchAction(ExternalActions.dial(event.number))
            is ResultEvent.ComposeEmailClicked -> launchAction(ExternalActions.composeEmail(event.address))
            is ResultEvent.MessageClicked -> launchAction(ExternalActions.composeSms(event.number))
            ResultEvent.AddToContactsClicked -> onAddToContacts()
            ResultEvent.DirectionsClicked -> onDirections()
            is ResultEvent.OpenProductSite -> onOpenProductSite(event.site)
            ResultEvent.ProductSearchOpened -> openProductSearch()
            ResultEvent.ProductSearchClosed -> updateProduct { it.copy(searchOpen = false) }
            ResultEvent.ProductRetry -> onProductRetry()
            is ResultEvent.NoteChanged -> onNoteChanged(event.text)
            ResultEvent.NoteCommit -> commitNote()
            ResultEvent.EditStyleClicked -> send(ResultEffect.OpenGenerated(recordId))
            ResultEvent.DeleteClicked -> _state.update { it.copy(deleteDialogVisible = true) }
            ResultEvent.DeleteDismissed -> _state.update { it.copy(deleteDialogVisible = false) }
            ResultEvent.DeleteConfirmed -> onDeleteConfirmed()
            ResultEvent.UnsafeGoBack -> onUnsafeGoBack()
            ResultEvent.UnsafeCopyLink -> onUnsafeCopyLink()
            ResultEvent.UnsafeOpenAnyway -> onUnsafeOpenAnyway()
            ResultEvent.UnsafeDismissed -> _state.update { it.copy(unsafeSheetVisible = false) }
            ResultEvent.WifiConnectClicked, ResultEvent.WifiRetry -> onWifiConnectClicked()
            ResultEvent.WifiCopyPassword -> onWifiCopyPassword()
            ResultEvent.WifiCancel -> onWifiCancel()
            ResultEvent.WifiBack -> onWifiBack()
            ResultEvent.WifiDone -> send(ResultEffect.NavigateToScan)
            ResultEvent.WhyLocationClicked -> onWhyLocation()
            ResultEvent.LocationRationaleContinue -> onLocationRationaleContinue()
            ResultEvent.LocationRationaleDismissed -> _state.update { it.copy(locationRationaleVisible = false) }
            is ResultEvent.LocationPermissionResult -> onLocationPermissionResult(event.granted)
        }
    }

    override fun onCleared() {
        if (_state.value.wifi.phase == WifiPhase.CONNECTING) wifiConnector.cancel()
    }

    // ---- loading ----------------------------------------------------------------------------------------------

    private suspend fun onRecord(record: ScanRecord?) {
        if (record == null) {
            if (!deleting) _state.update { it.copy(phase = ResultPhase.NOT_FOUND, record = null) }
            return
        }
        val firstLoad = _state.value.record == null
        _state.update { current ->
            current.copy(
                phase = ResultPhase.READY,
                record = record,
                note = if (noteInitialised) current.note else record.note,
            )
        }
        noteInitialised = true
        if (firstLoad) onFirstLoad(record)
    }

    private suspend fun onFirstLoad(record: ScanRecord) {
        refreshProductSites()
        val content = record.content as? ScanContent.Website ?: return
        val safety = linkChecker.check(content.url)
        _state.update { it.copy(safety = safety, riskReasons = LinkRiskReasons.of(safety)) }
        if (!fromHistory && safety.isSafe) maybeAutoOpen(content.url)
    }

    /** The one place a link opens without a tap: the user's own "auto open links" setting, SAFE links, once per result. */
    private suspend fun maybeAutoOpen(url: String) {
        if (savedState.get<Boolean>(KEY_AUTO_OPENED) == true) return
        if (!settings.current().autoOpenLinks) return
        savedState[KEY_AUTO_OPENED] = true
        openUrl(url, "auto")
    }

    private fun refreshProductSites() {
        val product = _state.value.record?.content as? ScanContent.Product
        val sites =
            product
                ?.let { ProductSearchUrls.ordered(productOrder, it.number) }
                ?.map { (site, url) -> ProductSiteLink(site, url) }
                .orEmpty()
        updateProduct { it.copy(sites = sites) }
    }

    // ---- primary action ---------------------------------------------------------------------------------------

    private fun onPrimaryAction() {
        val record = currentRecord() ?: return
        when (val content = record.content) {
            is ScanContent.Website -> openWebsite(content)
            is ScanContent.Phone -> launchAction(ExternalActions.dial(content.number))
            is ScanContent.Email -> launchAction(ExternalActions.composeEmail(content.address, content.subject, content.body))
            is ScanContent.Sms -> launchAction(ExternalActions.composeSms(content.number, content.message))
            is ScanContent.Location -> launchAction(ExternalActions.openInMaps(content.latitude, content.longitude, content.label))
            is ScanContent.Event -> launchAction(ExternalActions.addToCalendar(content))
            is ScanContent.Contact -> launchAction(ExternalActions.addToContacts(content))
            is ScanContent.Wifi -> onWifiConnectClicked()
            is ScanContent.Product -> openProductSearch()
            is ScanContent.Text, is ScanContent.Unknown -> Unit
        }
    }

    private fun onAddToContacts() {
        when (val content = currentRecord()?.content) {
            is ScanContent.Phone -> launchAction(ExternalActions.addNumberToContacts(content.number))
            is ScanContent.Email -> launchAction(ExternalActions.addEmailToContacts(content.address))
            is ScanContent.Contact -> launchAction(ExternalActions.addToContacts(content))
            else -> Unit
        }
    }

    private fun onDirections() {
        val event = currentRecord()?.content as? ScanContent.Event ?: return
        launchAction(ExternalActions.searchPlace(event.location))
    }

    private fun launchAction(action: ExternalAction?) {
        send(if (action == null) ResultEffect.ShowMessage(ResultMessage.CANNOT_OPEN) else ResultEffect.Launch(action))
    }

    // ---- website ----------------------------------------------------------------------------------------------

    private fun openWebsite(content: ScanContent.Website) {
        val safety: LinkSafetyResult = _state.value.safety ?: linkChecker.check(content.url)
        if (safety.isSafe) {
            openUrl(content.url, "tap")
        } else {
            _state.update { it.copy(unsafeSheetVisible = true) }
            log(Events.UNSAFE_LINK_SHOWN, safety.level.name)
        }
    }

    private fun openUrl(url: String, how: String) {
        if (!LinkRiskReasons.isWebUrl(url)) {
            send(ResultEffect.ShowMessage(ResultMessage.CANNOT_OPEN))
            return
        }
        log(Events.OPEN_LINK, how)
        send(ResultEffect.OpenUrl(url))
    }

    private fun onUnsafeGoBack() {
        _state.update { it.copy(unsafeSheetVisible = false) }
        send(ResultEffect.NavigateBack)
    }

    private fun onUnsafeCopyLink() {
        _state.update { it.copy(unsafeSheetVisible = false) }
        onCopy()
    }

    private fun onUnsafeOpenAnyway() {
        _state.update { it.copy(unsafeSheetVisible = false) }
        val content = currentRecord()?.content as? ScanContent.Website ?: return
        openUrl(content.url, "unsafe_confirmed")
    }

    // ---- copy, share, favorite --------------------------------------------------------------------------------

    private fun onCopy() {
        val record = currentRecord() ?: return
        log(Events.COPY_TAP)
        send(ResultEffect.CopyText(record.content.copyText()))
    }

    private fun onShare() {
        val record = currentRecord() ?: return
        if (record.source == ScanSource.CREATED) {
            _state.update { it.copy(shareSheetVisible = true) }
        } else {
            shareAsText(record)
        }
    }

    private fun onShareAsText() {
        _state.update { it.copy(shareSheetVisible = false) }
        currentRecord()?.let { shareAsText(it) }
    }

    private fun shareAsText(record: ScanRecord) {
        log(Events.SHARE_TAP, "text")
        send(ResultEffect.ShareText(record.content.shareText()))
    }

    private fun onShareAsImage() {
        _state.update { it.copy(shareSheetVisible = false) }
        log(Events.SHARE_TAP, "image")
        send(ResultEffect.OpenGenerated(recordId))
    }

    private fun onFavorite() {
        viewModelScope.launch {
            val favorite = toggleFavorite(recordId) ?: return@launch
            log(Events.FAVORITE_TOGGLE, if (favorite) "on" else "off")
        }
    }

    // ---- product ----------------------------------------------------------------------------------------------

    private fun openProductSearch() {
        val online = networkStatus.isOnline()
        updateProduct { it.copy(searchOpen = true, online = online) }
    }

    private fun onProductRetry() {
        val online = networkStatus.isOnline()
        updateProduct { it.copy(online = online) }
        if (!online) send(ResultEffect.ShowMessage(ResultMessage.STILL_OFFLINE))
    }

    private fun onOpenProductSite(site: ProductSite) {
        val link =
            _state.value.product.sites
                .firstOrNull { it.site == site } ?: return
        openUrl(link.url, site.name.lowercase())
    }

    private fun updateProduct(change: (ProductUiState) -> ProductUiState) {
        _state.update { it.copy(product = change(it.product)) }
    }

    // ---- note and delete --------------------------------------------------------------------------------------

    private fun onNoteChanged(text: String) {
        val clipped = text.take(MAX_NOTE_LENGTH)
        _state.update { it.copy(note = clipped) }
        noteJob?.cancel()
        noteJob =
            viewModelScope.launch {
                delay(noteDebounceMs)
                history.updateNote(recordId, clipped)
            }
    }

    private fun commitNote() {
        noteJob?.cancel()
        val note = _state.value.note
        if (!noteInitialised || note == _state.value.record?.note) return
        viewModelScope.launch { history.updateNote(recordId, note) }
    }

    private fun onDeleteConfirmed() {
        _state.update { it.copy(deleteDialogVisible = false) }
        deleting = true
        viewModelScope.launch {
            deleteRecords(setOf(recordId))
            _effects.send(ResultEffect.NavigateBack)
        }
    }

    // ---- Wi-Fi ------------------------------------------------------------------------------------------------

    private fun onWifiConnectClicked() {
        val wifi = currentWifi() ?: return
        val plan = WifiConnectPlanner.plan(wifiConnector.sdkInt, wifi.security, wifi.password)
        if (plan.failure == null && plan.needsLocation && !wifiConnector.hasLocationPermission()) {
            connectAfterPermission = true
            _state.update { it.copy(locationRationaleVisible = true) }
            return
        }
        startWifi(wifi)
    }

    private fun startWifi(wifi: ScanContent.Wifi) {
        val request = WifiConnectRequest(wifi.ssid, wifi.password, wifi.security, wifi.hidden)
        val plan = WifiConnectPlanner.plan(wifiConnector.sdkInt, wifi.security, wifi.password)
        wifiJob?.cancel()
        reduceWifi(WifiConnectEvent.Started)
        wifiJob =
            viewModelScope.launch {
                val outcome = plan.failure?.let { WifiConnectOutcome.Failed(it) } ?: connectSafely(request)
                applyWifiOutcome(outcome)
            }
    }

    private suspend fun connectSafely(request: WifiConnectRequest): WifiConnectOutcome =
        try {
            wifiConnector.connect(request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            WifiConnectOutcome.Failed(WifiFailure.ERROR)
        }

    private fun applyWifiOutcome(outcome: WifiConnectOutcome) {
        when (outcome) {
            WifiConnectOutcome.Connected -> {
                reduceWifi(WifiConnectEvent.Succeeded())
                log(Events.WIFI_CONNECT, "success")
            }
            WifiConnectOutcome.Suggested -> {
                reduceWifi(WifiConnectEvent.Succeeded(viaSuggestion = true))
                log(Events.WIFI_CONNECT, "suggested")
            }
            is WifiConnectOutcome.Failed -> {
                reduceWifi(WifiConnectEvent.Failed(outcome.reason))
                log(Events.WIFI_CONNECT, "failed_" + outcome.reason.name.lowercase())
            }
        }
    }

    private fun onWifiCopyPassword() {
        val wifi = currentWifi() ?: return
        log(Events.COPY_TAP, "password")
        send(ResultEffect.CopyText(wifi.password, sensitive = true))
    }

    private fun onWifiCancel() {
        cancelWifiAttempt()
        reduceWifi(WifiConnectEvent.Cancelled)
        log(Events.WIFI_CONNECT, "cancelled")
    }

    private fun onWifiBack() {
        if (_state.value.wifi.phase == WifiPhase.CONNECTING) cancelWifiAttempt()
        reduceWifi(WifiConnectEvent.Dismissed)
    }

    private fun cancelWifiAttempt() {
        wifiJob?.cancel()
        wifiConnector.cancel()
    }

    private fun onWhyLocation() {
        connectAfterPermission = false
        _state.update { it.copy(locationRationaleVisible = true) }
    }

    private fun onLocationRationaleContinue() {
        _state.update { it.copy(locationRationaleVisible = false) }
        send(ResultEffect.RequestLocationPermission)
    }

    private fun onLocationPermissionResult(granted: Boolean) {
        val wifi = currentWifi() ?: return
        if (!connectAfterPermission) return
        connectAfterPermission = false
        if (granted) {
            startWifi(wifi)
        } else {
            reduceWifi(WifiConnectEvent.Started)
            reduceWifi(WifiConnectEvent.Failed(WifiFailure.PERMISSION_DENIED))
            log(Events.WIFI_CONNECT, "failed_permission_denied")
        }
    }

    private fun reduceWifi(event: WifiConnectEvent) {
        _state.update { it.copy(wifi = WifiConnectMachine.reduce(it.wifi, event)) }
    }

    // ---- helpers ----------------------------------------------------------------------------------------------

    private fun currentRecord(): ScanRecord? = _state.value.record

    private fun currentWifi(): ScanContent.Wifi? = currentRecord()?.content as? ScanContent.Wifi

    private fun send(effect: ResultEffect) {
        _effects.trySend(effect)
    }

    /** Analytics: only the content type and a short enum-like result, never the content itself. */
    private fun log(event: String, result: String? = null) {
        val type = currentRecord()?.type?.name ?: return
        val params = if (result == null) mapOf(Params.TYPE to type) else mapOf(Params.TYPE to type, Params.RESULT to result)
        analytics.log(event, params)
    }

    companion object {
        /** Keys of the type-safe route arguments (property names of the Result route class). */
        const val ARG_RECORD_ID = "recordId"
        const val ARG_FROM_HISTORY = "fromHistory"
        private const val KEY_AUTO_OPENED = "autoOpened"
        private const val NOTE_DEBOUNCE_MS = 500L
        private const val MAX_NOTE_LENGTH = 200
    }
}

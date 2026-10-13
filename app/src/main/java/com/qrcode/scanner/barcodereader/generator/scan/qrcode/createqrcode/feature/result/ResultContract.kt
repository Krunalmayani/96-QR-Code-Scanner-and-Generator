package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.RiskReason
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult

enum class ResultPhase { LOADING, READY, NOT_FOUND }

/** One entry of the "Search on" list (s30): the site and the URL opened in a Custom Tab. */
data class ProductSiteLink(
    val site: ProductSite,
    val url: String,
)

/** State of the product search view (s30) and its offline variant (s70). */
data class ProductUiState(
    val searchOpen: Boolean = false,
    val online: Boolean = true,
    val sites: List<ProductSiteLink> = emptyList(),
)

data class ResultUiState(
    val phase: ResultPhase = ResultPhase.LOADING,
    val record: ScanRecord? = null,
    val fromHistory: Boolean = false,
    /** Safety verdict for Website results; null for every other type. */
    val safety: LinkSafetyResult? = null,
    val riskReasons: List<RiskReason> = emptyList(),
    val unsafeSheetVisible: Boolean = false,
    val shareSheetVisible: Boolean = false,
    val deleteDialogVisible: Boolean = false,
    /** Text of the editable note (history detail). Not overwritten by repository updates once edited. */
    val note: String = "",
    val wifi: WifiConnectState = WifiConnectState(),
    val locationRationaleVisible: Boolean = false,
    val product: ProductUiState = ProductUiState(),
)

/** Short messages shown in the snackbar; the UI maps each to a string resource. */
enum class ResultMessage { COPIED, NO_APP_FOUND, CANNOT_OPEN, STILL_OFFLINE }

sealed interface ResultEvent {
    /** The big button of the ActionBar (Open link, Call, Compose, Open in Maps, Add to calendar, ...). */
    data object PrimaryAction : ResultEvent

    data object CopyClicked : ResultEvent

    data object ShareClicked : ResultEvent

    data object ShareAsText : ResultEvent

    data object ShareAsImage : ResultEvent

    data object ShareSheetDismissed : ResultEvent

    data object FavoriteClicked : ResultEvent

    data class DialClicked(
        val number: String,
    ) : ResultEvent

    data class ComposeEmailClicked(
        val address: String,
    ) : ResultEvent

    data class MessageClicked(
        val number: String,
    ) : ResultEvent

    /** "Add contact" for Phone and Email results. */
    data object AddToContactsClicked : ResultEvent

    data object DirectionsClicked : ResultEvent

    data class OpenProductSite(
        val site: ProductSite,
    ) : ResultEvent

    data object ProductSearchOpened : ResultEvent

    data object ProductSearchClosed : ResultEvent

    data object ProductRetry : ResultEvent

    data class NoteChanged(
        val text: String,
    ) : ResultEvent

    data object NoteCommit : ResultEvent

    data object EditStyleClicked : ResultEvent

    data object DeleteClicked : ResultEvent

    data object DeleteConfirmed : ResultEvent

    data object DeleteDismissed : ResultEvent

    data object UnsafeGoBack : ResultEvent

    data object UnsafeCopyLink : ResultEvent

    data object UnsafeOpenAnyway : ResultEvent

    data object UnsafeDismissed : ResultEvent

    data object WifiConnectClicked : ResultEvent

    data object WifiCopyPassword : ResultEvent

    data object WifiCancel : ResultEvent

    data object WifiRetry : ResultEvent

    /** Back from the connecting / connected / failed screens to the normal Wi-Fi result. */
    data object WifiBack : ResultEvent

    data object WifiDone : ResultEvent

    /** The "Why Location?" link: shows the explainer sheet without starting a connection. */
    data object WhyLocationClicked : ResultEvent

    data object LocationRationaleContinue : ResultEvent

    data object LocationRationaleDismissed : ResultEvent

    data class LocationPermissionResult(
        val granted: Boolean,
    ) : ResultEvent
}

/** One-shot instructions for the UI layer. The ViewModel never touches Android APIs. */
sealed interface ResultEffect {
    data class CopyText(
        val text: String,
        val sensitive: Boolean = false,
    ) : ResultEffect

    data class ShareText(
        val text: String,
    ) : ResultEffect

    /** Open [url] (http or https only) in a Custom Tab. */
    data class OpenUrl(
        val url: String,
    ) : ResultEffect

    data class Launch(
        val action: ExternalAction,
    ) : ResultEffect

    data class ShowMessage(
        val message: ResultMessage,
    ) : ResultEffect

    data object RequestLocationPermission : ResultEffect

    data object NavigateBack : ResultEffect

    /** Wi-Fi "Done": return to the Scan tab. */
    data object NavigateToScan : ResultEffect

    /** Open the create feature's Generated screen (style editor and image sharing) for [recordId]. */
    data class OpenGenerated(
        val recordId: Long,
    ) : ResultEffect
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.DestructiveButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.WifiSecurity
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.product.ProductSearchUrls
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.LinkRiskReasons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiConnectState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiFailure
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.WifiPhase
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.Level
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyChecker

// Sample data and previews for artboards s15 to s30, s50, s67, s69, s70 and s74. Previews never use the ViewModel.

private const val HOUR_MS = 3_600_000L
private const val EVENT_START = 1_794_679_200_000L
private val QR = CodeFormat.QR_CODE

private val websiteContent =
    ScanContent.Website(
        "https://www.brewhouse-coffee.com/menu?table=12",
        QR,
        "https://www.brewhouse-coffee.com/menu?table=12",
    )
private val unsafeContent =
    ScanContent.Website(
        "http://secure-paypa1-verify.top/login?id=88213",
        QR,
        "http://secure-paypa1-verify.top/login?id=88213",
    )
private val textContent =
    ScanContent.Text(
        "Meeting notes: ship v2.4 on Friday. Update the release checklist, notify QA, and confirm the store listing screenshots.",
        QR,
    )
private val contactContent =
    ScanContent.Contact(
        rawValue = "BEGIN:VCARD",
        format = QR,
        name = "Sophie Lambert",
        organization = "Lumen Studio",
        phones = listOf("+1 (415) 555-0142"),
        emails = listOf("sophie.lambert@lumen.studio"),
        address = "48 Market Street, Suite 6\nAustin, TX 78701",
    )
private val wifiContent =
    ScanContent.Wifi(
        "WIFI:T:WPA;S:CafeNova_Guest;P:cafe-nova-2026;;",
        QR,
        "CafeNova_Guest",
        "cafe-nova-2026",
        WifiSecurity.WPA,
    )
private val eventContent =
    ScanContent.Event(
        rawValue = "BEGIN:VEVENT",
        format = QR,
        summary = "Product Launch Meetup",
        startMillis = EVENT_START,
        endMillis = EVENT_START + 150 * 60_000L,
        location = "Harbor Hall, 120 Pier Ave, San Francisco",
        description = "Meet the team, see the live demo and pick up early-access invites. Doors open at 5:30 PM.",
    )
private val phoneContent = ScanContent.Phone("tel:+12065550198", QR, "+1 (206) 555-0198")
private val emailContent =
    ScanContent.Email(
        "mailto:hello@lumen.studio",
        QR,
        "hello@lumen.studio",
        "Booking inquiry",
        "Hi, I would like to book a studio visit next week. Are there any slots on Thursday?",
    )
private val smsContent = ScanContent.Sms("smsto:+447700900123", QR, "+44 7700 900123", "Reply YES to confirm your table for 2 at 8:00 PM.")
private val locationContent = ScanContent.Location("geo:37.7749,-122.4194", QR, 37.7749, -122.4194, "San Francisco, CA")
private val productContent = ScanContent.Product("5449000000996", CodeFormat.EAN_13, "5449000000996")
private val unknownContent = ScanContent.Unknown("ID:9F2C-77A1|BATCH:2026-10|\nSEQ:004281|QA:PASS", CodeFormat.DATA_MATRIX)

private fun sampleRecord(content: ScanContent, source: ScanSource = ScanSource.CAMERA, favorite: Boolean = false): ScanRecord =
    ScanRecord(
        id = 1,
        content = content,
        createdAt = System.currentTimeMillis() - HOUR_MS,
        source = source,
        isFavorite = favorite,
    )

private fun sampleState(
    content: ScanContent,
    source: ScanSource = ScanSource.CAMERA,
    fromHistory: Boolean = false,
    favorite: Boolean = false,
    wifi: WifiConnectState = WifiConnectState(),
    product: ProductUiState = ProductUiState(sites = productLinks()),
): ResultUiState {
    val safety = (content as? ScanContent.Website)?.let { LinkSafetyChecker().check(it.url) }
    return ResultUiState(
        phase = ResultPhase.READY,
        record = sampleRecord(content, source, favorite),
        fromHistory = fromHistory,
        safety = safety,
        riskReasons = safety?.let { LinkRiskReasons.of(it) }.orEmpty(),
        wifi = wifi,
        product = product,
    )
}

private fun productLinks(): List<ProductSiteLink> =
    ProductSearchUrls
        .ordered(listOf(ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS, ProductSite.AMAZON), "5449000000996")
        .map { (site, url) -> ProductSiteLink(site, url) }

@Composable
private fun ScreenPreview(dark: Boolean, state: ResultUiState, overlay: (@Composable () -> Unit)? = null) {
    AppTheme(dark = dark) {
        Box(Modifier.fillMaxSize()) {
            ResultScreen(state, remember { SnackbarHostState() }, onEvent = {}, onBack = {})
            overlay?.invoke()
        }
    }
}

@Composable
private fun SheetFrame(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, AppTheme.shapes.sheet)
                    .padding(24.dp),
            content = content,
        )
    }
}

@Composable
private fun DeleteDialogFrame() {
    val c = AppTheme.colors
    Box(Modifier.fillMaxSize().background(c.scrim), contentAlignment = Alignment.Center) {
        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .background(c.surface, AppTheme.shapes.dialog)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(AppIcons.Delete, tone = Tone.Error, size = 48.dp)
            Text(stringResource(R.string.result_delete_title_created), style = AppTheme.type.titleLarge, color = c.ink)
            Text(stringResource(R.string.result_delete_message, "CafeNova_Guest"), style = AppTheme.type.body, color = c.inkSecondary)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                AppTextButton(stringResource(R.string.common_cancel), {})
                DestructiveButton(stringResource(R.string.common_delete), {})
            }
        }
    }
}

private fun unsafeState(): ResultUiState = sampleState(unsafeContent)

@Preview(name = "S15_WebsiteResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S15_WebsiteResult_Light() = ScreenPreview(false, sampleState(websiteContent), null)

@Preview(name = "S15_WebsiteResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S15_WebsiteResult_Dark() = ScreenPreview(true, sampleState(websiteContent), null)

@Preview(name = "S16_TextResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S16_TextResult_Light() = ScreenPreview(false, sampleState(textContent), null)

@Preview(name = "S16_TextResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S16_TextResult_Dark() = ScreenPreview(true, sampleState(textContent), null)

@Preview(name = "S17_ContactResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S17_ContactResult_Light() = ScreenPreview(false, sampleState(contactContent), null)

@Preview(name = "S17_ContactResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S17_ContactResult_Dark() = ScreenPreview(true, sampleState(contactContent), null)

@Preview(name = "S18_WifiResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S18_WifiResult_Light() = ScreenPreview(false, sampleState(wifiContent), null)

@Preview(name = "S18_WifiResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S18_WifiResult_Dark() = ScreenPreview(true, sampleState(wifiContent), null)

@Preview(name = "S19_EventResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S19_EventResult_Light() = ScreenPreview(false, sampleState(eventContent), null)

@Preview(name = "S19_EventResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S19_EventResult_Dark() = ScreenPreview(true, sampleState(eventContent), null)

@Preview(name = "S20_PhoneResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S20_PhoneResult_Light() = ScreenPreview(false, sampleState(phoneContent), null)

@Preview(name = "S20_PhoneResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S20_PhoneResult_Dark() = ScreenPreview(true, sampleState(phoneContent), null)

@Preview(name = "S21_EmailResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S21_EmailResult_Light() = ScreenPreview(false, sampleState(emailContent, favorite = true), null)

@Preview(name = "S21_EmailResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S21_EmailResult_Dark() = ScreenPreview(true, sampleState(emailContent, favorite = true), null)

@Preview(name = "S22_SmsResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S22_SmsResult_Light() = ScreenPreview(false, sampleState(smsContent), null)

@Preview(name = "S22_SmsResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S22_SmsResult_Dark() = ScreenPreview(true, sampleState(smsContent), null)

@Preview(name = "S23_LocationResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S23_LocationResult_Light() = ScreenPreview(false, sampleState(locationContent), null)

@Preview(name = "S23_LocationResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S23_LocationResult_Dark() = ScreenPreview(true, sampleState(locationContent), null)

@Preview(name = "S24_ProductResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S24_ProductResult_Light() = ScreenPreview(false, sampleState(productContent), null)

@Preview(name = "S24_ProductResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S24_ProductResult_Dark() = ScreenPreview(true, sampleState(productContent), null)

@Preview(name = "S25_UnknownResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S25_UnknownResult_Light() = ScreenPreview(false, sampleState(unknownContent), null)

@Preview(name = "S25_UnknownResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S25_UnknownResult_Dark() = ScreenPreview(true, sampleState(unknownContent), null)

@Preview(name = "S26_WifiConnecting_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S26_WifiConnecting_Light() = ScreenPreview(false, sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.CONNECTING)), null)

@Preview(name = "S26_WifiConnecting_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S26_WifiConnecting_Dark() = ScreenPreview(true, sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.CONNECTING)), null)

@Preview(name = "S27_WifiConnected_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S27_WifiConnected_Light() = ScreenPreview(false, sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.CONNECTED)), null)

@Preview(name = "S27_WifiConnected_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S27_WifiConnected_Dark() = ScreenPreview(true, sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.CONNECTED)), null)

@Preview(name = "S28_WifiCouldNotConnect_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S28_WifiCouldNotConnect_Light() =
    ScreenPreview(
        false,
        sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.FAILED, WifiFailure.UNAVAILABLE)),
        null,
    )

@Preview(name = "S28_WifiCouldNotConnect_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S28_WifiCouldNotConnect_Dark() =
    ScreenPreview(
        true,
        sampleState(wifiContent, wifi = WifiConnectState(WifiPhase.FAILED, WifiFailure.UNAVAILABLE)),
        null,
    )

@Preview(name = "S29_ShareSheet_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S29_ShareSheet_Light() =
    ScreenPreview(false, sampleState(wifiContent, ScanSource.CREATED, fromHistory = true), {
        SheetFrame { ShareSheetBody {} }
    })

@Preview(name = "S29_ShareSheet_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S29_ShareSheet_Dark() =
    ScreenPreview(true, sampleState(wifiContent, ScanSource.CREATED, fromHistory = true), {
        SheetFrame { ShareSheetBody {} }
    })

@Preview(name = "S30_ProductSearch_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S30_ProductSearch_Light() =
    ScreenPreview(
        false,
        sampleState(productContent, product = ProductUiState(searchOpen = true, online = true, sites = productLinks())),
        null,
    )

@Preview(name = "S30_ProductSearch_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S30_ProductSearch_Dark() =
    ScreenPreview(
        true,
        sampleState(productContent, product = ProductUiState(searchOpen = true, online = true, sites = productLinks())),
        null,
    )

@Preview(name = "S50_HistoryDetailCreatedWifi_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S50_HistoryDetailCreatedWifi_Light() =
    ScreenPreview(
        false,
        sampleState(wifiContent, ScanSource.CREATED, fromHistory = true, favorite = true),
        null,
    )

@Preview(name = "S50_HistoryDetailCreatedWifi_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S50_HistoryDetailCreatedWifi_Dark() =
    ScreenPreview(
        true,
        sampleState(wifiContent, ScanSource.CREATED, fromHistory = true, favorite = true),
        null,
    )

@Preview(name = "S67_DeleteConfirmation_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S67_DeleteConfirmation_Light() =
    ScreenPreview(false, sampleState(wifiContent, ScanSource.CREATED, fromHistory = true), {
        DeleteDialogFrame()
    })

@Preview(name = "S67_DeleteConfirmation_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S67_DeleteConfirmation_Dark() =
    ScreenPreview(true, sampleState(wifiContent, ScanSource.CREATED, fromHistory = true), {
        DeleteDialogFrame()
    })

@Preview(name = "S69_UnsafeLinkSheet_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S69_UnsafeLinkSheet_Light() =
    ScreenPreview(false, unsafeState(), {
        SheetFrame {
            UnsafeLinkSheetBody(
                unsafeState().safety?.level ?: Level.UNSAFE,
                unsafeState().riskReasons,
                {},
                {},
                {},
            )
        }
    })

@Preview(name = "S69_UnsafeLinkSheet_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S69_UnsafeLinkSheet_Dark() =
    ScreenPreview(true, unsafeState(), {
        SheetFrame {
            UnsafeLinkSheetBody(
                unsafeState().safety?.level ?: Level.UNSAFE,
                unsafeState().riskReasons,
                {},
                {},
                {},
            )
        }
    })

@Preview(name = "S70_ProductOffline_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S70_ProductOffline_Light() =
    ScreenPreview(
        false,
        sampleState(productContent, product = ProductUiState(searchOpen = true, online = false, sites = productLinks())),
        null,
    )

@Preview(name = "S70_ProductOffline_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S70_ProductOffline_Dark() =
    ScreenPreview(
        true,
        sampleState(productContent, product = ProductUiState(searchOpen = true, online = false, sites = productLinks())),
        null,
    )

@Preview(name = "S74_WebsiteResult_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S74_WebsiteResult_Light() = ScreenPreview(false, sampleState(websiteContent), null)

@Preview(name = "S74_WebsiteResult_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S74_WebsiteResult_Dark() = ScreenPreview(true, sampleState(websiteContent), null)

@Preview(name = "S15_ResultLoading_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S15_ResultLoading_Light() = ScreenPreview(false, ResultUiState(), null)

@Preview(name = "S15_ResultLoading_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S15_ResultLoading_Dark() = ScreenPreview(true, ResultUiState(), null)

@Preview(name = "S50_ResultNotFound_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S50_ResultNotFound_Light() = ScreenPreview(false, ResultUiState(phase = ResultPhase.NOT_FOUND, fromHistory = true), null)

@Preview(name = "S50_ResultNotFound_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S50_ResultNotFound_Dark() = ScreenPreview(true, ResultUiState(phase = ResultPhase.NOT_FOUND, fromHistory = true), null)

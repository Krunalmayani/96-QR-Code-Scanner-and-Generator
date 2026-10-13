package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBarHeight
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan.ScanMode

private const val PreviewWidth = 360
private const val PreviewHeight = 800

private val websiteRecord = ScanRecord(
    id = 1,
    content = ScanContent.Website(
        rawValue = "https://www.brewhouse-coffee.com/menu?table=12",
        format = CodeFormat.QR_CODE,
        url = "https://www.brewhouse-coffee.com/menu?table=12",
    ),
    createdAt = 0L,
    source = ScanSource.CAMERA,
)

private val batchRows = listOf(
    BatchRow(3, "5901234123457", ContentType.PRODUCT, CodeFormat.EAN_13),
    BatchRow(2, "lumen.studio/portfolio", ContentType.WEBSITE, CodeFormat.QR_CODE),
    BatchRow(1, "Ship v2.4 on Friday, update the checklist", ContentType.TEXT, CodeFormat.QR_CODE),
)

@Composable
private fun FakeCamera() {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A2F4A), Color(0xFF14172B)))))
}

@Composable
private fun ScanPreviewHost(
    dark: Boolean,
    state: ScanUiState,
    panel: (@Composable () -> Unit)? = null,
) {
    AppTheme(dark = dark) {
        CompositionLocalProvider(LocalBottomBarInset provides BottomNavBarHeight) {
            Box(Modifier.fillMaxSize()) {
                ScanScreen(state, CameraCapabilities(hasFlash = true, maxZoomRatio = 5f), {}, camera = { FakeCamera() })
                if (panel == null) BottomNavBar(BottomTab.Scan, {}, Modifier.align(Alignment.BottomCenter))
                if (panel != null) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(AppTheme.shapes.sheet)
                            .background(AppTheme.colors.surface)
                            .padding(24.dp),
                    ) { panel() }
                }
            }
        }
    }
}

@Composable
private fun PermissionPreviewHost(dark: Boolean, block: CameraBlock) {
    AppTheme(dark = dark) {
        CompositionLocalProvider(LocalBottomBarInset provides BottomNavBarHeight) {
            Box(Modifier.fillMaxSize()) {
                CameraBlockedScreen(block, {}, {}, {})
                BottomNavBar(BottomTab.Scan, {}, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

// S06 / S73: live camera, QR mode
@Preview(name = "S06_LiveCameraQr_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S06_LiveCameraQr_Light() = ScanPreviewHost(false, ScanUiState())

@Preview(name = "S06_LiveCameraQr_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S06_LiveCameraQr_Dark() = ScanPreviewHost(true, ScanUiState())

@Preview(name = "S73_ScanDark_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S73_ScanDark_Dark() = ScanPreviewHost(true, ScanUiState(mode = ScanMode.ALL))

// S07: barcode mode, flash on
@Preview(name = "S07_BarcodeFlashOn_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S07_BarcodeFlashOn_Light() = ScanPreviewHost(false, ScanUiState(mode = ScanMode.BARCODE, flashOn = true, zoom = 0.46f))

@Preview(name = "S07_BarcodeFlashOn_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S07_BarcodeFlashOn_Dark() = ScanPreviewHost(true, ScanUiState(mode = ScanMode.BARCODE, flashOn = true, zoom = 0.46f))

// S08: quick result sheet (the real sheet is a window, previews draw its body in a panel)
@Composable
private fun QuickPanel() {
    QuickResultContent(websiteRecord, onOpen = {}, onKeepScanning = {}, onToggleFavorite = {}, onCopy = {}, onShare = {})
}

@Preview(name = "S08_QuickResult_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S08_QuickResult_Light() = ScanPreviewHost(false, ScanUiState(detected = true), panel = { QuickPanel() })

@Preview(name = "S08_QuickResult_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S08_QuickResult_Dark() = ScanPreviewHost(true, ScanUiState(detected = true), panel = { QuickPanel() })

// S09: camera permission denied
@Preview(name = "S09_CameraDenied_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S09_CameraDenied_Light() = PermissionPreviewHost(false, CameraBlock.PermissionPermanentlyDenied)

@Preview(name = "S09_CameraDenied_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S09_CameraDenied_Dark() = PermissionPreviewHost(true, CameraBlock.PermissionPermanentlyDenied)

@Preview(name = "S09_CameraRequestAgain_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S09_CameraRequestAgain_Light() = PermissionPreviewHost(false, CameraBlock.PermissionDenied)

@Preview(name = "S09_CameraRequestAgain_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S09_CameraRequestAgain_Dark() = PermissionPreviewHost(true, CameraBlock.PermissionDenied)

// S10: batch scan (the list sheet body is drawn in a panel)
@Composable
private fun BatchPanel() {
    BatchSheetContent(batchRows, onRowClick = {}, onKeepScanning = {}, onFinish = {})
}

@Preview(name = "S10_BatchScan_Light", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S10_BatchScan_Light() = ScanPreviewHost(false, ScanUiState(batchMode = true, batch = batchRows), panel = { BatchPanel() })

@Preview(name = "S10_BatchScan_Dark", widthDp = PreviewWidth, heightDp = PreviewHeight)
@Composable
private fun S10_BatchScan_Dark() = ScanPreviewHost(true, ScanUiState(batchMode = true, batch = batchRows), panel = { BatchPanel() })

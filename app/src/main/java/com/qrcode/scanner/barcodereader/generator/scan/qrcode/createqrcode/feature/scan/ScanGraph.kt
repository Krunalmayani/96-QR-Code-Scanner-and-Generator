package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Result
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Scan
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.ScanFromImage
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.navigateToTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.image.ScanFromImageRoute

/** Registers the Scan tab (live camera) and the scan-from-image flow. Called by AppNav. */
fun NavGraphBuilder.scanGraph(navController: NavHostController) {
    composable<Scan> {
        ScanRoute(
            onOpenResult = { recordId -> navController.navigate(Result(recordId = recordId)) },
            onOpenGallery = { navController.navigate(ScanFromImage) },
            onOpenSettings = { navController.navigateToTab(BottomTab.Settings) },
        )
    }
    composable<ScanFromImage> {
        ScanFromImageRoute(
            onBack = { navController.popBackStack() },
            onOpenResult = { recordId, replaceScreen ->
                navController.navigate(Result(recordId = recordId)) {
                    if (replaceScreen) popUpTo<ScanFromImage> { inclusive = true }
                }
            },
            onUseCamera = {
                if (!navController.popBackStack<Scan>(inclusive = false)) navController.navigate(Scan)
            },
        )
    }
}

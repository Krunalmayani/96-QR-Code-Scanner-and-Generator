package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.ExportHistory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.History
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Result
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.navigateToTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab

/** History tab root. A row opens the Result screen in "from history" mode. */
fun NavGraphBuilder.historyGraph(navController: NavHostController) {
    composable<History> {
        HistoryRoute(
            onOpenRecord = { id -> navController.navigate(Result(recordId = id, fromHistory = true)) },
            onStartScanning = { navController.navigateToTab(BottomTab.Scan) },
            onCreateCode = { navController.navigateToTab(BottomTab.Create) },
            onOpenExportHistory = { navController.navigate(ExportHistory) },
        )
    }
}

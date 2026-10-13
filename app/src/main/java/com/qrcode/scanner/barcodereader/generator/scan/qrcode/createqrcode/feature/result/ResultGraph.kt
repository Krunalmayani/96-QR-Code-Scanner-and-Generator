package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Generated
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Result
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Scan

/**
 * Registers the result destination. [Result] serves both a fresh scan (fromHistory = false) and a history item
 * (fromHistory = true, including created codes); recordId may be negative for a transient record.
 */
fun NavGraphBuilder.resultGraph(navController: NavHostController) {
    composable<Result> {
        ResultRoute(
            onBack = { navController.popBackStack() },
            onDone = { navController.popBackStack(Scan, false) },
            onOpenGenerated = { recordId -> navController.navigate(Generated(recordId)) },
        )
    }
}

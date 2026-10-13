package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.e2e

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.CreateHub
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.History
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Scan
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Settings
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.navigateToTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBarHeight
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.MaxWidthContainer
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.createGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history.historyGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.resultGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.settingsGraph

/** Content description of the stand-in for the live camera screen. */
const val SCAN_PLACEHOLDER = "scan-placeholder"

/**
 * The shell of `AppNav` (same graphs, same bottom bar, same tab navigation) with two differences that make flows
 * automatable: the live camera destination is an empty placeholder, and the caller owns the [navController], so a
 * test can play the part of the scanner (save a record, then navigate to `Result(recordId)`).
 * The real `AppNav` is covered separately by `AppNavSmokeTest`.
 */
@Composable
fun E2eAppHost(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val tab = entry?.destination?.toTab()
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (tab != null) BottomNavBarHeight + navInset else navInset

    AppTheme(dark = false) {
        MaxWidthContainer {
            Box(Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalBottomBarInset provides bottomInset) {
                    NavHost(navController = navController, startDestination = Scan, modifier = Modifier.fillMaxSize()) {
                        composable<Scan> { Box(Modifier.fillMaxSize().semantics { contentDescription = SCAN_PLACEHOLDER }) }
                        resultGraph(navController)
                        createGraph(navController)
                        historyGraph(navController)
                        settingsGraph(navController)
                    }
                }
                if (tab != null) {
                    BottomNavBar(
                        selected = tab,
                        onSelect = { navController.navigateToTab(it) },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private fun NavDestination.toTab(): BottomTab? =
    when {
        hasRoute<Scan>() -> BottomTab.Scan
        hasRoute<CreateHub>() -> BottomTab.Create
        hasRoute<History>() -> BottomTab.History
        hasRoute<Settings>() -> BottomTab.Settings
        else -> null
    }

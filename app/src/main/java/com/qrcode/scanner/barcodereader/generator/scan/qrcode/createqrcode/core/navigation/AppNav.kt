package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomNavBarHeight
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.BottomTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.MaxWidthContainer
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.gallery.ComponentGallery
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.createGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history.historyGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding.onboardingGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.resultGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.scan.scanGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings.settingsGraph
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system.SystemPromptsHost

private const val FADE_MS = 200

/**
 * Root navigation host. Bottom bar is shown on the four tab roots only; screens read
 * [LocalBottomBarInset] to keep their content clear of it.
 */
@Composable
fun AppNav(onboardingDone: Boolean) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val tab = entry?.destination?.toTab()
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (tab != null) BottomNavBarHeight + navInset else navInset
    val start: Any = remember { if (onboardingDone) Scan else Onboarding }

    MaxWidthContainer {
        Box(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalBottomBarInset provides bottomInset) {
                NavHost(
                    navController = navController,
                    startDestination = start,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn(tween(FADE_MS)) },
                    exitTransition = { fadeOut(tween(FADE_MS)) },
                    popEnterTransition = { fadeIn(tween(FADE_MS)) },
                    popExitTransition = { fadeOut(tween(FADE_MS)) },
                ) {
                    onboardingGraph(navController)
                    scanGraph(navController)
                    resultGraph(navController)
                    createGraph(navController)
                    historyGraph(navController)
                    settingsGraph(navController)
                    if (BuildConfig.DEBUG) {
                        composable<ComponentGalleryRoute> { ComponentGallery(onBack = { navController.popBackStack() }) }
                    }
                }
            }
            if (tab != null) {
                BottomNavBar(
                    selected = tab,
                    onSelect = { navController.navigateToTab(it) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            SystemPromptsHost(navController = navController, scanningInProgress = tab == BottomTab.Scan)
        }
    }
}

private fun NavDestination.toTab(): BottomTab? = when {
    hasRoute<Scan>() -> BottomTab.Scan
    hasRoute<CreateHub>() -> BottomTab.Create
    hasRoute<History>() -> BottomTab.History
    hasRoute<Settings>() -> BottomTab.Settings
    else -> null
}

/** Switches tab keeping each tab's back stack and state; Scan is the root. */
fun NavHostController.navigateToTab(tab: BottomTab) {
    val route: Any = when (tab) {
        BottomTab.Create -> CreateHub
        BottomTab.Scan -> Scan
        BottomTab.History -> History
        BottomTab.Settings -> Settings
    }
    navigate(route) {
        popUpTo<Scan> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

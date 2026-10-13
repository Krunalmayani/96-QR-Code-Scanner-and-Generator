package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.CameraRationaleOnboarding
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Onboarding
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Scan

/**
 * Onboarding destinations: the three slides, then the camera rationale. Finishing clears both from the back
 * stack and opens Scan; "Skip" on the slides jumps straight to the rationale step.
 */
fun NavGraphBuilder.onboardingGraph(navController: NavHostController) {
    composable<Onboarding> {
        OnboardingRoute(onFinished = { navController.navigate(CameraRationaleOnboarding) })
    }
    composable<CameraRationaleOnboarding> {
        CameraRationaleRoute(onFinished = { navController.openScanAfterOnboarding() })
    }
}

private fun NavHostController.openScanAfterOnboarding() {
    navigate(Scan) {
        popUpTo<Onboarding> { inclusive = true }
        launchSingleTop = true
    }
}

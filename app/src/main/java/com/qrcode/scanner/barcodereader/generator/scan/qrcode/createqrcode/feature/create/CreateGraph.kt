package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.CreateForm
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.CreateHub
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation.Generated
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.form.CreateFormRoute
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.generated.GeneratedRoute
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub.CreateHubRoute

/**
 * Create feature destinations: the hub (a bottom-navigation root), one form per create type (`CreateForm(typeKey)`, the key is
 * [CreateType.name]) and the Generated screen with its customize editor (`Generated(recordId)`).
 */
fun NavGraphBuilder.createGraph(navController: NavHostController) {
    composable<CreateHub> {
        CreateHubRoute(onOpenType = { type -> navController.navigate(CreateForm(type.name)) })
    }
    composable<CreateForm> {
        CreateFormRoute(
            onBack = { navController.popBackStack() },
            onGenerated = { recordId -> navController.navigate(Generated(recordId)) },
            onSwitchToQr = {
                navController.navigate(CreateForm(CreateType.TEXT.name)) {
                    popUpTo<CreateForm> { inclusive = true }
                }
            },
        )
    }
    composable<Generated> {
        GeneratedRoute(onBack = { navController.popBackStack() })
    }
}

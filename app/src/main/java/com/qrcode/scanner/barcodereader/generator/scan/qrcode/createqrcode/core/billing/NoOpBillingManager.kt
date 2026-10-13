package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.billing

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Default binding: nobody is ad-free and the purchase does nothing. */
@Singleton
class NoOpBillingManager @Inject constructor() : BillingManager {
    override val isAdFree: Flow<Boolean> = flowOf(false)

    override val isPurchaseAvailable: Boolean = false

    override fun purchaseRemoveAds() = Unit
}

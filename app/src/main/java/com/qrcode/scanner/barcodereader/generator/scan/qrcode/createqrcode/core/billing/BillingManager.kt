package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.billing

import kotlinx.coroutines.flow.Flow

/**
 * Seam for the optional "Remove ads" one-time purchase (PRD 9, off by default). No Play Billing dependency
 * yet: [NoOpBillingManager] is bound. A future implementation reports [isAdFree] and the ad manager stops
 * showing ads as soon as it emits true.
 */
interface BillingManager {
    /** True once the user owns "Remove ads". */
    val isAdFree: Flow<Boolean>

    /** False while no real implementation is bound; UI hides the purchase entry point when false. */
    val isPurchaseAvailable: Boolean

    /** Starts the purchase flow. A real implementation tracks the foreground activity itself. */
    fun purchaseRemoveAds()
}

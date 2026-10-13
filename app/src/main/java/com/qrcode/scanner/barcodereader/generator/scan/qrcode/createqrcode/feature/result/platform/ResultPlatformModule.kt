package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hilt bindings for the Android-specific helpers of the result feature. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ResultPlatformModule {
    @Binds
    abstract fun bindWifiConnector(impl: AndroidWifiConnector): WifiConnector

    @Binds
    abstract fun bindNetworkStatus(impl: AndroidNetworkStatus): NetworkStatus
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads

import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {
    @Binds
    abstract fun bindAdManager(impl: GoogleAdManager): AdManager

    @Binds
    abstract fun bindAdContentSource(impl: GoogleAdManager): AdContentSource
}

/** Lets the ad composables reach the singleton without a ViewModel. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdsEntryPoint {
    fun adContentSource(): AdContentSource
}

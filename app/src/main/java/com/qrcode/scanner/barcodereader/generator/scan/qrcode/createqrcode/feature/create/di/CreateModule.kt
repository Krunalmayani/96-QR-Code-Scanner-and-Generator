package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.di

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.AndroidCodeBitmapRenderer
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.AndroidCodeExporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.AndroidLogoStore
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.CodeBitmapRenderer
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.CodeExporter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.export.LogoStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the Android implementations of the Create feature's rendering and export contracts. */
@Module
@InstallIn(SingletonComponent::class)
abstract class CreateModule {
    @Binds
    abstract fun bindCodeBitmapRenderer(impl: AndroidCodeBitmapRenderer): CodeBitmapRenderer

    @Binds
    abstract fun bindCodeExporter(impl: AndroidCodeExporter): CodeExporter

    @Binds
    abstract fun bindLogoStore(impl: AndroidLogoStore): LogoStore
}

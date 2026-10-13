package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.di

import javax.inject.Qualifier

/** Dispatcher for blocking or mapping work off the main thread. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Process-wide scope (SupervisorJob + Dispatchers.Default) for work that must outlive a screen. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

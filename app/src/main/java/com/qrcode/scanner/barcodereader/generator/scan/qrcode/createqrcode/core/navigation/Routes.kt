package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations (Navigation Compose 2.8). Every feature builds its screens
 * for the routes below and navigates to other features only through these objects.
 */

// Onboarding
@Serializable data object Onboarding

@Serializable data object CameraRationaleOnboarding

// Bottom-navigation roots
@Serializable data object Scan

@Serializable data object CreateHub

@Serializable data object History

@Serializable data object Settings

// Scan
@Serializable data object ScanFromImage

// Result / detail. [recordId] may be negative (transient record, history switched off).
@Serializable data class Result(
    val recordId: Long,
    val fromHistory: Boolean = false,
)

// Create. [typeKey] is CreateType.name from the create feature.
@Serializable data class CreateForm(
    val typeKey: String,
)

/** Generated code + customise editor for the created record [recordId]. */
@Serializable data class Generated(
    val recordId: Long,
)

// Settings sub-screens
@Serializable data object ThemePicker

@Serializable data object LanguagePicker

@Serializable data object Help

@Serializable data object PrivacyPolicy

@Serializable data object Feedback

@Serializable data object About

@Serializable data object ExportHistory

/** Debug-only catalogue of design-system components (registered only when BuildConfig.DEBUG). */
@Serializable data object ComponentGalleryRoute

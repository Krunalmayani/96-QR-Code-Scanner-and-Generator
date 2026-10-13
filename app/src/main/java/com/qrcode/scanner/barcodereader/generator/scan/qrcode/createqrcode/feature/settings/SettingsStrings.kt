package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FaqTopic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackTopic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.Languages

/** Maps domain enums to string resources so the pure domain code stays free of Android. */

@Composable
internal fun quantityString(
    @PluralsRes id: Int,
    count: Int,
): String =
    LocalContext.current.resources.getQuantityString(id, count, count)

internal fun themeLabelRes(mode: ThemeMode): Int =
    when (mode) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

/** Display name of the stored language tag: its own native name, or "System default" for an empty/unknown tag. */
@Composable
internal fun languageLabel(tag: String): String =
    Languages.find(tag)?.nativeName ?: stringResource(R.string.settings_language_system)

internal fun siteLabelRes(site: ProductSite): Int =
    when (site) {
        ProductSite.GOOGLE -> R.string.settings_site_google
        ProductSite.OPEN_FOOD_FACTS -> R.string.settings_site_open_food_facts
        ProductSite.AMAZON -> R.string.settings_site_amazon
    }

internal fun topicLabelRes(topic: FeedbackTopic): Int =
    when (topic) {
        FeedbackTopic.SCANNING -> R.string.settings_feedback_topic_scanning
        FeedbackTopic.CREATING -> R.string.settings_feedback_topic_creating
        FeedbackTopic.DESIGN -> R.string.settings_feedback_topic_design
        FeedbackTopic.OTHER -> R.string.settings_feedback_topic_other
    }

internal fun faqQuestionRes(topic: FaqTopic): Int =
    when (topic) {
        FaqTopic.WONT_SCAN -> R.string.settings_faq_wont_scan_q
        FaqTopic.OFFLINE -> R.string.settings_faq_offline_q
        FaqTopic.COLORS -> R.string.settings_faq_colors_q
        FaqTopic.HISTORY_STORAGE -> R.string.settings_faq_history_q
        FaqTopic.FROM_PHOTO -> R.string.settings_faq_photo_q
        FaqTopic.AUTO_OPEN -> R.string.settings_faq_auto_open_q
        FaqTopic.WIFI -> R.string.settings_faq_wifi_q
        FaqTopic.CODE_TYPES -> R.string.settings_faq_types_q
        FaqTopic.EXPORT -> R.string.settings_faq_export_q
        FaqTopic.LANGUAGE -> R.string.settings_faq_language_q
    }

internal fun faqAnswerRes(topic: FaqTopic): Int =
    when (topic) {
        FaqTopic.WONT_SCAN -> R.string.settings_faq_wont_scan_a
        FaqTopic.OFFLINE -> R.string.settings_faq_offline_a
        FaqTopic.COLORS -> R.string.settings_faq_colors_a
        FaqTopic.HISTORY_STORAGE -> R.string.settings_faq_history_a
        FaqTopic.FROM_PHOTO -> R.string.settings_faq_photo_a
        FaqTopic.AUTO_OPEN -> R.string.settings_faq_auto_open_a
        FaqTopic.WIFI -> R.string.settings_faq_wifi_a
        FaqTopic.CODE_TYPES -> R.string.settings_faq_types_a
        FaqTopic.EXPORT -> R.string.settings_faq_export_a
        FaqTopic.LANGUAGE -> R.string.settings_faq_language_a
    }

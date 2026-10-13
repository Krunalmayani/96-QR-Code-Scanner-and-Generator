package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import org.junit.jupiter.api.Assertions.fail

/**
 * Known violations of architecture rules that exist in the code today and are tracked, not fixed, by the hardening
 * work. A rule fails on any violation that is NOT listed here, so new code cannot add to the debt. Fixing a listed
 * violation is always allowed (stale entries are only printed); delete the entry in the same change.
 */
internal object ArchitectureBaseline {
    /** Rule id to the violations (as printed in the failure message) that are tolerated for now. */
    private val known: Map<String, Set<String>> =
        mapOf(
            // result renders created codes with create's GeneratedCodePreview instead of a shared component.
            Rules.FEATURE_ISOLATION to
                setOf(
                    "feature/result/ProductViews.kt -> feature.create",
                    "feature/result/HistoryExtras.kt -> feature.create",
                ),
        )

    /** Fails with the violations of [rule] that are not in the baseline; prints baseline entries that no longer occur. */
    fun assertNoNewViolations(
        rule: String,
        description: String,
        violations: Collection<String>,
    ) {
        val tolerated = known[rule].orEmpty()
        val fresh = violations.filterNot { it in tolerated }.sorted()
        (tolerated - violations.toSet()).forEach { println("[architecture] baseline entry for '$rule' no longer occurs, remove it: $it") }
        if (fresh.isNotEmpty()) {
            fail<Unit>("$description (${fresh.size}):\n" + fresh.joinToString("\n") { " - $it" })
        }
    }
}

internal object Rules {
    const val DOMAIN_PURITY = "domain-purity"
    const val LAYERING = "layering"
    const val FEATURE_ISOLATION = "feature-isolation"
    const val NO_TODO = "no-todo"
    const val NO_WILDCARD_IMPORTS = "no-wildcard-imports"
    const val HILT_VIEWMODEL = "hilt-viewmodel"
    const val HARD_CODED_TEXT = "hard-coded-text"
    const val STRING_REFERENCES = "string-references"
    const val TRANSLATIONS = "translations"
    const val MANIFEST = "manifest"
}

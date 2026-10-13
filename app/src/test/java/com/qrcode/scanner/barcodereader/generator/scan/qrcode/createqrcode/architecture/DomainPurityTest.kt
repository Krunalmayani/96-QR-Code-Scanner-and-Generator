package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture.SourceTree.PACKAGE
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * The domain package is pure Kotlin so parsers, validators, safety checks and use cases run as plain JVM unit tests
 * (CLAUDE.md "Layers", DECISIONS.md 3).
 *
 * The real rule, derived from what the code imports today: java, kotlin and kotlinx (coroutines Flow, serialization
 * annotations), ZXing core (pure Java; PRD section 2 names it as the encoder for the generator) and other `domain`
 * packages. Nothing from Android, AndroidX/Compose, ML Kit, Dagger/Hilt/javax.inject, nor from the layers above
 * (`feature`, `core`, `data`, `designsystem`).
 */
class DomainPurityTest {
    private val domainFiles get() = SourceTree.kotlinFiles(SourceTree.mainPackage("domain"))

    private val forbiddenPrefixes =
        listOf("android.", "androidx.", "com.google.mlkit", "com.google.android.", "com.google.firebase", "dagger.", "javax.inject")

    private val allowedPrefixes =
        listOf("java.", "kotlin.", "kotlinx.", "com.google.zxing.", "$PACKAGE.domain.")

    @Test
    fun domainSourcesWereFound() {
        assertFalse(domainFiles.isEmpty(), "no Kotlin files under ${SourceTree.mainPackage("domain")}")
    }

    @Test
    fun domainImportsNothingFromAndroidMlKitOrDependencyInjection() {
        val violations =
            domainFiles.flatMap { file ->
                file.imports
                    .filter { import -> forbiddenPrefixes.any { import.name.startsWith(it) } }
                    .map { "${file.relativePath}:${it.line} imports ${it.name}" }
            }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.DOMAIN_PURITY,
            "domain must stay free of Android, ML Kit and DI imports",
            violations,
        )
    }

    @Test
    fun domainImportsOnlyAllowedPackages() {
        val violations =
            domainFiles.flatMap { file ->
                file.imports
                    .filter { import -> allowedPrefixes.none { import.name.startsWith(it) } }
                    .map { "${file.relativePath}:${it.line} imports ${it.name}" }
            }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.DOMAIN_PURITY,
            "domain may import only java, kotlin, kotlinx, ZXing and domain",
            violations,
        )
    }

    @Test
    fun domainDoesNotUseForbiddenPackagesWithoutAnImport() {
        val pattern = Regex("""(?<![\w.])(android|androidx|dagger|com\.google\.mlkit)\.[a-z]""")
        val violations =
            domainFiles.flatMap { file ->
                pattern.findAll(file.code).map { "${file.location(it.range.first)} uses ${it.value}... by its full name" }.toList()
            }
        ArchitectureBaseline.assertNoNewViolations(Rules.DOMAIN_PURITY, "domain must not reference Android types by full name", violations)
    }

    @Test
    fun domainHasNoInjectionAnnotations() {
        val pattern = Regex("""@(Inject|Singleton|Module|Provides|Binds|HiltViewModel|AndroidEntryPoint|Composable|Preview)\b""")
        val violations =
            domainFiles.flatMap { file ->
                pattern.findAll(file.code).map { "${file.location(it.range.first)} uses ${it.value}" }.toList()
            }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.DOMAIN_PURITY,
            "domain classes are plain Kotlin (use cases are provided by UseCaseModule)",
            violations,
        )
    }

    @Test
    fun layersDoNotDependUpwards() {
        val layers = mapOf("data" to listOf("feature", "designsystem"), "designsystem" to listOf("feature", "data"))
        val violations =
            layers.flatMap { (layer, forbidden) ->
                SourceTree.kotlinFiles(SourceTree.mainPackage(layer)).flatMap { file ->
                    file.imports
                        .filter { import -> forbidden.any { import.name.startsWith("$PACKAGE.$it.") } }
                        .map { "${file.relativePath}:${it.line} ($layer) imports ${it.name}" }
                }
            }
        ArchitectureBaseline.assertNoNewViolations(Rules.LAYERING, "data and designsystem must not import feature code", violations)
    }

    @Test
    fun featuresDoNotImportEachOther() {
        val featureRoot = SourceTree.mainPackage("feature")
        val violations =
            SourceTree.kotlinFiles(featureRoot).flatMap { file ->
                val own = featureRoot.relativize(file.path).first().toString()
                file.imports
                    .map {
                        it.name
                            .removePrefix("$PACKAGE.feature.")
                            .takeIf { rest -> rest != it.name }
                            ?.substringBefore('.')
                    }.filter { other -> other != null && other != own }
                    .distinct()
                    .map { other -> "${file.relativePath.substringAfter("$PACKAGE_PATH/")} -> feature.$other" }
            }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.FEATURE_ISOLATION,
            "features talk to each other only through core/navigation routes and shared core components",
            violations,
        )
    }

    private companion object {
        val PACKAGE_PATH = "app/src/main/java/" + PACKAGE.replace('.', '/')
    }
}

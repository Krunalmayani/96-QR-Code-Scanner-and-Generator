package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

/**
 * User-visible text lives in resources: no literals in feature composables, every `R.string` used exists, and every
 * default string and plural has all seven translations with the same format placeholders.
 */
class StringResourcesTest {
    // ---- hard-coded strings in UI code ----

    private val featureFiles get() = SourceTree.kotlinFiles(SourceTree.mainPackage("feature"))

    @Test
    fun noHardCodedUserVisibleStringsInFeatureComposables() {
        val violations =
            featureFiles
                .filterNot {
                    it.path.fileName
                        .toString()
                        .endsWith("Previews.kt")
                }.flatMap { file -> hardCodedStrings(file.code.substring(0, previewStart(file.raw, file.code)), file) }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.HARD_CODED_TEXT,
            "user-visible text must come from string resources (previews and contentDescription = null are exempt)",
            violations,
        )
    }

    @Test
    fun hardCodedStringDetectionRecognisesTheForbiddenShapes() {
        val source = KotlinSourceForTest
        assertTrue(source.scan("""Text("Hello")""").isNotEmpty(), "Text(\"literal\")")
        assertTrue(source.scan("""Text(text = "Hello ${'$'}name")""").isNotEmpty(), "Text(text = \"literal\")")
        assertTrue(source.scan("""Icon(x, contentDescription = "Close")""").isNotEmpty(), "contentDescription literal")
        assertTrue(source.scan("""AppTopBar(title = "Settings")""").isNotEmpty(), "named UI argument")
        assertTrue(source.scan("""host.showSnackbar("Saved")""").isNotEmpty(), "snackbar literal")
        assertTrue(source.scan("""Icon(x, contentDescription = null)""").isEmpty(), "contentDescription = null is allowed")
        assertTrue(source.scan("""Text(stringResource(R.string.a))""").isEmpty(), "resources are allowed")
        assertTrue(source.scan("""Text("•")""").isEmpty(), "symbol-only text has nothing to translate")
        assertTrue(source.scan("""ScanContent.Text("raw payload", format)""").isEmpty(), "ScanContent.Text is data, not a composable")
        assertTrue(source.scan("""val url = "https://example.com"""").isEmpty(), "ordinary strings are allowed")
    }

    private object KotlinSourceForTest {
        fun scan(code: String): List<String> = hardCodedStrings(code, null)
    }

    // ---- R.string references ----

    @Test
    fun everyReferencedStringAndPluralExists() {
        val defined = definedNames(SourceTree.resDir.resolve("values"), onlyTranslatable = false)
        val definedByGradle = gradleStringResources()
        val used = LinkedHashMap<String, MutableList<String>>()

        SourceTree.kotlinFiles(SourceTree.mainDir.resolve("java")).forEach { file ->
            R_REFERENCE.findAll(file.code).forEach { match ->
                used.getOrPut("${match.groupValues[1]}/${match.groupValues[2]}") { mutableListOf() }.add(file.location(match.range.first))
            }
        }
        resourceXmlFiles().forEach { xml ->
            XML_REFERENCE.findAll(String(Files.readAllBytes(xml), Charsets.UTF_8)).forEach { match ->
                val type = if (match.groupValues[1] == "string") "string" else "plurals"
                used.getOrPut("$type/${match.groupValues[2]}") { mutableListOf() }.add(SourceTree.root.relativize(xml).toString())
            }
        }

        val missing =
            used.filterKeys { key ->
                val (type, name) = key.split('/')
                name !in defined.getValue(type) && !(type == "string" && name in definedByGradle)
            }
        assertTrue(missing.isEmpty()) {
            "resources used but not defined in res/values:\n" +
                missing.entries.joinToString("\n") { (key, places) -> " - $key (first use ${places.first()})" }
        }
        assertTrue(used.size > 100, "expected to find many resource references, found ${used.size}")
    }

    // ---- translations ----

    @Test
    fun everyDefaultStringAndPluralIsTranslatedIntoAllSevenLanguages() {
        val default = definedNames(SourceTree.resDir.resolve("values"))
        val problems =
            LANGUAGES.flatMap { language ->
                val translated = definedNames(SourceTree.resDir.resolve("values-$language"), onlyTranslatable = false)
                listOf("string", "plurals").flatMap { type ->
                    default
                        .getValue(
                            type,
                        ).filter { it !in translated.getValue(type) }
                        .sorted()
                        .map { "values-$language is missing $type $it" }
                }
            }
        assertTrue(problems.isEmpty()) {
            "untranslated resources (${problems.size}):\n" +
                problems.take(MAX_LISTED).joinToString("\n") { " - $it" }
        }
    }

    @Test
    fun translationsDoNotDefineNamesTheDefaultLanguageLacks() {
        val default = definedNames(SourceTree.resDir.resolve("values"), onlyTranslatable = false)
        val problems =
            LANGUAGES.flatMap { language ->
                val translated = definedNames(SourceTree.resDir.resolve("values-$language"), onlyTranslatable = false)
                listOf("string", "plurals").flatMap { type ->
                    translated
                        .getValue(
                            type,
                        ).filter { it !in default.getValue(type) }
                        .sorted()
                        .map { "values-$language has orphan $type $it" }
                }
            }
        assertTrue(problems.isEmpty()) {
            "orphan translations (${problems.size}):\n" +
                problems.take(MAX_LISTED).joinToString("\n") { " - $it" }
        }
    }

    @Test
    fun translationsKeepTheFormatPlaceholdersOfTheDefaultString() {
        val default = placeholdersByName(SourceTree.resDir.resolve("values"))
        val problems =
            LANGUAGES.flatMap { language ->
                placeholdersByName(SourceTree.resDir.resolve("values-$language")).mapNotNull { (name, found) ->
                    val expected = default[name] ?: return@mapNotNull null
                    if (found == expected) null else "values-$language $name: expected ${expected.sorted()} but found ${found.sorted()}"
                }
            }
        assertTrue(problems.isEmpty()) {
            "placeholder mismatches (${problems.size}):\n" +
                problems.take(MAX_LISTED).joinToString("\n") { " - $it" }
        }
    }

    @Test
    fun theSevenTranslationFoldersMatchTheLanguagesOfTheLocaleConfig() {
        val config = String(Files.readAllBytes(SourceTree.resDir.resolve("xml/locales_config.xml")), Charsets.UTF_8)
        val configured = Regex("""android:name="([\w-]+)"""").findAll(config).map { it.groupValues[1] }.toSet() - "en"
        val folders =
            Files.list(SourceTree.resDir).use { stream ->
                stream
                    .map { it.fileName.toString() }
                    .filter {
                        it.startsWith(
                            "values-",
                        )
                    }.map { it.removePrefix("values-") }
                    .toList()
                    .toSet()
            }
        assertEquals(LANGUAGES.toSet(), configured, "locales_config.xml languages (besides en)")
        assertEquals(LANGUAGES.toSet(), folders.filter { it in LANGUAGES }.toSet(), "values-xx folders")
        assertFalse(LANGUAGES.any { !Files.isDirectory(SourceTree.resDir.resolve("values-$it")) })
    }

    // ---- helpers ----

    private fun resourceXmlFiles(): List<Path> =
        Files.walk(SourceTree.mainDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".xml") }
                .filter { path ->
                    val folder = path.parent.fileName.toString()
                    path == SourceTree.manifest || folder == "values" || !folder.startsWith("values-")
                }.toList()
        }

    /** Names of `<string>` and `<plurals>` in every XML file of [dir]; strings marked translatable="false" are skipped by default. */
    private fun definedNames(
        dir: Path,
        onlyTranslatable: Boolean = true,
    ): Map<String, Set<String>> {
        val strings = LinkedHashSet<String>()
        val plurals = LinkedHashSet<String>()
        resourceElements(dir).forEach { element ->
            val name = element.getAttribute("name")
            val skip = onlyTranslatable && element.getAttribute("translatable") == "false"
            when {
                skip -> Unit
                element.tagName == "string" -> strings += name
                element.tagName == "plurals" -> plurals += name
            }
        }
        return mapOf("string" to strings, "plurals" to plurals)
    }

    /** Distinct placeholders per resource (all plural items together: a quantity form may legitimately omit the number). */
    private fun placeholdersByName(dir: Path): Map<String, List<String>> =
        resourceElements(dir)
            .filter { it.getAttribute("translatable") != "false" }
            .associate { element ->
                element.getAttribute("name") to
                    PLACEHOLDER
                        .findAll(element.textContent)
                        .map { it.value }
                        .toSortedSet()
                        .toList()
            }

    private fun resourceElements(dir: Path): List<Element> {
        if (!Files.isDirectory(dir)) return emptyList()
        val xmlFiles =
            Files.list(dir).use { stream -> stream.filter { it.fileName.toString().endsWith(".xml") }.sorted().toList() }
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        return xmlFiles.flatMap { file ->
            val document = builder.parse(file.toFile())
            val elements = document.documentElement.childNodes
            (0 until elements.length).mapNotNull { elements.item(it) as? Element }.filter {
                it.tagName == "string" ||
                    it.tagName == "plurals"
            }
        }
    }

    /** Strings the build script generates with resValue("string", name, ...), e.g. the AdMob app id. */
    private fun gradleStringResources(): Set<String> =
        Regex("""resValue\(\s*"string"\s*,\s*"(\w+)"""")
            .findAll(String(Files.readAllBytes(SourceTree.appBuildScript), Charsets.UTF_8))
            .map { it.groupValues[1] }
            .toSet()

    private companion object {
        val LANGUAGES = listOf("ar", "de", "es", "fr", "hi", "in", "pt")
        const val MAX_LISTED = 60

        val PLACEHOLDER = Regex("""%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[sdfxXeEgGcb]""")
        val R_REFERENCE = Regex("""(?<![\w.])R\.(string|plurals)\.(\w+)""")
        val XML_REFERENCE = Regex("""@(string|plurals)/(\w+)""")

        private val LETTER = Regex("""\p{L}""")
        private val TEMPLATE = Regex("""\$\{[^}]*}|\$\w+""")

        /** Quoted string literal with escapes; group 1 is its content. */
        private const val LITERAL = """"((?:[^"\\]|\\.)*)""""

        private val TEXT_CALL = Regex("""(?<![\w.])Text\(\s*(?:text\s*=\s*)?$LITERAL""")
        private val CONTENT_DESCRIPTION = Regex("""\b(?:contentDescription|stateDescription)\s*=\s*$LITERAL""")
        private val UI_ARGUMENT =
            Regex(
                """\b(?:title|label|message|subtitle|placeholder|helper|confirmText|dismissText|actionLabel|headline|hint)\s*=\s*$LITERAL""",
            )
        private val SNACKBAR = Regex("""showSnackbar\(\s*(?:message\s*=\s*)?$LITERAL""")

        fun hasWords(literal: String): Boolean = LETTER.containsMatchIn(TEMPLATE.replace(literal, ""))

        fun hardCodedStrings(
            code: String,
            file: KotlinSource?,
        ): List<String> =
            listOf(TEXT_CALL, CONTENT_DESCRIPTION, UI_ARGUMENT, SNACKBAR).flatMap { pattern ->
                pattern
                    .findAll(code)
                    .filter { hasWords(it.groupValues[1]) }
                    .map { match ->
                        val where = file?.location(match.range.first) ?: "sample"
                        "$where hard-codes \"${match.groupValues[1].take(40)}\""
                    }.toList()
            }

        /**
         * Everything from the first `@Preview` (or a `// Previews` section comment) to the end of the file is preview-only
         * sample code in this repo's layout, so it is exempt. Offsets of [raw] and [code] are identical.
         */
        fun previewStart(
            raw: String,
            code: String,
        ): Int {
            val annotation = Regex("""(?m)^\s*@Preview\b""").find(code)?.range?.first
            val section = Regex("""(?im)^\s*//\s*-*\s*previews?\b""").find(raw)?.range?.first
            return listOfNotNull(annotation, section, code.length).min()
        }
    }
}

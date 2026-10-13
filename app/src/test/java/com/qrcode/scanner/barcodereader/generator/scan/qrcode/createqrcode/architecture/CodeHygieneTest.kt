package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/** Repository rules from CLAUDE.md that can be checked by reading source files. */
class CodeHygieneTest {
    private val mainFiles get() = SourceTree.kotlinFiles(SourceTree.mainDir.resolve("java"))

    @Test
    fun sourcesWereFound() {
        assertFalse(mainFiles.isEmpty(), "no Kotlin files under ${SourceTree.mainDir}")
    }

    @Test
    fun noTodoOrFixmeLeftInCode() {
        // Built from pieces so this file does not trip its own rule. Comments count: "no TODOs left in code".
        val marker = Regex("""\b(${"TO" + "DO"}|${"FIX" + "ME"})\b""")
        val violations =
            SourceTree
                .allKotlinFiles()
                .filterNot { it.relativePath.contains("/architecture/") }
                .flatMap { file ->
                    marker.findAll(file.raw).map { "${file.location(it.range.first)} contains ${it.value}" }.toList()
                }
        ArchitectureBaseline.assertNoNewViolations(Rules.NO_TODO, "unfinished-work markers left in code", violations)
    }

    @Test
    fun noWildcardImports() {
        val violations =
            SourceTree.allKotlinFiles().flatMap { file ->
                file.imports.filter { it.name.endsWith(".*") }.map { "${file.relativePath}:${it.line} imports ${it.name}" }
            }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.NO_WILDCARD_IMPORTS,
            "wildcard imports (CLAUDE.md: explicit imports only)",
            violations,
        )
    }

    @Test
    fun everyHiltViewModelHasExactlyOneNonPrivateInjectConstructor() {
        val violations = mainFiles.flatMap { hiltViewModelProblems(it) }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.HILT_VIEWMODEL,
            "a @HiltViewModel needs one internal or public @Inject constructor (Hilt cannot call a private one)",
            violations,
        )
    }

    @Test
    fun hiltViewModelCheckRecognisesTheShapesUsedInTheApp() {
        val primary = "@HiltViewModel\nclass A\n    @Inject\n    constructor(x: Int) : ViewModel()"
        val secondary = "@HiltViewModel\nclass B internal constructor(x: Int) : ViewModel() {\n    @Inject\n    constructor() : this(1)\n}"
        val privateOne = "@HiltViewModel\nclass C @Inject private constructor() : ViewModel()"
        val missing = "@HiltViewModel\nclass D(x: Int) : ViewModel()"
        assertFalse(problemsIn(primary).isNotEmpty(), "primary @Inject constructor")
        assertFalse(problemsIn(secondary).isNotEmpty(), "secondary @Inject constructor with an internal primary one")
        assertFalse(problemsIn(privateOne).isEmpty(), "private @Inject constructor must be reported")
        assertFalse(problemsIn(missing).isEmpty(), "missing @Inject must be reported")
    }

    private fun problemsIn(source: String): List<String> = hiltViewModelProblems(source, "sample")

    private fun hiltViewModelProblems(file: KotlinSource): List<String> =
        if ("@HiltViewModel" in file.code) hiltViewModelProblems(file.code, file.relativePath) else emptyList()

    private fun hiltViewModelProblems(
        code: String,
        where: String,
    ): List<String> {
        val starts = Regex("""@HiltViewModel\b""").findAll(code).map { it.range.first }.toList()
        return starts
            .mapIndexed { index, start ->
                val end = starts.getOrNull(index + 1) ?: code.length
                val region = code.substring(start, end)
                val name = Regex("""\bclass\s+(\w+)""").find(region)?.groupValues?.get(1) ?: "?"
                val header = region.substringBefore("class")
                val injects = INJECT_CONSTRUCTOR.findAll(region).toList()
                when {
                    Regex("""\bprivate\b""").containsMatchIn(header) -> "$where: $name is private"
                    injects.isEmpty() -> "$where: $name has no @Inject constructor"
                    injects.size > 1 -> "$where: $name has ${injects.size} @Inject constructors"
                    injects.single().groupValues[1] == "private" || injects.single().groupValues[1] == "protected" ->
                        "$where: $name has a ${injects.single().groupValues[1]} @Inject constructor"
                    else -> null
                }
            }.filterNotNull()
    }

    private companion object {
        val INJECT_CONSTRUCTOR = Regex("""@Inject\s+(?:(private|protected|internal|public)\s+)?constructor\b""")
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * Where the sources live, for tests that scan files instead of running code. The repository root comes from the
 * `repo.root` system property (set by app/build.gradle.kts); when it is missing the tests walk up from the working
 * directory until they find `app/src/main/AndroidManifest.xml`, so they also run from an IDE.
 */
internal object SourceTree {
    const val PACKAGE = "com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode"

    val root: Path by lazy { locateRoot() }
    val srcDir: Path get() = root.resolve("app/src")
    val mainDir: Path get() = srcDir.resolve("main")
    val packageDir: Path get() = mainDir.resolve("java/" + PACKAGE.replace('.', '/'))
    val resDir: Path get() = mainDir.resolve("res")
    val manifest: Path get() = mainDir.resolve("AndroidManifest.xml")
    val appBuildScript: Path get() = root.resolve("app/build.gradle.kts")

    fun mainPackage(subPackage: String): Path = packageDir.resolve(subPackage)

    /** Every `.kt` file below [dir], sorted for stable reports. Empty when [dir] does not exist. */
    fun kotlinFiles(dir: Path): List<KotlinSource> {
        if (!Files.isDirectory(dir)) return emptyList()
        return Files
            .walk(dir)
            .use { stream ->
                stream
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                    .toList()
            }.sorted()
            .map { KotlinSource(it, root.relativize(it).toString().replace('\\', '/')) }
    }

    /** All Kotlin sources of the app: main, unit tests, instrumented tests and the debug-only source set. */
    fun allKotlinFiles(): List<KotlinSource> =
        listOf("main", "test", "androidTest", "debug").flatMap { kotlinFiles(srcDir.resolve(it)) }

    private fun locateRoot(): Path {
        System.getProperty("repo.root")?.takeIf { it.isNotBlank() }?.let { configured ->
            val path = Paths.get(configured).toAbsolutePath()
            check(Files.exists(path.resolve("app/src/main/AndroidManifest.xml"))) {
                "repo.root=$configured does not contain app/src/main/AndroidManifest.xml"
            }
            return path
        }
        var dir: Path? = Paths.get("").toAbsolutePath()
        while (dir != null) {
            if (Files.exists(dir.resolve("app/src/main/AndroidManifest.xml"))) return dir
            if (Files.exists(dir.resolve("src/main/AndroidManifest.xml"))) return dir.parent
            dir = dir.parent
        }
        error("Cannot find the repository root; set the repo.root system property")
    }
}

/** One Kotlin file: raw text, the same text with comments blanked (offsets and line numbers preserved) and its imports. */
internal class KotlinSource(
    val path: Path,
    /** Path relative to the repository root, with forward slashes. */
    val relativePath: String,
) {
    val raw: String by lazy { String(Files.readAllBytes(path), Charsets.UTF_8) }

    /** [raw] with every comment replaced by spaces; strings are kept intact. */
    val code: String by lazy { KotlinLexer.blankComments(raw) }

    /** Line number (1-based) and the imported name for every `import` line. */
    val imports: List<Import> by lazy {
        raw
            .lineSequence()
            .mapIndexedNotNull { index, line ->
                val match = IMPORT_LINE.matchEntire(line.trim()) ?: return@mapIndexedNotNull null
                Import(index + 1, match.groupValues[1].trim())
            }.toList()
    }

    fun lineOf(offset: Int): Int = raw.substring(0, offset.coerceAtMost(raw.length)).count { it == '\n' } + 1

    fun location(offset: Int): String = "$relativePath:${lineOf(offset)}"

    data class Import(
        val line: Int,
        /** The imported name, e.g. `androidx.compose.runtime.Composable` or `kotlin.math.*`. */
        val name: String,
    )

    private companion object {
        val IMPORT_LINE = Regex("""import\s+(\S+)(?:\s+as\s+\w+)?""")
    }
}

/** Just enough of the Kotlin grammar to tell comments from strings, so `"http://x"` is not mistaken for a comment. */
internal object KotlinLexer {
    private class Frame(
        val mode: Char,
        var braces: Int = 0,
    )

    private const val CODE = 'C'
    private const val STRING = 'S'
    private const val RAW = 'R'

    fun blankComments(src: String): String {
        val out = StringBuilder(src)
        val stack = ArrayDeque<Frame>()
        stack.addLast(Frame(CODE))
        var i = 0
        while (i < src.length) {
            val frame = stack.last()
            val c = src[i]
            i =
                when (frame.mode) {
                    CODE -> stepCode(src, out, stack, frame, i, c)
                    STRING -> stepString(src, stack, i, c)
                    else -> stepRaw(src, stack, i)
                }
        }
        return out.toString()
    }

    private fun stepCode(
        src: String,
        out: StringBuilder,
        stack: ArrayDeque<Frame>,
        frame: Frame,
        i: Int,
        c: Char,
    ): Int {
        when {
            src.startsWith("//", i) -> return blankUntil(src, out, i) { src[it] == '\n' }
            src.startsWith("/*", i) -> return blankBlockComment(src, out, i)
            src.startsWith("\"\"\"", i) -> {
                stack.addLast(Frame(RAW))
                return i + 3
            }
            c == '"' -> stack.addLast(Frame(STRING))
            c == '\'' -> return skipCharLiteral(src, i)
            c == '{' -> frame.braces++
            c == '}' -> if (frame.braces == 0 && stack.size > 1) stack.removeLast() else frame.braces--
        }
        return i + 1
    }

    private fun stepString(
        src: String,
        stack: ArrayDeque<Frame>,
        i: Int,
        c: Char,
    ): Int {
        when {
            c == '\\' -> return i + 2
            c == '"' || c == '\n' -> stack.removeLast()
            src.startsWith("\${", i) -> {
                stack.addLast(Frame(CODE))
                return i + 2
            }
        }
        return i + 1
    }

    private fun stepRaw(
        src: String,
        stack: ArrayDeque<Frame>,
        i: Int,
    ): Int {
        if (src.startsWith("\"\"\"", i)) {
            var end = i + 3
            while (end < src.length && src[end] == '"') end++
            stack.removeLast()
            return end
        }
        if (src.startsWith("\${", i)) {
            stack.addLast(Frame(CODE))
            return i + 2
        }
        return i + 1
    }

    private fun skipCharLiteral(src: String, start: Int): Int {
        var i = start + 1
        if (i < src.length && src[i] == '\\') i++
        i++
        return if (i < src.length && src[i] == '\'') i + 1 else start + 1
    }

    private inline fun blankUntil(
        src: String,
        out: StringBuilder,
        start: Int,
        stop: (Int) -> Boolean,
    ): Int {
        var i = start
        while (i < src.length && !stop(i)) {
            out.setCharAt(i, ' ')
            i++
        }
        return i
    }

    private fun blankBlockComment(src: String, out: StringBuilder, start: Int): Int {
        var depth = 0
        var i = start
        while (i < src.length) {
            when {
                src.startsWith("/*", i) -> {
                    depth++
                    out.setCharAt(i, ' ')
                    out.setCharAt(i + 1, ' ')
                    i += 2
                }
                src.startsWith("*/", i) -> {
                    depth--
                    out.setCharAt(i, ' ')
                    out.setCharAt(i + 1, ' ')
                    i += 2
                    if (depth == 0) return i
                }
                else -> {
                    if (src[i] != '\n') out.setCharAt(i, ' ')
                    i++
                }
            }
        }
        return i
    }
}

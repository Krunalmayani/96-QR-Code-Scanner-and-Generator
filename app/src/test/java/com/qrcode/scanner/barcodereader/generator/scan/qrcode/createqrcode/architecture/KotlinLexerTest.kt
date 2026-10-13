package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The architecture rules trust [KotlinLexer] to separate comments from code and strings; pin its behaviour. */
class KotlinLexerTest {
    private fun blank(src: String): String = KotlinLexer.blankComments(src)

    @Test
    fun lineCommentsAreBlankedButLengthAndLinesArePreserved() {
        val src = "val a = 1 // note\nval b = 2\n"
        val out = blank(src)
        assertEquals(src.length, out.length)
        assertEquals("val a = 1        \nval b = 2\n", out)
    }

    @Test
    fun urlsInsideStringsAreNotComments() {
        val src = "val url = \"https://example.com/a\" // trailing"
        assertEquals("val url = \"https://example.com/a\"            ", blank(src))
    }

    @Test
    fun blockCommentsNestAndKeepNewlines() {
        val src = "a /* x /* y */ z */ b\n/* one\ntwo */ c"
        // "/* x /* y */ z */" is 17 characters, plus one space on each side of it.
        assertEquals("a" + " ".repeat(19) + "b\n" + " ".repeat(6) + "\n" + " ".repeat(7) + "c", blank(src))
    }

    @Test
    fun rawStringsKeepTheirContent() {
        val src = "val r = \"\"\"// not a comment \"quoted\" \"\"\" // real"
        assertEquals("val r = \"\"\"// not a comment \"quoted\" \"\"\"        ", blank(src))
    }

    @Test
    fun stringTemplatesMayContainStringsAndComments() {
        val src = "val t = \"a \${f(\"}\")} // still string\" // gone"
        assertEquals("val t = \"a \${f(\"}\")} // still string\"        ", blank(src))
    }

    @Test
    fun charLiteralsWithQuotesDoNotStartStrings() {
        val src = "val q = '\"' // c\nval s = \"x\""
        assertEquals("val q = '\"'     \nval s = \"x\"", blank(src))
    }

    @Test
    fun importsAreParsedWithLineNumbersAndAliases() {
        val file = KotlinSourceForTest.fromText("package p\n\nimport a.b.C\nimport d.e.* \nimport f.G as H\n// import not.real.X\n")
        assertEquals(listOf(3 to "a.b.C", 4 to "d.e.*", 5 to "f.G"), file.imports.map { it.line to it.name })
    }

    private object KotlinSourceForTest {
        fun fromText(text: String): KotlinSource {
            val temp =
                java.nio.file.Files
                    .createTempFile("lexer", ".kt")
            java.nio.file.Files
                .write(temp, text.toByteArray(Charsets.UTF_8))
            temp.toFile().deleteOnExit()
            return KotlinSource(temp, temp.fileName.toString())
        }
    }
}

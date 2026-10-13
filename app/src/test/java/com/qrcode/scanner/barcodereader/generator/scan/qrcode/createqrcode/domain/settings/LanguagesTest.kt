package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LanguagesTest {
    @Test
    fun `lists the eight required languages with the tags the shell applies`() {
        assertEquals(
            listOf("en", "hi", "es", "pt", "fr", "de", "in", "ar"),
            Languages.supported.map { it.tag },
        )
    }

    @Test
    fun `find matches the primary subtag and the legacy Indonesian code`() {
        assertEquals("Português", Languages.find("pt-BR")?.nativeName)
        assertEquals("Bahasa Indonesia", Languages.find("id")?.nativeName)
        assertEquals("Bahasa Indonesia", Languages.find("in")?.nativeName)
        assertEquals("हिन्दी", Languages.find("HI")?.nativeName)
    }

    @Test
    fun `find returns null for the system default and unknown tags`() {
        assertNull(Languages.find(Languages.SYSTEM_TAG))
        assertNull(Languages.find("ja"))
    }

    @Test
    fun `filter searches native and English names`() {
        assertEquals(listOf("es"), Languages.filter("span").map { it.tag })
        assertEquals(listOf("fr"), Languages.filter("fran").map { it.tag })
        assertEquals(listOf("de"), Languages.filter("DEUT").map { it.tag })
        assertEquals(Languages.supported, Languages.filter("  "))
        assertEquals(emptyList<LanguageOption>(), Languages.filter("klingon"))
    }
}

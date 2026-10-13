package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

/** One app language. [tag] is the BCP-47 tag stored in settings (Indonesian is "in", as in locales_config.xml). */
data class LanguageOption(val tag: String, val nativeName: String, val englishName: String)

object Languages {
    /** Stored when the app follows the system language. */
    const val SYSTEM_TAG = ""

    val supported: List<LanguageOption> = listOf(
        LanguageOption("en", "English", "English"),
        LanguageOption("hi", "हिन्दी", "Hindi"),
        LanguageOption("es", "Español", "Spanish"),
        LanguageOption("pt", "Português", "Portuguese"),
        LanguageOption("fr", "Français", "French"),
        LanguageOption("de", "Deutsch", "German"),
        LanguageOption("in", "Bahasa Indonesia", "Indonesian"),
        LanguageOption("ar", "العربية", "Arabic"),
    )

    /** Finds the option for a stored tag such as "pt-BR" or "id"; null for the system default or unknown tags. */
    fun find(tag: String): LanguageOption? {
        val primary = tag.trim().lowercase().substringBefore('-').substringBefore('_')
        val canonical = if (primary == "id") "in" else primary
        return supported.firstOrNull { it.tag == canonical }
    }

    /** Options whose native name, English name or tag contains [query] (case-insensitive); blank returns all. */
    fun filter(query: String): List<LanguageOption> {
        val q = query.trim()
        if (q.isEmpty()) return supported
        return supported.filter {
            it.nativeName.contains(q, ignoreCase = true) ||
                it.englishName.contains(q, ignoreCase = true) ||
                it.tag.equals(q, ignoreCase = true)
        }
    }
}

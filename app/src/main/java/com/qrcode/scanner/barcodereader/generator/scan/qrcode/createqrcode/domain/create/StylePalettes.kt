package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

/** Code colors offered as swatches (design s41). Every one is readable on every [BackgroundSwatch]. */
enum class CodeSwatch(val argb: Long) {
    INDIGO(0xFF4F46E5),
    INK(0xFF13142B),
    TEAL(0xFF0B7A6E),
    RED(0xFFC62828),
    BROWN(0xFF9A5B00),
    BLUE(0xFF1E6FA8),
    PURPLE(0xFF6D28D9),
    ;

    companion object {
        fun fromArgb(argb: Long): CodeSwatch? = entries.firstOrNull { it.argb == argb }
    }
}

/** Background colors offered as swatches (design s41). */
enum class BackgroundSwatch(val argb: Long) {
    WHITE(0xFFFFFFFF),
    MIST(0xFFF5F6FC),
    CREAM(0xFFFFF0D1),
    MINT(0xFFD3F4EE),
    LAVENDER(0xFFE7E5FF),
    ;

    companion object {
        fun fromArgb(argb: Long): BackgroundSwatch? = entries.firstOrNull { it.argb == argb }
    }
}

/** Two-color gradient presets; both ends stay readable on a white background. */
enum class GradientPreset(val start: Long, val end: Long) {
    AURORA(0xFF4F46E5, 0xFF0B7A6E),
    BERRY(0xFF6D28D9, 0xFFBE185D),
    OCEAN(0xFF1E3A8A, 0xFF0E7490),
    EMBER(0xFFB91C1C, 0xFF9A5B00),
    FOREST(0xFF047857, 0xFF1E3A8A),
    ;

    companion object {
        /** The preset whose colors equal [start] and [end], if any. */
        fun match(start: Long, end: Long?): GradientPreset? = entries.firstOrNull { it.start == start && it.end == end }
    }
}

/** Parses and prints `#RRGGBB` colors for the custom color dialog. Always opaque. */
object ColorHex {
    private const val OPAQUE = 0xFF000000

    /** `#RRGGBB` (upper-case) of the color part of [argb]. */
    fun format(argb: Long): String = "#" + (argb and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

    /** Accepts `RRGGBB`, `#RRGGBB`, `RGB` and `#RGB` (any case, surrounding spaces); returns an opaque ARGB or null. */
    fun parse(input: String): Long? {
        val t = input.trim().removePrefix("#")
        if (t.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
        val full = when (t.length) {
            3 -> t.map { "$it$it" }.joinToString("")
            6 -> t
            else -> return null
        }
        return OPAQUE or full.toLong(16)
    }
}

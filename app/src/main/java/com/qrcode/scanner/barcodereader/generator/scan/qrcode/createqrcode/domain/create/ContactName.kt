package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

/**
 * The Contact form shows First and Last name (design s35) while [CreateSpec.Contact] keeps one `name`.
 * The two parts are joined with a single space; an existing name is split at its first space.
 */
object ContactName {
    private const val SEPARATOR = ' '

    /** [first] and [last] trimmed and joined with one space; a missing part leaves no stray space. */
    fun join(first: String, last: String): String =
        listOf(first.trim(), last.trim()).filter { it.isNotEmpty() }.joinToString(SEPARATOR.toString())

    /** Everything before the first space of [name]. */
    fun firstOf(name: String): String = name.substringBefore(SEPARATOR)

    /** Everything after the first space of [name], empty when there is none. */
    fun lastOf(name: String): String = name.substringAfter(SEPARATOR, "")
}

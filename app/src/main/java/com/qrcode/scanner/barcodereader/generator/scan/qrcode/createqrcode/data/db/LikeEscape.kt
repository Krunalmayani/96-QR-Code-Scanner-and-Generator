package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.db

private const val ESCAPE_CHAR = '\\'

/**
 * Builds a LIKE pattern that matches [query] literally anywhere in a column: wraps it in % and escapes the
 * LIKE wildcards % and _ (and the escape character itself) with a backslash. Pair with ESCAPE '\' in SQL.
 */
fun escapeLikePattern(query: String): String {
    val escaped = buildString {
        for (c in query.trim()) {
            if (c == '%' || c == '_' || c == ESCAPE_CHAR) append(ESCAPE_CHAR)
            append(c)
        }
    }
    return "%" + escaped + "%"
}

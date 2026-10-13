package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/** Low-level, allocation-light helpers shared by the payload parsers. Never throw. */
internal object FieldParsing {
    private val LINE_BREAK = Regex("\r\n|\n|\r")

    /**
     * Parses `KEY:value;KEY:value;;` payloads (WIFI, MECARD, MATMSG, MEBKM).
     * Keys are upper-cased, values are unescaped (`\;` `\:` `\\` `\,` `\"`), a value that is wrapped in
     * double quotes loses the quotes, and repeated keys accumulate in order.
     */
    fun parseKeyValue(body: String): Map<String, List<String>> {
        val out = LinkedHashMap<String, MutableList<String>>()
        var i = 0
        val n = body.length
        while (i < n) {
            val key = StringBuilder()
            while (i < n && body[i] != ':' && body[i] != ';') {
                key.append(body[i])
                i++
            }
            if (i >= n) break
            if (body[i] == ';') {
                i++
                continue
            }
            i++ // skip ':'
            val (value, next) = readValue(body, i)
            i = next
            val k = key.toString().trim().uppercase()
            if (k.isNotEmpty()) out.getOrPut(k) { mutableListOf() }.add(value)
        }
        return out
    }

    private fun readValue(s: String, start: Int): Pair<String, Int> {
        if (start < s.length && s[start] == '"') {
            val close = findClosingQuote(s, start + 1)
            if (close > 0) {
                val inner = unescapeSimple(s.substring(start + 1, close))
                var j = close + 1
                while (j < s.length && s[j] != ';') j++
                return inner to (j + 1)
            }
        }
        val sb = StringBuilder()
        var i = start
        while (i < s.length && s[i] != ';') {
            if (s[i] == '\\' && i + 1 < s.length) {
                sb.append(s[i + 1])
                i += 2
            } else {
                sb.append(s[i])
                i++
            }
        }
        return sb.toString() to (i + 1)
    }

    private fun findClosingQuote(s: String, from: Int): Int {
        var i = from
        while (i < s.length) {
            val c = s[i]
            if (c == '\\') {
                i += 2
                continue
            }
            if (c == '"' && (i + 1 == s.length || s[i + 1] == ';')) return i
            i++
        }
        return -1
    }

    private fun unescapeSimple(s: String): String {
        if ('\\' !in s) return s
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length) {
                sb.append(s[i + 1])
                i += 2
            } else {
                sb.append(s[i])
                i++
            }
        }
        return sb.toString()
    }

    /** Splits text into lines and unfolds RFC 5545/6350 continuation lines (a line starting with space or tab). */
    fun unfoldLines(text: String): List<String> {
        val out = ArrayList<String>()
        for (line in text.split(LINE_BREAK)) {
            if (out.isNotEmpty() && line.isNotEmpty() && (line[0] == ' ' || line[0] == '\t')) {
                out[out.lastIndex] = out.last() + line.substring(1)
            } else {
                out.add(line)
            }
        }
        return out
    }

    /** One `NAME;PARAM=V;FLAG:value` line from a vCard or iCalendar body. */
    class ContentLine(val name: String, val params: Map<String, String>, val value: String)

    fun parseContentLine(line: String): ContentLine? {
        var inQuotes = false
        var idx = -1
        for (i in line.indices) {
            val c = line[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (c == ':' && !inQuotes) {
                idx = i
                break
            }
        }
        if (idx <= 0) return null
        val parts = line.substring(0, idx).split(';')
        val name = parts[0].trim().substringAfterLast('.').uppercase()
        if (name.isEmpty()) return null
        val params = LinkedHashMap<String, String>()
        for (p in parts.drop(1)) {
            val eq = p.indexOf('=')
            if (eq > 0) {
                params[p.substring(0, eq).trim().uppercase()] = p.substring(eq + 1).trim().trim('"')
            } else if (p.isNotBlank()) {
                params[p.trim().uppercase()] = ""
            }
        }
        return ContentLine(name, params, line.substring(idx + 1))
    }

    /** vCard 2.1 `QUOTED-PRINTABLE` soft line breaks: a trailing `=` joins the next line. */
    fun joinQuotedPrintable(lines: List<String>): List<String> {
        val out = ArrayList<String>()
        var i = 0
        while (i < lines.size) {
            var cur = lines[i]
            if (cur.contains("QUOTED-PRINTABLE", ignoreCase = true)) {
                while (cur.endsWith("=") && i + 1 < lines.size) {
                    i++
                    cur = cur.dropLast(1) + lines[i]
                }
            }
            out.add(cur)
            i++
        }
        return out
    }

    /** Unescapes vCard/iCalendar TEXT: `\n` `\N` -> newline, `\,` `\;` `\\` `\:` -> the character. */
    fun unescapeText(s: String): String {
        if ('\\' !in s) return s
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                val next = s[i + 1]
                sb.append(if (next == 'n' || next == 'N') '\n' else next)
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    /** Splits [s] on [sep] unless the separator is escaped with a backslash. Escapes are kept in the parts. */
    fun splitUnescaped(s: String, sep: Char): List<String> {
        val out = ArrayList<String>()
        val cur = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                cur.append(c).append(s[i + 1])
                i += 2
            } else if (c == sep) {
                out.add(cur.toString())
                cur.setLength(0)
                i++
            } else {
                cur.append(c)
                i++
            }
        }
        out.add(cur.toString())
        return out
    }

    /** Percent-decoding as used by mailto:, sms: and geo: URIs. Malformed escapes are kept verbatim. */
    fun percentDecode(s: String, plusAsSpace: Boolean = false): String {
        if ('%' !in s && !(plusAsSpace && '+' in s)) return s
        return decodeEscapes(s, '%', plusAsSpace, Charsets.UTF_8)
    }

    /** vCard 2.1 quoted-printable decoding (`=C3=A9`). */
    fun decodeQuotedPrintable(s: String, charset: Charset = Charsets.UTF_8): String =
        if ('=' !in s) s else decodeEscapes(s, '=', false, charset)

    private fun decodeEscapes(s: String, marker: Char, plusAsSpace: Boolean, charset: Charset): String {
        val out = StringBuilder()
        val pending = ByteArrayOutputStream()
        fun flush() {
            if (pending.size() > 0) {
                out.append(String(pending.toByteArray(), charset))
                pending.reset()
            }
        }
        var i = 0
        while (i < s.length) {
            val c = s[i]
            val hi = if (c == marker && i + 2 < s.length) Character.digit(s[i + 1], 16) else -1
            val lo = if (hi >= 0) Character.digit(s[i + 2], 16) else -1
            if (hi >= 0 && lo >= 0) {
                pending.write(hi * 16 + lo)
                i += 3
            } else {
                flush()
                out.append(if (plusAsSpace && c == '+') ' ' else c)
                i++
            }
        }
        flush()
        return out.toString()
    }

    /** Parses `a=b&c=d` query strings. Keys are lower-cased; values percent-decoded. */
    fun parseQuery(query: String, plusAsSpace: Boolean = false): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        for (pair in query.split('&')) {
            if (pair.isEmpty()) continue
            val eq = pair.indexOf('=')
            val key = (if (eq >= 0) pair.substring(0, eq) else pair).lowercase()
            val value = if (eq >= 0) percentDecode(pair.substring(eq + 1), plusAsSpace) else ""
            if (key.isNotEmpty() && key !in out) out[key] = value
        }
        return out
    }
}

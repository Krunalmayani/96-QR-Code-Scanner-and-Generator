package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import java.nio.charset.Charset

/** Parses vCard 2.1 / 3.0 / 4.0 and MECARD payloads into [ScanContent.Contact]. */
internal object ContactParser {
    private const val MECARD_PREFIX = "MECARD:"

    fun parseVCard(text: String, raw: String, format: CodeFormat): ScanContent.Contact? {
        val lines = FieldParsing.joinQuotedPrintable(FieldParsing.unfoldLines(text))
        val acc = Accumulator()
        var inCard = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.equals("BEGIN:VCARD", ignoreCase = true)) {
                inCard = true
            } else if (trimmed.equals("END:VCARD", ignoreCase = true)) {
                if (inCard) break
            } else if (inCard) {
                FieldParsing.parseContentLine(line)?.let { acc.add(it) }
            }
        }
        return acc.build(raw, format)
    }

    fun parseMeCard(text: String, raw: String, format: CodeFormat): ScanContent.Contact? {
        val fields = FieldParsing.parseKeyValue(text.substring(MECARD_PREFIX.length))
        val acc = Accumulator()
        for ((key, values) in fields) {
            for (v in values) {
                when {
                    key == "N" -> acc.nameStructured = acc.nameStructured.ifBlank { meCardName(v) }
                    key.startsWith("TEL") -> acc.phone(v)
                    key == "EMAIL" -> acc.email(v)
                    key == "ADR" -> acc.address = acc.address.ifBlank { joinParts(v.split(',')) }
                    key == "URL" -> acc.url = acc.url.ifBlank { v.trim() }
                    key == "NOTE" -> acc.note = acc.note.ifBlank { v.trim() }
                    key == "ORG" -> acc.org = acc.org.ifBlank { v.trim() }
                    key == "TITLE" -> acc.title = acc.title.ifBlank { v.trim() }
                }
            }
        }
        return acc.build(raw, format)
    }

    /** MECARD `N:Last,First` -> "First Last". */
    private fun meCardName(v: String): String {
        val parts = v.split(',', limit = 2).map { it.trim() }
        return if (parts.size == 2) joinParts(listOf(parts[1], parts[0]), " ") else parts[0]
    }

    private fun joinParts(parts: List<String>, sep: String = ", "): String =
        parts.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(sep)

    private class Accumulator {
        var formattedName = ""
        var nameStructured = ""
        var org = ""
        var title = ""
        val phones = LinkedHashSet<String>()
        val emails = LinkedHashSet<String>()
        var address = ""
        var url = ""
        var note = ""

        fun phone(v: String) {
            val p = v.trim().removePrefix("tel:").removePrefix("TEL:").trim()
            if (p.isNotEmpty()) phones.add(p)
        }

        fun email(v: String) {
            val e = v.trim().removePrefix("mailto:").removePrefix("MAILTO:").trim()
            if (e.isNotEmpty()) emails.add(e)
        }

        fun add(line: FieldParsing.ContentLine) {
            val v = decode(line)
            when (line.name) {
                "FN" -> if (formattedName.isBlank()) formattedName = FieldParsing.unescapeText(v).trim()
                "N" -> if (nameStructured.isBlank()) nameStructured = structuredName(v)
                "ORG" -> if (org.isBlank()) org = joinComponents(v)
                "TITLE" -> if (title.isBlank()) title = FieldParsing.unescapeText(v).trim()
                "TEL" -> phone(FieldParsing.unescapeText(v))
                "EMAIL" -> email(FieldParsing.unescapeText(v))
                "ADR" -> if (address.isBlank()) address = joinComponents(v)
                "URL" -> if (url.isBlank()) url = FieldParsing.unescapeText(v).trim()
                "NOTE" -> if (note.isBlank()) note = FieldParsing.unescapeText(v).trim()
            }
        }

        private fun decode(line: FieldParsing.ContentLine): String {
            val isQp = line.params["ENCODING"].equals("QUOTED-PRINTABLE", ignoreCase = true) ||
                line.params.containsKey("QUOTED-PRINTABLE")
            if (!isQp) return line.value
            return FieldParsing.decodeQuotedPrintable(line.value, charsetOf(line.params["CHARSET"]))
        }

        private fun charsetOf(name: String?): Charset =
            try {
                if (name.isNullOrBlank()) Charsets.UTF_8 else Charset.forName(name)
            } catch (e: IllegalArgumentException) {
                Charsets.UTF_8
            }

        /** `N:Family;Given;Additional;Prefix;Suffix` -> "Prefix Given Additional Family Suffix". */
        private fun structuredName(v: String): String {
            val p = FieldParsing.splitUnescaped(v, ';').map { FieldParsing.unescapeText(it).trim() }
            fun at(i: Int) = p.getOrElse(i) { "" }
            return joinParts(listOf(at(3), at(1), at(2), at(0), at(4)), " ")
        }

        private fun joinComponents(v: String): String =
            joinParts(FieldParsing.splitUnescaped(v, ';').map { FieldParsing.unescapeText(it) })

        fun build(raw: String, format: CodeFormat): ScanContent.Contact? {
            val name = formattedName.ifBlank { nameStructured }
            val any = name.isNotBlank() || org.isNotBlank() || title.isNotBlank() || phones.isNotEmpty() ||
                emails.isNotEmpty() || address.isNotBlank() || url.isNotBlank() || note.isNotBlank()
            if (!any) return null
            return ScanContent.Contact(
                rawValue = raw,
                format = format,
                name = name,
                organization = org,
                jobTitle = title,
                phones = phones.toList(),
                emails = emails.toList(),
                address = address,
                website = url,
                note = note,
            )
        }
    }
}

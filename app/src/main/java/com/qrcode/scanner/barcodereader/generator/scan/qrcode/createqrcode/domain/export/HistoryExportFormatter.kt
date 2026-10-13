package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.export

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HistoryExportFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    JSON("json", "application/json"),
    TEXT("txt", "text/plain"),
}

/**
 * Serialises history rows for the Export action. Dates are ISO-8601 UTC (`2024-05-01T10:00:00Z`).
 * CSV follows RFC 4180 (CRLF, quotes doubled) and neutralises spreadsheet formula injection by prefixing cells that
 * start with `=`, `+`, `-`, `@` (or a tab/CR) with an apostrophe.
 */
object HistoryExportFormatter {
    private const val CSV_HEADER = "id,date,source,type,format,title,content,favorite,note"
    private val ISO_UTC: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).withZone(ZoneOffset.UTC)
    private val PRETTY = Json { prettyPrint = true }

    fun format(records: List<ScanRecord>, format: HistoryExportFormat): String = when (format) {
        HistoryExportFormat.CSV -> csv(records)
        HistoryExportFormat.JSON -> json(records)
        HistoryExportFormat.TEXT -> text(records)
    }

    private fun isoDate(millis: Long): String = ISO_UTC.format(Instant.ofEpochMilli(millis))

    private fun csv(records: List<ScanRecord>): String {
        val sb = StringBuilder(CSV_HEADER).append("\r\n")
        for (r in records) {
            val cells = listOf(
                r.id.toString(),
                isoDate(r.createdAt),
                r.source.name,
                r.type.name,
                r.format.name,
                safeCell(r.displayTitle),
                safeCell(r.rawValue),
                r.isFavorite.toString(),
                safeCell(r.note),
            )
            sb.append(cells.joinToString(",") { quote(it) }).append("\r\n")
        }
        return sb.toString()
    }

    /** Neutralises formula injection. */
    internal fun safeCell(value: String): String =
        if (value.isNotEmpty() && value[0] in FORMULA_STARTS) "'" + value else value

    private val FORMULA_STARTS = setOf('=', '+', '-', '@', '\t', '\r')

    private fun quote(cell: String): String =
        if (cell.any { it == ',' || it == '"' || it == '\r' || it == '\n' }) "\"" + cell.replace("\"", "\"\"") + "\"" else cell

    private fun json(records: List<ScanRecord>): String {
        val array: JsonArray = buildJsonArray {
            for (r in records) {
                add(
                    buildJsonObject {
                        put("id", r.id)
                        put("date", isoDate(r.createdAt))
                        put("createdAt", r.createdAt)
                        put("source", r.source.name)
                        put("type", r.type.name)
                        put("format", r.format.name)
                        put("title", r.displayTitle)
                        put("content", r.rawValue)
                        put("favorite", r.isFavorite)
                        put("note", r.note)
                    },
                )
            }
        }
        return PRETTY.encodeToString(JsonArray.serializer(), array)
    }

    private fun text(records: List<ScanRecord>): String =
        records.joinToString("\n\n") { r ->
            buildString {
                append(r.displayTitle.ifBlank { r.type.name }).append('\n')
                append("Type: ").append(r.type.name).append(" | Format: ").append(r.format.displayName)
                append(" | Source: ").append(r.source.name).append('\n')
                append("Date: ").append(isoDate(r.createdAt)).append('\n')
                if (r.isFavorite) append("Favorite: yes\n")
                if (r.note.isNotBlank()) append("Note: ").append(r.note).append('\n')
                append("Content:\n").append(r.rawValue)
            }
        }.let { if (it.isEmpty()) it else it + "\n" }
}

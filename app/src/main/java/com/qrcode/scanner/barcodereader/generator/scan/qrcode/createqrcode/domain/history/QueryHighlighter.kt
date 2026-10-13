package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

object QueryHighlighter {
    /**
     * Non-overlapping, case-insensitive ranges (inclusive ends, like IntRange) of [query] inside [text], used to highlight search
     * hits. A blank query highlights nothing.
     */
    fun ranges(text: String, query: String): List<IntRange> {
        val needle = query.trim()
        if (needle.isEmpty() || text.length < needle.length) return emptyList()
        val out = ArrayList<IntRange>()
        var from = 0
        while (from <= text.length - needle.length) {
            val hit = text.indexOf(needle, from, ignoreCase = true)
            if (hit < 0) break
            out += hit until (hit + needle.length)
            from = hit + needle.length
        }
        return out
    }
}

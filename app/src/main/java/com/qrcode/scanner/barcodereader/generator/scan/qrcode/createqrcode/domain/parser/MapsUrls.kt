package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

/**
 * Detects map links that are *clearly* just a coordinate pair.
 *
 * Choice: a map URL is a [com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent.Location]
 * only when the coordinates are the whole point of the link: `maps.google.com/?q=lat,lng` (also `ll=`, `query=`),
 * `google.<tld>/maps/@lat,lng,zoom`, `google.<tld>/maps/search/lat,lng` and `maps.apple.com/?ll=lat,lng`.
 * Place names, `/maps/place/...`, directions and short links (`maps.app.goo.gl`) stay Website.
 */
internal object MapsUrls {
    private val GOOGLE_HOST = Regex("""^(?:[a-z0-9-]+\.)*google\.(?:com?|[a-z]{2,3})(?:\.[a-z]{2})?$""")
    private val COORDS = Regex("""^\s*(-?\d{1,3}(?:\.\d+)?)\s*,\s*(-?\d{1,3}(?:\.\d+)?)""")

    fun coordinatesOrNull(url: String): Pair<Double, Double>? {
        if (url.length > 4096) return null
        val afterScheme = url.substringAfter("://")
        val host = WebUrls.hostOf(afterScheme) ?: return null
        val isGoogleMaps = host == "maps.google.com" || (isGoogleHost(host) && afterScheme.substringAfter('/', "").startsWith("maps"))
        val isApple = host == "maps.apple.com"
        if (!isGoogleMaps && !isApple) return null
        val pathAndQuery = afterScheme.substringAfter('/', "")
        val path = pathAndQuery.substringBefore('?').substringBefore('#')
        val query = pathAndQuery.substringAfter('?', "").substringBefore('#')
        val params = FieldParsing.parseQuery(query, plusAsSpace = true)
        for (key in listOf("q", "query", "ll", "center")) {
            params[key]?.let { v -> parseCoords(v)?.let { return it } }
        }
        return pathCoordinates(path)
    }

    private fun isGoogleHost(host: String): Boolean = GOOGLE_HOST.matches(host)

    private fun pathCoordinates(path: String): Pair<Double, Double>? {
        val segments = path.split('/').filter { it.isNotEmpty() }
        val maps = segments.indexOf("maps")
        if (maps < 0) return null
        val next = segments.getOrNull(maps + 1) ?: return null
        return when {
            next.startsWith("@") -> parseCoords(next.substring(1))
            next == "search" -> segments.getOrNull(maps + 2)?.let { parseCoords(FieldParsing.percentDecode(it)) }
            else -> null
        }
    }

    /** The value must be *only* a coordinate pair (optionally followed by zoom/altitude), nothing else. */
    fun parseCoords(value: String): Pair<Double, Double>? {
        val m = COORDS.find(value) ?: return null
        val lat = m.groupValues[1].toDoubleOrNull() ?: return null
        val lng = m.groupValues[2].toDoubleOrNull() ?: return null
        val rest = value.substring(m.range.last + 1).trim()
        val tail = rest.removePrefix(",").trim()
        val tailOk = rest.isEmpty() || (rest.startsWith(",") && tail.all { it.isDigit() || it == '.' || it == ',' || it == 'z' || it == 'm' || it == '-' })
        return if (tailOk && lat in -90.0..90.0 && lng in -180.0..180.0) lat to lng else null
    }
}

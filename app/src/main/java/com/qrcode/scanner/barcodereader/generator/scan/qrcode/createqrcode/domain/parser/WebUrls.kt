package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.parser

/** URL recognition and normalisation without java.net.URL (no network, no exceptions). */
internal object WebUrls {
    private const val MAX_URL_LENGTH = 1_000_000

    /** Generic TLDs accepted for a bare `domain.tld` (a scheme-less, www-less payload). Two-letter TLDs are also accepted. */
    private val GENERIC_TLDS =
        setOf(
            "com",
            "net",
            "org",
            "edu",
            "gov",
            "mil",
            "int",
            "info",
            "biz",
            "name",
            "pro",
            "mobi",
            "asia",
            "tel",
            "travel",
            "app",
            "dev",
            "io",
            "ai",
            "xyz",
            "online",
            "site",
            "shop",
            "store",
            "tech",
            "blog",
            "cloud",
            "page",
            "link",
            "club",
            "live",
            "news",
            "media",
            "agency",
            "digital",
            "email",
            "network",
            "world",
            "life",
            "today",
            "space",
            "website",
            "top",
            "icu",
            "vip",
            "art",
            "design",
            "studio",
            "services",
            "solutions",
            "systems",
            "center",
            "company",
            "group",
            "global",
            "market",
            "money",
            "bank",
            "health",
            "care",
            "school",
            "academy",
            "games",
            "game",
            "zone",
            "social",
            "photo",
            "photos",
            "video",
            "film",
            "music",
            "food",
            "pizza",
            "coffee",
            "cafe",
            "bar",
            "pub",
            "fit",
            "run",
            "men",
            "wiki",
            "work",
            "works",
            "team",
            "tools",
            "tips",
            "guru",
            "help",
        )

    /** Two-letter words that are far more often file extensions than country domains. */
    private val FILE_LIKE_TLDS = setOf("md", "py", "sh", "rs", "pl", "ts", "js", "rb", "cs", "kt", "pm", "rc", "gz", "tx", "db", "pb", "ps")

    /** Returns an absolute http(s) URL for a payload that starts with a web scheme, else null. */
    fun httpUrlOrNull(text: String): String? {
        if (text.length > MAX_URL_LENGTH || text.any { it.isWhitespace() }) return null
        val schemeEnd =
            when {
                text.startsWith("https://", ignoreCase = true) -> 8
                text.startsWith("http://", ignoreCase = true) -> 7
                else -> return null
            }
        return if (hostOf(text.substring(schemeEnd)) != null) text else null
    }

    /**
     * Accepts an http(s) URL, a `www.` host or a bare `domain.tld[/path]` and returns an absolute URL.
     * The user's scheme is kept; `https://` is added when it is missing.
     */
    fun toWebsiteUrl(candidate: String): String? {
        val t = candidate.trim()
        httpUrlOrNull(t)?.let { return it }
        if (t.isEmpty() || t.length > 2048 || t.any { it.isWhitespace() }) return null
        if ("://" in t || t.contains('@')) return null
        val host = hostOf(t) ?: return null
        val isWww = t.startsWith("www.", ignoreCase = true)
        return if (isWww || looksLikeDomain(host, t)) "https://$t" else null
    }

    /** Extracts the lower-case host from `authority[/path]` text, or null when it is not a plausible host. */
    fun hostOf(afterScheme: String): String? {
        val end = afterScheme.indexOfFirst { it == '/' || it == '?' || it == '#' || it == '\\' }
        val authority = if (end < 0) afterScheme else afterScheme.substring(0, end)
        val hostPort = authority.substringAfterLast('@')
        val host = stripPort(hostPort)
        if (host.isEmpty() || host.startsWith(".") || host.contains("..")) return null
        val ok =
            host.all {
                it.isLetterOrDigit() || it == '-' || it == '.' || it == '_' || it == '%' || it == '[' || it == ']' || it == ':'
            }
        return if (ok) host.lowercase() else null
    }

    private fun stripPort(hostPort: String): String {
        if (hostPort.startsWith("[")) return hostPort.substringBefore(']') + "]"
        val colon = hostPort.lastIndexOf(':')
        if (colon < 0) return hostPort
        val port = hostPort.substring(colon + 1)
        return if (port.isEmpty() || port.all { it in '0'..'9' }) hostPort.substring(0, colon) else hostPort
    }

    private fun looksLikeDomain(host: String, full: String): Boolean {
        val labels = host.split('.')
        if (labels.size < 2) return false
        if (labels.any { it.isEmpty() || it.startsWith("-") || it.endsWith("-") }) return false
        val tld = labels.last()
        if (tld.length < 2 || tld.length > 24 || !tld.all { it in 'a'..'z' }) return false
        if (tld in GENERIC_TLDS) return true
        return tld.length == 2 && tld !in FILE_LIKE_TLDS && full.length > tld.length
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

/** A parsed URL, reduced to what the safety checks need. */
internal class UrlParts(
    /** Lower-case scheme, or null when the text had none (treated as https). */
    val scheme: String?,
    val host: String,
    val hasUserInfo: Boolean,
    val isBracketedIp: Boolean,
) {
    companion object {
        private val SCHEME = Regex("^([A-Za-z][A-Za-z0-9+.-]*):")

        fun parse(input: String): UrlParts {
            var scheme: String? = null
            var rest = input
            val m = SCHEME.find(input)
            if (m != null) {
                val name = m.groupValues[1].lowercase()
                val after = input.substring(m.range.last + 1)
                val slashes = after.startsWith("//") || after.startsWith("\\\\")
                val portLike = after.isNotEmpty() && after[0] in '0'..'9'
                if (slashes || name in BrandData.DANGEROUS_SCHEMES || !portLike) {
                    scheme = name
                    rest = after
                }
            }
            val authorityStart = rest.trimStart('/', '\\')
            val end = authorityStart.indexOfFirst { it == '/' || it == '?' || it == '#' || it == '\\' }
            val authority = if (end < 0) authorityStart else authorityStart.substring(0, end)
            val hostPort = authority.substringAfterLast('@')
            val bracketed = hostPort.startsWith("[")
            val host = if (bracketed) hostPort.substringBefore(']').removePrefix("[") else stripPort(hostPort)
            return UrlParts(scheme, host.lowercase().trimEnd('.'), '@' in authority, bracketed)
        }

        private fun stripPort(hostPort: String): String {
            val colon = hostPort.lastIndexOf(':')
            if (colon < 0) return hostPort
            val port = hostPort.substring(colon + 1)
            return if (port.all { it in '0'..'9' }) hostPort.substring(0, colon) else hostPort
        }
    }
}

/** A host split into labels, public suffix and registrable domain. */
internal class HostParts(val labels: List<String>) {
    /** Number of labels in the public suffix: 1 for `com`, 2 for `co.uk` or `github.io`. */
    val suffixLength: Int = suffixLengthOf(labels)
    val suffix: String = labels.takeLast(suffixLength).joinToString(".")
    private val sldIndex: Int = labels.size - suffixLength - 1

    /** The label just left of the public suffix (`google` in `mail.google.com`), or null for a bare suffix/single label. */
    val sld: String? = labels.getOrNull(sldIndex)
    val registrable: String? = sld?.let { "$it.$suffix" }
    val subLabels: List<String> = if (sldIndex > 0) labels.take(sldIndex) else emptyList()

    /** Label count where a multi-part public suffix counts once, so `a.b.example.co.uk` is 5. */
    val effectiveLabelCount: Int get() = labels.size - (suffixLength - 1)

    companion object {
        fun of(host: String): HostParts = HostParts(host.split('.').filter { it.isNotEmpty() })

        private fun suffixLengthOf(labels: List<String>): Int {
            for (k in minOf(3, labels.size - 1) downTo 2) {
                if (labels.takeLast(k).joinToString(".") in BrandData.MULTI_PART_SUFFIXES) return k
            }
            return 1
        }
    }
}

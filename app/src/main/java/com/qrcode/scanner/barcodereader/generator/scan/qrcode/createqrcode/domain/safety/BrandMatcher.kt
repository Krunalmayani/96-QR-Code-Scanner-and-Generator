package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

/** Lookalike detection: confusable characters plus Damerau (optimal string alignment) edit distance. */
internal object BrandMatcher {
    /** Returns the brands that [host] imitates, or an empty list when it is official or unrelated. */
    fun lookalikes(host: HostParts): List<String> {
        if (isOfficial(host)) return emptyList()
        val found = LinkedHashSet<String>()
        host.sld?.let { sld ->
            if (sld !in BrandData.BENIGN_LABELS) matchSld(sld, found)
        }
        for (label in host.subLabels) matchSubLabel(label, found)
        return found.toList()
    }

    fun isOfficial(host: HostParts): Boolean {
        val sld = host.sld ?: return false
        val registrable = host.registrable ?: return false
        if (host.suffix in BrandData.SHARED_HOSTING) return false
        if (registrable in BrandData.OFFICIAL_DOMAINS) return true
        val tld = host.labels.last()
        if (tld in BrandData.SUSPICIOUS_TLDS) return false
        return sld in BrandData.BRANDS || sld in BrandData.OFFICIAL_LABELS
    }

    private fun matchSld(sld: String, found: MutableSet<String>) {
        for (brand in BrandData.BRANDS) {
            if (closeTo(sld, brand) || hyphenToken(sld, brand) || containsBrand(sld, brand)) found.add(brand)
        }
    }

    private fun matchSubLabel(label: String, found: MutableSet<String>) {
        for (brand in BrandData.BRANDS) {
            val exact = brand.length >= 4 && variants(label).contains(brand)
            if (exact || hyphenToken(label, brand) || containsBrand(label, brand)) found.add(brand)
        }
    }

    /** Same word after undoing look-alike characters, or within the allowed number of typos. */
    private fun closeTo(label: String, brand: String): Boolean {
        if (kotlin.math.abs(label.length - brand.length) > 2) return false
        for (v in variants(label)) {
            if (v == brand) return true
            if (brand.length <= 3) continue
            val d = editDistance(v, brand)
            if (d <= maxEdits(brand.length) && v[0] == brand[0]) return true
        }
        return false
    }

    private fun maxEdits(brandLength: Int): Int = if (brandLength <= 6) 1 else 2

    /** `paypal-login.com`, `appleid-verify.com`: a brand name (5+ letters) as, or at the edge of, a hyphen separated token. */
    private fun hyphenToken(label: String, brand: String): Boolean {
        if (brand.length < 5 || '-' !in label) return false
        return label.split('-').any { token ->
            token.isNotEmpty() && variants(token).any { it == brand || it.startsWith(brand) || it.endsWith(brand) }
        }
    }

    /** `securepaypal.com`, `amazonprime.net`: a long brand name glued to other words. */
    private fun containsBrand(label: String, brand: String): Boolean =
        brand.length >= 6 && label.length > brand.length && label.contains(brand) && label !in BrandData.OFFICIAL_LABELS

    /** The label itself plus de-obfuscated forms: 0->o, 1->l/i, 3->e, 4->a, 5->s, rn->m, vv->w, hyphens removed. */
    fun variants(label: String): Set<String> {
        val out = LinkedHashSet<String>()
        out.add(label)
        val ones = if ('1' in label) listOf('l', 'i') else listOf('l')
        for (one in ones) {
            out.add(
                buildString {
                    for (c in label) {
                        append(
                            when (c) {
                                '0' -> 'o'
                                '1' -> one
                                '3' -> 'e'
                                '4' -> 'a'
                                '5' -> 's'
                                else -> c
                            },
                        )
                    }
                },
            )
        }
        for (v in out.toList()) {
            out.add(v.replace("rn", "m").replace("vv", "w"))
            out.add(v.replace("-", ""))
        }
        out.remove("")
        return out
    }

    /** Optimal string alignment distance: insert, delete, substitute and adjacent transposition each cost 1. */
    fun editDistance(a: String, b: String): Int {
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i
        for (j in 0..b.length) d[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var best = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) best = minOf(best, d[i - 2][j - 2] + 1)
                d[i][j] = best
            }
        }
        return d[a.length][b.length]
    }
}

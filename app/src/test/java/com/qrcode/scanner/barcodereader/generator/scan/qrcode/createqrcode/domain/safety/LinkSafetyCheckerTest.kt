package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class LinkSafetyCheckerTest {
    private val checker = LinkSafetyChecker()

    private fun risks(url: String) = checker.check(url).risks

    private inline fun <reified T : LinkRisk> has(url: String) = risks(url).any { it is T }

    private val safe =
        listOf(
            "https://www.google.com/search?q=cats",
            "https://mail.google.com",
            "https://docs.google.com/document/d/abc/edit",
            "https://accounts.google.com/signin",
            "https://www.amazon.co.uk/dp/B000",
            "https://www.amazon.com/s?k=123",
            "https://paypal.me/someone",
            "https://github.com/user/repo",
            "https://en.wikipedia.org/wiki/Kotlin",
            "https://www.apple.com/iphone",
            "https://apps.apple.com/app/id1",
            "https://login.microsoftonline.com",
            "https://www.facebook.com/page",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://x.com/user",
            "https://web.whatsapp.com",
            "https://www.bbc.co.uk/news",
            "https://news.ycombinator.com/item?id=1",
            "https://example.com",
            "https://stackoverflow.com/questions/1",
            "https://www.paypal.com/signin",
            "https://s3.amazonaws.com/bucket/file",
            "https://lh3.googleusercontent.com/a",
            "www.google.com",
            "https://localhost:8080/app",
            "https://creditkarma.com",
            "https://www.finance.yahoo.com",
        )

    @TestFactory
    fun `real sites are safe`(): List<DynamicTest> =
        safe.map { url ->
            dynamicTest(url) {
                val r = checker.check(url)
                assertEquals(emptyList<LinkRisk>(), r.risks, url)
                assertEquals(Level.SAFE, r.level)
                assertTrue(r.isSafe)
            }
        }

    private class Bad(
        val url: String,
        val level: Level,
        val expect: (List<LinkRisk>) -> Boolean,
    )

    private val bad =
        listOf(
            Bad("http://example.com", Level.CAUTION) { LinkRisk.NotHttps in it },
            Bad("https://paypa1.com/login", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "paypal" } },
            Bad("https://g00gle.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "google" } },
            Bad("https://arnazon.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "amazon" } },
            Bad("https://micr0soft.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "microsoft" } },
            Bad("https://faceb00k.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "facebook" } },
            Bad("https://gooogle.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "google" } },
            Bad("https://netfliix.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "netflix" } },
            Bad("https://linkedln.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "linkedin" } },
            Bad("https://paypall.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand } },
            Bad("https://paypal.com.evil.xyz/login", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "paypal" } },
            Bad(
                "https://www.google.com.attacker.net",
                Level.UNSAFE,
            ) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "google" } },
            Bad("https://paypal-secure-login.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "paypal" } },
            Bad("https://securepaypal.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "paypal" } },
            Bad(
                "https://paypal.tk",
                Level.UNSAFE,
            ) { r -> r.any { it is LinkRisk.LookalikeBrand } && r.any { it is LinkRisk.SuspiciousTld } },
            Bad("https://paypal.github.io/login", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand } },
            Bad("https://xn--pypal-4ve.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.Punycode } },
            Bad("https://pаypal.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.Punycode } },
            Bad("https://google.com@evil.com/", Level.UNSAFE) { r -> LinkRisk.UserInfoInUrl in r },
            Bad("https://user:pw@example.org", Level.UNSAFE) { r -> LinkRisk.UserInfoInUrl in r },
            Bad("javascript:alert(1)", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme && it.scheme == "javascript" } },
            Bad("data:text/html;base64,PHNjcmlwdD4=", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme } },
            Bad("file:///etc/passwd", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme } },
            Bad("intent://scan/#Intent;scheme=zxing;end", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme } },
            Bad("VBScript:msgbox", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme && it.scheme == "vbscript" } },
            Bad("http://192.168.0.1/admin", Level.UNSAFE) { r -> r.any { it is LinkRisk.IpAddressHost } && LinkRisk.NotHttps in r },
            Bad("https://192.168.0.1/admin", Level.CAUTION) { r -> r.any { it is LinkRisk.IpAddressHost } },
            Bad("https://[2001:db8::1]/", Level.CAUTION) { r -> r.any { it is LinkRisk.IpAddressHost } },
            Bad("https://3232235777/", Level.CAUTION) { r -> r.any { it is LinkRisk.IpAddressHost } },
            Bad("https://free-prizes.xyz", Level.CAUTION) { r -> r.any { it is LinkRisk.SuspiciousTld && it.tld == "xyz" } },
            Bad("https://update.zip/now", Level.CAUTION) { r -> r.any { it is LinkRisk.SuspiciousTld } },
            Bad("https://something.click", Level.CAUTION) { r -> r.any { it is LinkRisk.SuspiciousTld } },
            Bad("https://bit.ly/3abcDEF", Level.CAUTION) { r -> r.any { it is LinkRisk.UrlShortener } },
            Bad("https://tinyurl.com/y9", Level.CAUTION) { r -> r.any { it is LinkRisk.UrlShortener } },
            Bad("https://t.co/abc", Level.CAUTION) { r -> r.any { it is LinkRisk.UrlShortener } },
            Bad("https://www.rebrand.ly/x", Level.CAUTION) { r -> r.any { it is LinkRisk.UrlShortener } },
            Bad("https://a.b.c.d.example.com/", Level.CAUTION) { r -> r.any { it is LinkRisk.ExcessiveSubdomains } },
            Bad("https://example.com/" + "a".repeat(600), Level.CAUTION) { r -> r.any { it is LinkRisk.VeryLongUrl } },
            Bad("http://bit.ly/x", Level.CAUTION) { r -> LinkRisk.NotHttps in r && r.any { it is LinkRisk.UrlShortener } },
            Bad("HTTPS://PAYPAL.COM.EVIL.TOP", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand } },
            Bad("ftp://example.com/file", Level.UNSAFE) { r -> r.any { it is LinkRisk.NonWebScheme && it.scheme == "ftp" } },
            Bad("https://appleid-verify.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "apple" } },
            Bad("https://bankofarnerica.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "bankofamerica" } },
            Bad("https://wellsfarg0.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "wellsfargo" } },
            Bad("https://dh1.com", Level.UNSAFE) { r -> r.any { it is LinkRisk.LookalikeBrand && it.brand == "dhl" } },
        )

    @TestFactory
    fun `risky links are flagged`(): List<DynamicTest> =
        bad.map { b ->
            dynamicTest(b.url.take(70)) {
                val r = checker.check(b.url)
                assertTrue(b.expect(r.risks), "risks were ${r.risks}")
                assertEquals(b.level, r.level, "risks were ${r.risks}")
            }
        }

    @Test
    fun `table has at least thirty cases both ways`() {
        assertTrue(safe.size >= 15 && bad.size >= 15 && safe.size + bad.size >= 30)
    }

    @Test
    fun `bare host without scheme is not flagged as http`() {
        assertTrue(!has<LinkRisk.NotHttps>("example.com/path"))
        assertTrue(has<LinkRisk.UrlShortener>("bit.ly/abc"))
    }

    @Test
    fun `host with port and userinfo in path is parsed correctly`() {
        assertEquals(Level.SAFE, checker.check("https://example.com:8443/a@b.com").level)
        assertTrue(has<LinkRisk.IpAddressHost>("http://10.0.0.1:8080"))
    }

    @Test
    fun `blank input is safe and whitespace is trimmed`() {
        assertEquals(LinkSafetyResult(Level.SAFE, emptyList()), checker.check("   "))
        assertEquals(Level.SAFE, checker.check("  https://example.com \n").level)
    }

    @Test
    fun `host without a name does not crash`() {
        assertEquals(listOf<LinkRisk>(LinkRisk.NotHttps), checker.check("http://").risks)
        assertEquals(Level.SAFE, checker.check("https:///").level)
    }

    @Test
    fun `two level public suffix counts as one label`() {
        assertTrue(!has<LinkRisk.ExcessiveSubdomains>("https://a.b.example.co.uk"))
        assertTrue(has<LinkRisk.ExcessiveSubdomains>("https://a.b.c.example.co.uk"))
    }

    @Test
    fun `severity flags`() {
        assertTrue(LinkRisk.UserInfoInUrl.severe)
        assertTrue(!LinkRisk.NotHttps.severe)
        assertTrue(!LinkRisk.UrlShortener("x").severe)
        assertTrue(!LinkRisk.ExcessiveSubdomains(5).severe)
        assertTrue(!LinkRisk.VeryLongUrl(900).severe)
        assertTrue(!LinkRisk.SuspiciousTld("xyz").severe)
        assertTrue(!LinkRisk.IpAddressHost("1.1.1.1").severe)
        assertTrue(LinkRisk.Punycode("x").severe)
    }

    @Test
    fun `edit distance basics`() {
        assertEquals(0, BrandMatcher.editDistance("google", "google"))
        assertEquals(1, BrandMatcher.editDistance("gogle", "google"))
        assertEquals(1, BrandMatcher.editDistance("googel", "google"))
        assertEquals(2, BrandMatcher.editDistance("gogel", "google"))
        assertEquals(3, BrandMatcher.editDistance("abc", "xyz"))
    }

    @Test
    fun `brand list is large enough`() {
        assertTrue(BrandData.BRANDS.size >= 40)
        assertTrue(BrandData.SUSPICIOUS_TLDS.size >= 20)
        assertTrue(BrandData.SHORTENERS.size >= 10)
    }
}

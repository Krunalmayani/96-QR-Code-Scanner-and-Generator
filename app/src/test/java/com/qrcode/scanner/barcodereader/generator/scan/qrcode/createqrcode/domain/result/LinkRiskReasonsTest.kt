package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.Level
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkRisk
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyChecker
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.safety.LinkSafetyResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LinkRiskReasonsTest {
    private val checker = LinkSafetyChecker()

    @Test
    fun `a safe link has no reasons`() {
        assertEquals(emptyList<RiskReason>(), LinkRiskReasons.of(checker.check("https://example.com")))
    }

    @Test
    fun `the s69 example lists not secure and look-alike`() {
        val kinds = LinkRiskReasons.of(checker.check("http://paypa1.com/login")).map { it.kind }
        assertTrue(RiskKind.NOT_HTTPS in kinds)
        assertTrue(RiskKind.LOOKALIKE_BRAND in kinds)
    }

    @Test
    fun `severe reasons come first`() {
        val result = LinkSafetyResult(Level.UNSAFE, listOf(LinkRisk.NotHttps, LinkRisk.UserInfoInUrl, LinkRisk.UrlShortener("bit.ly")))
        val reasons = LinkRiskReasons.of(result)
        assertEquals(listOf(RiskKind.USER_INFO, RiskKind.NOT_HTTPS, RiskKind.URL_SHORTENER), reasons.map { it.kind })
        assertTrue(reasons.first().severe)
    }

    @Test
    fun `every risk type maps to a kind with its detail`() {
        assertEquals(
            RiskReason(RiskKind.LOOKALIKE_BRAND, "paypal", true),
            LinkRiskReasons.reasonOf(LinkRisk.LookalikeBrand("paypal", "paypa1.com")),
        )
        assertEquals(RiskReason(RiskKind.PUNYCODE, "xn--a.com", true), LinkRiskReasons.reasonOf(LinkRisk.Punycode("xn--a.com")))
        assertEquals(RiskReason(RiskKind.SUSPICIOUS_TLD, "tk", false), LinkRiskReasons.reasonOf(LinkRisk.SuspiciousTld("tk")))
        assertEquals(RiskReason(RiskKind.IP_ADDRESS, "10.0.0.1", false), LinkRiskReasons.reasonOf(LinkRisk.IpAddressHost("10.0.0.1")))
        assertEquals(RiskReason(RiskKind.EXCESSIVE_SUBDOMAINS, "6", false), LinkRiskReasons.reasonOf(LinkRisk.ExcessiveSubdomains(6)))
        assertEquals(RiskReason(RiskKind.NON_WEB_SCHEME, "javascript", true), LinkRiskReasons.reasonOf(LinkRisk.NonWebScheme("javascript")))
        assertEquals(RiskReason(RiskKind.VERY_LONG_URL, "800", false), LinkRiskReasons.reasonOf(LinkRisk.VeryLongUrl(800)))
    }

    @Test
    fun `duplicates collapse and the list is capped`() {
        val risks =
            List(3) { LinkRisk.LookalikeBrand("google", "g00gle.com") } +
                listOf(
                    LinkRisk.NotHttps,
                    LinkRisk.UrlShortener("bit.ly"),
                    LinkRisk.SuspiciousTld("tk"),
                    LinkRisk.ExcessiveSubdomains(7),
                    LinkRisk.VeryLongUrl(900),
                )
        val reasons = LinkRiskReasons.of(LinkSafetyResult(Level.UNSAFE, risks))
        assertEquals(LinkRiskReasons.MAX_REASONS, reasons.size)
        assertEquals(1, reasons.count { it.kind == RiskKind.LOOKALIKE_BRAND })
    }

    @Test
    fun `secure and web url checks`() {
        assertTrue(LinkRiskReasons.isSecure("https://a.com"))
        assertTrue(LinkRiskReasons.isSecure("  HTTPS://a.com"))
        assertFalse(LinkRiskReasons.isSecure("http://a.com"))
        assertTrue(LinkRiskReasons.isWebUrl("http://a.com"))
        assertFalse(LinkRiskReasons.isWebUrl("javascript:alert(1)"))
        assertFalse(LinkRiskReasons.isWebUrl("intent://x#Intent;end"))
    }
}

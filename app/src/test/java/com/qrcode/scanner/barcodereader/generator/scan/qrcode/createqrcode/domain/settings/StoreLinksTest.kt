package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StoreLinksTest {
    @Test
    fun `links use the app id`() {
        assertEquals("market://details?id=com.example.app", StoreLinks.marketUrl("com.example.app"))
        assertEquals("https://play.google.com/store/apps/details?id=com.example.app", StoreLinks.webUrl("com.example.app"))
    }

    @Test
    fun `debug suffix is removed so the link points at the release listing`() {
        assertEquals("com.example.app", StoreLinks.releaseAppId("com.example.app.debug"))
        assertEquals("https://play.google.com/store/apps/details?id=com.example.app", StoreLinks.webUrl("com.example.app.debug"))
    }
}

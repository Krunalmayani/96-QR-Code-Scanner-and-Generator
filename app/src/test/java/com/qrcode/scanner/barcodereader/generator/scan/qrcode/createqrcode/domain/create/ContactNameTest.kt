package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContactNameTest {
    @Test fun joinsWithOneSpace() {
        assertEquals("Sophie Lambert", ContactName.join("Sophie", "Lambert"))
        assertEquals("Sophie Lambert", ContactName.join("  Sophie ", " Lambert  "))
    }

    @Test fun missingPartLeavesNoStraySpace() {
        assertEquals("Sophie", ContactName.join("Sophie", ""))
        assertEquals("Lambert", ContactName.join("", "Lambert"))
        assertEquals("", ContactName.join(" ", ""))
    }

    @Test fun splitsAtTheFirstSpace() {
        assertEquals("Sophie", ContactName.firstOf("Sophie Lambert"))
        assertEquals("Lambert", ContactName.lastOf("Sophie Lambert"))
        assertEquals("van der Berg", ContactName.lastOf("Jan van der Berg"))
    }

    @Test fun singleWordIsAFirstName() {
        assertEquals("Sophie", ContactName.firstOf("Sophie"))
        assertEquals("", ContactName.lastOf("Sophie"))
        assertEquals("", ContactName.firstOf(""))
        assertEquals("", ContactName.lastOf(""))
    }

    @Test fun splitAndJoinRoundTrip() {
        val name = "Ana María García"
        assertEquals(name, ContactName.join(ContactName.firstOf(name), ContactName.lastOf(name)))
    }
}

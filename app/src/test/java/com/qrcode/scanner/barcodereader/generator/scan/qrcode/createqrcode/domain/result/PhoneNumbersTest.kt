package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhoneNumbersTest {
    @Test
    fun `formatting characters are removed and the leading plus is kept`() {
        assertEquals("+12065550198", PhoneNumbers.dialable("+1 (206) 555-0198"))
        assertEquals("2065550198", PhoneNumbers.dialable("(206) 555.0198"))
    }

    @Test
    fun `a plus that is not leading is dropped`() {
        assertEquals("1555", PhoneNumbers.dialable("1+555"))
        assertEquals("+1555", PhoneNumbers.dialable("(+1) 555"))
    }

    @Test
    fun `dialer symbols are kept`() {
        assertEquals("*21*123#", PhoneNumbers.dialable("*21*123#"))
        assertEquals("555,123;4", PhoneNumbers.dialable("555,123;4"))
    }

    @Test
    fun `letters and whitespace are removed`() {
        assertEquals("5551234", PhoneNumbers.dialable(" 555 abc 1234\n"))
    }

    @Test
    fun `isDialable needs at least one digit`() {
        assertTrue(PhoneNumbers.isDialable("+1 555"))
        assertFalse(PhoneNumbers.isDialable("+-()"))
        assertFalse(PhoneNumbers.isDialable("#*"))
        assertFalse(PhoneNumbers.isDialable(""))
    }

    @Test
    fun `tel uri percent-encodes the hash sign`() {
        assertEquals("tel:+12065550198", PhoneNumbers.telUri("+1 (206) 555-0198"))
        assertEquals("tel:*21*123%23", PhoneNumbers.telUri("*21*123#"))
    }

    @Test
    fun `sms uri uses the smsto scheme`() {
        assertEquals("smsto:+447700900123", PhoneNumbers.smsUri("+44 7700 900123"))
    }
}

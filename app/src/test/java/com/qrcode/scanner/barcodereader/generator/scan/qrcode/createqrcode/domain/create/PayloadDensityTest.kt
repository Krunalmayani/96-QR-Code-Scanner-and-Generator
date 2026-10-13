package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayloadDensityTest {
    @Test fun shortTextIsLow() {
        assertEquals(DensityLevel.LOW, PayloadDensity.of("hello"))
        assertEquals(DensityLevel.LOW, PayloadDensity.of(""))
    }

    @Test fun longerTextIsMediumThenHigh() {
        assertEquals(DensityLevel.MEDIUM, PayloadDensity.of("Meeting notes: ship v2.4 on Friday. Update the release checklist, notify QA."))
        assertEquals(DensityLevel.HIGH, PayloadDensity.of("x".repeat(600)))
    }

    @Test fun tooLongToEncodeIsHigh() {
        assertEquals(DensityLevel.HIGH, PayloadDensity.of("x".repeat(5000)))
    }

    @Test fun densityNeverDecreasesWithLength() {
        var last = DensityLevel.LOW
        for (n in listOf(5, 30, 80, 150, 300, 700, 1200)) {
            val level = PayloadDensity.of("a".repeat(n))
            assertEquals(true, level.ordinal >= last.ordinal, "n=$n")
            last = level
        }
    }
}

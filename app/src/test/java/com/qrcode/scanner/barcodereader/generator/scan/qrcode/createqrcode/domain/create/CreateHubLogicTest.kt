package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.CodeFormat
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CreateHubLogicTest {
    private val labels = mapOf(
        CreateType.WEBSITE to "Website",
        CreateType.WIFI to "Wi-Fi",
        CreateType.TEXT to "Text",
        CreateType.SOCIAL to "Social",
        CreateType.BARCODE_EAN_13 to "EAN-13",
        CreateType.BARCODE_CODE_128 to "Code 128",
    )
    private val label: (CreateType) -> String = { labels.getValue(it) }
    private val all = labels.keys.toList()

    @Test fun blankQueryKeepsEverything() {
        assertEquals(all, CreateHubLogic.filter(all, "", label))
        assertEquals(all, CreateHubLogic.filter(all, "   ", label))
    }

    @Test fun matchesIgnoreCaseSpacesAndPunctuation() {
        assertEquals(listOf(CreateType.WIFI), CreateHubLogic.filter(all, "wifi", label))
        assertEquals(listOf(CreateType.WIFI), CreateHubLogic.filter(all, "WI-FI", label))
        assertEquals(listOf(CreateType.BARCODE_EAN_13), CreateHubLogic.filter(all, "ean13", label))
        assertEquals(listOf(CreateType.BARCODE_CODE_128), CreateHubLogic.filter(all, "code 128", label))
    }

    @Test fun matchesAnywhereInTheLabel() {
        assertEquals(listOf(CreateType.TEXT, CreateType.BARCODE_CODE_128), CreateHubLogic.filter(all, "e", label).filter { it == CreateType.TEXT || it == CreateType.BARCODE_CODE_128 })
        assertEquals(listOf(CreateType.WEBSITE), CreateHubLogic.filter(all, "site", label))
    }

    @Test fun noMatchGivesEmpty() {
        assertEquals(emptyList<CreateType>(), CreateHubLogic.filter(all, "zzz", label))
    }

    private fun record(content: ScanContent, source: ScanSource = ScanSource.CREATED) =
        ScanRecord(content = content, createdAt = 1L, source = source)

    @Test fun typeOfMapsContentToCreateTypes() {
        val q = CodeFormat.QR_CODE
        assertEquals(CreateType.WEBSITE, CreateHubLogic.typeOf(record(ScanContent.Website("https://a.io", q, "https://a.io"))))
        assertEquals(CreateType.TEXT, CreateHubLogic.typeOf(record(ScanContent.Text("hi", q))))
        assertEquals(CreateType.WIFI, CreateHubLogic.typeOf(record(ScanContent.Wifi("WIFI:", q, "x"))))
        assertEquals(CreateType.PHONE, CreateHubLogic.typeOf(record(ScanContent.Phone("tel:1", q, "1"))))
        assertEquals(CreateType.EVENT, CreateHubLogic.typeOf(record(ScanContent.Event("e", q))))
        assertNull(CreateHubLogic.typeOf(record(ScanContent.Unknown("", q))))
    }

    @Test fun barcodesMapByFormat() {
        assertEquals(CreateType.BARCODE_EAN_13, CreateHubLogic.typeOf(record(ScanContent.Product("5901234123457", CodeFormat.EAN_13, "5901234123457"))))
        assertEquals(CreateType.BARCODE_CODE_128, CreateHubLogic.typeOf(record(ScanContent.Text("ab", CodeFormat.CODE_128))))
        assertNull(CreateHubLogic.typeOf(record(ScanContent.Text("ab", CodeFormat.CODABAR))))
    }

    @Test fun recentIsDistinctNewestFirstCreatedOnlyAndLimited() {
        val q = CodeFormat.QR_CODE
        val records = listOf(
            record(ScanContent.Website("https://a.io", q, "https://a.io")),
            record(ScanContent.Website("https://b.io", q, "https://b.io")),
            record(ScanContent.Text("scanned", q), source = ScanSource.CAMERA),
            record(ScanContent.Wifi("WIFI:", q, "x")),
            record(ScanContent.Unknown("", q)),
            record(ScanContent.Phone("tel:1", q, "1")),
            record(ScanContent.Text("late", q)),
        )
        assertEquals(listOf(CreateType.WEBSITE, CreateType.WIFI, CreateType.PHONE), CreateHubLogic.recent(records))
        assertEquals(listOf(CreateType.WEBSITE), CreateHubLogic.recent(records, limit = 1))
        assertEquals(emptyList<CreateType>(), CreateHubLogic.recent(emptyList()))
    }
}

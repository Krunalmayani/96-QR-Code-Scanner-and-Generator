package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.data.settings

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductSiteOrderTest {
    private val all = listOf(ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS, ProductSite.AMAZON)

    @Test
    fun `null or blank gives the default order`() {
        assertEquals(all, parseProductSiteOrder(null))
        assertEquals(all, parseProductSiteOrder(""))
    }

    @Test
    fun `unknown names and duplicates are dropped and missing sites appended`() {
        assertEquals(
            listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE, ProductSite.AMAZON),
            parseProductSiteOrder("OPEN_FOOD_FACTS,NOPE,OPEN_FOOD_FACTS, GOOGLE"),
        )
    }

    @Test
    fun `format and parse round trip`() {
        val order = listOf(ProductSite.AMAZON, ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE)
        assertEquals("AMAZON,OPEN_FOOD_FACTS,GOOGLE", formatProductSiteOrder(order))
        assertEquals(order, parseProductSiteOrder(formatProductSiteOrder(order)))
    }

    @Test
    fun `parseThemeMode returns null for unknown values`() {
        assertEquals(null, parseThemeMode("NEON"))
        assertEquals(null, parseThemeMode(null))
    }
}

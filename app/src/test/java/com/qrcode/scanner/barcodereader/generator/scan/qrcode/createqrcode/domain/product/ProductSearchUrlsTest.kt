package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.product

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductSearchUrlsTest {
    @Test
    fun `each site has its url`() {
        assertEquals("https://www.google.com/search?q=5901234123457", ProductSearchUrls.urlFor(ProductSite.GOOGLE, "5901234123457"))
        assertEquals("https://world.openfoodfacts.org/product/5901234123457", ProductSearchUrls.urlFor(ProductSite.OPEN_FOOD_FACTS, "5901234123457"))
        assertEquals("https://www.amazon.com/s?k=5901234123457", ProductSearchUrls.urlFor(ProductSite.AMAZON, "5901234123457"))
    }

    @Test
    fun `only digits are sent`() {
        assertEquals("https://www.google.com/search?q=123456", ProductSearchUrls.urlFor(ProductSite.GOOGLE, " 123 456&x=y "))
    }

    @Test
    fun `ordered follows the preference order`() {
        val result = ProductSearchUrls.ordered(listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS), "1234567")
        assertEquals(listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS), result.map { it.first })
        assertEquals("https://www.amazon.com/s?k=1234567", result.first().second)
    }

    @Test
    fun `ordered drops duplicates and appends missing sites`() {
        val result = ProductSearchUrls.ordered(listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.OPEN_FOOD_FACTS), "1234567")
        assertEquals(listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE, ProductSite.AMAZON), result.map { it.first })
    }

    @Test
    fun `empty order gives the default three sites`() {
        assertEquals(3, ProductSearchUrls.ordered(emptyList(), "1234567").size)
    }
}

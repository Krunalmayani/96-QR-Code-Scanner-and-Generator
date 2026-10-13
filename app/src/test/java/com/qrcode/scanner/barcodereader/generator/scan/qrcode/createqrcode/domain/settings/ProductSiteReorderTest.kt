package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProductSiteReorderTest {
    private val default = listOf(ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS, ProductSite.AMAZON)

    @Test
    fun `move down swaps with the next site`() {
        assertEquals(
            listOf(ProductSite.OPEN_FOOD_FACTS, ProductSite.GOOGLE, ProductSite.AMAZON),
            ProductSiteOrder.moveDown(default, ProductSite.GOOGLE),
        )
    }

    @Test
    fun `move up swaps with the previous site`() {
        assertEquals(
            listOf(ProductSite.GOOGLE, ProductSite.AMAZON, ProductSite.OPEN_FOOD_FACTS),
            ProductSiteOrder.moveUp(default, ProductSite.AMAZON),
        )
    }

    @Test
    fun `moving past either end changes nothing`() {
        assertEquals(default, ProductSiteOrder.moveUp(default, ProductSite.GOOGLE))
        assertEquals(default, ProductSiteOrder.moveDown(default, ProductSite.AMAZON))
    }

    @Test
    fun `a site missing from the list is ignored`() {
        val partial = listOf(ProductSite.GOOGLE)
        assertEquals(partial, ProductSiteOrder.moveDown(partial, ProductSite.AMAZON))
    }

    @Test
    fun `can move flags match the ends of the list`() {
        assertFalse(ProductSiteOrder.canMoveUp(default, ProductSite.GOOGLE))
        assertTrue(ProductSiteOrder.canMoveDown(default, ProductSite.GOOGLE))
        assertTrue(ProductSiteOrder.canMoveUp(default, ProductSite.AMAZON))
        assertFalse(ProductSiteOrder.canMoveDown(default, ProductSite.AMAZON))
    }

    @Test
    fun `normalize drops duplicates and appends missing sites`() {
        assertEquals(
            listOf(ProductSite.AMAZON, ProductSite.GOOGLE, ProductSite.OPEN_FOOD_FACTS),
            ProductSiteOrder.normalize(listOf(ProductSite.AMAZON, ProductSite.AMAZON, ProductSite.GOOGLE)),
        )
        assertEquals(default, ProductSiteOrder.normalize(emptyList()))
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite

/** Reorder rules for the product-search site list (up/down buttons in Settings). */
object ProductSiteOrder {
    /** Removes duplicates and appends any site that is missing, so every site appears exactly once. */
    fun normalize(order: List<ProductSite>): List<ProductSite> =
        (order.distinct() + ProductSite.entries).distinct()

    fun canMoveUp(order: List<ProductSite>, site: ProductSite): Boolean = order.indexOf(site) > 0

    fun canMoveDown(order: List<ProductSite>, site: ProductSite): Boolean {
        val index = order.indexOf(site)
        return index in 0 until order.lastIndex
    }

    fun moveUp(order: List<ProductSite>, site: ProductSite): List<ProductSite> = move(order, site, -1)

    fun moveDown(order: List<ProductSite>, site: ProductSite): List<ProductSite> = move(order, site, 1)

    private fun move(order: List<ProductSite>, site: ProductSite, delta: Int): List<ProductSite> {
        val from = order.indexOf(site)
        val to = from + delta
        if (from < 0 || to !in order.indices) return order
        val result = order.toMutableList()
        result[from] = order[to]
        result[to] = site
        return result
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.product

import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite

/** Builds the "Search on" links of the product result screen. Only the digits of the barcode are ever sent. */
object ProductSearchUrls {
    fun urlFor(site: ProductSite, barcode: String): String {
        val digits = barcode.filter { it in '0'..'9' }
        return when (site) {
            ProductSite.GOOGLE -> "https://www.google.com/search?q=$digits"
            ProductSite.OPEN_FOOD_FACTS -> "https://world.openfoodfacts.org/product/$digits"
            ProductSite.AMAZON -> "https://www.amazon.com/s?k=$digits"
        }
    }

    /** One entry per site, in the user's preferred [order]; duplicates are dropped and missing sites are appended. */
    fun ordered(order: List<ProductSite>, barcode: String): List<Pair<ProductSite, String>> =
        (order + ProductSite.values()).distinct().map { it to urlFor(it, barcode) }
}

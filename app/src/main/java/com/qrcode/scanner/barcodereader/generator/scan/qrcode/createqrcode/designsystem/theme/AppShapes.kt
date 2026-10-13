package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Corner radii: 12 chips-in-tiles, 16 fields, 20 cards, 24 pills/buttons, 28 sheets and dialogs. */
object AppShapes {
    val small = RoundedCornerShape(12.dp)
    val field = RoundedCornerShape(16.dp)
    val tile = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(24.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val dialog = RoundedCornerShape(28.dp)
}

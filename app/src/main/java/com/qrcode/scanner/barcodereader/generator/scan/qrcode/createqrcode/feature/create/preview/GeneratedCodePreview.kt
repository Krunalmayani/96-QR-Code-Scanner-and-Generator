package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CodeImageFactory
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.QrStyle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

/**
 * Renders the code of a created [record] (content, format and saved style) on a white card with the shared Compose
 * renderer. The card keeps the code's aspect ratio (square for QR codes, wide for 1D barcodes); the caller only
 * decides the width. Used by the Generated screen and by Result / History detail.
 */
@Composable
fun GeneratedCodePreview(record: ScanRecord, modifier: Modifier = Modifier) {
    val style = record.style ?: QrStyle()
    val image =
        remember(record.rawValue, record.format, style) {
            CodeImageFactory(record.format, record.rawValue).imageOrNull(style)
        }
    val logo = rememberLogoBitmap(style.logoUri)
    CodeCard(
        image = image,
        logo = logo,
        contentDescription = stringResource(R.string.create_preview_description, record.format.displayName),
        modifier = modifier,
    )
}

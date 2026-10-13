package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.QrVectorImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A printed code always sits on white paper, whatever the app theme: the card stays white-ish in dark mode too. */
private val PaperColor = Color(0xFFFFFFFF)

/** Draws [image] with the shared renderer, keeping the picture's own aspect ratio inside the available space. */
@Composable
fun VectorCodeImage(
    image: QrVectorImage,
    logo: ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.aspectRatio(image.width / image.height)) {
        drawVectorImage(image, logo)
    }
}

/**
 * White card holding a code. [image] null (the content cannot be encoded) shows a neutral placeholder of the same
 * square shape. The card's size follows the code's aspect ratio, so it never stretches.
 */
@Composable
fun CodeCard(
    image: QrVectorImage?,
    logo: ImageBitmap?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.card
    Box(
        modifier = modifier
            .clip(shape)
            .background(PaperColor)
            .border(1.dp, AppTheme.colors.outline, shape)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            VectorCodeImage(
                image = image,
                logo = logo,
                modifier = Modifier.semantics { this.contentDescription = contentDescription },
            )
        } else {
            Box(Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
                Icon(
                    AppIcons.QrCode,
                    contentDescription = contentDescription,
                    tint = AppTheme.colors.inkSecondary,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

/** Decodes the center logo off the main thread; null while loading, when [uri] is null or when the file is unreadable. */
@Composable
fun rememberLogoBitmap(uri: String?): ImageBitmap? {
    val loaded by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) {
            null
        } else {
            withContext(Dispatchers.IO) { LogoDecoder.decode(uri)?.asImageBitmap() }
        }
    }
    return if (uri == null) null else loaded
}

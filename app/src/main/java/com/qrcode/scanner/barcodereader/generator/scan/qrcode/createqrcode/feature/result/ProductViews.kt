package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SmallButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ProductSite
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.BarcodeNumber

/** s24: the barcode number and its format, with the external-websites disclaimer. */
@Composable
internal fun ProductContent(content: ScanContent.Product, record: ScanRecord) {
    val grouped = remember(content.number, content.format) { BarcodeNumber.group(content.number, content.format) }
    val scannedAt = rememberWhenText(record.createdAt)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_product_barcode), Modifier.fillMaxWidth())
            SelectionContainer {
                Text(
                    grouped,
                    style = AppTheme.type.mono.copy(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.ink,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        DetailsCard(
            listOf(
                DetailLine(stringResource(R.string.result_label_format), content.format.displayName),
                DetailLine(stringResource(R.string.result_product_number), content.number, mono = true),
                DetailLine(stringResource(R.string.result_product_scanned), scannedAt),
            ),
        )
        NoteBox(stringResource(R.string.result_product_disclaimer))
    }
}

/** s30 (online) and s70 (offline): the "Search on" list in the user's preferred site order. */
@Composable
internal fun ProductSearchContent(
    content: ScanContent.Product,
    product: ProductUiState,
    onEvent: (ResultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(content.number, content.format) { BarcodeNumber.group(content.number, content.format) }
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppCard(Modifier.fillMaxWidth()) {
            OverlineLabel(stringResource(R.string.result_product_barcode))
            Text(grouped, style = AppTheme.type.titleLarge, color = AppTheme.colors.ink, modifier = Modifier.padding(top = 4.dp))
            if (!product.online) {
                Text(
                    content.format.displayName,
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.inkSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (product.online) {
            SiteList(product.sites, onEvent)
            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.result_product_search_note),
                    style = AppTheme.type.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        } else {
            OfflineCard(onRetry = { onEvent(ResultEvent.ProductRetry) })
            AppOutlinedButton(
                text = stringResource(R.string.result_action_copy_number),
                onClick = { onEvent(ResultEvent.CopyClicked) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = AppIcons.Copy,
            )
        }
    }
}

@Composable
private fun SiteList(sites: List<ProductSiteLink>, onEvent: (ResultEvent) -> Unit) {
    SectionLabel(stringResource(R.string.result_product_search_on))
    CardGroup {
        sites.forEachIndexed { index, link ->
            if (index > 0) AppDivider()
            ListRow(
                title = stringResource(link.site.nameRes()),
                subtitle = stringResource(link.site.hintRes()),
                leading = { IconTile(link.site.icon()) },
                trailing = { Icon(AppIcons.Chevron, contentDescription = null, tint = AppTheme.colors.inkSecondary) },
                onClick = { onEvent(ResultEvent.OpenProductSite(link.site)) },
            )
        }
    }
}

@Composable
private fun OfflineCard(onRetry: () -> Unit) {
    val c = AppTheme.colors
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Wifi, contentDescription = null, tint = c.onPrimaryContainer, modifier = Modifier.size(32.dp))
            }
            Text(
                stringResource(R.string.result_product_offline_title),
                style = AppTheme.type.titleLarge,
                color = c.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                stringResource(R.string.result_product_offline_body),
                style = AppTheme.type.body,
                color = c.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            SmallButton(
                text = stringResource(R.string.common_retry),
                onClick = onRetry,
                modifier = Modifier.padding(top = 20.dp),
                tonal = false,
            )
        }
    }
}

private fun ProductSite.nameRes(): Int =
    when (this) {
        ProductSite.GOOGLE -> R.string.result_product_google
        ProductSite.OPEN_FOOD_FACTS -> R.string.result_product_off
        ProductSite.AMAZON -> R.string.result_product_amazon
    }

private fun ProductSite.hintRes(): Int =
    when (this) {
        ProductSite.GOOGLE -> R.string.result_product_google_hint
        ProductSite.OPEN_FOOD_FACTS -> R.string.result_product_off_hint
        ProductSite.AMAZON -> R.string.result_product_amazon_hint
    }

private fun ProductSite.icon() =
    when (this) {
        ProductSite.GOOGLE -> AppIcons.Search
        ProductSite.OPEN_FOOD_FACTS -> AppIcons.Product
        ProductSite.AMAZON -> AppIcons.Link
    }

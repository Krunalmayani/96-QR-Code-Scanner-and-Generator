package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSearchBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateHubLogic
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.create.CreateType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.icon
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.labelRes
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.tone

private const val GRID_COLUMNS = 3
private val EmptyStateMinHeight = 320.dp

/** Extra space above a section label: with the 10dp grid gap and the label's own 8dp it makes the artboard's 20dp. */
private val SectionGap = 2.dp

@Composable
fun CreateHubRoute(
    onOpenType: (CreateType) -> Unit,
    viewModel: CreateHubViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateHubScreen(state = state, onEvent = viewModel::onEvent, onOpenType = onOpenType)
}

/** Create hub (design s31, dark s75): search, recent types and the grid of everything that can be created. */
@Composable
fun CreateHubScreen(
    state: CreateHubUiState,
    onEvent: (CreateHubEvent) -> Unit,
    onOpenType: (CreateType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = CreateType.entries.associateWith { stringResource(it.labelRes()) }
    val labelOf: (CreateType) -> String = { labels.getValue(it) }
    val allTypes = CreateType.hubOrder
    val barcodes = CreateType.barcodeTypes
    val visibleTypes = CreateHubLogic.filter(allTypes, state.query, labelOf)
    val visibleBarcodes = CreateHubLogic.filter(barcodes, state.query, labelOf)
    val searching = state.query.isNotBlank()

    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.create_hub_title)) },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = LocalBottomBarInset.current + 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            fullWidth {
                AppSearchBar(
                    query = state.query,
                    onQueryChange = { onEvent(CreateHubEvent.QueryChanged(it)) },
                    placeholder = stringResource(R.string.create_hub_search),
                )
            }
            if (!searching && state.recent.isNotEmpty()) {
                fullWidth { RecentRow(state.recent, labelOf, onOpenType) }
            }
            if (visibleTypes.isNotEmpty()) {
                fullWidth { SectionLabel(stringResource(R.string.create_hub_all_types), Modifier.padding(top = SectionGap)) }
                typeTiles(visibleTypes, labelOf, onOpenType)
            }
            if (visibleBarcodes.isNotEmpty()) {
                fullWidth { SectionLabel(stringResource(R.string.create_hub_barcodes), Modifier.padding(top = SectionGap)) }
                typeTiles(visibleBarcodes, labelOf, onOpenType)
            }
            if (visibleTypes.isEmpty() && visibleBarcodes.isEmpty()) {
                fullWidth {
                    EmptyState(
                        icon = AppIcons.Search,
                        title = stringResource(R.string.create_hub_no_results),
                        message = stringResource(R.string.create_hub_no_results_hint),
                        modifier = Modifier.heightIn(min = EmptyStateMinHeight),
                    )
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { content() }
}

private fun LazyGridScope.typeTiles(
    types: List<CreateType>,
    labelOf: (CreateType) -> String,
    onOpenType: (CreateType) -> Unit,
) {
    items(types, key = { it.name }) { type ->
        TypeTile(label = labelOf(type), type = type, onClick = { onOpenType(type) })
    }
}

@Composable
private fun RecentRow(
    recent: List<CreateType>,
    labelOf: (CreateType) -> String,
    onOpenType: (CreateType) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.create_hub_recent))
        ChipRow(
            chips = recent.map { ChipItem(labelOf(it), selected = false, icon = it.icon()) },
            onChipClick = { index -> recent.getOrNull(index)?.let(onOpenType) },
            contentPadding = PaddingValues(0.dp),
        )
    }
}

@Composable
private fun TypeTile(
    label: String,
    type: CreateType,
    onClick: () -> Unit,
) {
    AppCard(
        onClick = onClick,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconTile(icon = type.icon(), tone = type.tone())
            Text(
                text = label,
                style = AppTheme.type.captionStrong,
                color = AppTheme.colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "S31_CreateHub_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S31_CreateHub_Light() {
    AppTheme(dark = false) {
        CreateHubScreen(
            state = CreateHubUiState(recent = listOf(CreateType.WEBSITE, CreateType.WIFI, CreateType.CONTACT)),
            onEvent = {},
            onOpenType = {},
        )
    }
}

@Preview(name = "S31_CreateHub_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S31_CreateHub_Dark() {
    AppTheme(dark = true) {
        CreateHubScreen(
            state = CreateHubUiState(recent = listOf(CreateType.WEBSITE, CreateType.WIFI, CreateType.CONTACT)),
            onEvent = {},
            onOpenType = {},
        )
    }
}

@Preview(name = "S75_CreateHub_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S75_CreateHub_Light() {
    AppTheme(dark = false) {
        CreateHubScreen(state = CreateHubUiState(), onEvent = {}, onOpenType = {})
    }
}

@Preview(name = "S75_CreateHub_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S75_CreateHub_Dark() {
    AppTheme(dark = true) {
        CreateHubScreen(state = CreateHubUiState(), onEvent = {}, onOpenType = {})
    }
}

@Preview(name = "S31_CreateHubNoResults_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S31_CreateHubNoResults_Light() {
    AppTheme(dark = false) {
        CreateHubScreen(state = CreateHubUiState(query = "zzz"), onEvent = {}, onOpenType = {})
    }
}

@Preview(name = "S31_CreateHubNoResults_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S31_CreateHubNoResults_Dark() {
    AppTheme(dark = true) {
        CreateHubScreen(state = CreateHubUiState(query = "zzz"), onEvent = {}, onOpenType = {})
    }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.BannerAd
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSearchBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.EmptyState
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.LocalBottomBarInset
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SegmentedControl
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.TonalButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryChip
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryEmptyKind
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryTab
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.SearchScope

/**
 * History tab root (artboards s45 to s49, s51 to s53, s76). Stateless: all state comes in through [state] and all
 * input leaves through [onEvent] and the navigation lambdas.
 */
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HistoryEvent) -> Unit,
    onStartScanning: () -> Unit,
    onCreateCode: () -> Unit,
    onOpenExportHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomInset = LocalBottomBarInset.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = state.selection.active || state.searchActive) {
        if (state.selection.active) {
            onEvent(HistoryEvent.ExitSelection)
        } else {
            focusManager.clearFocus()
            onEvent(HistoryEvent.CloseSearch)
        }
    }

    AppScaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                BannerAd()
                Spacer(Modifier.height(bottomInset))
            }
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            HistoryHeader(state, onEvent, onOpenExportHistory)
            if (!state.selection.active) {
                if (!state.searchActive) {
                    TabSwitcher(state.tab, onEvent)
                }
                SearchRow(state, onEvent, focusManager)
                FilterChips(state, onEvent)
            }
            HistoryContent(
                state = state,
                onEvent = onEvent,
                onStartScanning = onStartScanning,
                onCreateCode = onCreateCode,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HistoryHeader(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onOpenExportHistory: () -> Unit,
) {
    when {
        state.selection.active -> SelectionTopBar(state.selection.count, onEvent)
        state.searchActive -> Unit
        else -> {
            val hasActions = !(state.isEmpty && state.emptyKind.isTabEmpty())
            AppTopBar(
                title = stringResource(R.string.hist_title),
                actions = {
                    if (hasActions) {
                        IconButtonRounded(
                            icon = AppIcons.Check,
                            contentDescription = stringResource(R.string.hist_cd_select_items),
                            onClick = { onEvent(HistoryEvent.BeginSelection) },
                        )
                        OverflowMenu(
                            items = listOf(MenuEntry(R.string.hist_menu_export_history, onOpenExportHistory)),
                        )
                    }
                },
            )
        }
    }
}

private fun HistoryEmptyKind.isTabEmpty(): Boolean = this == HistoryEmptyKind.SCANNED || this == HistoryEmptyKind.CREATED

private class MenuEntry(val labelRes: Int, val onClick: () -> Unit)

/** Three-dot button with a dropdown holding [items]. */
@Composable
private fun OverflowMenu(items: List<MenuEntry>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButtonRounded(
            icon = AppIcons.More,
            contentDescription = stringResource(R.string.common_more),
            onClick = { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(stringResource(entry.labelRes)) },
                    onClick = {
                        expanded = false
                        entry.onClick()
                    },
                )
            }
        }
    }
}

/** Multi-select action bar (design s47): count, select all, favorite, delete, and share/export in the overflow. */
@Composable
private fun SelectionTopBar(count: Int, onEvent: (HistoryEvent) -> Unit) {
    val c = AppTheme.colors
    val hasSelection = count > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.primaryContainer)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButtonRounded(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.hist_cd_exit_selection),
            onClick = { onEvent(HistoryEvent.ExitSelection) },
            tint = c.onPrimaryContainer,
        )
        Text(
            text = quantityString(R.plurals.hist_selected_count, count),
            style = AppTheme.type.titleLarge,
            color = c.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )
        IconButtonRounded(
            icon = AppIcons.CheckCircle,
            contentDescription = stringResource(R.string.hist_cd_select_all),
            onClick = { onEvent(HistoryEvent.ToggleSelectAll) },
            tint = c.onPrimaryContainer,
        )
        IconButtonRounded(
            icon = AppIcons.Star,
            contentDescription = stringResource(R.string.hist_cd_favorite_selected),
            onClick = { onEvent(HistoryEvent.FavoriteSelected) },
            enabled = hasSelection,
            tint = c.onPrimaryContainer,
        )
        IconButtonRounded(
            icon = AppIcons.Delete,
            contentDescription = stringResource(R.string.common_delete),
            onClick = { onEvent(HistoryEvent.DeleteSelected) },
            enabled = hasSelection,
            tint = c.onPrimaryContainer,
        )
        if (hasSelection) {
            OverflowMenu(
                items = listOf(
                    MenuEntry(R.string.common_share) { onEvent(HistoryEvent.ShareSelected) },
                    MenuEntry(R.string.hist_menu_export_selected) { onEvent(HistoryEvent.ExportSelected) },
                ),
            )
        }
    }
}

@Composable
private fun TabSwitcher(tab: HistoryTab, onEvent: (HistoryEvent) -> Unit) {
    SegmentedControl(
        options = listOf(stringResource(R.string.hist_tab_scanned), stringResource(R.string.hist_tab_created)),
        selectedIndex = if (tab == HistoryTab.SCANNED) 0 else 1,
        onSelect = { index ->
            onEvent(HistoryEvent.SelectTab(if (index == 0) HistoryTab.SCANNED else HistoryTab.CREATED))
        },
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/**
 * Search field. It keeps the same position in the tree whether or not search is active, so focus survives the
 * switch into search mode (design s53) that is triggered by the field gaining focus.
 */
@Composable
private fun SearchRow(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    focusManager: FocusManager,
) {
    val searching = state.searchActive
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (searching) Modifier.statusBarsPadding() else Modifier)
            .padding(start = if (searching) 4.dp else 16.dp, end = 16.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (searching) {
            IconButtonRounded(
                icon = AppIcons.Back,
                contentDescription = stringResource(R.string.hist_cd_close_search),
                onClick = {
                    focusManager.clearFocus()
                    onEvent(HistoryEvent.CloseSearch)
                },
            )
        }
        AppSearchBar(
            query = state.query,
            onQueryChange = { onEvent(HistoryEvent.QueryChanged(it)) },
            placeholder = stringResource(R.string.hist_search_placeholder),
            modifier = Modifier.weight(1f),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            onFocusChanged = { focused -> if (focused) onEvent(HistoryEvent.OpenSearch) },
        )
    }
}

@Composable
private fun FilterChips(state: HistoryUiState, onEvent: (HistoryEvent) -> Unit) {
    val items: List<ChipItem>
    val onClick: (Int) -> Unit
    if (state.searchActive) {
        val scopes = SearchScope.entries
        items = scopes.map { scope ->
            ChipItem(
                label = stringResource(scope.labelRes()),
                selected = scope == state.searchScope,
                icon = if (scope == SearchScope.FAVORITES) AppIcons.Star else null,
            )
        }
        onClick = { index -> onEvent(HistoryEvent.SelectScope(scopes[index])) }
    } else {
        val chips = HistoryChip.entries
        items = chips.map { chip ->
            val selected = chip == state.chip
            val showCount = selected && chip == HistoryChip.FAVORITES && !state.loading
            ChipItem(
                label = if (showCount) {
                    stringResource(R.string.hist_chip_favorites_count, state.recordCount)
                } else {
                    stringResource(chip.labelRes())
                },
                selected = selected,
                icon = if (chip == HistoryChip.FAVORITES) AppIcons.Star else null,
            )
        }
        onClick = { index -> onEvent(HistoryEvent.SelectChip(chips[index])) }
    }
    ChipRow(chips = items, onChipClick = onClick, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onStartScanning: () -> Unit,
    onCreateCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        when {
            state.loading -> Unit
            state.isEmpty -> HistoryEmptyContent(state, onEvent, onStartScanning, onCreateCode)
            else -> HistoryList(state, onEvent)
        }
    }
}

/** One empty state per situation: each tab, favorites, a type filter and a search without hits. */
@Composable
private fun HistoryEmptyContent(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onStartScanning: () -> Unit,
    onCreateCode: () -> Unit,
) {
    when (state.emptyKind) {
        HistoryEmptyKind.SCANNED -> EmptyState(
            icon = AppIcons.History,
            title = stringResource(R.string.hist_empty_scanned_title),
            message = stringResource(R.string.hist_empty_scanned_message),
            action = {
                PrimaryButton(stringResource(R.string.hist_empty_scanned_action), onStartScanning, leadingIcon = AppIcons.Scan)
            },
        )
        HistoryEmptyKind.CREATED -> EmptyState(
            icon = AppIcons.QrCode,
            title = stringResource(R.string.hist_empty_created_title),
            message = stringResource(R.string.hist_empty_created_message),
            action = {
                PrimaryButton(stringResource(R.string.hist_empty_created_action), onCreateCode, leadingIcon = AppIcons.Create)
            },
        )
        HistoryEmptyKind.FAVORITES -> EmptyState(
            icon = AppIcons.Star,
            title = stringResource(R.string.hist_empty_favorites_title),
            message = stringResource(R.string.hist_empty_favorites_message),
            action = {
                TonalButton(stringResource(R.string.hist_empty_favorites_action), { onEvent(HistoryEvent.BrowseAll) })
            },
        )
        HistoryEmptyKind.FILTER -> EmptyState(
            icon = AppIcons.Apps,
            title = stringResource(R.string.hist_empty_filter_title),
            message = stringResource(R.string.hist_empty_filter_message),
            action = {
                TonalButton(stringResource(R.string.hist_empty_filter_action), { onEvent(HistoryEvent.BrowseAll) })
            },
        )
        HistoryEmptyKind.SEARCH -> EmptyState(
            icon = AppIcons.Search,
            title = stringResource(R.string.hist_empty_search_title),
            message = stringResource(R.string.hist_empty_search_message, state.appliedQuery.trim()),
        )
    }
}

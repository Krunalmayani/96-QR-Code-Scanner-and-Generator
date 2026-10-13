package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.BannerAd
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSearchBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.DestructiveButton
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
                if (state.selection.active) SelectionActionBar(state.selection.count, onEvent) else BannerAd()
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
                if (!state.tabIsEmpty) {
                    SearchRow(state, onEvent, focusManager)
                    FilterChips(state, onEvent)
                }
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
            TabTitleBar(title = stringResource(R.string.hist_title)) {
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
            }
        }
    }
}

/**
 * Tab-root title bar of the design: 24/32 extra-bold title indented 24dp, actions on the right, 12dp above the
 * content line (AppTopBar only offers the 20sp title of sub-screens).
 */
@Composable
private fun TabTitleBar(title: String, actions: @Composable RowScope.() -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 24.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.type.topBar.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = AppTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
    }
}

private fun HistoryEmptyKind.isTabEmpty(): Boolean = this == HistoryEmptyKind.SCANNED || this == HistoryEmptyKind.CREATED

private class MenuEntry(
    val labelRes: Int,
    val onClick: () -> Unit,
)

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

/** Multi-select top bar (design s47): close, live count, select all, and share/export in the overflow. */
@Composable
private fun SelectionTopBar(count: Int, onEvent: (HistoryEvent) -> Unit) {
    val c = AppTheme.colors
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(c.primaryContainer)
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButtonRounded(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.hist_cd_exit_selection),
            onClick = { onEvent(HistoryEvent.ExitSelection) },
            tint = c.onPrimaryContainer,
        )
        Text(
            text = quantityString(R.plurals.hist_selected_count, count),
            style = AppTheme.type.topBar,
            color = c.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics {
                        heading()
                        liveRegion = LiveRegionMode.Polite
                    },
        )
        IconButtonRounded(
            icon = AppIcons.Check,
            contentDescription = stringResource(R.string.hist_cd_select_all),
            onClick = { onEvent(HistoryEvent.ToggleSelectAll) },
            tint = c.onPrimaryContainer,
        )
        if (count > 0) {
            OverflowMenu(
                items =
                    listOf(
                        MenuEntry(R.string.common_share) { onEvent(HistoryEvent.ShareSelected) },
                        MenuEntry(R.string.hist_menu_export_selected) { onEvent(HistoryEvent.ExportSelected) },
                    ),
            )
        }
    }
}

/** Bottom action bar of the multi-select state (design s47): Favorite and Delete N. */
@Composable
private fun SelectionActionBar(count: Int, onEvent: (HistoryEvent) -> Unit) {
    val hasSelection = count > 0
    Column(Modifier.fillMaxWidth().background(AppTheme.colors.surface)) {
        AppDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TonalButton(
                text = stringResource(R.string.hist_action_favorite),
                onClick = { onEvent(HistoryEvent.FavoriteSelected) },
                modifier = Modifier.weight(1f),
                enabled = hasSelection,
                leadingIcon = AppIcons.StarOutline,
            )
            DestructiveButton(
                text =
                    if (hasSelection) {
                        stringResource(R.string.hist_action_delete_count, count)
                    } else {
                        stringResource(R.string.common_delete)
                    },
                onClick = { onEvent(HistoryEvent.DeleteSelected) },
                modifier = Modifier.weight(1f),
                enabled = hasSelection,
                leadingIcon = AppIcons.Delete,
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
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (searching) Modifier.statusBarsPadding() else Modifier)
                .padding(start = if (searching) 8.dp else 16.dp, end = 16.dp, top = if (searching) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        items =
            scopes.map { scope ->
                ChipItem(
                    label = stringResource(scope.labelRes()),
                    selected = scope == state.searchScope,
                )
            }
        onClick = { index -> onEvent(HistoryEvent.SelectScope(scopes[index])) }
    } else {
        val chips = HistoryChip.entries
        items =
            chips.map { chip ->
                val selected = chip == state.chip
                val showCount = selected && chip == HistoryChip.FAVORITES && !state.loading && state.recordCount > 0
                ChipItem(
                    label =
                        if (showCount) {
                            stringResource(R.string.hist_chip_favorites_count, state.recordCount)
                        } else {
                            stringResource(chip.labelRes())
                        },
                    selected = selected,
                    icon = if (chip == HistoryChip.FAVORITES) (if (selected) AppIcons.Star else AppIcons.StarOutline) else null,
                )
            }
        onClick = { index -> onEvent(HistoryEvent.SelectChip(chips[index])) }
    }
    ChipRow(chips = items, onChipClick = onClick, modifier = Modifier.padding(top = if (state.searchActive) 14.dp else 12.dp))
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
        HistoryEmptyKind.SCANNED ->
            HistoryEmptyView(
                title = stringResource(R.string.hist_empty_scanned_title),
                message = stringResource(R.string.hist_empty_scanned_message),
                topPadding = 96.dp,
                illustration = { TabEmptyIllustration(AppIcons.History, AppIcons.QrCode) },
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.hist_empty_scanned_action),
                        onClick = onStartScanning,
                        modifier = Modifier.heightIn(min = EmptyButtonHeight),
                        leadingIcon = AppIcons.Scan,
                    )
                },
            )
        HistoryEmptyKind.CREATED ->
            HistoryEmptyView(
                title = stringResource(R.string.hist_empty_created_title),
                message = stringResource(R.string.hist_empty_created_message),
                topPadding = 96.dp,
                illustration = { TabEmptyIllustration(AppIcons.QrCode, AppIcons.Create) },
                action = {
                    PrimaryButton(
                        text = stringResource(R.string.hist_empty_created_action),
                        onClick = onCreateCode,
                        modifier = Modifier.heightIn(min = EmptyButtonHeight),
                        leadingIcon = AppIcons.Create,
                    )
                },
            )
        HistoryEmptyKind.FAVORITES ->
            HistoryEmptyView(
                title = stringResource(R.string.hist_empty_favorites_title),
                message = stringResource(R.string.hist_empty_favorites_message),
                topPadding = 84.dp,
                illustration = { DiscIllustration(AppIcons.StarOutline) },
                action = {
                    TonalButton(
                        text = stringResource(R.string.hist_empty_favorites_action),
                        onClick = { onEvent(HistoryEvent.BrowseAll) },
                        modifier = Modifier.heightIn(min = EmptyButtonHeight),
                    )
                },
            )
        HistoryEmptyKind.FILTER ->
            HistoryEmptyView(
                title = stringResource(R.string.hist_empty_filter_title),
                message = stringResource(R.string.hist_empty_filter_message),
                topPadding = 84.dp,
                illustration = { DiscIllustration(AppIcons.Apps) },
                action = {
                    TonalButton(
                        text = stringResource(R.string.hist_empty_filter_action),
                        onClick = { onEvent(HistoryEvent.BrowseAll) },
                        modifier = Modifier.heightIn(min = EmptyButtonHeight),
                    )
                },
            )
        HistoryEmptyKind.SEARCH ->
            HistoryEmptyView(
                title = stringResource(R.string.hist_empty_search_title),
                message = stringResource(R.string.hist_empty_search_message, state.appliedQuery.trim()),
                topPadding = 84.dp,
                illustration = { DiscIllustration(AppIcons.Search) },
            )
    }
}

private val EmptyButtonHeight = 52.dp

/**
 * Empty state of the design (s49, s52): illustration near the top, 24/30 extra-bold title, 15/22 message and a
 * 52dp button. Scrolls so it stays reachable at large font sizes.
 */
@Composable
private fun HistoryEmptyView(
    title: String,
    message: String,
    topPadding: Dp,
    illustration: @Composable () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    val c = AppTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        illustration()
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = AppTheme.type.headline.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold),
                color = c.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = message,
                style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = c.inkSecondary,
                textAlign = TextAlign.Center,
            )
        }
        action?.invoke()
    }
}

/** 160dp disc with a white inner disc, a 64dp [icon] and a rounded [badge] tile on the lower right (s49). */
@Composable
private fun TabEmptyIllustration(icon: ImageVector, badge: ImageVector) {
    val c = AppTheme.colors
    Box(Modifier.size(160.dp).background(c.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Box(Modifier.size(124.dp).background(c.surface, CircleShape))
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(64.dp))
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 14.dp)
                    .size(48.dp)
                    .background(c.primary, AppTheme.shapes.field),
            contentAlignment = Alignment.Center,
        ) {
            Icon(badge, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(24.dp))
        }
    }
}

/** 152dp primary-container disc with a 72dp [icon] (s52). */
@Composable
private fun DiscIllustration(icon: ImageVector) {
    val c = AppTheme.colors
    Box(Modifier.size(152.dp).background(c.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(72.dp))
    }
}

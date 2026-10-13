package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.NativeAdCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCheckbox
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SectionLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryListBuilder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryListItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.QueryHighlighter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

private const val TYPE_HEADER = "header"
private const val TYPE_ROW = "row"
private const val TYPE_AD = "ad"
private const val TYPE_COUNT = "count"
private val CardRadius = 20.dp

/** Day-grouped list with sticky headers, swipe-to-delete rows and a native ad slot every 8 records. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoryList(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listItems = remember(state.groups) { HistoryListBuilder.build(state.groups) }
    val formats = rememberHistoryFormats()
    val showCount = state.searchActive && state.appliedQuery.isNotBlank()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        if (showCount) {
            item(key = "result-count", contentType = TYPE_COUNT) {
                SectionLabel(quantityString(R.plurals.hist_search_results, state.recordCount))
            }
        }
        listItems.forEach { entry ->
            when (entry) {
                is HistoryListItem.Header ->
                    stickyHeader(key = entry.key, contentType = TYPE_HEADER) {
                        DayHeader(entry.label, formats)
                    }
                is HistoryListItem.Row ->
                    item(key = entry.key, contentType = TYPE_ROW) {
                        HistoryListRow(entry, state, formats, onEvent)
                    }
                is HistoryListItem.Ad ->
                    item(key = entry.key, contentType = TYPE_AD) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { NativeAdCard() }
                    }
            }
        }
    }
}

@Composable
private fun DayHeader(label: DayLabel, formats: HistoryFormats) {
    Box(Modifier.fillMaxWidth().background(AppTheme.colors.background).padding(top = 8.dp)) {
        SectionLabel(label.displayText(formats))
    }
}

@Composable
private fun HistoryListRow(
    entry: HistoryListItem.Row,
    state: HistoryUiState,
    formats: HistoryFormats,
    onEvent: (HistoryEvent) -> Unit,
) {
    val record = entry.record
    val selecting = state.selection.active
    val shape = remember(entry.first, entry.last) { cardShape(entry.first, entry.last) }
    val typeLabel = stringResource(record.type.historyLabelRes())
    val time = remember(record.createdAt) { formats.time(record.createdAt) }
    val subtitle = stringResource(R.string.hist_row_subtitle, typeLabel, time)

    SwipeToDeleteBox(
        enabled = !selecting,
        shape = shape,
        onDelete = { onEvent(HistoryEvent.SwipeDeleted(record.id)) },
    ) {
        HistoryRow(
            record = record,
            subtitle = subtitle,
            highlightQuery = state.appliedQuery,
            selecting = selecting,
            isSelected = state.selection.isSelected(record.id),
            showDivider = !entry.last,
            onClick = { onEvent(HistoryEvent.RowClicked(record.id)) },
            onLongClick = { onEvent(HistoryEvent.RowLongClicked(record.id)) },
            onStar = { onEvent(HistoryEvent.StarClicked(record.id)) },
            onDelete = { onEvent(HistoryEvent.SwipeDeleted(record.id)) },
        )
    }
}

private fun cardShape(first: Boolean, last: Boolean): Shape =
    RoundedCornerShape(
        topStart = if (first) CardRadius else 0.dp,
        topEnd = if (first) CardRadius else 0.dp,
        bottomStart = if (last) CardRadius else 0.dp,
        bottomEnd = if (last) CardRadius else 0.dp,
    )

/** Swipe left to delete (the row then reports the delete; the list update removes it). */
@Composable
private fun SwipeToDeleteBox(
    enabled: Boolean,
    shape: Shape,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { target ->
                if (target == SwipeToDismissBoxValue.EndToStart) {
                    currentOnDelete()
                    true
                } else {
                    false
                }
            },
        )
    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.clip(shape),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        backgroundContent = {
            DeleteBackground(active = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
        },
        content = { content() },
    )
}

@Composable
private fun DeleteBackground(active: Boolean) {
    val c = AppTheme.colors
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(if (active) c.error else Color.Transparent)
                .padding(end = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (active) Icon(AppIcons.Delete, contentDescription = null, tint = c.onError)
    }
}

/** One history row: optional checkbox, type tile, title with search highlight, subtitle and the star toggle. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    record: ScanRecord,
    subtitle: String,
    highlightQuery: String,
    selecting: Boolean,
    isSelected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = AppTheme.colors
    val deleteLabel = stringResource(R.string.common_delete)
    val background = if (isSelected) c.primaryContainer else c.surface
    Column(Modifier.fillMaxWidth().background(background)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .combinedClickable(
                        onClickLabel = stringResource(R.string.hist_cd_open_row),
                        onLongClickLabel = stringResource(R.string.hist_cd_select_row),
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ).semantics {
                        selected = isSelected
                        customActions =
                            listOf(
                                CustomAccessibilityAction(deleteLabel) {
                                    onDelete()
                                    true
                                },
                            )
                    }.padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = if (selecting) 16.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selecting) AppCheckbox(checked = isSelected)
            IconTile(icon = record.type.historyIcon(), tone = record.type.historyTone())
            RowTexts(record.displayTitle, subtitle, highlightQuery, Modifier.weight(1f))
            if (!selecting) {
                FavoriteStar(record.isFavorite, onStar)
            }
        }
        if (showDivider) AppDivider(Modifier.padding(start = if (selecting) 104.dp else 68.dp))
    }
}

@Composable
private fun RowTexts(title: String, subtitle: String, highlightQuery: String, modifier: Modifier) {
    val c = AppTheme.colors
    val highlightBg = c.primaryContainer
    val highlightFg = c.onPrimaryContainer
    val titleText =
        remember(title, highlightQuery, highlightBg, highlightFg) {
            highlighted(title, highlightQuery, highlightBg, highlightFg)
        }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titleText, style = AppTheme.type.title, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, style = AppTheme.type.caption, color = c.inkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FavoriteStar(favorite: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    IconButtonRounded(
        icon = if (favorite) AppIcons.Star else AppIcons.StarOutline,
        contentDescription = stringResource(if (favorite) R.string.common_unfavorite else R.string.common_favorite),
        onClick = onClick,
        tint = if (favorite) c.primary else c.inkSecondary,
    )
}

/** [text] with every case-insensitive hit of [query] painted in the highlight colours. */
private fun highlighted(text: String, query: String, background: Color, foreground: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        for (range in QueryHighlighter.ranges(text, query)) {
            addStyle(SpanStyle(background = background, color = foreground), range.first, range.last + 1)
        }
    }

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.ads.NativeAdCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCheckbox
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.DayLabel
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryListBuilder
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.HistoryListItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history.QueryHighlighter
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ContentType
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

private const val TYPE_HEADER = "header"
private const val TYPE_ROW = "row"
private const val TYPE_AD = "ad"
private const val TYPE_COUNT = "count"
private const val TYPE_HINT = "hint"
private val CardRadius = 20.dp

/**
 * Day-grouped list with sticky headers, swipe-to-delete rows and a native ad slot every 8 records.
 * Favorites and typed searches ([HistoryUiState.flatResults]) are one flat card under a single label (s48, s53).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoryList(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val flat = state.flatResults
    val listItems = remember(state.groups, flat) { buildListItems(state.groups, flat) }
    val formats = rememberHistoryFormats()
    val searchCount = flat && state.searchActive
    val starred = flat && !state.searchActive

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        if (searchCount) {
            item(key = "result-count", contentType = TYPE_COUNT) {
                ListLabel(quantityString(R.plurals.hist_search_results, state.recordCount))
            }
        }
        if (starred) {
            item(key = "starred-label", contentType = TYPE_COUNT) { ListLabel(stringResource(R.string.hist_section_starred)) }
        }
        listItems.forEach { entry ->
            when (entry) {
                is HistoryListItem.Header ->
                    stickyHeader(key = entry.key, contentType = TYPE_HEADER) {
                        Box(Modifier.fillMaxWidth().background(AppTheme.colors.background)) {
                            ListLabel(entry.label.displayText(formats))
                        }
                    }
                is HistoryListItem.Row ->
                    item(key = entry.key, contentType = TYPE_ROW) {
                        HistoryListRow(entry, state, formats, onEvent, dated = flat, badge = starred)
                    }
                is HistoryListItem.Ad ->
                    item(key = entry.key, contentType = TYPE_AD) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { NativeAdCard() }
                    }
            }
        }
        if (starred) {
            item(key = "starred-hint", contentType = TYPE_HINT) {
                Text(
                    text = stringResource(R.string.hist_favorites_hint),
                    style = AppTheme.type.caption,
                    color = AppTheme.colors.inkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
            }
        }
    }
}

/** Day groups with headers, or (flat) all rows in one card without day headers. */
private fun buildListItems(groups: List<DayGroup>, flat: Boolean): List<HistoryListItem> {
    if (!flat) return HistoryListBuilder.build(groups)
    val records = groups.flatMap { it.records }
    return HistoryListBuilder.build(listOf(DayGroup(DayLabel.Today, records))).filterNot { it is HistoryListItem.Header }
}

/** Overline label above a card (design `.lb`, margin 14 4 6). */
@Composable
private fun ListLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = AppTheme.type.label,
        color = AppTheme.colors.inkSecondary,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 14.dp, bottom = 6.dp).semantics { heading() },
    )
}

@Composable
private fun HistoryListRow(
    entry: HistoryListItem.Row,
    state: HistoryUiState,
    formats: HistoryFormats,
    onEvent: (HistoryEvent) -> Unit,
    dated: Boolean,
    badge: Boolean,
) {
    val record = entry.record
    val selecting = state.selection.active
    val shape = remember(entry.first, entry.last) { cardShape(entry.first, entry.last) }
    val typeLabel = stringResource(record.type.historyLabelRes())
    val time = remember(record.createdAt) { formats.time(record.createdAt) }
    val subtitle =
        if (dated) {
            val day = remember(record.createdAt) { formats.dayLabel(record.createdAt) }
            stringResource(R.string.hist_row_subtitle_dated, typeLabel, day.displayText(formats), time)
        } else {
            stringResource(R.string.hist_row_subtitle, typeLabel, time)
        }

    SwipeToDeleteBox(
        enabled = !selecting,
        shape = shape,
        first = entry.first,
        last = entry.last,
        onDelete = { onEvent(HistoryEvent.SwipeDeleted(record.id)) },
    ) {
        HistoryRow(
            record = record,
            subtitle = subtitle,
            highlightQuery = state.appliedQuery,
            selecting = selecting,
            isSelected = state.selection.isSelected(record.id),
            showDivider = !entry.last,
            starBadge = badge && record.isFavorite,
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

/**
 * The 1dp outline of the rounded card these rows form. Each row draws its own slice: side lines always, the top arc on
 * the first row, the bottom arc on the last one (the divider already draws the lines between rows).
 */
private fun Modifier.cardOutline(first: Boolean, last: Boolean, color: Color): Modifier =
    drawWithContent {
        drawContent()
        val stroke = 1.dp.toPx()
        val half = stroke / 2
        val radius = CardRadius.toPx()
        val top = if (first) half else -radius * 2
        val bottom = if (last) size.height - half else size.height + radius * 2
        clipRect {
            drawRoundRect(
                color = color,
                topLeft = Offset(half, top),
                size = Size(size.width - stroke, bottom - top),
                cornerRadius = CornerRadius(radius - half),
                style = Stroke(stroke),
            )
        }
    }

/** Swipe left to delete (the row then reports the delete; the list update removes it). */
@Composable
private fun SwipeToDeleteBox(
    enabled: Boolean,
    shape: Shape,
    first: Boolean,
    last: Boolean,
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
        modifier = Modifier.clip(shape).cardOutline(first, last, AppTheme.colors.outline),
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
                .padding(end = 44.dp),
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
    starBadge: Boolean,
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
                        if (selecting) {
                            role = Role.Checkbox
                            toggleableState = ToggleableState(isSelected)
                        }
                        customActions =
                            listOf(
                                CustomAccessibilityAction(deleteLabel) {
                                    onDelete()
                                    true
                                },
                            )
                    }.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = if (selecting) 16.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selecting) AppCheckbox(checked = isSelected)
            TypeTile(record.type, starBadge)
            RowTexts(record.displayTitle, subtitle, highlightQuery, record.type == ContentType.PRODUCT, Modifier.weight(1f))
            if (!selecting) {
                FavoriteStar(record.isFavorite, onStar)
            }
        }
        if (showDivider) AppDivider()
    }
}

/** Type icon tile; in the Favorites view a small star badge sits on its corner (design s48). */
@Composable
private fun TypeTile(type: ContentType, starBadge: Boolean) {
    val c = AppTheme.colors
    Box {
        IconTile(icon = type.historyIcon(), tone = type.historyTone())
        if (starBadge) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-5).dp)
                        .size(20.dp)
                        .background(c.primary, CircleShape)
                        .border(2.dp, c.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Star, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(11.dp))
            }
        }
    }
}

@Composable
private fun RowTexts(title: String, subtitle: String, highlightQuery: String, mono: Boolean, modifier: Modifier) {
    val c = AppTheme.colors
    val highlightBg = c.primaryContainer
    val highlightFg = c.onPrimaryContainer
    val titleText =
        remember(title, highlightQuery, highlightBg, highlightFg) {
            highlighted(title, highlightQuery, highlightBg, highlightFg)
        }
    val titleStyle =
        AppTheme.type.title.copy(
            fontSize = 15.sp,
            fontFamily = if (mono) FontFamily.Monospace else AppTheme.type.title.fontFamily,
        )
    Column(modifier) {
        Text(titleText, style = titleStyle, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

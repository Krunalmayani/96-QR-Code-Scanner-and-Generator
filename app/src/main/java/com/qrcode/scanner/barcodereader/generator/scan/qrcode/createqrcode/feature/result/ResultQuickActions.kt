package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.QuickAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanSource

/** What one round button of the row does. [Favorite] is separate because it animates and shows state. */
internal sealed interface QuickActionSpec {
    data class Plain(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit,
        val tone: Tone = Tone.Primary,
    ) : QuickActionSpec

    data class Favorite(
        val favorite: Boolean,
        val onClick: () -> Unit,
    ) : QuickActionSpec
}

/** The secondary round actions under the content: type specific ones, then Copy, Share and Favorite (and Delete in history). */
@Composable
internal fun ResultQuickActions(
    record: ScanRecord,
    fromHistory: Boolean,
    onEvent: (ResultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val specs = quickActionSpecs(record, fromHistory, onEvent)
    if (specs.isNotEmpty()) QuickActionRow(specs, modifier)
}

/** The buttons shared by most types, built once per result. */
private class CommonActions(
    val copy: QuickActionSpec.Plain,
    val share: QuickActionSpec.Plain,
    val favorite: QuickActionSpec.Favorite,
    val searchWeb: QuickActionSpec.Plain,
)

@Composable
private fun spec(
    icon: ImageVector,
    @StringRes label: Int,
    event: ResultEvent,
    onEvent: (ResultEvent) -> Unit,
): QuickActionSpec.Plain =
    QuickActionSpec.Plain(icon, stringResource(label), { onEvent(event) })

@Composable
private fun commonActions(record: ScanRecord, onEvent: (ResultEvent) -> Unit) =
    CommonActions(
        copy = spec(AppIcons.Copy, R.string.common_copy, ResultEvent.CopyClicked, onEvent),
        share = spec(AppIcons.Share, R.string.common_share, ResultEvent.ShareClicked, onEvent),
        favorite = QuickActionSpec.Favorite(record.isFavorite) { onEvent(ResultEvent.FavoriteClicked) },
        searchWeb = spec(AppIcons.Search, R.string.result_action_search_web, ResultEvent.SearchWebClicked, onEvent),
    )

@Composable
private fun quickActionSpecs(record: ScanRecord, fromHistory: Boolean, onEvent: (ResultEvent) -> Unit): List<QuickActionSpec> {
    val common = commonActions(record, onEvent)
    val created = fromHistory && record.source == ScanSource.CREATED
    val specs =
        if (created) {
            // s50: Favorited, Save image, Copy (Share is the pinned button), then Delete.
            listOf(
                common.favorite,
                spec(AppIcons.Download, R.string.result_action_save_image, ResultEvent.SaveImageClicked, onEvent),
                common.copy,
            )
        } else {
            typeSpecs(record.content, common, fromHistory, onEvent)
        }
    if (!fromHistory) return specs
    val delete =
        QuickActionSpec.Plain(
            AppIcons.Delete,
            stringResource(R.string.common_delete),
            { onEvent(ResultEvent.DeleteClicked) },
            Tone.Error,
        )
    return specs + delete
}

/** Per artboard: s15/s16 Copy Share Favorite Search web, s17 Call Email Copy Share, s20 to s25 as drawn. */
@Composable
private fun typeSpecs(
    content: ScanContent,
    common: CommonActions,
    fromHistory: Boolean,
    onEvent: (ResultEvent) -> Unit,
): List<QuickActionSpec> {
    val addContact = spec(AppIcons.Create, R.string.result_action_add_contact_short, ResultEvent.AddToContactsClicked, onEvent)
    return when (content) {
        is ScanContent.Website, is ScanContent.Text -> listOf(common.copy, common.share, common.favorite, common.searchWeb)
        is ScanContent.Unknown -> listOf(common.copy, common.share, common.searchWeb, common.favorite)
        is ScanContent.Contact -> contactSpecs(content, common, onEvent)
        is ScanContent.Phone ->
            listOf(
                spec(AppIcons.Sms, R.string.result_action_send_sms, ResultEvent.MessageClicked(content.number), onEvent),
                addContact,
                common.copy,
                common.share,
            )
        is ScanContent.Email ->
            listOf(spec(AppIcons.Copy, R.string.result_action_copy_address, ResultEvent.CopyClicked, onEvent), addContact, common.share)
        is ScanContent.Sms ->
            listOf(
                spec(AppIcons.Phone, R.string.result_action_call, ResultEvent.DialClicked(content.number), onEvent),
                common.copy,
                common.share,
            )
        is ScanContent.Location ->
            listOf(
                spec(AppIcons.Copy, R.string.result_action_copy_coordinates, ResultEvent.CopyClicked, onEvent),
                common.share,
                common.favorite,
            )
        is ScanContent.Event ->
            listOfNotNull(
                if (content.location.isBlank()) {
                    null
                } else {
                    spec(AppIcons.Location, R.string.result_action_directions, ResultEvent.DirectionsClicked, onEvent)
                },
                common.share,
                common.copy,
            )
        // s18 has no round buttons; a saved Wi-Fi in history keeps Share next to Delete.
        is ScanContent.Wifi -> if (fromHistory) listOf(common.share) else emptyList()
        is ScanContent.Product -> listOf(common.copy, common.share, common.favorite)
    }
}

@Composable
private fun contactSpecs(content: ScanContent.Contact, common: CommonActions, onEvent: (ResultEvent) -> Unit): List<QuickActionSpec> =
    listOfNotNull(
        content.phones.firstOrNull()?.let { spec(AppIcons.Phone, R.string.result_action_call, ResultEvent.DialClicked(it), onEvent) },
        content.emails.firstOrNull()?.let {
            spec(
                AppIcons.Email,
                R.string.result_action_email,
                ResultEvent.ComposeEmailClicked(it),
                onEvent,
            )
        },
        common.copy,
        common.share,
    )

private const val EVENLY_SPACED_MAX = 4

@Composable
internal fun QuickActionRow(items: List<QuickActionSpec>, modifier: Modifier = Modifier) {
    if (items.size <= EVENLY_SPACED_MAX) {
        Row(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            items.forEach { spec ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) { QuickActionItem(spec) }
            }
        }
    } else {
        Row(
            modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { spec ->
                Box(Modifier.width(76.dp), contentAlignment = Alignment.TopCenter) { QuickActionItem(spec) }
            }
        }
    }
}

@Composable
private fun QuickActionItem(spec: QuickActionSpec) {
    when (spec) {
        is QuickActionSpec.Plain -> QuickAction(spec.icon, spec.label, spec.onClick, tone = spec.tone)
        is QuickActionSpec.Favorite -> FavoriteAction(spec.favorite, spec.onClick)
    }
}

/** Scale that pops to 1.3 and settles when [favorite] turns on (PRD 14: "Favorite fills the star with a small pop animation"). */
@Composable
internal fun rememberPopScale(favorite: Boolean): Animatable<Float, AnimationVector1D> {
    val scale = remember { Animatable(1f) }
    val previous = remember { mutableStateOf(favorite) }
    LaunchedEffect(favorite) {
        if (favorite && !previous.value) {
            scale.animateTo(POP_SCALE, tween(POP_UP_MS))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        previous.value = favorite
    }
    return scale
}

/** Star in the top bar (s15, s50): same state and pop as the round Favorite button. */
@Composable
internal fun FavoriteTopAction(favorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale = rememberPopScale(favorite)
    IconButtonRounded(
        icon = if (favorite) AppIcons.Star else AppIcons.StarOutline,
        contentDescription = stringResource(if (favorite) R.string.result_favorite_remove else R.string.result_favorite_add),
        onClick = onClick,
        modifier =
            modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        tint = if (favorite) AppTheme.colors.primary else null,
    )
}

/** Round star button; the star fills with a small pop (scale up, then settle) when it turns on. */
@Composable
internal fun FavoriteAction(favorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val scale = rememberPopScale(favorite)
    val shape = AppTheme.shapes.card
    Column(
        modifier =
            modifier
                .clip(shape)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }.clip(shape)
                .background(if (favorite) c.primary else c.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (favorite) AppIcons.Star else AppIcons.StarOutline,
                contentDescription = null,
                tint = if (favorite) c.onPrimary else c.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(if (favorite) R.string.result_favorited else R.string.result_favorite),
            style = AppTheme.type.captionStrong,
            color = c.ink,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private const val POP_SCALE = 1.3f
private const val POP_UP_MS = 110

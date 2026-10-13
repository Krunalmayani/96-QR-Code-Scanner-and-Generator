package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.animation.core.Animatable
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
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.QuickAction
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanContent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord

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
    QuickActionRow(specs, modifier)
}

@Composable
private fun quickActionSpecs(record: ScanRecord, fromHistory: Boolean, onEvent: (ResultEvent) -> Unit): List<QuickActionSpec> {
    val copyLabel = stringResource(R.string.common_copy)
    val shareLabel = stringResource(R.string.common_share)
    val copy = QuickActionSpec.Plain(AppIcons.Copy, copyLabel, { onEvent(ResultEvent.CopyClicked) })
    val share = QuickActionSpec.Plain(AppIcons.Share, shareLabel, { onEvent(ResultEvent.ShareClicked) })
    val favorite = QuickActionSpec.Favorite(record.isFavorite) { onEvent(ResultEvent.FavoriteClicked) }
    val addContact =
        QuickActionSpec.Plain(
            AppIcons.Create,
            stringResource(R.string.result_action_add_contact_short),
            { onEvent(ResultEvent.AddToContactsClicked) },
        )
    val specs: List<QuickActionSpec> =
        when (val content = record.content) {
            is ScanContent.Phone ->
                listOf(
                    QuickActionSpec.Plain(AppIcons.Sms, stringResource(R.string.result_action_send_sms), {
                        onEvent(ResultEvent.MessageClicked(content.number))
                    }),
                    addContact,
                    copy,
                    share,
                    favorite,
                )
            is ScanContent.Email ->
                listOf(
                    QuickActionSpec.Plain(
                        AppIcons.Copy,
                        stringResource(R.string.result_action_copy_address),
                        { onEvent(ResultEvent.CopyClicked) },
                    ),
                    addContact,
                    share,
                    favorite,
                )
            is ScanContent.Sms ->
                listOf(
                    QuickActionSpec.Plain(
                        AppIcons.Phone,
                        stringResource(R.string.result_action_call),
                        { onEvent(ResultEvent.DialClicked(content.number)) },
                    ),
                    copy,
                    share,
                    favorite,
                )
            is ScanContent.Location ->
                listOf(
                    QuickActionSpec.Plain(
                        AppIcons.Copy,
                        stringResource(R.string.result_action_copy_coordinates),
                        { onEvent(ResultEvent.CopyClicked) },
                    ),
                    share,
                    favorite,
                )
            is ScanContent.Event ->
                listOfNotNull(
                    if (content.location.isBlank()) {
                        null
                    } else {
                        QuickActionSpec.Plain(
                            AppIcons.Location,
                            stringResource(R.string.result_action_directions),
                            { onEvent(ResultEvent.DirectionsClicked) },
                        )
                    },
                    share,
                    copy,
                    favorite,
                )
            is ScanContent.Wifi -> listOf(share, favorite)
            else -> listOf(copy, share, favorite)
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

/** Round star button; the star fills with a small pop (scale up, then settle) when it turns on. */
@Composable
internal fun FavoriteAction(favorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val scale = remember { Animatable(1f) }
    val previous = remember { mutableStateOf(favorite) }
    LaunchedEffect(favorite) {
        if (favorite && !previous.value) {
            scale.animateTo(POP_SCALE, tween(POP_UP_MS))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        previous.value = favorite
    }
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

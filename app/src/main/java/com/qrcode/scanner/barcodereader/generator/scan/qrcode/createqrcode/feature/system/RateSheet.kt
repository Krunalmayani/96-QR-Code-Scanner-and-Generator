package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.containerOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.contentOf
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.system.RatingAction

private const val STAR_COUNT = RatingAction.MAX_STARS

/**
 * Artboard s71: "Enjoying the scanner?" bottom sheet. [onRate] receives the picked 1..5 stars when the main
 * button is tapped; [onLater] covers "Maybe later", swipe down and scrim tap.
 */
@Composable
internal fun RateSheet(
    onRate: (stars: Int) -> Unit,
    onLater: () -> Unit,
) {
    AppBottomSheet(onDismissRequest = onLater) {
        RateSheetBody(onRate = onRate, onLater = onLater)
    }
}

@Composable
private fun RateSheetBody(
    onRate: (stars: Int) -> Unit,
    onLater: () -> Unit,
    initialStars: Int = 0,
) {
    val c = AppTheme.colors
    var stars by rememberSaveable { mutableStateOf(initialStars) }
    val feedbackPath = RatingAction.forStars(stars) == RatingAction.FEEDBACK
    val actionText = stringResource(if (feedbackPath) R.string.system_rate_action_feedback else R.string.system_rate_action_play)

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(c.containerOf(Tone.Success)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Star, contentDescription = null, tint = c.contentOf(Tone.Success), modifier = Modifier.size(32.dp))
        }
        Text(
            text = stringResource(R.string.system_rate_title),
            style = AppTheme.type.headline,
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        Text(
            text = stringResource(R.string.system_rate_body),
            style = AppTheme.type.body,
            color = c.inkSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        StarRow(stars = stars, onSelect = { stars = it }, modifier = Modifier.padding(top = 20.dp))
        PrimaryButton(
            text = actionText,
            onClick = { onRate(stars) },
            enabled = stars > 0,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 52.dp),
        )
        AppTextButton(
            text = stringResource(R.string.system_rate_later),
            onClick = onLater,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

/** Five tappable stars; the filled ones are the rating. Each star is a 48dp radio-style target. */
@Composable
private fun StarRow(stars: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val groupDescription = stringResource(R.string.system_rate_stars_group)
    Row(
        modifier = modifier.selectableGroup().semantics { contentDescription = groupDescription },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (value in 1..STAR_COUNT) {
            Star(value = value, filled = value <= stars, selected = value == stars, onSelect = onSelect)
        }
    }
}

@Composable
private fun Star(value: Int, filled: Boolean, selected: Boolean, onSelect: (Int) -> Unit) {
    val c = AppTheme.colors
    val description = pluralStringResource(R.plurals.system_rate_star, value, value)
    Box(
        modifier =
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(value) })
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        // Filled vs outlined glyph carries the rating as well as the color, so it never relies on color alone.
        Icon(
            imageVector = if (filled) AppIcons.Star else AppIcons.StarOutline,
            contentDescription = null,
            tint = if (filled) c.primary else c.inkSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun RatePreviewFrame(dark: Boolean, stars: Int) {
    AppTheme(dark = dark) {
        Box(Modifier.fillMaxSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surface, AppTheme.shapes.sheet)
                        .padding(24.dp),
            ) {
                RateSheetBody(onRate = {}, onLater = {}, initialStars = stars)
            }
        }
    }
}

@Preview(name = "S71_RateSheet_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S71_RateSheet_Light() = RatePreviewFrame(dark = false, stars = 4)

@Preview(name = "S71_RateSheet_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S71_RateSheet_Dark() = RatePreviewFrame(dark = true, stars = 4)

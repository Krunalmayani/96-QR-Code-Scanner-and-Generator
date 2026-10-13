package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppBottomSheet
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone

/**
 * Artboard s66: bottom sheet explaining why Location is requested for Wi-Fi connect. [onContinue] starts the
 * permission request, [onDismiss] is "Not now" / swipe down / scrim tap (the caller offers "copy password" instead).
 */
@Composable
fun LocationRationaleSheet(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismissRequest = onDismiss) {
        LocationRationaleBody(onContinue = onContinue, onDismiss = onDismiss)
    }
}

@Composable
private fun LocationRationaleBody(onContinue: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconTile(AppIcons.Location, tone = Tone.Primary, size = 56.dp)
            Text(
                text = stringResource(R.string.perm_location_title),
                style = AppTheme.type.headline,
                color = c.ink,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
        }
        Text(
            text = stringResource(R.string.perm_location_body),
            style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = c.inkSecondary,
            modifier = Modifier.padding(top = 12.dp),
        )
        CardGroup(Modifier.padding(top = 16.dp)) {
            LocationPoint(stringResource(R.string.perm_location_point1))
            AppDivider()
            LocationPoint(stringResource(R.string.perm_location_point2))
        }
        PrimaryButton(
            text = stringResource(R.string.perm_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).heightIn(min = 52.dp),
        )
        AppTextButton(
            text = stringResource(R.string.perm_not_now),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

@Composable
private fun LocationPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(AppIcons.Check, tone = Tone.Success, size = 32.dp, iconSize = 20.dp)
        Text(text, style = AppTheme.type.body, color = AppTheme.colors.ink, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PreviewSheetFrame(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppTheme.colors.scrim), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, AppTheme.shapes.sheet)
                    .padding(24.dp),
        ) {
            content()
        }
    }
}

@Preview(name = "S66_LocationRationaleSheet_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S66_LocationRationaleSheet_Light() {
    AppTheme(dark = false) { PreviewSheetFrame { LocationRationaleBody(onContinue = {}, onDismiss = {}) } }
}

@Preview(name = "S66_LocationRationaleSheet_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S66_LocationRationaleSheet_Dark() {
    AppTheme(dark = true) { PreviewSheetFrame { LocationRationaleBody(onContinue = {}, onDismiss = {}) } }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.core.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconTile
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ListRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.Tone

/**
 * Artboard s05: full-screen camera permission rationale shown after onboarding. The caller starts the
 * runtime permission request in [onAllow] and continues in [onNotNow]; this composable holds no state.
 */
@Composable
fun CameraRationaleContent(
    onAllow: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.size(48.dp))
            CameraBadge()
            Spacer(Modifier.size(24.dp))
            RationaleHeader()
            Spacer(Modifier.size(32.dp))
            RationalePoints()
            Spacer(Modifier.size(16.dp))
        }
        RationaleActions(onAllow = onAllow, onNotNow = onNotNow)
    }
}

@Composable
private fun CameraBadge() {
    val c = AppTheme.colors
    Box(
        modifier = Modifier.size(160.dp).clip(CircleShape).background(c.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(128.dp).shadow(12.dp, CircleShape).background(c.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Camera, contentDescription = null, tint = c.primary, modifier = Modifier.size(64.dp))
        }
    }
}

@Composable
private fun RationaleHeader() {
    val c = AppTheme.colors
    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.perm_camera_screen_title),
            style = AppTheme.type.headline.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.perm_camera_screen_body),
            style = AppTheme.type.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
            color = c.inkSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RationalePoints() {
    CardGroup {
        ListRow(
            title = stringResource(R.string.perm_camera_point1_title),
            subtitle = stringResource(R.string.perm_camera_point1_body),
            leading = { IconTile(AppIcons.Scan, tone = Tone.Success) },
        )
        AppDivider()
        ListRow(
            title = stringResource(R.string.perm_camera_point2_title),
            subtitle = stringResource(R.string.perm_camera_point2_body),
            leading = { IconTile(AppIcons.Image, tone = Tone.Primary) },
        )
        AppDivider()
        ListRow(
            title = stringResource(R.string.perm_camera_point3_title),
            subtitle = stringResource(R.string.perm_camera_point3_body),
            leading = { IconTile(AppIcons.Shield, tone = Tone.Warning) },
        )
    }
}

@Composable
private fun RationaleActions(onAllow: () -> Unit, onNotNow: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PrimaryButton(
            text = stringResource(R.string.perm_camera_allow),
            onClick = onAllow,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            leadingIcon = AppIcons.Camera,
        )
        AppTextButton(
            text = stringResource(R.string.perm_not_now),
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "S05_CameraRationale_Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S05_CameraRationale_Light() {
    AppTheme(dark = false) { CameraRationaleContent(onAllow = {}, onNotNow = {}) }
}

@Preview(name = "S05_CameraRationale_Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun S05_CameraRationale_Dark() {
    AppTheme(dark = true) { CameraRationaleContent(onAllow = {}, onNotNow = {}) }
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme

enum class BottomTab { Create, Scan, History, Settings }

/**
 * Space at the bottom of the screen taken by the bottom navigation bar (bar height + system nav inset), or just
 * the system nav inset when the bar is hidden. Tab-root screens add this as bottom content padding.
 */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

/** Height of the visible bar (excluding the navigation-bar inset); screens reserve this at the bottom. */
val BottomNavBarHeight = 80.dp

/**
 * Bottom navigation with the raised centre Scan button (design "nv"). Total height is the bar plus 20dp of
 * overhang for the Scan pill, plus the navigation-bar inset. Tabs: Create, Scan (centre), History, Settings.
 */
@Composable
fun BottomNavBar(
    selected: BottomTab?,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(modifier.fillMaxWidth().height(BottomNavBarHeight + 20.dp + navInset)) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(BottomNavBarHeight + navInset).background(c.surface),
        ) {
            AppDivider()
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top,
        ) {
            NavItem(BottomTab.Create, stringResource(R.string.common_nav_create), AppIcons.Create, selected == BottomTab.Create, onSelect)
            NavItem(BottomTab.Scan, stringResource(R.string.common_nav_scan), AppIcons.Scan, selected == BottomTab.Scan, onSelect, center = true)
            NavItem(BottomTab.History, stringResource(R.string.common_nav_history), AppIcons.History, selected == BottomTab.History, onSelect)
            NavItem(BottomTab.Settings, stringResource(R.string.common_nav_settings), AppIcons.Settings, selected == BottomTab.Settings, onSelect)
        }
    }
}

@Composable
private fun NavItem(
    tab: BottomTab,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onSelect: (BottomTab) -> Unit,
    center: Boolean = false,
) {
    val c = AppTheme.colors
    val tint = if (selected || center) c.primary else c.inkSecondary
    Column(
        modifier = Modifier
            .width(72.dp)
            .padding(top = if (center) 0.dp else 28.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(tab) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (center) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = c.primary, spotColor = c.primary)
                    .clip(RoundedCornerShape(24.dp))
                    .background(c.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(32.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(56.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) c.primaryContainer else androidx.compose.ui.graphics.Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (selected) c.onPrimaryContainer else c.inkSecondary, modifier = Modifier.size(24.dp))
            }
        }
        Text(label, style = AppTheme.type.navLabel, color = tint, maxLines = 1)
    }
}

@Preview(name = "BottomNavBar")
@Composable
private fun BottomNavBarPreview() {
    AppTheme(dark = false) { Box(Modifier.background(AppTheme.colors.background)) { BottomNavBar(BottomTab.History, {}) } }
}

@Preview(name = "BottomNavBar dark")
@Composable
private fun BottomNavBarPreviewDark() {
    AppTheme(dark = true) { Box(Modifier.background(AppTheme.colors.background)) { BottomNavBar(BottomTab.Scan, {}) } }
}

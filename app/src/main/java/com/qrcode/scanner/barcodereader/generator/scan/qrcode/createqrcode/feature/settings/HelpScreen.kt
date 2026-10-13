package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppOutlinedButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FaqAccordion
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FaqCatalog
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FaqTopic
import kotlinx.coroutines.launch

@Composable
fun HelpRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var expandedIndex by rememberSaveable { mutableStateOf(NONE_OPEN) }
    HelpScreen(
        expanded = FaqCatalog.topics.getOrNull(expandedIndex),
        onToggle = { topic ->
            expandedIndex = FaqAccordion
                .toggle(FaqCatalog.topics.getOrNull(expandedIndex), topic)
                ?.let { FaqCatalog.topics.indexOf(it) } ?: NONE_OPEN
        },
        onContactSupport = {
            if (!SettingsIntents.openSupportMail(context)) {
                scope.launch {
                    snackbar.showSnackbar(
                        context.getString(R.string.settings_feedback_no_mail_app, context.getString(R.string.settings_support_email)),
                    )
                }
            }
        },
        onBack = onBack,
        snackbarHostState = snackbar,
    )
}

private const val NONE_OPEN = -1

/** Help and FAQ (s59): accordion, one answer open at a time, with a pinned "Contact support" button. */
@Composable
fun HelpScreen(
    expanded: FaqTopic?,
    onToggle: (FaqTopic) -> Unit,
    onContactSupport: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_help_title), onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            ActionBar {
                AppOutlinedButton(
                    text = stringResource(R.string.settings_contact_support),
                    onClick = onContactSupport,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = AppIcons.Email,
                )
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FaqCatalog.topics.forEach { topic ->
                FaqItem(topic = topic, expanded = topic == expanded, onClick = { onToggle(topic) })
            }
        }
    }
}

@Composable
private fun FaqItem(topic: FaqTopic, expanded: Boolean, onClick: () -> Unit) {
    val state = stringResource(if (expanded) R.string.settings_faq_expanded else R.string.settings_faq_collapsed)
    AppCard(contentPadding = PaddingValues(0.dp), modifier = Modifier.animateContentSize()) {
        SettingsRow(
            title = stringResource(faqQuestionRes(topic)),
            minHeight = 64.dp,
            trailing = {
                Icon(
                    imageVector = if (expanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                    contentDescription = null,
                    tint = AppTheme.colors.inkSecondary,
                )
            },
            modifier =
                Modifier
                    .clickable(role = Role.Button, onClick = onClick)
                    .semantics { stateDescription = state },
        )
        if (expanded) {
            Text(
                text = stringResource(faqAnswerRes(topic)),
                style = AppTheme.type.body,
                color = AppTheme.colors.inkSecondary,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

@Preview(name = "S59_Help_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S59_Help_Light() =
    AppTheme(dark = false) {
        HelpScreen(expanded = FaqTopic.WONT_SCAN, onToggle = {}, onContactSupport = {}, onBack = {})
    }

@Preview(name = "S59_Help_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S59_Help_Dark() =
    AppTheme(dark = true) {
        HelpScreen(expanded = FaqTopic.WONT_SCAN, onToggle = {}, onContactSupport = {}, onBack = {})
    }

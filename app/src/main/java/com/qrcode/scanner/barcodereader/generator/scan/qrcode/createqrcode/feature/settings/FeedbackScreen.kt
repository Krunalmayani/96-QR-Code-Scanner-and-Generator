package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ActionBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSnackbarHost
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipItem
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.ChipRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.IconButtonRounded
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.PrimaryButton
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackMail
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.FeedbackTopic

@Composable
fun FeedbackRoute(onBack: () -> Unit, viewModel: FeedbackViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val supportEmail = stringResource(R.string.settings_support_email)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FeedbackEffect.OpenMail ->
                    if (!SettingsIntents.openMail(context, effect.mailtoUri)) {
                        snackbar.showSnackbar(context.getString(R.string.settings_feedback_no_mail_app, supportEmail))
                    }
            }
        }
    }
    FeedbackScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onSend = {
            viewModel.onEvent(
                FeedbackEvent.Send(
                    recipient = supportEmail,
                    appInfo = SettingsIntents.appInfo(),
                    deviceInfo = SettingsIntents.deviceInfo(),
                ),
            )
        },
        onBack = onBack,
        snackbarHostState = snackbar,
    )
}

/** Send feedback form (s61): stars, topic chips, message and optional reply e-mail. Opens the mail app on Send. */
@Composable
fun FeedbackScreen(
    state: FeedbackUiState,
    onEvent: (FeedbackEvent) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    AppScaffold(
        modifier = modifier.imePadding(),
        topBar = { AppTopBar(title = stringResource(R.string.settings_feedback_title), onBack = onBack) },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        bottomBar = {
            ActionBar {
                PrimaryButton(
                    text = stringResource(R.string.settings_feedback_send),
                    onClick = onSend,
                    modifier = Modifier.weight(1f),
                    enabled = state.canSend,
                    leadingIcon = AppIcons.Send,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RatingCard(rating = state.rating, onRate = { onEvent(FeedbackEvent.SetRating(it)) })
            ChipRow(
                chips = FeedbackTopic.entries.map { ChipItem(stringResource(topicLabelRes(it)), it == state.topic) },
                onChipClick = { onEvent(FeedbackEvent.SelectTopic(FeedbackTopic.entries[it])) },
                contentPadding = PaddingValues(0.dp),
            )
            AppTextField(
                value = state.message,
                onValueChange = { onEvent(FeedbackEvent.MessageChanged(it)) },
                label = stringResource(R.string.settings_feedback_message_label),
                singleLine = false,
                minLines = 4,
                maxLines = 8,
            )
            AppTextField(
                value = state.email,
                onValueChange = { onEvent(FeedbackEvent.EmailChanged(it)) },
                label = stringResource(R.string.settings_feedback_email_label),
                placeholder = stringResource(R.string.settings_feedback_email_hint),
                error = if (state.emailError) stringResource(R.string.settings_feedback_email_error) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                onFocusChanged = { focused -> if (!focused) onEvent(FeedbackEvent.EmailFocusLost) },
            )
        }
    }
}

@Composable
private fun RatingCard(rating: Int, onRate: (Int) -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_feedback_rating_label),
            style = AppTheme.type.title,
            color = AppTheme.colors.ink,
        )
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (star in 1..FeedbackMail.MAX_STARS) {
                val filled = star <= rating
                IconButtonRounded(
                    icon = if (filled) AppIcons.Star else AppIcons.StarOutline,
                    contentDescription = stringResource(R.string.settings_feedback_star, star),
                    onClick = { onRate(star) },
                    modifier = Modifier.semantics { selected = filled },
                    tint = if (filled) AppTheme.colors.primary else AppTheme.colors.inkSecondary,
                )
            }
        }
    }
}

private val PreviewState = FeedbackUiState(
    rating = 4,
    topic = FeedbackTopic.SCANNING,
    message = "Scanning small barcodes in low light takes a few tries.",
)

@Preview(name = "S61_Feedback_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S61_Feedback_Light() = AppTheme(dark = false) {
    FeedbackScreen(state = PreviewState, onEvent = {}, onSend = {}, onBack = {})
}

@Preview(name = "S61_Feedback_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S61_Feedback_Dark() = AppTheme(dark = true) {
    FeedbackScreen(state = PreviewState.copy(email = "bad", emailChecked = true), onEvent = {}, onSend = {}, onBack = {})
}

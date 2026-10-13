package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppDivider
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppScaffold
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppSearchBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTopBar
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.CardGroup
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.SettingsRadioRow
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.icons.AppIcons
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.theme.AppTheme
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.repository.ThemeMode
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.settings.Languages

@Composable
fun ThemePickerRoute(onBack: () -> Unit, viewModel: AppearanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ThemePickerScreen(
        selected = state.theme,
        onSelect = { viewModel.onEvent(AppearanceEvent.SelectTheme(it)) },
        onBack = onBack,
    )
}

/** Theme picker (s57): System default, Light, Dark. A tap applies immediately. */
@Composable
fun ThemePickerScreen(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_theme), onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            CardGroup {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    if (index > 0) AppDivider()
                    SettingsRadioRow(
                        title = stringResource(themeLabelRes(mode)),
                        selected = mode == selected,
                        onClick = { onSelect(mode) },
                        icon = themeIcon(mode),
                    )
                }
            }
        }
    }
}

private fun themeIcon(mode: ThemeMode) = when (mode) {
    ThemeMode.SYSTEM -> AppIcons.Palette
    ThemeMode.LIGHT -> AppIcons.LightMode
    ThemeMode.DARK -> AppIcons.DarkMode
}

@Composable
fun LanguagePickerRoute(onBack: () -> Unit, viewModel: AppearanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AppearanceEffect.ApplyLanguage ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(effect.tag))
            }
        }
    }
    LanguagePickerScreen(
        selectedTag = state.languageTag,
        onSelect = { viewModel.onEvent(AppearanceEvent.SelectLanguage(it)) },
        onBack = onBack,
    )
}

/** Language picker (s58): searchable list with "System default" first. Tags are BCP-47 ("hi", "in", ...). */
@Composable
fun LanguagePickerScreen(
    selectedTag: String,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val selected = Languages.find(selectedTag)?.tag ?: Languages.SYSTEM_TAG
    val options = Languages.filter(query)
    val showSystem = query.isBlank()
    AppScaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.settings_language), onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            AppSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(R.string.settings_language_search),
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                if (options.isEmpty() && !showSystem) {
                    Text(
                        text = stringResource(R.string.settings_language_no_match),
                        style = AppTheme.type.body,
                        color = AppTheme.colors.inkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                    )
                } else {
                    CardGroup {
                        if (showSystem) {
                            SettingsRadioRow(
                                title = stringResource(R.string.settings_language_system),
                                selected = selected == Languages.SYSTEM_TAG,
                                onClick = { onSelect(Languages.SYSTEM_TAG) },
                            )
                        }
                        options.forEachIndexed { index, option ->
                            if (showSystem || index > 0) AppDivider()
                            SettingsRadioRow(
                                title = option.nativeName,
                                subtitle = option.englishName.takeIf { it != option.nativeName },
                                selected = selected == option.tag,
                                onClick = { onSelect(option.tag) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "S57_ThemePicker_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S57_ThemePicker_Light() = AppTheme(dark = false) {
    ThemePickerScreen(selected = ThemeMode.SYSTEM, onSelect = {}, onBack = {})
}

@Preview(name = "S57_ThemePicker_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S57_ThemePicker_Dark() = AppTheme(dark = true) {
    ThemePickerScreen(selected = ThemeMode.DARK, onSelect = {}, onBack = {})
}

@Preview(name = "S58_LanguagePicker_Light", widthDp = 360, heightDp = 800)
@Composable
private fun S58_LanguagePicker_Light() = AppTheme(dark = false) {
    LanguagePickerScreen(selectedTag = "", onSelect = {}, onBack = {})
}

@Preview(name = "S58_LanguagePicker_Dark", widthDp = 360, heightDp = 800)
@Composable
private fun S58_LanguagePicker_Dark() = AppTheme(dark = true) {
    LanguagePickerScreen(selectedTag = "es", onSelect = {}, onBack = {})
}

package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.R
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppCard
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.designsystem.components.AppTextField
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.model.ScanRecord
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.create.preview.GeneratedCodePreview

/** s50: the created code's picture, shown for CREATED rows opened from history. */
@Composable
internal fun CreatedPreviewCard(record: ScanRecord, modifier: Modifier = Modifier) {
    AppCard(modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            GeneratedCodePreview(record, Modifier.width(PREVIEW_WIDTH))
        }
    }
}

/** Editable note / label for a history item. Saved by the ViewModel after a short pause and when focus leaves. */
@Composable
internal fun NoteField(note: String, onEvent: (ResultEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppTextField(
            value = note,
            onValueChange = { onEvent(ResultEvent.NoteChanged(it)) },
            label = stringResource(R.string.result_note_label),
            placeholder = stringResource(R.string.result_note_placeholder),
            helper = stringResource(R.string.result_note_helper),
            singleLine = false,
            minLines = 1,
            maxLines = 4,
            modifier = Modifier.padding(top = 4.dp),
            onFocusChanged = { focused -> if (!focused) onEvent(ResultEvent.NoteCommit) },
        )
    }
}

/** 192dp of code plus the 12dp paper margin on each side (s50). */
private val PREVIEW_WIDTH = 216.dp

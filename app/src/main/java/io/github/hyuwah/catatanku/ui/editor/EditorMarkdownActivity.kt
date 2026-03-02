package io.github.hyuwah.catatanku.ui.editor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import dev.jeziellago.compose.markdowntext.MarkdownText
import io.github.hyuwah.catatanku.ui.common.DefaultAppToolBar
import io.github.hyuwah.catatanku.ui.theme.AppTheme

@AndroidEntryPoint
class EditorMarkdownActivity : AppCompatActivity() {
    private val viewModel: EditorViewModel by viewModels()

    private val noteId by lazy {
        intent.getStringExtra("ID").orEmpty()
    }
    private val legacyTitle by lazy {
        intent.getStringExtra("TITLE").orEmpty()
    }
    private val legacyContent by lazy {
        intent.getStringExtra("BODY").orEmpty()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (noteId.isNotBlank()) {
            viewModel.getNoteById(noteId)
        }
        setContent {
            val note by viewModel.note.observeAsState()
            AppTheme {
                Scaffold(
                    topBar = {
                        DefaultAppToolBar(
                            text = legacyTitle.ifBlank { note?.title.orEmpty() }.ifBlank { "Untitled" },
                            onNavClicked = { finish() }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .scrollable(rememberScrollState(), orientation = Orientation.Vertical),
                    ) {
                        MarkdownText(
                            markdown = legacyContent.ifBlank { note?.contentText.orEmpty() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
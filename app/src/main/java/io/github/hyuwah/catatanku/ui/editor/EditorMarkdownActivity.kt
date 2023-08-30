package io.github.hyuwah.catatanku.ui.editor

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.tiagohm.markdownview.css.styles.Github
import dagger.hilt.android.AndroidEntryPoint
import io.github.hyuwah.catatanku.databinding.ActivityEditorMarkdownBinding
import io.github.hyuwah.catatanku.domain.model.Note

@AndroidEntryPoint
class EditorMarkdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorMarkdownBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorMarkdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editorMarkdownView.addStyleSheet(Github())

        viewModel.note.observe(this, ::setupMarkdown)

        if (noteId.isNotBlank()) {
            viewModel.getNoteById(noteId)
        } else {
            title = legacyTitle.ifBlank { "Untitled" }
            binding.editorMarkdownView.loadMarkdown(legacyContent)
        }

    }

    private fun setupMarkdown(note: Note) {
        title = note.title
        binding.editorMarkdownView.loadMarkdown(note.contentText)
    }
}
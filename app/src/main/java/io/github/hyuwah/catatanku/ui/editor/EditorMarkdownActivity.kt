package io.github.hyuwah.catatanku.ui.editor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.hyuwah.catatanku.databinding.ActivityEditorMarkdownBinding
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.utils.markdown.MarkwonFactory

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

    private val markwon by lazy {
        MarkwonFactory.get(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorMarkdownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.note.observe(this, ::setupMarkdown)

        if (noteId.isNotBlank()) {
            viewModel.getNoteById(noteId)
        } else {
            title = legacyTitle.ifBlank { "Untitled" }
            markwon.setMarkdown(binding.markdownContainer, legacyContent)
        }

    }

    private fun setupMarkdown(note: Note) {
        title = note.title
        markwon.setMarkdown(binding.markdownContainer, note.contentText)
    }
}
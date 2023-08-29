package io.github.hyuwah.catatanku.ui.editor

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import br.tiagohm.markdownview.css.styles.Github
import io.github.hyuwah.catatanku.databinding.ActivityEditorMarkdownBinding

class EditorMarkdownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorMarkdownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorMarkdownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        val mTitle = bundle!!.getString("TITLE")
        val mBody = bundle.getString("BODY")

        // TODO make bundle into variable, handle lifecycle change

        // TODO Handle Empty view?
        title = if (TextUtils.isEmpty(mTitle)) {
            "Untitled"
        } else {
            mTitle
        }
        binding.editorMarkdownView.addStyleSheet(Github())
        binding.editorMarkdownView.loadMarkdown(mBody)
    }
}
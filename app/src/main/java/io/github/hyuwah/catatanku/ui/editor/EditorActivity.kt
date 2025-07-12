package io.github.hyuwah.catatanku.ui.editor

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.databinding.ActivityEditorBinding
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.utils.adjustInsets
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private var hasChanged = false

    private val viewModel: EditorViewModel by viewModels()

    private val noteId by lazy {
        intent.getStringExtra("ID").orEmpty()
    }

    /**
     * Lifecycle Override
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24)

        viewModel.note.observe(this, ::onNoteLoaded)

        setupView()
        hasChanged = false
    }

    /**
     * Overflow menu related
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        // Hide delete action if not on edit mode
        if (viewModel.note.value == null) {
            val menuItem = menu.findItem(R.id.editor_action_delete)
            menuItem.isVisible = false
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editor_action_markdown -> {
                val intent = Intent(this, EditorMarkdownActivity::class.java).apply {
                    putExtra("TITLE", binding.editorNoteTitle.text.toString())
                    putExtra("BODY", binding.editorNoteBody.text.toString())
                }
                startActivity(intent)
                return true
            }

            R.id.editor_action_delete -> {
                showDeleteConfirmationDialog(
                    onDeleteClicked = {
                        deleteNote()
                    }
                )
                return true
            }

            R.id.editor_action_clear -> {
                clearEditor()
                return true
            }

            R.id.editor_action_save -> {
                saveNote()
                return true
            }

            android.R.id.home -> {
                return if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog(
                        onDiscardClicked = {
                            NavUtils.navigateUpFromSameTask(this)
                        }
                    )
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Backpress behaviour override
     */
    override fun onBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog(
                onDiscardClicked = {
                    super.onBackPressed()
                }
            )
        } else {
            super.onBackPressed()
        }
    }

    private fun adjustInsets() {
        binding.llNotesInfo.adjustInsets(navigationBar = true)
    }

    private fun hasUnsavedChanges(): Boolean {
        return viewModel.hasUnsavedChanges(
            title = binding.editorNoteTitle.text.toString().trim(),
            contentText = binding.editorNoteBody.text.toString().trim()
        )
    }

    private fun onNoteLoaded(note: Note) {
        binding.editorNoteTitle.setText(note.title)
        binding.editorNoteBody.setText(note.contentText)

        val currentDatetime = Date(note.createdAt)
        val currentDatetimeString = SimpleDateFormat("HH:mm:ss - EE, dd/MM/yy")
            .format(currentDatetime)
        binding.editorNoteDatetime.text = "Created @ $currentDatetimeString"
        binding.editorNoteDatetime.isVisible = true
        invalidateOptionsMenu()
    }

    /**
     * Activity Methods
     */

    private fun setupView() {
        binding.editorNoteBody.doOnTextChanged { text, _, _, _ ->
            statsCount(text.toString())
        }
        if (noteId.isBlank()) {
            title = "Add new note"
            binding.editorNoteDatetime.isGone = true
        } else {
            title = "Edit note"
            viewModel.getNoteById(noteId)
        }
    }

    private fun clearEditor() {
        if (binding.editorNoteTitle.text.isNotBlank() || binding.editorNoteBody.text.isNotBlank()) {
            showClearConfirmationDialog(
                onClearClicked = {
                    hasChanged = true
                    binding.editorNoteTitle.setText("")
                    binding.editorNoteBody.setText("")
                    Toast.makeText(this, "Editor cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun saveNote() {
        val title = binding.editorNoteTitle.text.toString().trim()
        val body = binding.editorNoteBody.text.toString().trim()

        if (title.isBlank() && body.isBlank()) return

        viewModel.save(title, body)
        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
        hasChanged = false
    }

    private fun deleteNote() {
        viewModel.deleteNote(
            onDeleted = {
                Toast.makeText(applicationContext, "Note deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }

    private fun showUnsavedChangesDialog(onDiscardClicked: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("There are unsaved changes!")
            .setPositiveButton("Keep Editing") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .setNegativeButton("Discard") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
                onDiscardClicked.invoke()
            }
            .show()
    }

    private fun showClearConfirmationDialog(onClearClicked: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("This note will be cleared!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .setNegativeButton("Continue") { _: DialogInterface?, _: Int ->
                onClearClicked.invoke()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(onDeleteClicked: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("This note will be deleted!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .setNegativeButton("Delete") { _: DialogInterface?, _: Int ->
                onDeleteClicked.invoke()
            }
            .show()
    }

    private fun statsCount(bodyText: String) {
        val charsCount = bodyText.length
        val wordsCount = bodyText.split(Regex("[\\s\\W]+"))
            .dropLastWhile { it.isEmpty() }
            .count()
        binding.editorNoteStats.text = "$charsCount Chars $wordsCount Words"
    }
}
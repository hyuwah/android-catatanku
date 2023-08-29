package io.github.hyuwah.catatanku.ui.editor

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.ContentValues
import android.content.CursorLoader
import android.content.DialogInterface
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.databinding.ActivityEditorBinding
import io.github.hyuwah.catatanku.utils.storage.NoteContract
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date

class EditorActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var binding: ActivityEditorBinding
    private var currentTitle: String? = null
    private var currentBody: String? = null
    private var currentDatetime: Date? = null
    private var hasChanged = false
    private var mCurrentNote: Uri? = null
    val TAG = this.javaClass.simpleName

    /**
     * Lifecycle Override
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        mCurrentNote = intent.data
        if (mCurrentNote == null) {
            title = "Add new note"
            binding.editorNoteDatetime.visibility = View.GONE
        } else {
            title = "Edit note"
            binding.editorNoteDatetime.visibility = View.VISIBLE
            loaderManager.initLoader(1, null, this)
        }
        hasChanged = false
    }

    /**
     * Overflow menu related
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        // Hide delete action if not on edit mode
        if (mCurrentNote == null) {
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
                val markdownIntent = Intent(this, EditorMarkdownActivity::class.java)
                // TODO emptyview data disini atau di activity nya?
                markdownIntent.putExtra("TITLE", binding.editorNoteTitle.text.toString())
                markdownIntent.putExtra("BODY", binding.editorNoteBody.text.toString())
                startActivity(markdownIntent)
                return true
            }

            R.id.editor_action_delete -> {
                showDeleteConfirmationDialog { dialogInterface: DialogInterface?, i: Int -> deleteNote() }
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
                var titleChanged = false
                var bodyChanged = false
                if (binding.editorNoteTitle.text.toString().trim { it <= ' ' } != currentTitle) {
                    titleChanged = true
                }
                if (binding.editorNoteBody.text.toString().trim { it <= ' ' } != currentBody) {
                    bodyChanged = true
                }

                //Log.i(TAG, "Title: ("+binding.editorNoteTitle.getText().toString().trim()+") - ("+currentTitle+")");
                //Log.i(TAG, "Body: ("+binding.editorNoteBody.getText().toString().trim()+") - ("+currentBody+")");
                Log.i(
                    TAG,
                    "titleChanged: " + titleChanged + " - bodyChanged: " + bodyChanged + " = " + (titleChanged || bodyChanged)
                )
                if (!titleChanged && !bodyChanged) {
                    return super.onOptionsItemSelected(item)
                }
                showUnsavedChangesDialog { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    NavUtils.navigateUpFromSameTask(this@EditorActivity)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Backpress behaviour override
     */
    override fun onBackPressed() {
        var titleChanged = false
        var bodyChanged = false
        if (binding.editorNoteTitle.text.toString().trim { it <= ' ' } != currentTitle) {
            titleChanged = true
        }
        if (binding.editorNoteBody.text.toString().trim { it <= ' ' } != currentBody) {
            bodyChanged = true
        }

        //Log.i(TAG, "Title: ("+binding.editorNoteTitle.getText().toString().trim()+") - ("+currentTitle+")");
        //Log.i(TAG, "Body: ("+binding.editorNoteBody.getText().toString().trim()+") - ("+currentBody+")");
        Log.i(
            TAG,
            "titleChanged: $titleChanged - bodyChanged: $bodyChanged = " + (titleChanged
                    || bodyChanged)
        )
        if (!titleChanged && !bodyChanged) {
            super.onBackPressed()
        } else {
            showUnsavedChangesDialog { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                super.onBackPressed()
            }
        }
    }

    /**
     * CursorLoader Callbacks
     */
    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            NoteContract.NotesEntry._ID,
            NoteContract.NotesEntry.COLUMN_NOTE_TITLE,
            NoteContract.NotesEntry.COLUMN_NOTE_BODY,
            NoteContract.NotesEntry.COLUMN_NOTE_DATETIME
        )
        return CursorLoader(this, mCurrentNote, projection, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        if (cursor.moveToFirst()) {
            currentTitle = cursor
                .getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE))
            currentBody = cursor
                .getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_BODY))
            currentDatetime = Date(
                cursor.getLong(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME))
            )
            val currentDatetimeString = SimpleDateFormat("HH:mm:ss - EE, dd/MM/yy")
                .format(currentDatetime)
            binding.editorNoteTitle.setText(currentTitle)
            binding.editorNoteBody.setText(currentBody)
            binding.editorNoteDatetime.text = "Created @ $currentDatetimeString"
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        binding.editorNoteTitle.setText("")
        binding.editorNoteBody.setText("")
    }

    /**
     * Activity Methods
     */
    fun setHasChanged(): Boolean {
        hasChanged = true
        return false
    }

    private fun setupView() {
        binding.editorNoteTitle.setOnTouchListener { view: View?, motionEvent: MotionEvent? -> setHasChanged() }
        binding.editorNoteBody.setOnTouchListener { view: View?, motionEvent: MotionEvent? -> setHasChanged() }
        binding.editorNoteTitle.imeOptions = EditorInfo.IME_ACTION_NEXT
        binding.editorNoteTitle.setHorizontallyScrolling(false)
        binding.editorNoteTitle.maxLines = 5
        currentTitle = ""
        currentBody = ""

        // Note stats, Body text listener
        binding.editorNoteBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                statsCount(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun clearEditor() {
        if (mCurrentNote != null) {
            showClearConfirmationDialog { dialogInterface: DialogInterface?, i: Int ->
                hasChanged = true
                binding.editorNoteTitle.setText("")
                binding.editorNoteBody.setText("")
                Toast.makeText(this, "Editor cleared", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.editorNoteTitle.setText("")
            binding.editorNoteBody.setText("")
            Toast.makeText(this, "Editor cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        val title = binding.editorNoteTitle.text.toString().trim { it <= ' ' }
        val body = binding.editorNoteBody.text.toString().trim { it <= ' ' }
        val values = ContentValues()
        values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, title)
        values.put(NoteContract.NotesEntry.COLUMN_NOTE_BODY, body)
        if (mCurrentNote == null) {
            // Add new
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, Date().time)
            val newUri = contentResolver.insert(NoteContract.NotesEntry.CONTENT_URI, values)
            if (newUri != null) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Edit existing
            val rowEdited = contentResolver.update(mCurrentNote!!, values, null, null)
            if (rowEdited > 0) {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    private fun deleteNote() {
        val rowDeleted = contentResolver.delete(mCurrentNote!!, null, null)
        if (rowDeleted > 0) {
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun showUnsavedChangesDialog(discardClickListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("There are unsaved changes!")
            .setPositiveButton("Keep Editing") { dialogInterface: DialogInterface?, i: Int -> dialogInterface?.dismiss() }
            .setNegativeButton("Discard", discardClickListener).show()
    }

    private fun showClearConfirmationDialog(clearClickListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This note will be cleared!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> dialogInterface?.dismiss() }
            .setNegativeButton("Continue", clearClickListener).show()
    }

    private fun showDeleteConfirmationDialog(deleteClickListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This note will be deleted!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> dialogInterface?.dismiss() }
            .setNegativeButton("Delete", deleteClickListener).show()
    }

    // on edit text change
    private fun statsCount(bodyText: String) {
        val charsCount = bodyText.length
        val words =
            bodyText.split("[\\s\\W]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Log.i(TAG, "statsCount: " + Arrays.toString(words))
        val wordsCount = if (bodyText.isEmpty()) 0 else words.size
        binding.editorNoteStats.text = "$charsCount Chars $wordsCount Words"
    }
}
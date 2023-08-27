package io.github.hyuwah.catatanku.notelist

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.databinding.ItemNoteListBinding
import io.github.hyuwah.catatanku.utils.storage.NoteContract
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by hyuwah on 26/01/18.
 */
class NoteCursorAdapter(context: Context, c: Cursor?) : CursorAdapter(context, c, 0) {
    var selectedIds: SparseBooleanArray
        private set

    init {
        selectedIds = SparseBooleanArray()
    }

    override fun newView(context: Context, cursor: Cursor, viewGroup: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.item_note_list, viewGroup, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val binding = ItemNoteListBinding.bind(view)

        // Truncate title if too long
        binding.noteTitle.maxLines = 3
        binding.noteTitle.ellipsize = TextUtils.TruncateAt.END

        // Just show excerpt of note body
        binding.noteExcerpt.maxLines = 3
        binding.noteExcerpt.ellipsize = TextUtils.TruncateAt.END

        // Get data from cursor
        var noteTitle =
            cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_TITLE))
        val noteBody =
            cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_BODY))
        val utcTime =
            Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)))
        val noteTime = SimpleDateFormat("EE, dd/MM/yy - HH:mm:ss", Locale.getDefault()).format(utcTime)
        if (TextUtils.isEmpty(noteTitle)) {
            noteTitle = ""
        }
        binding.noteTitle.text = noteTitle
        binding.noteExcerpt.text = noteBody
        binding.noteTime.text = noteTime
    }

    /**
     * Selection on ListView
     */
    fun toggleSelection(position: Int) {
        selectView(position, !selectedIds[position])
    }

    // Remove selection after unchecked
    fun removeSelection() {
        selectedIds = SparseBooleanArray()
        notifyDataSetChanged()
    }

    // Item checked on selection
    private fun selectView(position: Int, value: Boolean) {
        if (value) {
            selectedIds.put(position, value)
        } else {
            selectedIds.delete(position)
        }
        notifyDataSetChanged()
    }

    val selectedCount: Int
        // Get number of selected item
        get() = selectedIds.size()
}
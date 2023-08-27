package io.github.hyuwah.catatanku.notelist

import android.app.LoaderManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import io.github.hyuwah.catatanku.notelist.NoteListContract.Presenter
import io.github.hyuwah.catatanku.utils.storage.NoteContract
import java.util.Date
import kotlin.math.floor

class NoteListPresenter(
    private val mLoaderManager: LoaderManager, 
    private val noteCursorAdapter: NoteCursorAdapter,
    private val mView: NoteListContract.View
) : Presenter, LoaderManager.LoaderCallbacks<Cursor> {

    override fun start() {
        mLoaderManager.initLoader(NOTE_LOADER, null, this)
    }

    /**
     * Implements CursorLoader
     */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            mView.activityContext, NoteContract.NotesEntry.CONTENT_URI,
            NoteContract.NotesEntry.DEFAULT_PROJECTION,
            null,
            null,
            NoteContract.NotesEntry.SORT_TIME_DESC
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        noteCursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        noteCursorAdapter.swapCursor(null)
    }

    override fun searchQuery(s: String?) {
        val selectionClause = (NoteContract.NotesEntry.COLUMN_NOTE_TITLE + " LIKE ? OR "
                + NoteContract.NotesEntry.COLUMN_NOTE_BODY + " LIKE ?")
        val selectionArgs = arrayOf(
            "%$s%",
            "%$s%"
        )
        val cursor = mView.activityContext?.contentResolver?.query(
            NoteContract.NotesEntry.CONTENT_URI,
            NoteContract.NotesEntry.DEFAULT_PROJECTION,
            selectionClause,
            selectionArgs,
            NoteContract.NotesEntry.SORT_TIME_DESC
        )
        if (cursor != null) {
            Log.i(this.javaClass.simpleName, "onQueryTextChange: " + cursor.count)
            noteCursorAdapter.swapCursor(cursor)
        }
    }

    /**
     * Dummy notes data
     */
    override fun generateDummyNotes() {
        for (i in 0 until dummyDataCount) {
            val randomTitleNum = floor(Math.random() * 100).toInt()
            val randomBodyNum = floor(Math.random() * 1000).toInt()
            val values = ContentValues()
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, "Judul$randomTitleNum")
            values.put(
                NoteContract.NotesEntry.COLUMN_NOTE_BODY,
                "$randomBodyNum. Lorem ipsum dolor sit amet"
            )
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, Date().time)
            val newUri = mView.activityContext?.contentResolver?.insert(
                NoteContract.NotesEntry.CONTENT_URI, values
            )
            Log.i(this.javaClass.simpleName, "generateDummyNote: $values")
        }
    }

    override fun deleteAllNotes() {
        val rowsDeleted = mView.activityContext?.contentResolver?.delete(
            NoteContract.NotesEntry.CONTENT_URI, null, null
        )
    }

    override fun deleteSelectedNotes(): Int {
        val rowsDeleted = noteCursorAdapter.selectedCount
        for (x in 0 until noteCursorAdapter.selectedCount) {
            val listId = noteCursorAdapter.selectedIds.keyAt(x)
            val realId = noteCursorAdapter.getItemId(listId)
            //                Log.i(this.getClass().getSimpleName(),
//                    "Selected: list id=" + listId + ", db_id=" + realId);
            mView.activityContext?.contentResolver?.delete(
                    ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, realId),
                    null, null
            )
            //                Log.i(this.getClass().getSimpleName(),
//                    "onActionItemClicked: rowsDeleted=" + rowsDeleted);
        }
        noteCursorAdapter.removeSelection()
        return rowsDeleted
    }

    companion object {
        private const val NOTE_LOADER = 0
        const val dummyDataCount = 10
    }
}
package io.github.hyuwah.catatanku.utils.storage

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log

class NoteProvider : ContentProvider() {

    private lateinit var mDbHelper: CatatanKuDbHelper

    override fun onCreate(): Boolean {
        mDbHelper = CatatanKuDbHelper(context)
        return true
    }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var selection = selection
        var selectionArgs = selectionArgs
        val db = mDbHelper.writableDatabase
        return when (val match = sUriMatcher.match(uri)) {
            NOTES -> {
                val rowsDeleted = db.delete(NoteContract.NotesEntry.TABLE_NAME, selection, selectionArgs)
                if (rowsDeleted != 0) {
                    context?.contentResolver?.notifyChange(uri, null)
                }
                rowsDeleted
            }

            NOTE_ID -> {
                selection = NoteContract.NotesEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                val specificRowsDeleted = db.delete(NoteContract.NotesEntry.TABLE_NAME, selection, selectionArgs)
                if (specificRowsDeleted != 0) {
                    context?.contentResolver?.notifyChange(uri, null)
                }
                specificRowsDeleted
            }

            else -> throw IllegalArgumentException("Unknown URI $uri with match $match")
        }
    }

    override fun getType(uri: Uri): String {
        return when (val match = sUriMatcher.match(uri)) {
            NOTES -> NoteContract.NotesEntry.CONTENT_LIST_TYPE
            NOTE_ID -> NoteContract.NotesEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI $uri with match $match")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (sUriMatcher.match(uri)) {
            NOTES -> insertNote(uri, values)
            else -> throw IllegalArgumentException("Insert is not supported for $uri")
        }
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var selection = selection
        var selectionArgs = selectionArgs
        val db = mDbHelper.readableDatabase
        val cursor: Cursor
        when (sUriMatcher.match(uri)) {
            NOTES -> {
                Log.i(TAG, "query: case Notes")
                cursor = db.query(
                    NoteContract.NotesEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
                Log.i(TAG, "query: has assigned cursor")
            }

            NOTE_ID -> {
                selection = NoteContract.NotesEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                cursor = db.query(
                    NoteContract.NotesEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }

            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var selection = selection
        var selectionArgs = selectionArgs
        return when (sUriMatcher.match(uri)) {
            NOTES -> updateNote(uri, values, selection, selectionArgs)
            NOTE_ID -> {
                selection = NoteContract.NotesEntry._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                updateNote(uri, values, selection, selectionArgs)
            }

            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }
    }

    private fun insertNote(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        values?.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_BODY)
            ?: throw IllegalArgumentException("Note requires body")
        val db = mDbHelper.writableDatabase
        val id = db.insert(NoteContract.NotesEntry.TABLE_NAME, null, values)
        if (id == -1L) {
            Log.e(TAG, "Failed to insert row for $uri")
            return null
        }
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, id)
    }

    private fun updateNote(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values?.containsKey(NoteContract.NotesEntry.COLUMN_NOTE_BODY) == true) {
            values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_BODY)
                ?: throw IllegalArgumentException("Note requires body")
        }
        if (values?.size() == 0) {
            return 0
        }
        val db = mDbHelper.writableDatabase
        val rowsUpdated = db.update(
            NoteContract.NotesEntry.TABLE_NAME, values, selection, selectionArgs
        )
        if (rowsUpdated != 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    companion object {
        val TAG = NoteProvider::class.java.simpleName
        private const val NOTES = 100
        private const val NOTE_ID = 101

        /**
         * Set URI Matcher
         */
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES)
            addURI(NoteContract.CONTENT_AUTHORITY, "${NoteContract.PATH_NOTES}/#", NOTE_ID)
        }
    }
}
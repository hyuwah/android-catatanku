package io.github.hyuwah.catatanku.utils.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.github.hyuwah.catatanku.utils.storage.NoteContract.NotesEntry

/**
 * Created by hyuwah on 26/01/18.
 */
class CatatanKuDbHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        Log.i("CatatanKuDbHelper", "onCreate: Will exec sql :$SQL_CREATE_NOTES_TABLE")
        sqLiteDatabase.execSQL(SQL_CREATE_NOTES_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "catatanku.db"

        // Create Table
        const val SQL_CREATE_NOTES_TABLE = "CREATE TABLE " + NotesEntry.TABLE_NAME +
                " (" + NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NotesEntry.COLUMN_NOTE_DATETIME + " DATETIME NOT NULL, " +
                NotesEntry.COLUMN_NOTE_TITLE + " TEXT, " +
                NotesEntry.COLUMN_NOTE_BODY + " TEXT NOT NULL);"


    }
}
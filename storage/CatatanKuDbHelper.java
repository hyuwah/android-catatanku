package io.github.hyuwah.catatanku.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import io.github.hyuwah.catatanku.storage.NoteContract.NotesEntry;

/**
 * Created by hyuwah on 26/01/18.
 */

public class CatatanKuDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "catatanku.db";

    // Create Table
    public static final String SQL_CREATE_NOTES_TABLE = "CREATE TABLE "+ NotesEntry.TABLE_NAME +
            " (" + NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NotesEntry.COLUMN_NOTE_DATETIME + " DATETIME NOT NULL, " +
            NotesEntry.COLUMN_NOTE_TITLE + " TEXT, " +
            NotesEntry.COLUMN_NOTE_BODY + " TEXT NOT NULL);";

    public CatatanKuDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i("CatatanKuDbHelper", "onCreate: Will exec sql :"+SQL_CREATE_NOTES_TABLE );
        sqLiteDatabase.execSQL(SQL_CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

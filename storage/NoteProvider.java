package io.github.hyuwah.catatanku.storage;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class NoteProvider extends ContentProvider {

    public static final String TAG = NoteProvider.class.getSimpleName();

    private CatatanKuDbHelper mDbHelper;

    private static final int NOTES = 100;
    private static final int NOTE_ID = 101;

    /**
     * Set URI Matcher
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES);
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES + "/#", NOTE_ID);
    }

    // Mandatory empty constructor
    public NoteProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                int rowsDeleted = db.delete(NoteContract.NotesEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rowsDeleted;
            case NOTE_ID:
                selection = NoteContract.NotesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int specificRowsDeleted = db.delete(NoteContract.NotesEntry.TABLE_NAME,selection,selectionArgs);
                if(specificRowsDeleted!=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return specificRowsDeleted;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteContract.NotesEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NotesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
       final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return insertNote(uri, values);
            default:
                throw new IllegalArgumentException("Insert is not supported for " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate: will instantiate mDbHelper");
        mDbHelper = new CatatanKuDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Log.i(TAG, "query: will getReadableDatabase");
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Log.i(TAG, "query: had getReadableDatabase");
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                Log.i(TAG, "query: case Notes");
                cursor = db.query(NoteContract.NotesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                Log.i(TAG, "query: has assigned cursor");
                break;
            case NOTE_ID:
                selection = NoteContract.NotesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(NoteContract.NotesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return updateNote(uri, values, selection, selectionArgs);
            case NOTE_ID:
                selection= NoteContract.NotesEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return updateNote(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);
        }
    }

    /**
     * METHOD Insert, Update, Delete dengan data validation sesuai kebutuhan
     */

    private Uri insertNote(Uri uri, ContentValues values){
        String title = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);
        String body = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_BODY);
        if(body==null){
            throw new IllegalArgumentException("Note requires body");
        }

        long datetime = values.getAsLong(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(NoteContract.NotesEntry.TABLE_NAME, null, values);
        if(id==-1){
            Log.e(TAG, "Failed to insert row for "+uri );
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,id);
    }


    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if(values.containsKey(NoteContract.NotesEntry.COLUMN_NOTE_BODY)){
            String body = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_BODY);
            if(body==null){
                throw new IllegalArgumentException("Note requires body");
            }
        }

        if(values.size()==0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(NoteContract.NotesEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;

    }

}

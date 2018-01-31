package io.github.hyuwah.catatanku.storage;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hyuwah on 26/01/18.
 */

public final class NoteContract {

    public NoteContract() {
    }

    public static final String CONTENT_AUTHORITY = "io.github.hyuwah.catatanku";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NOTES = "notes";

    //Table Notes
    public static abstract class NotesEntry implements BaseColumns {

        public static final String TABLE_NAME = "notes";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NOTE_TITLE = "title";
        public static final String COLUMN_NOTE_BODY = "body";
        public static final String COLUMN_NOTE_DATETIME = "datetime";

        // Query Constant
        public static final String[] DEFAULT_PROJECTION = {
                _ID,
                COLUMN_NOTE_TITLE,
                COLUMN_NOTE_BODY,
                COLUMN_NOTE_DATETIME
        };

        public static final String SORT_TIME_DESC = COLUMN_NOTE_DATETIME+" DESC";
        public static final String SORT_TIME_ASC = COLUMN_NOTE_DATETIME+" ASC";

        // Content Type
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        // URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES);
    }
}

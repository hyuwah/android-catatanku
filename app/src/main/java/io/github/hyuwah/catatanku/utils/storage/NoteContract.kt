package io.github.hyuwah.catatanku.utils.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

/**
 * Created by hyuwah on 26/01/18.
 */
object NoteContract {
    const val CONTENT_AUTHORITY = "io.github.hyuwah.catatanku"
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")
    const val PATH_NOTES = "notes"

    //Table Notes
    object NotesEntry : BaseColumns {
        const val TABLE_NAME = "notes"
        const val _ID = BaseColumns._ID
        const val COLUMN_NOTE_TITLE = "title"
        const val COLUMN_NOTE_BODY = "body"
        const val COLUMN_NOTE_DATETIME = "datetime"

        // Query Constant
        val DEFAULT_PROJECTION = arrayOf(
            _ID,
            COLUMN_NOTE_TITLE,
            COLUMN_NOTE_BODY,
            COLUMN_NOTE_DATETIME
        )
        const val SORT_TIME_DESC = "$COLUMN_NOTE_DATETIME DESC"
        const val SORT_TIME_ASC = "$COLUMN_NOTE_DATETIME ASC"

        // Content Type
        const val CONTENT_LIST_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES
        const val CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES

        // URI
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES)
    }
}
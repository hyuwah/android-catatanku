package io.github.hyuwah.catatanku.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.storage.NoteContract;

/**
 * Created by hyuwah on 26/01/18.
 */

public class NoteCursorAdapter extends CursorAdapter {

    public NoteCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_note_list, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTitle = (TextView) view.findViewById(R.id.note_title);
        TextView tvBody = (TextView) view.findViewById(R.id.note_excerpt);
        TextView tvDatetime = (TextView) view.findViewById(R.id.note_time);

        String noteTitle = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_TITLE));
        String noteBody = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_BODY));
        String noteTime = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)));

        if(TextUtils.isEmpty(noteTitle)){
            noteTitle="";
        }

        tvTitle.setText(noteTitle);
        tvBody.setText(noteBody);
        tvDatetime.setText(noteTime);
    }
}

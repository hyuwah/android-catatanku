package io.github.hyuwah.catatanku.notelist;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.utils.storage.NoteContract;

/**
 * Created by hyuwah on 26/01/18.
 */

public class NoteCursorAdapter extends CursorAdapter {

    TextView tvTitle;
    TextView tvBody;
    TextView tvDatetime;

    private SparseBooleanArray mSelectedItemsIds;

    public NoteCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_note_list, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        tvTitle = view.findViewById(R.id.note_title);
        tvBody = view.findViewById(R.id.note_excerpt);
        tvDatetime = view.findViewById(R.id.note_time);

        // Truncate title if too long
        tvTitle.setMaxLines(3);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);

        // Just show excerpt of note body
        tvBody.setMaxLines(3);
        tvBody.setEllipsize(TextUtils.TruncateAt.END);

        // Get data from cursor
        String noteTitle = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_TITLE));
        String noteBody = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_BODY));

        Date utcTime = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)));
        String noteTime = new SimpleDateFormat("EE, dd/MM/yy - HH:mm:ss").format(utcTime);

        if(TextUtils.isEmpty(noteTitle)){
            noteTitle="";
        }

        tvTitle.setText(noteTitle);
        tvBody.setText(noteBody);
        tvDatetime.setText(noteTime);
    }


    /**
     * Selection on ListView
     */
    public void  toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        }
        else{
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


}

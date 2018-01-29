package io.github.hyuwah.catatanku.adapter;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.hyuwah.catatanku.CustomView.RecyclerViewEmptySupport;
import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.model.Note;
import io.github.hyuwah.catatanku.storage.NoteContract;

/**
 * Created by hyuwah on 26/01/18.
 */

// Using Custom RecyclerViewEmptySupport
public class NoteAdapter extends RecyclerViewEmptySupport.Adapter<NoteAdapter.ViewHolder> {




    /**
     * ViewHolder
     */
    public class ViewHolder extends RecyclerViewEmptySupport.ViewHolder{

        public TextView tvNoteTitle;
        public TextView tvNoteExcerpt;
        public TextView tvNoteDatetime;

        public ViewHolder(final View itemView) {
            super(itemView);

            tvNoteTitle = itemView.findViewById(R.id.note_title);
            tvNoteExcerpt = itemView.findViewById(R.id.note_excerpt);
            tvNoteDatetime = itemView.findViewById(R.id.note_time);

            /**
             * For ItemClickListner
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position!= RecyclerViewEmptySupport.NO_POSITION){
                            listener.OnItemClick(itemView,position);
                        }
                    }
                }
            });
        }
    }

    /**
     * Member variables and Constructor
     */
    private List<Note> mNotes;
    private Context mContext;

    private Cursor mCursor;

    public NoteAdapter(Context mContext,List<Note> mNotes) {
        this.mNotes = mNotes;
        this.mContext = mContext;
    }

    public NoteAdapter(Context context, Cursor cursor){
        this.mContext = context;
        this.mCursor = cursor;
    }

    private Context getContext(){
        return mContext;
    }



    /**
     * Implementation of RecyclerView.Adapter
     */

    // Inflate View
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View noteView = inflater.inflate(R.layout.item_note_list,parent,false);

        ViewHolder viewHolder = new ViewHolder(noteView);

        return viewHolder;
    }

    // Bind data to View
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView tvNoteTitle = holder.tvNoteTitle;
        TextView tvNoteExcerpt = holder.tvNoteExcerpt;
        TextView tvNoteDatetime = holder.tvNoteDatetime;

        if(mNotes!= null && !mNotes.isEmpty()) {
            Note note = mNotes.get(position);

            tvNoteTitle.setText(note.getTitle());
            tvNoteExcerpt.setText(note.getBody());
            tvNoteDatetime.setText(String.valueOf(note.getDatetime()));
        }else{

            if(mCursor!=null) {

                mCursor.moveToPosition(position);

                int testid = mCursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);


                Log.i(this.getClass().getSimpleName(), "onBindViewHolder: "+mCursor.getCount());
                String noteTitle = mCursor.getString(testid);
                String noteBody = mCursor.getString(mCursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_BODY));
                String noteTime = String.valueOf(mCursor.getLong(mCursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)));

                if (TextUtils.isEmpty(noteTitle)) {
                    noteTitle = "";
                }
                tvNoteTitle.setText(noteTitle);
                tvNoteExcerpt.setText(noteBody);
                tvNoteDatetime.setText(String.valueOf(noteTime));
            }
        }

    }

    @Override
    public int getItemCount() {

        return mCursor == null? 0 : mCursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor){
        mCursor = cursor;
        return mCursor;
    }

    /**
     * For OnItemClickListener
     */
    private OnItemClickListener listener;
    public interface OnItemClickListener{
        void OnItemClick(View itemView, int posittion);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


}

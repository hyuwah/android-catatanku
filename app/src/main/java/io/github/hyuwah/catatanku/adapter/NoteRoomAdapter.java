package io.github.hyuwah.catatanku.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.storage.model.Note;
import java.util.List;

/**
 * Created by hyuwah on 05/03/18.
 */

public class NoteRoomAdapter extends RecyclerView.Adapter<NoteRoomAdapter.ViewHolder> {

  public class ViewHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.note_title)
    TextView tvNoteTitle;
    @BindView(R.id.note_excerpt)
    TextView tvNoteExcerpt;
    @BindView(R.id.note_time)
    TextView tvNoteTime;

    public ViewHolder(View itemView) {
      super(itemView);

      ButterKnife.bind(itemView);

    }
  }


  private List<Note> mNotes;
  private Context mContext;

  public NoteRoomAdapter(Context context, List<Note> notes){
    mContext = context;
    mNotes = notes;
  }

  private Context getContext(){
    return mContext;
  }


  public NoteRoomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View noteView = inflater.inflate(R.layout.item_note_list,parent,false);
    ViewHolder viewHolder = new ViewHolder(noteView);

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {

    // Truncate title if too long
    holder.tvNoteTitle.setMaxLines(3);
    holder.tvNoteTitle.setEllipsize(TextUtils.TruncateAt.END);

    // Just show excerpt of note body
    holder.tvNoteExcerpt.setMaxLines(3);
    holder.tvNoteExcerpt.setEllipsize(TextUtils.TruncateAt.END);

    Note note = mNotes.get(position);

    holder.tvNoteTitle.setText(note.getTitle());
    holder.tvNoteExcerpt.setText(note.getBody());
  }

  @Override
  public int getItemCount() {
    return mNotes!=null?mNotes.size():0;
  }
}

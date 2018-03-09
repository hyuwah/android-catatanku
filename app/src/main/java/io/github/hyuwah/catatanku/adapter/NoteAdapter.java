package io.github.hyuwah.catatanku.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.storage.model.Note;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by hyuwah on 06/03/18.
 */

public class NoteAdapter extends ArrayAdapter<Note> {

  private SparseBooleanArray mSelectedItemsIds;

  private static class ViewHolder{
    TextView tvTitle, tvExcerpt, tvTime;
  }

  public NoteAdapter(@NonNull Context context, @NonNull List<Note> objects) {
    super(context, 0, objects);
    mSelectedItemsIds = new SparseBooleanArray();
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

    Note currentNote = getItem(position);

    ViewHolder viewHolder;

    if(convertView==null){
      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.item_note_list,parent,false);
      viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.note_title);
      viewHolder.tvExcerpt = (TextView) convertView.findViewById(R.id.note_excerpt);
      viewHolder.tvTime = (TextView) convertView.findViewById(R.id.note_time);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    // Truncate title if too long
    viewHolder.tvTitle.setMaxLines(3);
    viewHolder.tvTitle.setEllipsize(TextUtils.TruncateAt.END);

    // Just show excerpt of note body
    viewHolder.tvExcerpt.setMaxLines(3);
    viewHolder.tvExcerpt.setEllipsize(TextUtils.TruncateAt.END);

    viewHolder.tvTitle.setText(currentNote.getTitle());
    viewHolder.tvExcerpt.setText(currentNote.getBody());
    Date utcTime = new Date(currentNote.getCreatedTime());
    viewHolder.tvTime.setText(new SimpleDateFormat("EEEE, dd MMM yyyy\nHH:mm:ss").format(utcTime));

    return convertView;
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

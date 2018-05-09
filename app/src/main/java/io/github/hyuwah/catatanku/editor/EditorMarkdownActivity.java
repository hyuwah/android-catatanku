package io.github.hyuwah.catatanku.editor;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.notelist.NoteListActivity;
import io.github.hyuwah.catatanku.utils.storage.NoteContract;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditorMarkdownActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  @BindView(R.id.editor_markdown_view)
  MarkdownView markdownView;

  private Uri mCurrentNote;
  private String currentTitle;
  private String currentBody;
  private Date currentDatetime;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_markdown);

    ButterKnife.bind(this);

    mCurrentNote = getIntent().getData();
    if(null!=mCurrentNote) { //From NoteList
      getLoaderManager().initLoader(1, null, this);
    } else { // From Editor
      Bundle bundle = getIntent().getExtras();
      String mTitle = bundle.getString("TITLE");
      String mBody = bundle.getString("BODY");

      showMarkdown(mTitle, mBody);
    }

    // TODO make bundle into variable, handle lifecycle change

    // TODO Handle Empty view?

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if(null!=mCurrentNote)
      getMenuInflater().inflate(R.menu.menu_note_detail_markdown, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()){
      case R.id.detail_markown_action_edit:
        Intent intent = new Intent(EditorMarkdownActivity.this, EditorActivity.class);
        intent.setData(mCurrentNote);
        startActivity(intent);
        finish();
        break;
      case R.id.detail_markdown_action_delete:
        showDeleteConfirmationDialog((dialogInterface, i) -> deleteNote());
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * CursorLoader Callbacks
   */

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    String[] projection = {
        NoteContract.NotesEntry._ID,
        NoteContract.NotesEntry.COLUMN_NOTE_TITLE,
        NoteContract.NotesEntry.COLUMN_NOTE_BODY,
        NoteContract.NotesEntry.COLUMN_NOTE_DATETIME
    };
    return new CursorLoader(this, mCurrentNote, projection, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (cursor.moveToFirst()) {
      currentTitle = cursor
          .getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE));
      currentBody = cursor
          .getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_BODY));
      currentDatetime = new Date(
          cursor.getLong(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)));
      String currentDatetimeString = new SimpleDateFormat("HH:mm:ss - EE, dd/MM/yy")
          .format(currentDatetime);

      showMarkdown(currentTitle, currentBody);

      Toast.makeText(this,"Created @ " + currentDatetimeString,Toast.LENGTH_SHORT);


    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {


  }

  public void showMarkdown(String title, String body){
    if(TextUtils.isEmpty(title)){
      setTitle("Untitled");
    }else{
      setTitle(title);
    }

    markdownView.addStyleSheet(new Github());
    markdownView.loadMarkdown(body);
  }

  private void deleteNote() {

    int rowDeleted = getContentResolver().delete(mCurrentNote, null, null);

    if (rowDeleted > 0) {
      Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
    }

    finish();
  }

  private void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteClickListener) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("This note will be deleted!")
        .setPositiveButton("Cancel", (dialogInterface, i) -> {
          if (dialogInterface != null) {
            dialogInterface.dismiss();
          }
        })
        .setNegativeButton("Delete", deleteClickListener).show();
  }
}

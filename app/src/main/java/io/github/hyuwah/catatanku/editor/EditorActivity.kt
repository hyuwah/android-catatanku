package io.github.hyuwah.catatanku.editor;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.utils.storage.NoteContract;

public class EditorActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor> {

  //Views
  EditText etTitle;
  EditText etBody;
  TextView tvDatetime;
  TextView tvStats;

  private String currentTitle;
  private String currentBody;
  private Date currentDatetime;
  private boolean hasChanged;

  private Uri mCurrentNote;

  final String TAG = this.getClass().getSimpleName();

  /**
   * Lifecycle Override
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);
    setupView();

    mCurrentNote = getIntent().getData();

    if (mCurrentNote == null) {
      setTitle("Add new note");
      tvDatetime.setVisibility(View.GONE);
    } else {
      setTitle("Edit note");
      tvDatetime.setVisibility(View.VISIBLE);
      getLoaderManager().initLoader(1, null, this);
    }

    hasChanged = false;
  }

  /**
   * Overflow menu related
   */

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    // Hide delete action if not on edit mode
    if (mCurrentNote == null) {
      MenuItem menuItem = menu.findItem(R.id.editor_action_delete);
      menuItem.setVisible(false);
    }

    return true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(R.menu.menu_note_editor, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {

      case R.id.editor_action_markdown:
        Intent markdownIntent = new Intent(this, EditorMarkdownActivity.class);
        // TODO emptyview data disini atau di activity nya?
        markdownIntent.putExtra("TITLE", etTitle.getText().toString());
        markdownIntent.putExtra("BODY", etBody.getText().toString());
        startActivity(markdownIntent);

        return true;

      case R.id.editor_action_delete:
        showDeleteConfirmationDialog((dialogInterface, i) -> deleteNote());
        return true;

      case R.id.editor_action_clear:
        clearEditor();
        return true;

      case R.id.editor_action_save:
        saveNote();
        return true;

      case android.R.id.home:

        boolean titleChanged = false;
        boolean bodyChanged = false;
        if (!etTitle.getText().toString().trim().equals(currentTitle)) {
          titleChanged = true;
        }
        if (!etBody.getText().toString().trim().equals(currentBody)) {
          bodyChanged = true;
        }

        //Log.i(TAG, "Title: ("+etTitle.getText().toString().trim()+") - ("+currentTitle+")");
        //Log.i(TAG, "Body: ("+etBody.getText().toString().trim()+") - ("+currentBody+")");

        Log.i(TAG, "titleChanged: " + titleChanged + " - bodyChanged: " + bodyChanged + " = " + (
            titleChanged || bodyChanged));
        if (!titleChanged && !bodyChanged) {
          return super.onOptionsItemSelected(item);
        }
        showUnsavedChangesDialog((dialogInterface, i) -> {
          dialogInterface.dismiss();
          NavUtils.navigateUpFromSameTask(EditorActivity.this);
        });

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Backpress behaviour override
   */

  @Override
  public void onBackPressed() {

    boolean titleChanged = false;
    boolean bodyChanged = false;
    if (!etTitle.getText().toString().trim().equals(currentTitle)) {
      titleChanged = true;
    }
    if (!etBody.getText().toString().trim().equals(currentBody)) {
      bodyChanged = true;
    }

    //Log.i(TAG, "Title: ("+etTitle.getText().toString().trim()+") - ("+currentTitle+")");
    //Log.i(TAG, "Body: ("+etBody.getText().toString().trim()+") - ("+currentBody+")");

    Log.i(TAG,
        "titleChanged: " + titleChanged + " - bodyChanged: " + bodyChanged + " = " + (titleChanged
            || bodyChanged));
    if (!titleChanged && !bodyChanged) {
      super.onBackPressed();
    } else {
      showUnsavedChangesDialog((dialogInterface, i) -> {
        dialogInterface.dismiss();
        super.onBackPressed();
      });
    }

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

      etTitle.setText(currentTitle);
      etBody.setText(currentBody);
      tvDatetime.setText("Created @ " + currentDatetimeString);


    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    etTitle.setText("");
    etBody.setText("");

  }

  /**
   * Activity Methods
   */

  public boolean setHasChanged() {
    hasChanged = true;
    return false;
  }

  private void setupView() {

    etTitle = findViewById(R.id.editor_note_title);
    etBody = findViewById(R.id.editor_note_body);
    tvDatetime = findViewById(R.id.editor_note_datetime);
    tvStats = findViewById(R.id.editor_note_stats);

    etTitle.setOnTouchListener((view, motionEvent) -> setHasChanged());
    etBody.setOnTouchListener((view, motionEvent) -> setHasChanged());

    etTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    etTitle.setHorizontallyScrolling(false);
    etTitle.setMaxLines(5);

    currentTitle = "";
    currentBody = "";

    // Note stats, Body text listener
    etBody.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        statsCount(charSequence.toString());
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });

  }

  private void clearEditor() {

    if (mCurrentNote != null) {
      showClearConfirmationDialog((dialogInterface, i) -> {
        hasChanged = true;
        etTitle.setText("");
        etBody.setText("");
        Toast.makeText(this, "Editor cleared", Toast.LENGTH_SHORT).show();
      });
    } else {
      etTitle.setText("");
      etBody.setText("");
      Toast.makeText(this, "Editor cleared", Toast.LENGTH_SHORT).show();
    }

  }

  private void saveNote() {

    String title = etTitle.getText().toString().trim();
    String body = etBody.getText().toString().trim();

    ContentValues values = new ContentValues();
    values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, title);
    values.put(NoteContract.NotesEntry.COLUMN_NOTE_BODY, body);

    if (mCurrentNote == null) {
      // Add new
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, new Date().getTime());

      Uri newUri = getContentResolver().insert(NoteContract.NotesEntry.CONTENT_URI, values);

      if (newUri != null) {
        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
      }

    } else {
      // Edit existing
      int rowEdited = getContentResolver().update(mCurrentNote, values, null, null);
      if (rowEdited > 0) {
        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
      }
    }

    finish();
  }

  private void deleteNote() {

    int rowDeleted = getContentResolver().delete(mCurrentNote, null, null);

    if (rowDeleted > 0) {
      Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
    }

    finish();
  }

  private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardClickListener) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("There are unsaved changes!")
        .setPositiveButton("Keep Editing", (dialogInterface, i) -> {
          if (dialogInterface != null) {
            dialogInterface.dismiss();
          }
        })
        .setNegativeButton("Discard", discardClickListener).show();
  }

  private void showClearConfirmationDialog(DialogInterface.OnClickListener clearClickListener) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("This note will be cleared!")
        .setPositiveButton("Cancel", (dialogInterface, i) -> {
          if (dialogInterface != null) {
            dialogInterface.dismiss();
          }
        })
        .setNegativeButton("Continue", clearClickListener).show();
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

  // on edit text change
  private void statsCount(String bodyText) {
    int charsCount = bodyText.length();

    String[] words = bodyText.split("[\\s\\W]+");
    Log.i(TAG, "statsCount: " + Arrays.toString(words));
    int wordsCount = bodyText.isEmpty() ? 0 : words.length;

    tvStats.setText(charsCount + " Chars " + wordsCount + " Words");

  }
}

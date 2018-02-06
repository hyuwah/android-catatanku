package io.github.hyuwah.catatanku;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import io.github.hyuwah.catatanku.storage.NoteContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Views
    @BindView(R.id.editor_note_title) EditText etTitle;
    @BindView(R.id.editor_note_body) EditText etBody;
    @BindView(R.id.editor_note_datetime) TextView tvDatetime;

    private String currentTitle;
    private String currentBody;
    private Date currentDatetime;
    private boolean hasChanged;

    private Uri mCurrentNote;

    final String TAG = this.getClass().getSimpleName();

    /**
     *  Lifecycle Override
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

            case R.id.editor_action_delete:
                showDeleteConfirmationDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteNote();
                    }
                });
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
                if (!etTitle.getText().toString().trim().equals(currentTitle)) titleChanged = true;
                if (!etBody.getText().toString().trim().equals(currentBody)) bodyChanged = true;

                //Log.i(TAG, "Title: ("+etTitle.getText().toString().trim()+") - ("+currentTitle+")");
                //Log.i(TAG, "Body: ("+etBody.getText().toString().trim()+") - ("+currentBody+")");

                Log.i(TAG, "titleChanged: " + titleChanged + " - bodyChanged: " + bodyChanged + " = " + (titleChanged || bodyChanged));
                if (!titleChanged && !bodyChanged) {
                    return super.onOptionsItemSelected(item);
                }
                showUnsavedChangesDialog((dialogInterface, i) -> {
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
        if (!etTitle.getText().toString().trim().equals(currentTitle)) titleChanged = true;
        if (!etBody.getText().toString().trim().equals(currentBody)) bodyChanged = true;

        //Log.i(TAG, "Title: ("+etTitle.getText().toString().trim()+") - ("+currentTitle+")");
        //Log.i(TAG, "Body: ("+etBody.getText().toString().trim()+") - ("+currentBody+")");

        Log.i(TAG, "titleChanged: " + titleChanged + " - bodyChanged: " + bodyChanged + " = " + (titleChanged || bodyChanged));
        if (!titleChanged && !bodyChanged) {
            super.onBackPressed();
        } else {
            showUnsavedChangesDialog((dialogInterface, i) -> {
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
            currentTitle = cursor.getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE));
            currentBody = cursor.getString(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_BODY));
            currentDatetime = new Date(cursor.getLong(cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME)));
            String currentDatetimeString = new SimpleDateFormat("HH:mm:ss - EEEE, dd MMM yyyy").format(currentDatetime);

            etTitle.setText(currentTitle);
            etBody.setText(currentBody);
            tvDatetime.setText("Created at ➞ " + currentDatetimeString);
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

    @OnTouch({R.id.editor_note_title,R.id.editor_note_body})
    public boolean setHasChanged(){
        hasChanged = true;
        return false;
    }

    private void setupView() {

        ButterKnife.bind(this);

        etTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etTitle.setHorizontallyScrolling(false);
        etTitle.setMaxLines(5);

        currentTitle = "";
        currentBody = "";

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
}

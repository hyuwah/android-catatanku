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
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.hyuwah.catatanku.adapter.NoteCursorAdapter;
import io.github.hyuwah.catatanku.storage.NoteContract;

public class NoteListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Views
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.lv_note_list)
    ListView lvNoteList;
    @BindView(R.id.empty_note_list_view)
    View lvEmptyNoteList;


    @BindColor(R.color.white)
    int color_white;
    @BindColor(R.color.black)
    int color_black;

    private Toast mToast;

    private int dummyDataCount = 10;

    // Storage
    private static final int NOTE_LOADER = 0;
    NoteCursorAdapter noteCursorAdapter;

    /**
     * Lifecycle Override
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        ButterKnife.bind(this);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(NoteListActivity.this, EditorActivity.class);
            startActivity(intent);
        });


        noteCursorAdapter = new NoteCursorAdapter(this, null);
        // noteAdapter = new NoteAdapter(NoteListActivity.this, mCursor);

        lvNoteList.setAdapter(noteCursorAdapter);
        lvNoteList.setEmptyView(lvEmptyNoteList);

        // Multi select listview
        lvNoteList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvNoteList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                final int checkedCount = lvNoteList.getCheckedItemCount();
                actionMode.setTitle(checkedCount + " Selected");
                noteCursorAdapter.toggleSelection(i);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.menu_note_list_onselect, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.onselect_select_all:
                        Toast.makeText(NoteListActivity.this, "select all", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.onselect_delete:
                        //Toast.makeText(NoteListActivity.this, noteCursorAdapter.getSelectedIds() + " delete" + noteCursorAdapter.getSelectedCount(), Toast.LENGTH_SHORT).show();

                        //TODO dialog confirmation delete
                        showDeleteConfirmationDialog((dialogInterface, i) -> {
                            for (int x = 0; x < noteCursorAdapter.getSelectedCount(); x++) {
                                int listId = noteCursorAdapter.getSelectedIds().keyAt(x);
                                long realId = noteCursorAdapter.getItemId(listId);
                                Log.i(this.getClass().getSimpleName(), "Selected: list id=" + listId+", db_id="+realId);
                                int rowsDeleted = getContentResolver().delete(ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, realId), null, null);
                                Log.i(this.getClass().getSimpleName(), "onActionItemClicked: rowsDeleted=" + rowsDeleted);

                            }
                            Toast.makeText(NoteListActivity.this, "Deleted "+noteCursorAdapter.getSelectedCount()+" notes", Toast.LENGTH_SHORT).show();
                            noteCursorAdapter.removeSelection();
                            actionMode.finish();
                        });
                        return true;
                    default:
                        return false;

                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });

        lvNoteList.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(NoteListActivity.this, EditorActivity.class);
            Uri currentNoteUri = ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, id);
            intent.setData(currentNoteUri);
            startActivity(intent);
        });

        getLoaderManager().initLoader(NOTE_LOADER, null, this);

    }

    /**
     * Overflow Menu Related
     */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem insertDummyData = menu.findItem(R.id.action_insert);
        insertDummyData.setTitle("Insert " + String.valueOf(dummyDataCount) + " data");
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_delete_all:
                int rowsDeleted = getContentResolver().delete(NoteContract.NotesEntry.CONTENT_URI, null, null);

                return true;
            case R.id.action_insert:
                //  generateOneDummyNote();
                generateOneDummyNoteDB();
                return true;

            case R.id.action_gitbook_journal:

                // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
                String url = "https://hyuwah.gitbooks.io/journal-refactory/content/";
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccent));
                // add share action to menu list
                builder.addDefaultShareMenuItem();
                // set toolbar color and/or setting custom actions before invoking build()
                // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
                CustomTabsIntent customTabsIntent = builder.build();
                // and launch the desired Url with CustomTabsIntent.launchUrl()
                customTabsIntent.launchUrl(this, Uri.parse(url));

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    /**
     * Dummy notes data
     */

    private void generateOneDummyNoteDB() {

        for (int i = 0; i < dummyDataCount; i++) {

            int randomTitleNum = (int) Math.floor(Math.random() * 100);
            int randomBodyNum = (int) Math.floor(Math.random() * 1000);

            ContentValues values = new ContentValues();
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, "Judul" + randomTitleNum);
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_BODY, randomBodyNum + ". Lorem ipsum dolor sit amet");
            values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, new Date().getTime());
            Uri newUri = getContentResolver().insert(NoteContract.NotesEntry.CONTENT_URI, values);
            Log.i(this.getClass().getSimpleName(), "generateOneDummyNoteDB: " + values.toString());
        }

    }

    /**
     * Implements CursorLoader
     */

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                NoteContract.NotesEntry._ID,
                NoteContract.NotesEntry.COLUMN_NOTE_TITLE,
                NoteContract.NotesEntry.COLUMN_NOTE_BODY,
                NoteContract.NotesEntry.COLUMN_NOTE_DATETIME,
        };

        return new CursorLoader(this, NoteContract.NotesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        noteCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        noteCursorAdapter.swapCursor(null);
    }

    /**
     *  Activity Methods
     */

    private void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Selected notes will be deleted!")
                .setPositiveButton("Cancel", (dialogInterface, i) -> {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Delete", deleteClickListener).show();
    }

}

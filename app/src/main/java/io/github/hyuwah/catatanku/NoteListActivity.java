package io.github.hyuwah.catatanku;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.SearchView;
import android.widget.Toast;

import java.util.Date;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.hyuwah.catatanku.adapter.NoteCursorAdapter;
import io.github.hyuwah.catatanku.chrome.CustomTabActivityHelper;
import io.github.hyuwah.catatanku.storage.NoteContract;

public class NoteListActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor> {

  // Views
  @BindView(R.id.fab)
  FloatingActionButton fab;
  @BindView(R.id.lv_note_list)
  ListView lvNoteList;
  @BindView(R.id.empty_note_list_view)
  View lvEmptyNoteList;


  // Debugging variable
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

    prepareListView();

    getLoaderManager().initLoader(NOTE_LOADER, null, this);

  }

  @Override
  protected void onStart() {
    super.onStart();

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

    //Associate searchable config with the Searchview
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String s) {

        String selectionClause = NoteContract.NotesEntry.COLUMN_NOTE_TITLE + " LIKE ? OR "
            + NoteContract.NotesEntry.COLUMN_NOTE_BODY + " LIKE ?";
        String[] selectionArgs = new String[]{
            "%" + s + "%",
            "%" + s + "%"
        };

        Cursor cursor = getContentResolver().query(
            NoteContract.NotesEntry.CONTENT_URI,
            NoteContract.NotesEntry.DEFAULT_PROJECTION,
            selectionClause,
            selectionArgs,
            NoteContract.NotesEntry.SORT_TIME_DESC
        );
        Log.i(this.getClass().getSimpleName(), "onQueryTextChange: " + cursor.getCount());
        if (cursor != null) {
          noteCursorAdapter.swapCursor(cursor);
        }

        return false;
      }
    });

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_delete_all:
        int rowsDeleted = getContentResolver()
            .delete(NoteContract.NotesEntry.CONTENT_URI, null, null);
        return true;

      case R.id.action_insert:
        generateDummyNote();
        return true;

      case R.id.action_gitbook_journal:
        openGitbookJournal();
        return true;

      case R.id.action_about:
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        return true;

      case R.id.action_search:
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setFocusable(true);
        searchView.setQueryHint("Text to search");
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        return false;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * Implements CursorLoader
   */

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

    return new CursorLoader(this, NoteContract.NotesEntry.CONTENT_URI,
        NoteContract.NotesEntry.DEFAULT_PROJECTION,
        null,
        null,
        NoteContract.NotesEntry.SORT_TIME_DESC);
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
   * Activity Methods
   */

  // ListView Related
  private void prepareListView() {

    noteCursorAdapter = new NoteCursorAdapter(this, null);

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

      // ActionMode Menu
      @Override
      public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        switch (menuItem.getItemId()) {

          case R.id.onselect_select_all:

            noteCursorAdapter.removeSelection();
            for (int x = 0; x < noteCursorAdapter.getCount(); x++) {
              lvNoteList.setItemChecked(x, true);
            }
            final int checkedCount = lvNoteList.getCheckedItemCount();
            actionMode.setTitle(checkedCount + " Selected");

            return true;
          case R.id.onselect_delete:

            showDeleteConfirmationDialog((dialogInterface, i) -> {
              for (int x = 0; x < noteCursorAdapter.getSelectedCount(); x++) {
                int listId = noteCursorAdapter.getSelectedIds().keyAt(x);
                long realId = noteCursorAdapter.getItemId(listId);
                Log.i(this.getClass().getSimpleName(),
                    "Selected: list id=" + listId + ", db_id=" + realId);
                int rowsDeleted = getContentResolver()
                    .delete(ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, realId),
                        null, null);
                Log.i(this.getClass().getSimpleName(),
                    "onActionItemClicked: rowsDeleted=" + rowsDeleted);

              }
              Toast.makeText(NoteListActivity.this,
                  "Deleted " + noteCursorAdapter.getSelectedCount() + " notes", Toast.LENGTH_SHORT)
                  .show();
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
        noteCursorAdapter.removeSelection();
      }
    });

    // Onclick listView
    lvNoteList.setOnItemClickListener((adapterView, view, position, id) -> {
      Intent intent = new Intent(NoteListActivity.this, EditorActivity.class);
      Uri currentNoteUri = ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, id);
      intent.setData(currentNoteUri);
      startActivity(intent);
    });

  }

  private void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteClickListener) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Selected note(s) will be deleted!")
        .setPositiveButton("Cancel", (dialogInterface, i) -> {
          if (dialogInterface != null) {
            dialogInterface.dismiss();
          }
        })
        .setNegativeButton("Delete", deleteClickListener).show();
  }

  private void openGitbookJournal() {
    String url = "https://hyuwah.gitbooks.io/journal-refactory/content/";
    Uri uri = Uri.parse(url);

    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    builder.addDefaultShareMenuItem();
    builder.setShowTitle(true);

    CustomTabsIntent customTabsIntent = builder.build();
    CustomTabActivityHelper.openCustomTab(this, customTabsIntent, uri,
        (activity, uri1) -> {
          Intent intent = new Intent(Intent.ACTION_VIEW, uri1);
          activity.startActivity(intent);
        });
  }

  /**
   * Dummy notes data
   */

  private void generateDummyNote() {

    for (int i = 0; i < dummyDataCount; i++) {

      int randomTitleNum = (int) Math.floor(Math.random() * 100);
      int randomBodyNum = (int) Math.floor(Math.random() * 1000);

      ContentValues values = new ContentValues();
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, "Judul" + randomTitleNum);
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_BODY,
          randomBodyNum + ". Lorem ipsum dolor sit amet");
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, new Date().getTime());

      Uri newUri = getContentResolver().insert(NoteContract.NotesEntry.CONTENT_URI, values);
      Log.i(this.getClass().getSimpleName(), "generateDummyNote: " + values.toString());
    }

  }

}

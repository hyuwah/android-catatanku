package io.github.hyuwah.catatanku.notelist;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.github.hyuwah.catatanku.R;
import io.github.hyuwah.catatanku.about.AboutActivity;
import io.github.hyuwah.catatanku.editor.EditorActivity;
import io.github.hyuwah.catatanku.utils.chrome.CustomTabActivityHelper;
import io.github.hyuwah.catatanku.utils.storage.NoteContract;

public class NoteListActivity extends AppCompatActivity implements
    NoteListContract.View {

  // Views
  FloatingActionButton fab;
  ListView lvNoteList;
  View lvEmptyNoteList;

  Menu menu;

  private NoteListPresenter mPresenter;

  // Double tap back to exit
  private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
  private long mBackPressed;

  // Debugging variable
  private Toast mToast;

  NoteCursorAdapter noteCursorAdapter;

  SharedPreferences sharedPref;

  /**
   * Lifecycle Override
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_note_list);

    fab = findViewById(R.id.fab);
    lvNoteList = findViewById(R.id.lv_note_list);
    lvEmptyNoteList = findViewById(R.id.empty_note_list_view);

    fab.setOnClickListener(view -> {
      Intent intent = new Intent(NoteListActivity.this, EditorActivity.class);
      startActivity(intent);
    });

    prepareListView();

    mPresenter = new NoteListPresenter(getLoaderManager(), noteCursorAdapter, this);

    checkSharedPreferences();

  }

  @Override
  protected void onStart() {
    super.onStart();
    invalidateOptionsMenu();
  }

  @Override
  public void onResume() {
    super.onResume();
    // need to kickoff the loadermanager again
    mPresenter.start();
  }

  @Override
  public void onBackPressed() {

    if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
      super.onBackPressed();
      return;
    } else {
      Toast.makeText(getBaseContext(), "Tap back again to exit", Toast.LENGTH_SHORT).show();
    }

    mBackPressed = System.currentTimeMillis();
  }

  /**
   * Overflow Menu Related
   */

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem insertDummyData = menu.findItem(R.id.action_insert);
    insertDummyData
        .setTitle("Insert " + String.valueOf(NoteListPresenter.dummyDataCount) + " data");
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
        mPresenter.searchQuery(s);
        return false;
      }
    });

    // Get inflated menu
    this.menu = menu;
    // Check SharedPreferences for Overflow Menu
    boolean debugToggle = sharedPref.getBoolean(getString(R.string.pref_key_isdebug), false);
    menu.findItem(R.id.action_group_debug).setVisible(debugToggle);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_delete_all:
        mPresenter.deleteAllNotes();
        return true;

      case R.id.action_insert:
        mPresenter.generateDummyNotes();
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

              int rowsDeleted = mPresenter.deleteSelectedNotes();
              Toast.makeText(NoteListActivity.this,
                  "Deleted " + rowsDeleted + (rowsDeleted>1?" notes":" note"), Toast.LENGTH_SHORT)
                  .show();
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

  @Override
  public void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteClickListener) {

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

  private void checkSharedPreferences() {

    sharedPref = this.getSharedPreferences(
        getString(R.string.pref_file_key), Context.MODE_PRIVATE);
  }

  @Override
  public Context getActivityContext() {
    return this;
  }

  @Override
  public void setPresenter(NoteListContract.Presenter presenter) {
//    mPresenter = presenter;
  }
}

package io.github.hyuwah.catatanku.notelist;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import io.github.hyuwah.catatanku.utils.storage.NoteContract;
import java.util.Date;

public class NoteListPresenter implements NoteListContract.Presenter,
    LoaderManager.LoaderCallbacks<Cursor> {

  private static final int NOTE_LOADER = 0;
  private NoteCursorAdapter noteCursorAdapter;
  private final NoteListContract.View mView;
  private final LoaderManager mLoaderManager;

  public static final int dummyDataCount = 10;


  public NoteListPresenter(@NonNull LoaderManager loaderManager, @NonNull NoteCursorAdapter adapter,
      @NonNull NoteListContract.View view) {
    mLoaderManager = loaderManager;
    mView = view;
    noteCursorAdapter = adapter;
    mView.setPresenter(this);
  }

  @Override
  public Context getActivityContext() {
    return null;
  }

  @Override
  public void start() {
    mLoaderManager.initLoader(NOTE_LOADER, null, this);
  }

  /**
   * Implements CursorLoader
   */
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(mView.getActivityContext(), NoteContract.NotesEntry.CONTENT_URI,
        NoteContract.NotesEntry.DEFAULT_PROJECTION,
        null,
        null,
        NoteContract.NotesEntry.SORT_TIME_DESC);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    noteCursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    noteCursorAdapter.swapCursor(null);
  }

  @Override
  public void searchQuery(String s) {
    String selectionClause = NoteContract.NotesEntry.COLUMN_NOTE_TITLE + " LIKE ? OR "
        + NoteContract.NotesEntry.COLUMN_NOTE_BODY + " LIKE ?";
    String[] selectionArgs = new String[]{
        "%" + s + "%",
        "%" + s + "%"
    };

    Cursor cursor = mView.getActivityContext().getContentResolver().query(
        NoteContract.NotesEntry.CONTENT_URI,
        NoteContract.NotesEntry.DEFAULT_PROJECTION,
        selectionClause,
        selectionArgs,
        NoteContract.NotesEntry.SORT_TIME_DESC
    );
    if (cursor != null) {
      Log.i(this.getClass().getSimpleName(), "onQueryTextChange: " + cursor.getCount());
      noteCursorAdapter.swapCursor(cursor);
    }
  }


  /**
   * Dummy notes data
   */

  @Override
  public void generateDummyNotes() {
    for (int i = 0; i < dummyDataCount; i++) {

      int randomTitleNum = (int) Math.floor(Math.random() * 100);
      int randomBodyNum = (int) Math.floor(Math.random() * 1000);

      ContentValues values = new ContentValues();
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE, "Judul" + randomTitleNum);
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_BODY,
          randomBodyNum + ". Lorem ipsum dolor sit amet");
      values.put(NoteContract.NotesEntry.COLUMN_NOTE_DATETIME, new Date().getTime());

      Uri newUri = mView.getActivityContext().getContentResolver()
          .insert(NoteContract.NotesEntry.CONTENT_URI, values);
      Log.i(this.getClass().getSimpleName(), "generateDummyNote: " + values.toString());
    }
  }

  @Override
  public void deleteAllNotes() {
    int rowsDeleted = mView.getActivityContext().getContentResolver()
        .delete(NoteContract.NotesEntry.CONTENT_URI, null, null);
  }

  @Override
  public int deleteSelectedNotes() {

    int rowsDeleted = noteCursorAdapter.getSelectedCount();

    for (int x = 0; x < noteCursorAdapter.getSelectedCount(); x++) {
      int listId = noteCursorAdapter.getSelectedIds().keyAt(x);
      long realId = noteCursorAdapter.getItemId(listId);
//                Log.i(this.getClass().getSimpleName(),
//                    "Selected: list id=" + listId + ", db_id=" + realId);
      mView.getActivityContext().getContentResolver()
          .delete(ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, realId),
              null, null);
//                Log.i(this.getClass().getSimpleName(),
//                    "onActionItemClicked: rowsDeleted=" + rowsDeleted);
    }
    noteCursorAdapter.removeSelection();

    return rowsDeleted;
  }

}

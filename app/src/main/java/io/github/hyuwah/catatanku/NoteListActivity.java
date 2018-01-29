package io.github.hyuwah.catatanku;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import io.github.hyuwah.catatanku.CustomView.RecyclerViewEmptySupport;
import io.github.hyuwah.catatanku.adapter.NoteAdapter;
import io.github.hyuwah.catatanku.adapter.NoteCursorAdapter;
import io.github.hyuwah.catatanku.model.Note;
import io.github.hyuwah.catatanku.storage.NoteContract;

public class NoteListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ArrayList<Note> notes;
    private NoteAdapter noteAdapter;
    private Toast mToast;

    // Storage
    private static final int NOTE_LOADER = 0;
    NoteCursorAdapter noteCursorAdapter;
    Cursor mCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(NoteListActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });


        noteCursorAdapter = new NoteCursorAdapter(this,null);

        noteAdapter = new NoteAdapter(NoteListActivity.this, mCursor);



        RecyclerViewEmptySupport rvNoteList = findViewById(R.id.rv_note_list);
        rvNoteList.setAdapter(noteAdapter);
        rvNoteList.setLayoutManager(new LinearLayoutManager(this));
        rvNoteList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvNoteList.setEmptyView(findViewById(R.id.empty_note_list_view));

        getLoaderManager().initLoader(NOTE_LOADER,null,this);

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
                int rowsDeleted = getContentResolver().delete(NoteContract.NotesEntry.CONTENT_URI,null,null);

                return true;
            case R.id.action_insert:
//                generateOneDummyNote();
                generateOneDummyNoteDB();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    /**
     * Dummy notes data
     */
    private void generateDummyNotes() {
        for (int i = 0; i < 10; i++) {
            notes.add(new Note("Judul", "Lorem ipsum dolor sit amet", 1516958785));
        }
    }

    private void generateOneDummyNote() {
        notes.add(Note.dbg_addItem());
        noteAdapter.notifyDataSetChanged();
        Log.i(NoteListActivity.class.getSimpleName(), "Notes added : " + notes.toString());
    }

    private void generateOneDummyNoteDB(){

        for(int i=0;i<100;i++) {

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
        noteAdapter.swapCursor(cursor);
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        noteAdapter.swapCursor(null);
    }
}

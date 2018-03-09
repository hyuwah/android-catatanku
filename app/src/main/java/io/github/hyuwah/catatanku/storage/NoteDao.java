package io.github.hyuwah.catatanku.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;
import io.github.hyuwah.catatanku.storage.model.Note;
import java.util.List;

/**
 * Created by hyuwah on 07/02/18.
 */

@Dao
public interface NoteDao {

  @Query("SELECT * FROM note")
  List<Note> getAll();

  @Query("SELECT * FROM note Where title LIKE :keyword OR body LIKE :keyword")
  List<Note> getNoteByKeyword(String keyword);

  @Query("SELECT COUNT(*) FROM note")
  int notesCount();

  // More Custom Query

  @Insert
  void insertNote(Note note);

  @Update
  void updateNote(Note note);

  @Delete
  void deleteNote(Note note);

  @Delete
  void deleteAllNotes(List<Note> notes);

  // Cursor

  @Query("SELECT * FROM note")
  Cursor cursorGetAll();

  @Query("SELECT * FROM note Where title LIKE :keyword OR body LIKE :keyword")
  Cursor cursorGetNoteByKeyword(String keyword);
//
//  // More Custom Query
//
//  @Insert
//  Cursor cursorInsertNote(Note note);
//
//  @Update
//  Cursor cursorUpdateNote(Note note);
//
//  @Delete
//  Cursor cursorDeleteNote(Note note);
//
//  @Delete
//  Cursor cursorDeleteAllNotes();
}

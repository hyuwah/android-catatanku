package io.github.hyuwah.catatanku.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Upsert
    suspend fun upsertNote(noteEntity: NoteEntity)

    @Upsert
    suspend fun upsertNotes(vararg noteEntity: NoteEntity)

    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM note_table ORDER BY updated_at DESC")
    fun getNotesPaged(): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :query || '%' OR content_text LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    fun searchNotesPaged(query: String): PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM note_table WHERE id LIKE :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity
}

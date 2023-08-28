package io.github.hyuwah.catatanku.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Upsert
    suspend fun upsert(noteEntity: NoteEntity)

    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM note_table")
    fun getNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note_table WHERE id LIKE :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity
}
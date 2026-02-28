package io.github.hyuwah.catatanku.domain.repository

import androidx.paging.PagingData
import io.github.hyuwah.catatanku.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun upsertNote(note: Note)

    suspend fun upsertNotes(notes: List<Note>)

    suspend fun deleteById(id: String)

    suspend fun getNoteById(id: String): Note

    fun getNotesPaged(): Flow<PagingData<Note>>

    fun searchNotesPaged(query: String): Flow<PagingData<Note>>
}

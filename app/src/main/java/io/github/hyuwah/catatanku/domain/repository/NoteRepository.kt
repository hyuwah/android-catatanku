package io.github.hyuwah.catatanku.domain.repository

import io.github.hyuwah.catatanku.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun upsertNote(note: Note)

    suspend fun upsertNotes(notes: List<Note>)

    suspend fun deleteById(id: String)

    suspend fun getNoteById(id: String): Note

    fun getNotes(): Flow<List<Note>>
}
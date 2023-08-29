package io.github.hyuwah.catatanku.domain.repository

import io.github.hyuwah.catatanku.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun upsert(note: Note)

    suspend fun deleteById(id: String)

    suspend fun getNoteById(id: String): Note

    fun getNotes(): Flow<List<Note>>
}
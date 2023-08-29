package io.github.hyuwah.catatanku.data.repository

import io.github.hyuwah.catatanku.data.NotesDao
import io.github.hyuwah.catatanku.data.toDomain
import io.github.hyuwah.catatanku.data.toEntity
import io.github.hyuwah.catatanku.domain.repository.NoteRepository
import io.github.hyuwah.catatanku.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NotesDao
) : NoteRepository {
    override suspend fun upsert(note: Note) {
        dao.upsert(note.toEntity())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    override suspend fun getNoteById(id: String): Note {
        return dao.getNoteById(id).toDomain()
    }

    override fun getNotes(): Flow<List<Note>> {
        return dao.getNotes().map { notes -> notes.map { it.toDomain() } }
    }
}
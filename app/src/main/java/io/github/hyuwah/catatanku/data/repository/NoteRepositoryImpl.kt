package io.github.hyuwah.catatanku.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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

    companion object {
        private const val PAGE_SIZE = 20
    }

    override suspend fun upsertNote(note: Note) {
        dao.upsertNotes(note.toEntity())
    }

    override suspend fun upsertNotes(notes: List<Note>) {
        val noteEntities = notes.map { it.toEntity() }
        dao.upsertNotes(*noteEntities.toTypedArray())
    }

    override suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    override suspend fun getNoteById(id: String): Note {
        return dao.getNoteById(id).toDomain()
    }

    override fun getNotesPaged(): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getNotesPaged() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun searchNotesPaged(query: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.searchNotesPaged(query) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}

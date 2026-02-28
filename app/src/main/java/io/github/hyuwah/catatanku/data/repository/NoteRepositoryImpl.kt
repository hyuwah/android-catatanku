package io.github.hyuwah.catatanku.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.hyuwah.catatanku.data.FolderDao
import io.github.hyuwah.catatanku.data.NotesDao
import io.github.hyuwah.catatanku.data.TagDao
import io.github.hyuwah.catatanku.data.NoteTagCrossRef
import io.github.hyuwah.catatanku.data.toDomain
import io.github.hyuwah.catatanku.data.toEntity
import io.github.hyuwah.catatanku.domain.repository.NoteRepository
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.model.Tag
import io.github.hyuwah.catatanku.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val tagDao: TagDao,
    private val folderDao: FolderDao
) : NoteRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    // --- Note operations ---
    override suspend fun upsertNote(note: Note) {
        notesDao.upsertNote(note.toEntity())
    }

    override suspend fun upsertNotes(notes: List<Note>) {
        val noteEntities = notes.map { it.toEntity() }
        notesDao.upsertNotes(*noteEntities.toTypedArray())
    }

    override suspend fun deleteById(id: String) {
        notesDao.deleteById(id)
    }

    override suspend fun getNoteById(id: String): Note {
        return notesDao.getNoteById(id).toDomain()
    }

    override fun getNotesPaged(): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { notesDao.getNotesPaged() }
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
            pagingSourceFactory = { notesDao.searchNotesPaged(query) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    // Folder-based queries
    override fun getNotesByFolderPaged(folderId: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.getNotesByFolderPaged(folderId) }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    override fun getRootNotesPaged(): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.getRootNotesPaged() }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    override fun searchNotesByFolderPaged(folderId: String, query: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.searchNotesByFolderPaged(folderId, query) }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    override fun searchRootNotesPaged(query: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.searchRootNotesPaged(query) }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    override suspend fun updateNoteFolder(noteId: String, folderId: String?) {
        notesDao.updateNoteFolder(noteId, folderId, System.currentTimeMillis())
    }

    // Tag-based queries
    override fun getNotesByTagsPaged(tagIds: List<String>): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.getNotesByTagsPaged(tagIds, tagIds.size) }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    override fun searchNotesByTagsPaged(tagIds: List<String>, query: String): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.searchNotesByTagsPaged(tagIds, tagIds.size, query) }
        ).flow.map { it.map { entity -> entity.toDomain() } }
    }

    // --- Tag operations ---
    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTag(tag: Tag) {
        tagDao.insertTag(tag.toEntity())
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag.toEntity())
    }

    override suspend fun deleteTag(id: String) {
        // First remove all tag associations
        val tagEntity = tagDao.getTagById(id)
        if (tagEntity != null) {
            tagDao.deleteById(id)
        }
    }

    override suspend fun getTagById(id: String): Tag? {
        return tagDao.getTagById(id)?.toDomain()
    }

    override fun getTagsForNote(noteId: String): Flow<List<Tag>> {
        return tagDao.getTagsForNote(noteId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addTagToNote(noteId: String, tagId: String) {
        tagDao.addTagToNote(NoteTagCrossRef(noteId, tagId))
    }

    override suspend fun removeTagFromNote(noteId: String, tagId: String) {
        tagDao.removeTagFromNote(NoteTagCrossRef(noteId, tagId))
    }

    override suspend fun setTagsForNote(noteId: String, tagIds: List<String>) {
        // Remove all existing tags
        tagDao.removeAllTagsFromNote(noteId)
        // Add new tags
        tagIds.forEach { tagId ->
            tagDao.addTagToNote(NoteTagCrossRef(noteId, tagId))
        }
    }

    // --- Folder operations ---
    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { list -> list.map { it.toDomain() } }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return folderDao.getRootFolders().map { list -> list.map { it.toDomain() } }
    }

    override fun getSubfolders(parentId: String): Flow<List<Folder>> {
        return folderDao.getSubfolders(parentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertFolder(folder: Folder) {
        folderDao.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(id: String) {
        // Delete all notes in folder first
        folderDao.deleteNotesInFolder(id)
        // Then delete the folder
        folderDao.deleteById(id)
    }

    override suspend fun getFolderById(id: String): Folder? {
        return folderDao.getFolderById(id)?.toDomain()
    }

    override suspend fun getNotesCountInFolder(folderId: String): Int {
        return folderDao.getNotesCountInFolder(folderId)
    }
}

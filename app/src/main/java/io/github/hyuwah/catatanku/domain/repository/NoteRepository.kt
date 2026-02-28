package io.github.hyuwah.catatanku.domain.repository

import androidx.paging.PagingData
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.model.Tag
import io.github.hyuwah.catatanku.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    // --- Note operations ---
    suspend fun upsertNote(note: Note)
    suspend fun upsertNotes(notes: List<Note>)
    suspend fun deleteById(id: String)
    suspend fun getNoteById(id: String): Note
    
    fun getNotesPaged(): Flow<PagingData<Note>>
    fun searchNotesPaged(query: String): Flow<PagingData<Note>>
    
    // Folder-based queries
    fun getNotesByFolderPaged(folderId: String): Flow<PagingData<Note>>
    fun getRootNotesPaged(): Flow<PagingData<Note>>
    fun searchNotesByFolderPaged(folderId: String, query: String): Flow<PagingData<Note>>
    fun searchRootNotesPaged(query: String): Flow<PagingData<Note>>
    suspend fun updateNoteFolder(noteId: String, folderId: String?)
    
    // Tag-based queries
    fun getNotesByTagsPaged(tagIds: List<String>): Flow<PagingData<Note>>
    fun searchNotesByTagsPaged(tagIds: List<String>, query: String): Flow<PagingData<Note>>
    
    // --- Tag operations ---
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertTag(tag: Tag)
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(id: String)
    suspend fun getTagById(id: String): Tag?
    fun getTagsForNote(noteId: String): Flow<List<Tag>>
    suspend fun addTagToNote(noteId: String, tagId: String)
    suspend fun removeTagFromNote(noteId: String, tagId: String)
    suspend fun setTagsForNote(noteId: String, tagIds: List<String>)
    
    // --- Folder operations ---
    fun getAllFolders(): Flow<List<Folder>>
    fun getRootFolders(): Flow<List<Folder>>
    fun getSubfolders(parentId: String): Flow<List<Folder>>
    suspend fun insertFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(id: String)
    suspend fun getFolderById(id: String): Folder?
    suspend fun getNotesCountInFolder(folderId: String): Int
}

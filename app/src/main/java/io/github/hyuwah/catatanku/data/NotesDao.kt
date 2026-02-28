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

    // --- Folder queries ---

    // Get notes in a specific folder (paged)
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId ORDER BY updated_at DESC")
    fun getNotesByFolderPaged(folderId: String): PagingSource<Int, NoteEntity>

    // Get notes in root (no folder) - paged
    @Query("SELECT * FROM note_table WHERE folder_id IS NULL ORDER BY updated_at DESC")
    fun getRootNotesPaged(): PagingSource<Int, NoteEntity>

    // Search notes within a folder
    @Query("SELECT * FROM note_table WHERE folder_id = :folderId AND (title LIKE '%' || :query || '%' OR content_text LIKE '%' || :query || '%') ORDER BY updated_at DESC")
    fun searchNotesByFolderPaged(folderId: String, query: String): PagingSource<Int, NoteEntity>

    // Search notes in root
    @Query("SELECT * FROM note_table WHERE folder_id IS NULL AND (title LIKE '%' || :query || '%' OR content_text LIKE '%' || :query || '%') ORDER BY updated_at DESC")
    fun searchRootNotesPaged(query: String): PagingSource<Int, NoteEntity>

    // Update note's folder
    @Query("UPDATE note_table SET folder_id = :folderId, updated_at = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteFolder(noteId: String, folderId: String?, updatedAt: Long)

    // Delete all notes in a folder (for folder deletion)
    @Query("DELETE FROM note_table WHERE folder_id = :folderId")
    suspend fun deleteNotesInFolder(folderId: String)

    // --- Tag queries (via cross ref) ---

    // Get notes with specific tag(s)
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_tag_cross_ref ntcr ON n.id = ntcr.note_id
        WHERE ntcr.tag_id IN (:tagIds)
        GROUP BY n.id
        HAVING COUNT(DISTINCT ntcr.tag_id) = :tagCount
        ORDER BY n.updated_at DESC
    """)
    fun getNotesByTagsPaged(tagIds: List<String>, tagCount: Int): PagingSource<Int, NoteEntity>

    // Search notes by tag(s)
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_tag_cross_ref ntcr ON n.id = ntcr.note_id
        WHERE ntcr.tag_id IN (:tagIds)
        AND (n.title LIKE '%' || :query || '%' OR n.content_text LIKE '%' || :query || '%')
        GROUP BY n.id
        HAVING COUNT(DISTINCT ntcr.tag_id) = :tagCount
        ORDER BY n.updated_at DESC
    """)
    fun searchNotesByTagsPaged(tagIds: List<String>, tagCount: Int, query: String): PagingSource<Int, NoteEntity>
}

package io.github.hyuwah.catatanku.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tag_table WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tag_table ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag_table ORDER BY name ASC")
    fun getAllTagsPaged(): PagingSource<Int, TagEntity>

    @Query("SELECT * FROM tag_table WHERE id = :id")
    suspend fun getTagById(id: String): TagEntity?

    @Query("SELECT * FROM tag_table WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<TagEntity>>

    // Get tags for a specific note
    @Query("""
        SELECT t.* FROM tag_table t
        INNER JOIN note_tag_cross_ref ntcr ON t.id = ntcr.tag_id
        WHERE ntcr.note_id = :noteId
    """)
    fun getTagsForNote(noteId: String): Flow<List<TagEntity>>

    // Get tags for a specific note (non-flow for single fetch)
    @Query("""
        SELECT t.* FROM tag_table t
        INNER JOIN note_tag_cross_ref ntcr ON t.id = ntcr.tag_id
        WHERE ntcr.note_id = :noteId
    """)
    suspend fun getTagsForNoteSync(noteId: String): List<TagEntity>

    // Add tag to note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToNote(crossRef: NoteTagCrossRef)

    // Remove tag from note
    @Delete
    suspend fun removeTagFromNote(crossRef: NoteTagCrossRef)

    // Remove all tags from note
    @Query("DELETE FROM note_tag_cross_ref WHERE note_id = :noteId")
    suspend fun removeAllTagsFromNote(noteId: String)

    // Get notes count by tag
    @Query("""
        SELECT COUNT(*) FROM note_tag_cross_ref WHERE tag_id = :tagId
    """)
    suspend fun getNotesCountByTag(tagId: String): Int
}

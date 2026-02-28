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
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folder_table WHERE id = :id")
    suspend fun deleteById(id: String)

    // Get all root folders (no parent)
    @Query("SELECT * FROM folder_table WHERE parent_id IS NULL ORDER BY name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    // Get all folders
    @Query("SELECT * FROM folder_table ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    // Get all folders paged
    @Query("SELECT * FROM folder_table ORDER BY name ASC")
    fun getAllFoldersPaged(): PagingSource<Int, FolderEntity>

    // Get subfolders of a parent
    @Query("SELECT * FROM folder_table WHERE parent_id = :parentId ORDER BY name ASC")
    fun getSubfolders(parentId: String): Flow<List<FolderEntity>>

    // Get folder by ID
    @Query("SELECT * FROM folder_table WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    // Get folder by ID (Flow)
    @Query("SELECT * FROM folder_table WHERE id = :id")
    fun getFolderByIdFlow(id: String): Flow<FolderEntity?>

    // Search folders
    @Query("SELECT * FROM folder_table WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFolders(query: String): Flow<List<FolderEntity>>

    // Get notes count in folder
    @Query("SELECT COUNT(*) FROM note_table WHERE folder_id = :folderId")
    suspend fun getNotesCountInFolder(folderId: String): Int

    // Get all subfolder IDs recursively (for delete cascade)
    @Query("WITH RECURSIVE subfolders AS (SELECT id FROM folder_table WHERE parent_id = :folderId UNION ALL SELECT f.id FROM folder_table f INNER JOIN subfolders sf ON f.parent_id = sf.id) SELECT id FROM subfolders")
    suspend fun getAllSubfolderIds(folderId: String): List<String>

    // Delete folder and all notes inside (call from repository with confirmation)
    @Query("DELETE FROM note_table WHERE folder_id = :folderId")
    suspend fun deleteNotesInFolder(folderId: String)
}

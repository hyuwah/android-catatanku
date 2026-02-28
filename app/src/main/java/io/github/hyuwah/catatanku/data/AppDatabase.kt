package io.github.hyuwah.catatanku.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoteEntity::class,
        TagEntity::class,
        FolderEntity::class,
        NoteTagCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
    abstract fun tagDao(): TagDao
    abstract fun folderDao(): FolderDao
}

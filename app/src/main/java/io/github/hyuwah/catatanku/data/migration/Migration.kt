package io.github.hyuwah.catatanku.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create tags table
        database.execSQL("""
            CREATE TABLE tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Create folders table
        database.execSQL("""
            CREATE TABLE folders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                parentId INTEGER,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY (parentId) REFERENCES folders(id) ON DELETE SET NULL
            )
        """.trimIndent())

        // 3. Create note_tag_cross_ref table
        database.execSQL("""
            CREATE TABLE note_tag_cross_ref (
                noteId INTEGER NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY (noteId, tagId),
                FOREIGN KEY (noteId) REFERENCES note_table(id) ON DELETE CASCADE,
                FOREIGN KEY (tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // 4. Add folderId column to note_table (nullable, default null)
        database.execSQL("""
            ALTER TABLE note_table ADD COLUMN folderId INTEGER
        """.trimIndent())

        // 5. Create index for faster folder queries
        database.execSQL("""
            CREATE INDEX index_notes_folderId ON note_table(folderId)
        """.trimIndent())
    }
}
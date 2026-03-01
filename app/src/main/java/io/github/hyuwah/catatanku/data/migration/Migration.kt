package io.github.hyuwah.catatanku.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create tag_table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `tag_table` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `color` TEXT, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        // 2. Create folder_table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `folder_table` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `parent_id` TEXT, 
                `created_at` INTEGER NOT NULL, 
                `updated_at` INTEGER NOT NULL, 
                PRIMARY KEY(`id`), 
                FOREIGN KEY(`parent_id`) REFERENCES `folder_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_folder_table_parent_id` ON `folder_table` (`parent_id`)")

        // 3. Create note_tag_cross_ref
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `note_tag_cross_ref` (
                `note_id` TEXT NOT NULL, 
                `tag_id` TEXT NOT NULL, 
                PRIMARY KEY(`note_id`, `tag_id`), 
                FOREIGN KEY(`note_id`) REFERENCES `note_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                FOREIGN KEY(`tag_id`) REFERENCES `tag_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_tag_cross_ref_note_id` ON `note_tag_cross_ref` (`note_id`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_tag_cross_ref_tag_id` ON `note_tag_cross_ref` (`tag_id`)")

        // 4. Update note_table to add folder_id with Foreign Key
        // SQLite doesn't support adding FOREIGN KEY to an existing table via ALTER TABLE.
        // We must use the "table recreation" strategy.
        
        // a. Create a new temporary table with the new schema (including folder_id and FK)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `note_table_new` (
                `id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `content_text` TEXT NOT NULL, 
                `created_at` INTEGER NOT NULL, 
                `updated_at` INTEGER NOT NULL, 
                `folder_id` TEXT, 
                PRIMARY KEY(`id`), 
                FOREIGN KEY(`folder_id`) REFERENCES `folder_table`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
            )
        """.trimIndent())

        // b. Copy data from the old table to the new one
        database.execSQL("""
            INSERT INTO `note_table_new` (`id`, `title`, `content_text`, `created_at`, `updated_at`)
            SELECT `id`, `title`, `content_text`, `created_at`, `updated_at` FROM `note_table`
        """.trimIndent())

        // c. Drop the old table
        database.execSQL("DROP TABLE `note_table`")

        // d. Rename the new table to the original name
        database.execSQL("ALTER TABLE `note_table_new` RENAME TO `note_table`")
        
        // e. Recreate indices for the renamed table
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_note_table_folder_id` ON `note_table` (`folder_id`)")
    }
}

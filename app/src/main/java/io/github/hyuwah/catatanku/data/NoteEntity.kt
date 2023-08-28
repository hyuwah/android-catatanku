package io.github.hyuwah.catatanku.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("note_table")
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("title")
    val title: String,
    @ColumnInfo("content_text")
    var contentText: String,
    @ColumnInfo("created_at")
    var createdAt: Long,
    @ColumnInfo("updated_at")
    var updatedAt: Long
)

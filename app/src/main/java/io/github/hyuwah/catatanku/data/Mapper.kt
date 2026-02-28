package io.github.hyuwah.catatanku.data

import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.model.Tag
import io.github.hyuwah.catatanku.domain.model.Folder

// Note mappers
fun NoteEntity.toDomain(): Note {
    return Note(
        id, title, contentText, createdAt, updatedAt, folderId
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id, title, contentText, createdAt, updatedAt, folderId
    )
}

// Tag mappers
fun TagEntity.toDomain(): Tag {
    return Tag(id, name, color)
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(id, name, color)
}

// Folder mappers
fun FolderEntity.toDomain(): Folder {
    return Folder(id, name, parentId, createdAt, updatedAt)
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(id, name, parentId, createdAt, updatedAt)
}

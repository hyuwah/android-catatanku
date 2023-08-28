package io.github.hyuwah.catatanku.data

import io.github.hyuwah.catatanku.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id, title, contentText, createdAt, updatedAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id, title, contentText, createdAt, updatedAt
    )
}
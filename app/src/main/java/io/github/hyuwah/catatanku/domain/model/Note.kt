package io.github.hyuwah.catatanku.domain.model

data class Note(
    var id: String,
    var title: String,
    var contentText: String,
    var createdAt: Long,
    var updatedAt: Long,
    var folderId: String? = null,
    val tags: List<Tag> = emptyList()
)

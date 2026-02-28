package io.github.hyuwah.catatanku.domain.model

data class Folder(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

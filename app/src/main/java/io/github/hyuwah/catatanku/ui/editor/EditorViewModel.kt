package io.github.hyuwah.catatanku.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.repository.NoteRepository
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private var currentNote: Note? = null
    private val _note: MutableLiveData<Note> = MutableLiveData()
    val note: LiveData<Note> = _note

    fun getNoteById(id: String) {
        viewModelScope.launch {
            val result = repository.getNoteById(id)
            currentNote = result
            _note.value = result
        }
    }

    fun save(title: String, contentText: String) {
        viewModelScope.launch {
            val noteToBeSaved = Note(
                id = currentNote?.id ?: UUID.randomUUID().toString(),
                title = title,
                contentText = contentText,
                createdAt = currentNote?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.upsertNote(noteToBeSaved)
            currentNote = noteToBeSaved
            _note.value = noteToBeSaved
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            currentNote?.let {
                repository.deleteById(it.id)
                onDeleted.invoke()
            }
        }
    }

    fun hasUnsavedChanges(
        title: String,
        contentText: String
    ): Boolean {
        val titleChanged = title != currentNote?.title
        val contentTextChanged = contentText != currentNote?.contentText
        return titleChanged || contentTextChanged
    }

}
package io.github.hyuwah.catatanku.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.repository.NoteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val notesLiveData = combine(
        repository.getNotes(),
        searchQuery.debounce(500).distinctUntilChanged(),
    ) { notes, query ->
        if (query.isNotBlank()) {
            notes.filter {
                it.title.contains(query) || it.contentText.contains(query)
            }
        } else {
            notes
        }
    }.asLiveData()

    fun deleteByIds(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { id ->
                repository.deleteById(id)
            }
        }
    }

    fun deleteById(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    fun debugInsert() {
        viewModelScope.launch {
            val dummyNotes = (1..10).map {
                val randTitle = floor(Math.random() * 100).toInt()
                val randBody = floor(Math.random() * 1000).toInt()
                Note(
                    id = UUID.randomUUID().toString(),
                    title = "Title $randTitle",
                    contentText = "$randBody. Lorem ipsum dolor sit amet",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            repository.upsertNotes(dummyNotes)
        }
    }

    fun deleteAll() {
        val noteIds = notesLiveData.value?.map { it.id } ?: emptyList()
        if (noteIds.isEmpty()) return
        deleteByIds(noteIds)
    }

}
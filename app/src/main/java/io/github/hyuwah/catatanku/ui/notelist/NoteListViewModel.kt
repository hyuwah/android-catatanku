package io.github.hyuwah.catatanku.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val notesPagingData: Flow<PagingData<Note>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getNotesPaged()
            } else {
                repository.searchNotesPaged(query)
            }
        }
        .cachedIn(viewModelScope)

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
        viewModelScope.launch {
            // Note: With pagination, getting all IDs is trickier
            // For simplicity, we'll skip this implementation for now
            // In production, you'd want to handle this via a dedicated repository method
        }
    }

}

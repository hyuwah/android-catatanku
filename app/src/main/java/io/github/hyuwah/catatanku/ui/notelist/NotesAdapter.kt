package io.github.hyuwah.catatanku.ui.notelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.hyuwah.catatanku.databinding.ItemNoteListBinding
import io.github.hyuwah.catatanku.domain.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val noteItemListener: NoteItemListener
) : ListAdapter<Note, NotesAdapter.ViewHolder>(differ) {
    companion object {
        private val differ = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNoteListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], noteItemListener)
    }

    class ViewHolder(
        private val binding: ItemNoteListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, noteItemListener: NoteItemListener) = with(binding) {
            root.setOnClickListener {
                noteItemListener.onNoteClicked(note)
            }
            root.setOnLongClickListener {
                noteItemListener.onNoteLongClicked(note)
                true
            }
            noteTitle.text = note.title
            noteExcerpt.text = note.contentText
            val createdAtDate = Date(note.createdAt)
            val dateFormat = SimpleDateFormat("EE, dd/MM/yy - HH:mm:ss", Locale.getDefault())
            noteTime.text = dateFormat.format(createdAtDate)
        }
    }

    interface NoteItemListener {
        fun onNoteClicked(note: Note)
        fun onNoteLongClicked(note: Note)
    }
}
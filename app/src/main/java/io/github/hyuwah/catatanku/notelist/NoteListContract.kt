package io.github.hyuwah.catatanku.notelist

import android.content.Context
import android.content.DialogInterface
import io.github.hyuwah.catatanku.BasePresenter
import io.github.hyuwah.catatanku.BaseView

interface NoteListContract {
    interface View : BaseView<Presenter> {
        val activityContext: Context?
        fun showDeleteConfirmationDialog(deleteClickListener: DialogInterface.OnClickListener?)
    }

    interface Presenter : BasePresenter {
        fun generateDummyNotes()
        fun deleteAllNotes()
        fun deleteSelectedNotes(): Int
        fun searchQuery(s: String?)
    }
}
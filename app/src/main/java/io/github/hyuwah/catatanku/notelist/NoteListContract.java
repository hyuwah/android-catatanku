package io.github.hyuwah.catatanku.notelist;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.support.annotation.NonNull;
import io.github.hyuwah.catatanku.BasePresenter;
import io.github.hyuwah.catatanku.BaseView;

public interface NoteListContract {
  interface View extends BaseView<Presenter>{

    Context getActivityContext();

    void showDeleteConfirmationDialog(DialogInterface.OnClickListener deleteClickListener);

  }

  interface Presenter extends BasePresenter{

    Context getActivityContext();

    void generateDummyNotes();

    void deleteAllNotes();

    int deleteSelectedNotes();

    void searchQuery(String s);
  }
}

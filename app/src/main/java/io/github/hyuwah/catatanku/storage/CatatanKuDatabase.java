package io.github.hyuwah.catatanku.storage;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import io.github.hyuwah.catatanku.storage.model.Note;

/**
 * Created by hyuwah on 12/02/18.
 * Only Create ONE INSTANCE of CatatanKuDatabase (Singleton Pattern)
 */

@Database(entities = {Note.class}, version = 2, exportSchema = false)
public abstract class CatatanKuDatabase extends RoomDatabase {

  private static final String DB_NAME = "catatankuDatabase.db";
  private static CatatanKuDatabase INSTANCE;

  public abstract NoteDao getNoteDao();

  static final Migration MIGRATION_1_2 = new Migration(1,2) {
    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

    }
  };

  public static CatatanKuDatabase getCatatanKuDatabase(Context context){
    if(INSTANCE==null){
      INSTANCE = Room.databaseBuilder(
          context.getApplicationContext(),
          CatatanKuDatabase.class,
          DB_NAME)
          .addMigrations(MIGRATION_1_2)
//          .fallbackToDestructiveMigration()
          .build();
    }
    return INSTANCE;
  }

  public static void destroyInstance(){
    INSTANCE = null;
  }

}

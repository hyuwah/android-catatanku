package io.github.hyuwah.catatanku.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.hyuwah.catatanku.data.AppDatabase
import io.github.hyuwah.catatanku.data.FolderDao
import io.github.hyuwah.catatanku.data.NotesDao
import io.github.hyuwah.catatanku.data.TagDao
import io.github.hyuwah.catatanku.data.migration.MIGRATION_1_2
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    private const val DATABASE_NAME = "catatanku-db"

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideNotesDao(database: AppDatabase): NotesDao = database.notesDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()

}
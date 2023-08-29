package io.github.hyuwah.catatanku.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.hyuwah.catatanku.data.AppDatabase
import io.github.hyuwah.catatanku.data.NotesDao
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
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }

    @Singleton
    @Provides
    fun provideNotesDao(
        appDatabase: AppDatabase
    ): NotesDao {
        return appDatabase.notesDao()
    }

}
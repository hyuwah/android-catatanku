package io.github.hyuwah.catatanku.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.github.hyuwah.catatanku.data.repository.NoteRepositoryImpl
import io.github.hyuwah.catatanku.domain.repository.NoteRepository


@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
        @Binds
        abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
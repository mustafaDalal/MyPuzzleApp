package com.md.mypuzzleapp.di

import android.content.Context
import com.md.mypuzzleapp.data.repository.PuzzleProgressRepositoryImpl
import com.md.mypuzzleapp.data.repository.PuzzleRepositoryImpl
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.data.source.remote.SupabasePuzzleDataSource
import com.md.mypuzzleapp.data.source.remote.SupabasePuzzleProgressDataSource
import com.md.mypuzzleapp.domain.repository.PuzzleProgressRepository
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSupabasePuzzleDataSource(@ApplicationContext context: Context): SupabasePuzzleDataSource {
        return SupabasePuzzleDataSource(context)
    }
    
    @Provides
    @Singleton
    fun provideSupabasePuzzleProgressDataSource(): SupabasePuzzleProgressDataSource {
        return SupabasePuzzleProgressDataSource()
    }
    
    @Provides
    @Singleton
    fun providePuzzleDataSource(
        supabasePuzzleDataSource: SupabasePuzzleDataSource
    ): PuzzleDataSource {
        return supabasePuzzleDataSource
    }
    
    @Provides
    @Singleton
    fun providePuzzleProgressDataSource(
        supabasePuzzleProgressDataSource: SupabasePuzzleProgressDataSource
    ): PuzzleProgressDataSource {
        return supabasePuzzleProgressDataSource
    }
    
    @Provides
    @Singleton
    fun providePuzzleRepository(
        puzzleRepositoryImpl: PuzzleRepositoryImpl
    ): PuzzleRepository {
        return puzzleRepositoryImpl
    }

    @Provides
    @Singleton
    fun providePuzzleRepositoryImpl(
        context: Context,
        puzzleDataSource: PuzzleDataSource
    ): PuzzleRepositoryImpl {
        return PuzzleRepositoryImpl(context, puzzleDataSource = puzzleDataSource)
    }
    
    @Provides
    @Singleton
    fun providePuzzleProgressRepository(
        puzzleProgressRepositoryImpl: PuzzleProgressRepositoryImpl
    ): PuzzleProgressRepository {
        return puzzleProgressRepositoryImpl
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext context: Context): Context {
        return context
    }
} 
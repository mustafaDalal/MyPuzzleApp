package com.md.mypuzzleapp.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.md.mypuzzleapp.data.repository.PuzzleProgressRepositoryImpl
import com.md.mypuzzleapp.data.repository.PuzzleRepositoryImpl
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.data.source.remote.FirebasePuzzleDataSource
import com.md.mypuzzleapp.data.source.remote.FirebasePuzzleProgressDataSource
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebasePuzzleDataSource(
        firebasePuzzleDataSource: FirebasePuzzleDataSource
    ): PuzzleDataSource {
        return firebasePuzzleDataSource
    }
    
    @Provides
    @Singleton
    fun provideFirebasePuzzleProgressDataSource(
        firebasePuzzleProgressDataSource: FirebasePuzzleProgressDataSource
    ): PuzzleProgressDataSource {
        return firebasePuzzleProgressDataSource
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
    fun providePuzzleProgressRepository(
        puzzleProgressRepositoryImpl: PuzzleProgressRepository
    ): PuzzleProgressRepository {
        return puzzleProgressRepositoryImpl
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext context: Context): Context {
        return context
    }
} 
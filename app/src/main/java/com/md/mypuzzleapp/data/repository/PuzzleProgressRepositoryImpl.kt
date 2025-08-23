package com.md.mypuzzleapp.data.repository

import com.md.mypuzzleapp.data.source.PuzzleProgressDataSource
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.repository.PuzzleProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleProgressRepositoryImpl @Inject constructor(
    private val puzzleProgressDataSource: PuzzleProgressDataSource
) : PuzzleProgressRepository {
    
    override fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?> {
        return puzzleProgressDataSource.getPuzzleProgress(puzzleId)
    }
    
    override suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress) {
        puzzleProgressDataSource.savePuzzleProgress(puzzleProgress)
    }
    
    override suspend fun deletePuzzleProgress(puzzleId: String) {
        puzzleProgressDataSource.deletePuzzleProgress(puzzleId)
    }
}
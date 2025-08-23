package com.md.mypuzzleapp.domain.repository

import com.md.mypuzzleapp.domain.model.PuzzleProgress
import kotlinx.coroutines.flow.Flow

interface PuzzleProgressRepository {
    fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?>
    suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress)
    suspend fun deletePuzzleProgress(puzzleId: String)
} 
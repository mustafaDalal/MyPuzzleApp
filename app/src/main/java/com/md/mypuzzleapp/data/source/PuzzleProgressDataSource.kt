package com.md.mypuzzleapp.data.source

import com.md.mypuzzleapp.domain.model.PiecePlacement
import com.md.mypuzzleapp.domain.model.PuzzleProgress
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the contract for puzzle progress data operations.
 * This allows for different implementations (Firebase, Room, etc.)
 */
interface PuzzleProgressDataSource {
    fun getPuzzleProgress(puzzleId: String): Flow<PuzzleProgress?>
    suspend fun savePuzzleProgress(puzzleProgress: PuzzleProgress)
    suspend fun updatePiecePlacement(puzzleId: String, piecePlacement: PiecePlacement)
    suspend fun deletePuzzleProgress(puzzleId: String)
} 
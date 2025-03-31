package com.md.mypuzzleapp.data.source

import android.net.Uri
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining the contract for puzzle data operations.
 * This allows for different implementations (Firebase, Room, etc.)
 */
interface PuzzleDataSource {
    fun getAllPuzzles(): Flow<List<Puzzle>>
    fun getPuzzleById(id: String): Flow<Puzzle?>
    suspend fun addPuzzle(puzzle: Puzzle): String
    suspend fun updatePuzzle(puzzle: Puzzle)
    suspend fun deletePuzzle(id: String)
    suspend fun uploadCustomImage(uri: Uri, name: String, difficulty: PuzzleDifficulty): Puzzle
    suspend fun getDefaultPuzzles(): List<Puzzle>
} 
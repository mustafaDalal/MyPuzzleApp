package com.md.mypuzzleapp.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import kotlinx.coroutines.flow.Flow

interface PuzzleRepository {
    fun getAllPuzzles(): Flow<List<Puzzle>>
    fun getPuzzleById(id: String): Flow<Puzzle?>
    suspend fun addPuzzle(puzzle: Puzzle): String
    suspend fun updatePuzzle(puzzle: Puzzle)
    suspend fun deletePuzzle(id: String)
    suspend fun createPuzzleFromBitmap(bitmap: Bitmap, name: String, difficulty: PuzzleDifficulty): Puzzle
    suspend fun createBitmapFromUri(uri: Uri): Bitmap
    suspend fun addPuzzleWithImage(
        name: String,
        difficulty: String,
        bitmap: Bitmap,
        context: Context
    ): Result<Puzzle>

    fun getAllPuzzlesForDevice(context: Context): Flow<List<Puzzle>>

    suspend fun renamePuzzle(puzzleId: String, newName: String)

    suspend fun deletePuzzle(puzzleId: String, imageUrl: String)
} 
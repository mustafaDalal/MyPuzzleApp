package com.md.mypuzzleapp.domain.repository

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
//    suspend fun uploadCustomImage(uri: Uri, name: String, difficulty: PuzzleDifficulty): Puzzle
    suspend fun getDefaultPuzzles(): List<Puzzle>
    suspend fun createPuzzleFromBitmap(bitmap: Bitmap, name: String, difficulty: PuzzleDifficulty): Puzzle
    suspend fun createBitmapFromUri(uri: Uri): Bitmap
} 
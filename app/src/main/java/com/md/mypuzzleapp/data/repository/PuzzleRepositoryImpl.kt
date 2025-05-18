package com.md.mypuzzleapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.md.mypuzzleapp.data.source.PuzzleDataSource
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.model.PuzzlePiece
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepositoryImpl @Inject constructor(
    private val context: Context,
    private val puzzleDataSource: PuzzleDataSource
) : PuzzleRepository {
    
    private val puzzles = mutableListOf<Puzzle>()
    
    override fun getAllPuzzles(): Flow<List<Puzzle>> = flow {
        emit(puzzles)
    }
    
    override fun getPuzzleById(id: String): Flow<Puzzle?> = flow {
        emit(puzzles.find { it.id == id })
    }
    
    override suspend fun addPuzzle(puzzle: Puzzle): String {
        puzzles.add(puzzle)
        return puzzle.id
    }
    
    override suspend fun updatePuzzle(puzzle: Puzzle) {
        val index = puzzles.indexOfFirst { it.id == puzzle.id }
        if (index != -1) {
            puzzles[index] = puzzle
        }
    }
    
    override suspend fun deletePuzzle(id: String) {
        puzzles.removeIf { it.id == id }
    }
    
    override suspend fun createPuzzleFromBitmap(
        bitmap: Bitmap,
        name: String,
        difficulty: PuzzleDifficulty
    ): Puzzle = withContext(Dispatchers.IO) {
        // Create puzzle pieces based on difficulty
        val pieces = when (difficulty) {
            PuzzleDifficulty.EASY -> createPieces(bitmap, 3, 3)
            PuzzleDifficulty.MEDIUM -> createPieces(bitmap, 4, 4)
            PuzzleDifficulty.HARD -> createPieces(bitmap, 5, 5)
        }

        // Create and return puzzle
        Puzzle(
            id = UUID.randomUUID().toString(),
            name = name,
            difficulty = difficulty,
            pieces = pieces,
            originalImage = bitmap,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createPieces(bitmap: Bitmap, rows: Int, cols: Int): List<PuzzlePiece> {
        val pieces = mutableListOf<PuzzlePiece>()
        val pieceWidth = bitmap.width / cols
        val pieceHeight = bitmap.height / rows

        var id = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pieceBitmap = Bitmap.createBitmap(
                    bitmap,
                    col * pieceWidth,
                    row * pieceHeight,
                    pieceWidth,
                    pieceHeight
                )

                val position = row * cols + col
                pieces.add(
                    PuzzlePiece(
                        id = id++,
                        bitmap = pieceBitmap,
                        correctPosition = position,
                        currentPosition = position
                    )
                )
            }
        }

        // Shuffle pieces
        pieces.shuffle()
        return pieces
    }

    override suspend fun createBitmapFromUri(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw Exception("Failed to load image from URI")
    }
} 
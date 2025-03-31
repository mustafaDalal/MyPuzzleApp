package com.md.mypuzzleapp.domain.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import com.md.mypuzzleapp.presentation.puzzle.PuzzlePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.random.Random

class PreparePuzzlePiecesUseCase @Inject constructor(
    private val repository: PuzzleRepository
) {
//    suspend operator fun invoke(puzzle: Puzzle): List<PuzzlePiece> = withContext(Dispatchers.Default) {
//        val image = repository.getPuzzleById(puzzle.id)
//        val gridSize = puzzle.difficulty.gridSize
//        val pieceWidth = image.width / gridSize
//        val pieceHeight = image.height / gridSize
//
//        val pieces = mutableListOf<PuzzlePiece>()
//
//        // Create pieces
//        for (row in 0 until gridSize) {
//            for (col in 0 until gridSize) {
//                val pieceBitmap = Bitmap.createBitmap(
//                    image,
//                    col * pieceWidth,
//                    row * pieceHeight,
//                    pieceWidth,
//                    pieceHeight
//                )
//
//                val position = row * gridSize + col
//                pieces.add(
//                    PuzzlePiece(
//                        id = position,
//                        bitmap = pieceBitmap,
//                        currentPosition = position,
//                        correctPosition = position
//                    )
//                )
//            }
//        }
//
//        // Shuffle pieces
//        val shuffledPieces = pieces.mapIndexed { index, piece ->
//            piece.copy(currentPosition = index)
//        }.shuffled()
//
//        shuffledPieces
//    }
} 
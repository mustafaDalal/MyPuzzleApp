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
    suspend operator fun invoke(puzzle: Puzzle): List<PuzzlePiece> = withContext(Dispatchers.Default) {

        var shuffledPieces = emptyList<PuzzlePiece>()
        repository.getPuzzleById(puzzle.id).collect {

            if(it != null){

                val gridSize = it.difficulty.gridSize
                val pieceWidth = (it.originalImage?.width ?: 0) / gridSize
                val pieceHeight = (it.originalImage?.height ?: 0) / gridSize

                val pieces = mutableListOf<PuzzlePiece>()

                // Create pieces

                it.originalImage?.let { image ->
                    for (row in 0 until gridSize) {
                        for (col in 0 until gridSize) {
                            val pieceBitmap = Bitmap.createBitmap(
                                image,
                                col * pieceWidth,
                                row * pieceHeight,
                                pieceWidth,
                                pieceHeight
                            )

                            val position = row * gridSize + col
                            pieces.add(
                                PuzzlePiece(
                                    id = position,
                                    bitmap = pieceBitmap,
                                    currentPosition = position,
                                    correctPosition = position
                                )
                            )
                        }
                    }
                }


                // Shuffle pieces
                shuffledPieces = pieces.mapIndexed { index, piece ->
                    piece.copy(currentPosition = index)
                }.shuffled()
            }

        }
        shuffledPieces

    }
} 
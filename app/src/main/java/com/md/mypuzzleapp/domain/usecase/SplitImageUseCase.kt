package com.md.mypuzzleapp.domain.usecase

import android.graphics.Bitmap
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.model.PuzzlePiece
import javax.inject.Inject
import kotlin.random.Random

class SplitImageUseCase @Inject constructor() {
    operator fun invoke(
        bitmap: Bitmap,
        difficulty: PuzzleDifficulty
    ): List<PuzzlePiece> {
        val pieces = mutableListOf<PuzzlePiece>()
        val rows = difficulty.gridSize
        val cols = difficulty.gridSize
        
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
                
                pieces.add(
                    PuzzlePiece(
                        id = id++,
                        bitmap = pieceBitmap,
                        correctX = col,
                        correctY = row
                    )
                )
            }
        }
        
        return pieces.shuffled(Random(System.currentTimeMillis()))
    }
} 
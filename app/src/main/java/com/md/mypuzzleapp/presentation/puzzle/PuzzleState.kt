package com.md.mypuzzleapp.presentation.puzzle

import android.graphics.Bitmap
import com.md.mypuzzleapp.domain.model.Puzzle

data class PuzzleState(
    val puzzle: Puzzle? = null,
    val puzzlePieces: List<PuzzlePiece> = emptyList(),
    val isLoading: Boolean = false,
    val isComplete: Boolean = false,
    val moves: Int = 0,
    val isDragging: Boolean = false,
    val isRevealingImage: Boolean = false
)

data class PuzzlePiece(
    val id: Int,
    val bitmap: Bitmap,
    val currentPosition: Int,
    val correctPosition: Int
) 
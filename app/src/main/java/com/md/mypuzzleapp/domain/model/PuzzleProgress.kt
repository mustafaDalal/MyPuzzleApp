package com.md.mypuzzleapp.domain.model

data class PuzzleProgress(
    val puzzleId: String,
    val piecePlacements: Map<Int, PiecePlacement>,
    val startTime: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class PiecePlacement(
    val pieceId: Int,
    val currentX: Int,
    val currentY: Int,
    val isPlaced: Boolean
) 
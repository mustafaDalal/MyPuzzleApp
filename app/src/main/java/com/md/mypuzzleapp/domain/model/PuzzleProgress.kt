package com.md.mypuzzleapp.domain.model

/**
 * Model for persisting puzzle progress in Supabase.
 * Currently not used in the main implementation as progress is managed in the ViewModel.
 * This will be used in future updates to enable progress persistence across sessions.
 */
data class PuzzleProgress(
    val puzzleId: String,
    val piecePlacements: Map<String, PiecePlacement>,
    val startTime: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Represents the placement state of a single puzzle piece.
 * Used in conjunction with PuzzleProgress for Supabase persistence.
 */
data class PiecePlacement(
    val pieceId: Int,
    val currentX: Int,
    val currentY: Int,
    val isPlaced: Boolean
) 
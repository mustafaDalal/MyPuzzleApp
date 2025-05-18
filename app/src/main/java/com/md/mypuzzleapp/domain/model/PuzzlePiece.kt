package com.md.mypuzzleapp.domain.model

import android.graphics.Bitmap

data class PuzzlePiece(
    val id: Int,
    val bitmap: Bitmap,
    val correctPosition: Int,
    val currentPosition: Int
) {
    fun isInCorrectPosition(): Boolean = currentPosition == correctPosition
} 
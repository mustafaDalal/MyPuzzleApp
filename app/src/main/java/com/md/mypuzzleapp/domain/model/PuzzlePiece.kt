package com.md.mypuzzleapp.domain.model

import android.graphics.Bitmap

data class PuzzlePiece(
    val id: Int,
    val bitmap: Bitmap,
    val correctX: Int,
    val correctY: Int,
    var currentX: Int = correctX,
    var currentY: Int = correctY,
    var isPlaced: Boolean = false
) {
    fun isInCorrectPosition(): Boolean {
        return currentX == correctX && currentY == correctY
    }
} 
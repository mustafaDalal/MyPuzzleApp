package com.md.mypuzzleapp.domain.model

import android.graphics.Bitmap
import android.net.Uri
import java.util.UUID

enum class PuzzleDifficulty(val gridSize: Int) {
    EASY(3),
    MEDIUM(4),
    HARD(5)
}

data class Puzzle(
    val id: String,
    val name: String,
    val difficulty: PuzzleDifficulty,
    val pieces: List<PuzzlePiece>,
    val originalImage: Bitmap? = null,
    val localImageUri: Uri? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val moves: Int = 0
) 
package com.md.mypuzzleapp.domain.usecase

import android.graphics.Bitmap
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class CreatePuzzleFromBitmapUseCase @Inject constructor(
    private val repository: PuzzleRepository
) {
    suspend operator fun invoke(name: String, bitmap: Bitmap, difficulty: PuzzleDifficulty) {
        repository.createPuzzleFromBitmap(bitmap, name, difficulty)
    }
} 
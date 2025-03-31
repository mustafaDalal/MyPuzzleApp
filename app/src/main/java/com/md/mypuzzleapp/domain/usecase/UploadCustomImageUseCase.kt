package com.md.mypuzzleapp.domain.usecase

import android.net.Uri
import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class UploadCustomImageUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    suspend operator fun invoke(
        uri: Uri,
        name: String,
        difficulty: PuzzleDifficulty
    ): Puzzle {
        return puzzleRepository.uploadCustomImage(uri, name, difficulty)
    }
} 
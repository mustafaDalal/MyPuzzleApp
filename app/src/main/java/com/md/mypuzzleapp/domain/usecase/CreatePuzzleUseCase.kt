package com.md.mypuzzleapp.domain.usecase

import android.net.Uri
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class CreatePuzzleUseCase @Inject constructor(
    private val repository: PuzzleRepository
) {
    suspend operator fun invoke(name: String, imageUri: Uri, difficulty: PuzzleDifficulty) {
        repository.createPuzzleFromUri(imageUri, name, difficulty)
    }
} 
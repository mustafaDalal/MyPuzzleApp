package com.md.mypuzzleapp.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class CreatePuzzleUseCase @Inject constructor(
    private val repository: PuzzleRepository
) {
    suspend operator fun invoke(imageUri: Uri): Bitmap {
        return repository.createBitmapFromUri(imageUri)
    }
} 
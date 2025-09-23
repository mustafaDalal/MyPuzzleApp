package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class DeletePuzzleUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    suspend operator fun invoke(id: String, imageUrl: String) {
        puzzleRepository.deletePuzzle(id, imageUrl)
    }
}

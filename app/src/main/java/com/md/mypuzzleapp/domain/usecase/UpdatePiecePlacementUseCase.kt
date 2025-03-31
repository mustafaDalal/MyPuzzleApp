package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.model.PiecePlacement
import com.md.mypuzzleapp.domain.repository.PuzzleProgressRepository
import javax.inject.Inject

class UpdatePiecePlacementUseCase @Inject constructor(
    private val puzzleProgressRepository: PuzzleProgressRepository
) {
    suspend operator fun invoke(puzzleId: String, piecePlacement: PiecePlacement) {
        puzzleProgressRepository.updatePiecePlacement(puzzleId, piecePlacement)
    }
} 
package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.repository.PuzzleProgressRepository
import javax.inject.Inject

class SavePuzzleProgressUseCase @Inject constructor(
    private val puzzleProgressRepository: PuzzleProgressRepository
) {
    suspend operator fun invoke(puzzleProgress: PuzzleProgress) {
        puzzleProgressRepository.savePuzzleProgress(puzzleProgress)
    }
} 
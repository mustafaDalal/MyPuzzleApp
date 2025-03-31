package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.model.PuzzleProgress
import com.md.mypuzzleapp.domain.repository.PuzzleProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPuzzleProgressUseCase @Inject constructor(
    private val puzzleProgressRepository: PuzzleProgressRepository
) {
    operator fun invoke(puzzleId: String): Flow<PuzzleProgress?> {
        return puzzleProgressRepository.getPuzzleProgress(puzzleId)
    }
} 
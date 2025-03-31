package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPuzzleByIdUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    operator fun invoke(id: String): Flow<Puzzle?> {
        return puzzleRepository.getPuzzleById(id)
    }
} 
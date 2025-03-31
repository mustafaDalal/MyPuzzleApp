package com.md.mypuzzleapp.domain.usecase

import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.repository.PuzzleRepository
import javax.inject.Inject

class AddPuzzleUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    suspend operator fun invoke(puzzle: Puzzle): String {
        return puzzleRepository.addPuzzle(puzzle)
    }
} 
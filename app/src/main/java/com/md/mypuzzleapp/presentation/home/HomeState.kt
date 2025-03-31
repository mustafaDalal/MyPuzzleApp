package com.md.mypuzzleapp.presentation.home

import com.md.mypuzzleapp.domain.model.Puzzle
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty

data class HomeState(
    val puzzles: List<Puzzle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploadDialogVisible: Boolean = false,
    val uploadImageName: String = "",
    val selectedDifficulty: PuzzleDifficulty = PuzzleDifficulty.EASY
) 
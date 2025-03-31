package com.md.mypuzzleapp.presentation.home

import android.net.Uri
import com.md.mypuzzleapp.domain.model.PuzzleDifficulty

sealed class HomeEvent {
    object LoadPuzzles : HomeEvent()
    data class SelectPuzzle(val puzzleId: String) : HomeEvent()
    object ShowUploadDialog : HomeEvent()
    object DismissUploadDialog : HomeEvent()
    data class UploadImageNameChanged(val name: String) : HomeEvent()
    data class DifficultyChanged(val difficulty: PuzzleDifficulty) : HomeEvent()
    data class UploadImage(val uri: Uri) : HomeEvent()
    object FetchRandomImage : HomeEvent()
} 
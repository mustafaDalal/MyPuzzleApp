package com.md.mypuzzleapp.presentation.puzzle

sealed class PuzzleEvent {
    data class MovePiece(val fromIndex: Int, val toIndex: Int) : PuzzleEvent()
    data class DragPiece(val pieceId: Int, val fromPosition: Int) : PuzzleEvent()
    data class DropPiece(val pieceId: Int, val toPosition: Int) : PuzzleEvent()
    data class ReturnPiece(val piece: PuzzlePiece, val position: Int) : PuzzleEvent()
    object NavigateBack : PuzzleEvent()
    object RestartPuzzle : PuzzleEvent()
    object CheckCompletion : PuzzleEvent()
    object RevealImage : PuzzleEvent()
    object HideImage : PuzzleEvent()
    object StopDragging : PuzzleEvent()
} 